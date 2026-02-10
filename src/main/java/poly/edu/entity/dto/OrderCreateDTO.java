package poly.edu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor  // Cần thiết để Jackson (JSON parser) khởi tạo object
@AllArgsConstructor
public class OrderCreateDTO {
    
    // --- 1. THÔNG TIN NGƯỜI NHẬN & ĐỊA CHỈ ---
    // Bắt buộc phải có để Shipper liên lạc
    
    @NotBlank(message = "Tên người nhận không được để trống.")
    private String recipientName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống.")
    private String recipientPhone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống.")
    private String shippingAddress; // Lưu chuỗi địa chỉ đầy đủ (Snapshot)
    
    // --- 2. THÔNG TIN ĐƠN HÀNG ---
    
    private String notes; // Ghi chú của khách hàng (Có thể để trống)
    
    @NotBlank(message = "Phương thức thanh toán không được để trống.")
    private String paymentMethod; // VD: "COD", "VNPAY"

    // --- 3. KHUYẾN MÃI (Tùy chọn) ---
    private String voucherCode; // Mã giảm giá (nếu có)

    // --- 4. CHI TIẾT SẢN PHẨM MUA ---
    // Bắt buộc phải có ít nhất 1 sản phẩm mới cho đặt hàng
    @NotEmpty(message = "Giỏ hàng đang trống, vui lòng chọn sản phẩm.")
    private List<CartItemDTO> items;
}