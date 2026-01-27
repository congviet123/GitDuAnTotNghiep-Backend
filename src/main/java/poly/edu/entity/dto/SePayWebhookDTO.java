package poly.edu.entity.dto;

import lombok.Data;

@Data
public class SePayWebhookDTO {
    private long id;                // ID giao dịch trên SePay
    private String gateway;         // Cổng thanh toán (MB, VCB...)
    private String transactionDate; // Ngày giờ giao dịch
    private String accountNumber;   // Số tài khoản người nhận
    private String content;         // Nội dung chuyển khoản (Ví dụ: DH123)
    private double transferAmount;  // Số tiền chuyển
    private double accumulatad;     // Số dư lũy kế
}