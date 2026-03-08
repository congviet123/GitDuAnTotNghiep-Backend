package poly.edu.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import poly.edu.service.EmailService;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/rest/contact")
@CrossOrigin(origins = "*")
public class ContactController {
    
    @Autowired
    private EmailService emailService;
    
    // THÊM: Hàm kiểm tra email đơn giản
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        email = email.trim();
        // Kiểm tra có @ và có dấu chấm sau @
        int atIndex = email.indexOf('@');
        int dotIndex = email.lastIndexOf('.');
        return atIndex > 0 && dotIndex > atIndex + 1 && dotIndex < email.length() - 1;
    }
    
    @PostMapping("/send-message")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Lấy dữ liệu từ request
            String name = request.get("name");
            String email = request.get("email");
            String message = request.get("message");
            String toEmail = request.get("toEmail");
            
            // Kiểm tra dữ liệu đầu vào
            if (name == null || name.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập họ tên");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập email");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập nội dung tin nhắn");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (toEmail == null || toEmail.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email người nhận không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }
            
            // SỬA: Kiểm tra định dạng email với hàm isValidEmail
            if (!isValidEmail(email)) {
                response.put("success", false);
                response.put("message", "Email người gửi không đúng định dạng (vd: example@gmail.com)");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!isValidEmail(toEmail)) {
                response.put("success", false);
                response.put("message", "Email người nhận không đúng định dạng (vd: example@gmail.com)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Gửi email
            boolean sent = emailService.sendContactEmail(name, email, message, toEmail);
            
            if (sent) {
                response.put("success", true);
                response.put("message", "Tin nhắn đã được gửi thành công!");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không thể gửi email. Vui lòng thử lại sau!");
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            System.out.println("Lỗi trong ContactController: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}