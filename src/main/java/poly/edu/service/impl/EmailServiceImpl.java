package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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
    
    // ========== THÊM: Gửi email thông báo voucher ==========
    @Override
    public void sendVoucherEmail(String toEmail, String voucherCode, String voucherName, String discountValue) {
        try {
            String subject = "🎁 BẠN NHẬN ĐƯỢC MÃ GIẢM GIÁ ĐẶC BIỆT!";
            
            String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 5px 15px rgba(0,0,0,0.1);'>" +
                "<div style='background: linear-gradient(135deg, #28a745, #1e7e34); padding: 20px; text-align: center;'>" +
                "<h1 style='color: white; margin: 0;'>🍎 TRÁI CÂY BAY</h1>" +
                "<p style='color: #ffd966; margin: 5px 0 0;'>Ưu đãi đặc biệt dành riêng cho bạn</p>" +
                "</div>" +
                "<div style='padding: 30px; text-align: center;'>" +
                "<h2 style='color: #28a745;'>🎉 BẠN NHẬN ĐƯỢC VOUCHER GIẢM GIÁ! 🎉</h2>" +
                "<div style='background: #f8f9fa; border: 2px dashed #28a745; border-radius: 10px; padding: 20px; margin: 20px 0;'>" +
                "<p style='font-size: 14px; color: #666; margin-bottom: 10px;'>Mã voucher của bạn:</p>" +
                "<div style='background: white; border: 1px solid #ddd; border-radius: 8px; padding: 15px; display: inline-block;'>" +
                "<span style='font-size: 28px; font-weight: bold; letter-spacing: 2px; color: #28a745;'>" + voucherCode + "</span>" +
                "</div>" +
                "<p style='font-size: 18px; font-weight: bold; color: #dc3545; margin: 15px 0 0;'>Giảm " + discountValue + "</p>" +
                "<p style='font-size: 14px; color: #666; margin-top: 10px;'>" + voucherName + "</p>" +
                "</div>" +
                "<div style='margin-top: 20px;'>" +
                "<a href='http://localhost:5173/cart' style='background: #28a745; color: white; padding: 12px 30px; text-decoration: none; border-radius: 30px; display: inline-block; font-weight: bold;'>🛒 MUA NGAY</a>" +
                "</div>" +
                "<p style='color: #999; font-size: 12px; margin-top: 20px;'>*Voucher có hiệu lực đến ngày hết hạn. Số lượng có hạn, nhanh tay bạn nhé!</p>" +
                "</div>" +
                "<div style='background: #f8f9fa; padding: 15px; text-align: center; font-size: 12px; color: #999;'>" +
                "<p>Trái Cây Bay - Tươi ngon mỗi ngày</p>" +
                "<p>Hotline: 1900 1234 | Email: support@traicaybay.com</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            
            System.out.println("✅ Đã gửi email voucher đến: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("❌ Lỗi gửi email: " + e.getMessage());
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }
    // ========== KẾT THÚC THÊM ==========
}