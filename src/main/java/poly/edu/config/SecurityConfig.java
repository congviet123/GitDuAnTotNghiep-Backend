package poly.edu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException; // [MỚI]
import org.springframework.security.oauth2.core.OAuth2Error; // [MỚI]
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import poly.edu.service.CustomOAuth2UserService;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired private UserDetailsService userDetailsService;
    @Autowired private CustomOAuth2UserService oauth2UserService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String errCode = "error";
            
            // 1. Kiểm tra xem lỗi có phải từ OAuth2 không
            if (exception instanceof OAuth2AuthenticationException) {
                OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
                // Lấy mã lỗi chính xác (account_disabled hoặc unregistered)
                String code = error.getErrorCode();
                
                if ("account_disabled".equals(code)) {
                    errCode = "disabled";
                } else if ("unregistered".equals(code)) {
                    errCode = "unregistered";
                }
            } 
            // 2. Fallback: Kiểm tra message nếu không bắt được mã lỗi
            else if (exception.getMessage() != null) {
                if (exception.getMessage().contains("account_disabled")) errCode = "disabled";
                else if (exception.getMessage().contains("unregistered")) errCode = "unregistered";
            }

            // Redirect về Frontend kèm mã lỗi chuẩn
            response.sendRedirect("http://localhost:5173/login?google_error=" + errCode);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .securityContext(securityContext -> securityContext.requireExplicitSave(false))
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/imgs/**", "/css/**", "/js/**", "/static/**", "/error").permitAll()
                .requestMatchers("/rest/account/register").permitAll() 
                .requestMatchers("/rest/client/**", "/rest/auth/**").permitAll()
                .requestMatchers("/rest/admin/**").hasAnyRole("ADMIN", "DIRE", "STAF")
                .requestMatchers("/rest/account/**", "/rest/orders/**", "/rest/cart/**", "/rest/reviews/**")
                .hasAnyRole("USER", "ADMIN", "DIRE", "STAF", "CUST")
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
                .defaultSuccessUrl("http://localhost:5173/login?google_success=true", true)
                .failureHandler(authenticationFailureHandler()) // Sử dụng Handler đã sửa
            );

        return http.build();
    }
}