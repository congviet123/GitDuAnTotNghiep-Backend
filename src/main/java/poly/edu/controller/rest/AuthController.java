package poly.edu.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.User;
import poly.edu.entity.dto.LoginRequest;
import poly.edu.entity.dto.UserRegistrationDTO;
import poly.edu.repository.UserRepository;
import poly.edu.service.CustomOAuth2UserService;
import poly.edu.service.UserService;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@CrossOrigin("*") 
@RestController
@RequestMapping("/rest/auth") 
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;
    
    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    UserRepository userRepository;

    // --- ĐĂNG NHẬP ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userService.findById(loginRequest.getUsername());
            return ResponseEntity.ok(user);

        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Tài khoản của bạn đã tạm bị khóa, vui lòng liên hệ admin để mở tài khoản");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tên đăng nhập hoặc mật khẩu");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Lỗi xác thực: " + e.getMessage());
        }
    }
    
    // --- ĐĂNG KÝ BẰNG GOOGLE ---
    @PostMapping("/register-google")
    public ResponseEntity<?> registerGoogleUser(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("Token không hợp lệ.");
        }

        OAuth2User oauth2User = CustomOAuth2UserService.pendingGoogleUsers.get(token);
        if (oauth2User == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Phiên đăng ký đã hết hạn. Vui lòng bấm Đăng nhập Google lại.");
        }

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        try {
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body("Email này đã được đăng ký trong hệ thống.");
            }

            UserRegistrationDTO dto = new UserRegistrationDTO();
            
            // ---XỬ LÝ TÊN ĐĂNG NHẬP THÔNG MINH ---
            String baseUsername = email.split("@")[0];
            String username = baseUsername;
            int counter = 1;
            
            while (userRepository.existsById(username)) {
                username = baseUsername + "_" + counter;
                counter++;
            }
            // ----------------------------------------------------

            dto.setUsername(username);
            dto.setFullname(name);
            dto.setEmail(email);
            
            String randomPass = UUID.randomUUID().toString();
            dto.setPassword(randomPass);
            dto.setConfirmPassword(randomPass);

            userService.register(dto);

            CustomOAuth2UserService.pendingGoogleUsers.remove(token);

            User newUser = userService.findById(username);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    newUser.getUsername(), 
                    null, 
                    Collections.singleton(new SimpleGrantedAuthority(newUser.getRole().getName()))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return ResponseEntity.ok(newUser);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi đăng ký tự động: " + e.getMessage());
        }
    }

    // =========================================================
    // LẤY LẠI MẬT KHẨU BẰNG OTP (3 BƯỚC)
    // =========================================================

    // Bước 1: Gửi mã OTP về Email
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Vui lòng nhập email.");
            }
            
            // Sẽ code hàm này trong UserServiceImpl
            userService.generateAndSendOtp(email);
            return ResponseEntity.ok("Mã OTP đã được gửi đến email của bạn.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Bước 2: Xác thực mã OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String otp = payload.get("otp");
            
            if (email == null || otp == null) {
                return ResponseEntity.badRequest().body("Thiếu thông tin xác thực.");
            }

            // Sẽ code hàm này trong UserServiceImpl
            userService.verifyOtp(email, otp);
            return ResponseEntity.ok("Xác thực OTP thành công.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Bước 3: Đặt lại mật khẩu mới
    @PostMapping("/reset-password-otp")
    public ResponseEntity<?> resetPasswordWithOtp(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String newPassword = payload.get("newPassword");
            String confirmPassword = payload.get("confirmPassword");

            if (email == null || newPassword == null || confirmPassword == null) {
                return ResponseEntity.badRequest().body("Vui lòng điền đầy đủ thông tin.");
            }
            
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body("Mật khẩu xác nhận không khớp.");
            }

            // Sẽ code hàm này trong UserServiceImpl
            userService.resetPasswordWithOtp(email, newPassword);
            return ResponseEntity.ok("Đổi mật khẩu thành công. Vui lòng đăng nhập lại.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =========================================================

    // --- ĐĂNG XUẤT ---
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Đăng xuất thành công");
    }

    // --- RESET MẬT KHẨU NHANH ---
    @GetMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String username) {
        User user = userService.findById(username);
        if (user != null) {
            String newPass = "123456";
            user.setPassword(passwordEncoder.encode(newPass));
            userRepository.save(user);
            return ResponseEntity.ok("Thành công! Mật khẩu mới của [" + username + "] là: " + newPass);
        }
        return ResponseEntity.badRequest().body("Không tìm thấy user: " + username);
    }
}