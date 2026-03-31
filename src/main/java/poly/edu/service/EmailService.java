package poly.edu.service;

public interface EmailService {
    boolean sendContactEmail(String name, String fromEmail, String message, String toEmail);
    // ========== THÊM: Gửi email thông báo voucher ==========
    void sendVoucherEmail(String toEmail, String voucherCode, String voucherName, String discountValue);
    // ========== KẾT THÚC THÊM ==========
}