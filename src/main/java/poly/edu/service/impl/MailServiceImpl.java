package poly.edu.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource; // [QUAN TRỌNG] Import để sửa lỗi addInline
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.entity.Order;
import poly.edu.service.MailService;

import java.io.UnsupportedEncodingException;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender mailSender; 

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ---------------------------------------------------------
    // 1. GỬI EMAIL CƠ BẢN
    // ---------------------------------------------------------
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            System.out.println("Đang bắt đầu gửi mail tới: " + to); 
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, "Trái Cây Nhập Khẩu"));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            System.out.println(">>> GỬI MAIL THÀNH CÔNG: " + to);

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            System.err.println(">>> LỖI GỬI MAIL: " + e.getMessage());
            throw new RuntimeException("Lỗi kết nối Gmail: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi không xác định khi gửi mail");
        }
    }

    // ---------------------------------------------------------
    // 2. GỬI EMAIL CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG
    // ---------------------------------------------------------
    @Override
    public void sendOrderUpdateEmail(String to, String subject, String message, Order order) {
        try {
            System.out.println(">>> Đang gửi mail cập nhật đơn hàng tới: " + to);

            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true, "UTF-8");
            
            helper.setFrom(new InternetAddress(fromEmail, "Trái Cây Nhập Khẩu"));
            helper.setTo(to);
            helper.setSubject(subject);
            
            String statusVN = convertStatusToVN(order.getStatus());
            String shopMessage = (message != null && !message.trim().isEmpty()) ? message : "Không có lời nhắn chi tiết.";
            
            String customerName = "Quý khách";
            if (order.getAccount() != null) {
                if (order.getAccount().getFullname() != null) customerName = order.getAccount().getFullname();
                else if (order.getAccount().getUsername() != null) customerName = order.getAccount().getUsername();
            }

            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>"
                    + "<div style='background-color: #007bff; padding: 20px; text-align: center; color: white;'>"
                    + "<h2 style='margin: 0;'>THÔNG BÁO CẬP NHẬT ĐƠN HÀNG</h2>"
                    + "</div>"
                    + "<div style='padding: 20px; background-color: #ffffff;'>"
                    + "<p>Xin chào <strong>" + customerName + "</strong>,</p>"
                    + "<p>Đơn hàng <strong>#DH-" + order.getId() + "</strong> của bạn đã được shop xử lý:</p>"
                    + "<div style='text-align: center; margin: 25px 0;'>"
                    + "<span style='background-color: #e2e6ea; color: #0056b3; padding: 12px 24px; border-radius: 30px; font-weight: bold; font-size: 18px; border: 1px solid #b8daff;'>" 
                    + statusVN + "</span>"
                    + "</div>"
                    + "<div style='background-color: #fff3cd; border-left: 5px solid #ffc107; padding: 15px; margin-bottom: 20px; border-radius: 4px;'>"
                    + "<p style='margin: 0; font-weight: bold; color: #856404;'>💬 Lời nhắn từ Shop Trái Cây Nhập Khẩu:</p>"
                    + "<p style='margin: 8px 0 0 0; color: #333; font-style: italic;'>\"" + shopMessage + "\"</p>"
                    + "</div>"
                    + "<p>Cảm ơn bạn đã tin tưởng và mua sắm tại <strong>Trái Cây Bay</strong>!</p>"
                    + "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>"
                    + "<p style='font-size: 12px; color: #666; text-align: center;'>Đây là email tự động, vui lòng không trả lời trực tiếp email này.</p>"
                    + "</div>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(mail);
            
            System.out.println(">>> Đã gửi mail cập nhật thành công cho đơn: " + order.getId());

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            System.err.println("Lỗi gửi mail cập nhật: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // 3. GỬI EMAIL CÓ ĐÍNH KÈM FILE (Logic cũ)
    // ---------------------------------------------------------
    @Override
    public void sendEmailWithAttachment(String subject, String body, MultipartFile[] files) {
        sendEmailWithReturnRequest(subject, body, null, files);
    }

    // ---------------------------------------------------------
    // 4. [MỚI] GỬI EMAIL HOÀN TRẢ (CÓ ẢNH QR INLINE & ĐÍNH KÈM MINH CHỨNG)
    // ---------------------------------------------------------
    @Override
    public void sendEmailWithReturnRequest(String subject, String body, MultipartFile qrCode, MultipartFile[] attachments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true = multipart (cho phép đính kèm file và ảnh inline)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(new InternetAddress(fromEmail, "Hệ Thống Trái Cây"));
            helper.setTo(fromEmail); // Gửi về Admin để xử lý
            helper.setSubject(subject);
            helper.setText(body, true); // true = nội dung là HTML

            // [ĐÃ SỬA LỖI Ở ĐÂY] 
            // 1. Xử lý QR Code Inline (Hiển thị ảnh trực tiếp trong nội dung email)
            if (qrCode != null && !qrCode.isEmpty()) {
                // Chuyển MultipartFile thành ByteArrayResource để helper.addInline chấp nhận
                String contentType = qrCode.getContentType();
                if (contentType == null) contentType = "image/png"; // Fallback nếu không có content type
                
                helper.addInline("qrCodeImage", new ByteArrayResource(qrCode.getBytes()), contentType); 
            }

            // 2. Xử lý file đính kèm (Ảnh minh chứng lỗi sản phẩm)
            if (attachments != null && attachments.length > 0) {
                for (MultipartFile file : attachments) {
                    if (!file.isEmpty()) {
                        helper.addAttachment(file.getOriginalFilename(), file);
                    }
                }
            }

            mailSender.send(message);
            System.out.println(">>> Đã gửi mail hoàn trả (kèm QR & ảnh minh chứng) thành công.");

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            System.err.println("Lỗi gửi mail hoàn trả: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi IO/Data khi gửi mail: " + e.getMessage());
        }
    }

    // Helper chuyển đổi trạng thái
    private String convertStatusToVN(String status) {
        if (status == null) return "Không xác định";
        switch (status) {
            case "PENDING": return "Đang chờ xử lý";
            case "CONFIRMED": return "Đã xác nhận";
            case "PREPARING": return "Đang chuẩn bị đơn"; 
            case "SHIPPING": return "Đang vận chuyển";
            case "SHIPPED": return "Đang giao hàng";
            case "DELIVERED": return "Giao hàng thành công";
            case "COMPLETED": return "Hoàn tất đơn hàng";
            case "CANCEL_REQUESTED": return "Đang chờ xác nhận hủy";
            case "CANCELLED": return "Đã hủy";
            case "CANCELLED_REFUNDED": return "Hủy thành công - Đã hoàn tiền";
            case "HIDDEN": return "Đơn hàng đã ẩn";
            case "RETURN_REQUESTED": return "Yêu cầu hoàn trả";
            default: return status;
        }
    }
}