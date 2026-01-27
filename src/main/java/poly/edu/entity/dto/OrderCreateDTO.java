package poly.edu.entity.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

// Cần import cho CartItemDTO
import poly.edu.entity.dto.CartItemDTO; 

@Data
public class OrderCreateDTO {
    
    // Thông tin vận chuyển
    @NotBlank(message = "Địa chỉ giao hàng không được để trống.")
    private String shippingAddress;
    
    private String notes;
    
    // BỔ SUNG: Thêm trường Payment Method (Tùy chọn, nếu bạn muốn lưu vào Order Entity)
    // private String paymentMethod; 

    // Danh sách sản phẩm được chuyển từ giỏ hàng (CartItemDTO)
    private List<CartItemDTO> items;
    private String paymentMethod;
}