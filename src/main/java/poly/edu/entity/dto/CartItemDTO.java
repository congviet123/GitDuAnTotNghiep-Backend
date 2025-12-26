package poly.edu.entity.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Integer productId;
    private String productName;
    private BigDecimal price; // Giá tại thời điểm thêm vào giỏ
    private Integer quantity;
    private String image; // Ảnh chính để hiển thị trong giỏ
}