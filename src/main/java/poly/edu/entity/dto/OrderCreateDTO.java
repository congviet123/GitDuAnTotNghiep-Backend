package poly.edu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor  // Cần thiết để Jackson (JSON parser) khởi tạo object
@AllArgsConstructor
public class OrderCreateDTO {
    
    // --- 1. THÔNG TIN NGƯỜI NHẬN & ĐỊA CHỈ ---
    
    @NotBlank(message = "Tên người nhận không được để trống.")
    private String recipientName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống.")
    private String recipientPhone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống.")
    private String shippingAddress; 
    
    // --- 2. THÔNG TIN ĐƠN HÀNG ---
    
    private String notes; 
    
    @NotBlank(message = "Phương thức thanh toán không được để trống.")
    private String paymentMethod; // "COD", "VNPAY"

    // --- 3. KHUYẾN MÃI ---
    private String voucherCode; 

    // --- 4. CHI TIẾT SẢN PHẨM MUA ---
    @NotEmpty(message = "Giỏ hàng đang trống, vui lòng chọn sản phẩm.")
    private List<OrderItem> items;

    // [CLASS CON]: Định nghĩa cấu trúc sản phẩm ngay tại đây để không phụ thuộc file khác
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        @NotNull(message = "ID sản phẩm không được để trống")
        private Integer productId;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 0, message = "Số lượng phải lớn hơn 0")
        private Double quantity;
    }
}