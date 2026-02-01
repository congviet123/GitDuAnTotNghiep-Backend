package poly.edu;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class test {
	public class AppConfig {

	    // Đây là nơi duy nhất khai báo Bean PasswordEncoder
	    @Bean
	    public PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	        public PasswordEncoder passwordEncoder() {
		        return new BCryptPasswordEncoder();
	        
	    }
	}
}
