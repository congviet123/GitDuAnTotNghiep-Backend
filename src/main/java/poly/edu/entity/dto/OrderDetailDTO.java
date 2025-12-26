package poly.edu.entity.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderDetailDTO {
    private Integer id;
    private Integer productId;
    private String productName;
    private BigDecimal price; // Giá tại thời điểm mua
    private Integer quantity;
}