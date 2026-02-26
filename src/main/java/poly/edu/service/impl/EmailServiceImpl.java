package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import poly.edu.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail; // Lấy email từ cấu hình
    
    @Override
    public boolean sendContactEmail(String name, String fromEmail, String message, String toEmail) {
        try {
            System.out.println("=== BẮT ĐẦU GỬI EMAIL ===");
            System.out.println("Tên người gửi: " + name);
            System.out.println("Email người gửi: " + fromEmail);
            System.out.println("Email người nhận: " + toEmail);
            System.out.println("Nội dung: " + message);
            
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            
            // Địa chỉ email người nhận
            mailMessage.setTo(toEmail);
            
            // Địa chỉ email người gửi (phải là email đã cấu hình trong application.properties)
            mailMessage.setFrom(this.fromEmail);
            
            // Reply-to để khi trả lời sẽ gửi về email người gửi
            mailMessage.setReplyTo(fromEmail);
            
            // Tiêu đề email
            mailMessage.setSubject("Tin nhắn liên hệ mới từ: " + name);
            
            // Nội dung email
            String content = "Bạn vừa nhận được tin nhắn liên hệ mới:\n\n" +
                           "━━━━━━━━━━━━━━━━━━━━━━\n" +
                           "👤 Họ tên: " + name + "\n" +
                           "📧 Email: " + fromEmail + "\n" +
                           "━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                           "📝 Nội dung tin nhắn:\n" + 
                           message + "\n\n" +
                           "━━━━━━━━━━━━━━━━━━━━━━\n" +
                           "Vui lòng trả lời email này để phản hồi khách hàng.";
            
            mailMessage.setText(content);
            
            // Gửi email
            mailSender.send(mailMessage);
            
            System.out.println("=== GỬI EMAIL THÀNH CÔNG ===");
            return true;
            
        } catch (Exception e) {
            System.out.println("=== LỖI GỬI EMAIL ===");
            System.out.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}