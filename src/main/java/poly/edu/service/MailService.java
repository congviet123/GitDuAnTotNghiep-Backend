package poly.edu.service;

import org.springframework.web.multipart.MultipartFile; // [QUAN TRỌNG] Bắt buộc phải có dòng này
import poly.edu.entity.Order;

public interface MailService {
    // 1. Gửi mail thông thường
    void sendEmail(String to, String subject, String body);

    // 2. Gửi mail cập nhật trạng thái đơn hàng
    void sendOrderUpdateEmail(String to, String subject, String message, Order order);

    // 3. Gửi mail có đính kèm file (Yêu cầu trả hàng)
    void sendEmailWithAttachment(String subject, String body, MultipartFile[] files);
    
	// 4. Gửi mail có đính kèm file & QR Code (Yêu cầu hoàn trả đầy đủ)
    void sendEmailWithReturnRequest(String subject, String body, MultipartFile qrCode, MultipartFile[] attachments);
}