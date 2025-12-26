package poly.edu.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductClientDTO {
    private Integer id;
    private String name;
    private BigDecimal price;
    private String description;
    private String mainImage; // Tên chuẩn theo DTO
    private String categoryName;
    
    // Danh sách đường dẫn ảnh phụ để hiển thị trên trang chi tiết
    private List<String> secondaryImages; 
}