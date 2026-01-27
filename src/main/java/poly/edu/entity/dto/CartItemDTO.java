package poly.edu.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor; 
import lombok.AllArgsConstructor; 
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Integer productId;
    
    private String productName;
    
    private BigDecimal price; // Giá tiền giữ nguyên BigDecimal là chuẩn
    
    // Đổi Integer sang Double để nhận dữ liệu số lẻ từ VueJS (ví dụ: 1.5)
    private Double quantity; 
    
    private String image; 
}