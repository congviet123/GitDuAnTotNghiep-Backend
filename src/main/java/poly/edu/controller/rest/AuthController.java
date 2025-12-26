package poly.edu.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder; // [MỚI]
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.User;
import poly.edu.entity.dto.LoginRequest;
import poly.edu.repository.UserRepository; // [MỚI]
import poly.edu.service.UserService;

@RestController
@RequestMapping("/rest/auth") 
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;
    
    // [THÊM] Inject 2 cái này để làm chức năng reset mật khẩu nhanh
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. Xác thực username/password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            // 2. Lưu vào Context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Lấy thông tin User
            User user = userService.findById(loginRequest.getUsername());
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tài khoản không tồn tại.");
            }

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tên đăng nhập hoặc mật khẩu");
        }
    }
    
    // [CHỨC NĂNG CỨU CÁNH] Reset mật khẩu về 123456
    // Gọi trên trình duyệt: http://localhost:8080/rest/auth/reset-password?username=admin
    @GetMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String username) {
        User user = userService.findById(username);
        if (user != null) {
            String newPass = "123456";
            // Mã hóa mật khẩu chuẩn BCrypt
            user.setPassword(passwordEncoder.encode(newPass));
            userRepository.save(user);
            return ResponseEntity.ok("Thành công! Mật khẩu mới của [" + username + "] là: " + newPass);
        }
        return ResponseEntity.badRequest().body("Không tìm thấy user: " + username);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Đăng xuất thành công");
    }
}