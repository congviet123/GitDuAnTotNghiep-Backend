package poly.edu.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException; // [QUAN TRỌNG]
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.User;
import poly.edu.entity.dto.LoginRequest;
import poly.edu.repository.UserRepository;
import poly.edu.service.UserService;

@CrossOrigin("*") // Cho phép Vuejs gọi API không bị lỗi CORS
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
            // 1. Thực hiện xác thực
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            // 2. Nếu thành công, lưu vào Context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Lấy thông tin User để trả về Frontend
            User user = userService.findById(loginRequest.getUsername());
            return ResponseEntity.ok(user);

        } catch (DisabledException e) {
            // ---  THÔNG BÁO TÀI KHOẢN BỊ KHÓA ---
            // Trả về đúng nội dung bạn yêu cầu
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Tài khoản của bạn đã tạm bị khóa, vui lòng liên hệ admin để mở tài khoản");
            
        } catch (BadCredentialsException e) {
            // Lỗi sai User/Pass
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tên đăng nhập hoặc mật khẩu");
            
        } catch (Exception e) {
            // Lỗi khác
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Lỗi xác thực: " + e.getMessage());
        }
    }
    
    // --- ĐĂNG XUẤT ---
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Đăng xuất thành công");
    }

    // --- RESET MẬT KHẨU NHANH (Giữ nguyên) ---
    // URL: http://localhost:8080/rest/auth/reset-password?username=admin
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