package poly.edu.entity.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ProductAdminDTO {
    private Integer id;
    private String name;
    private BigDecimal price;
    private String description;
    private String image; 
    private Boolean available; // Trạng thái có sẵn/hết hàng
    private Date createDate;
    private String categoryName; 
    private Integer categoryId;
    
    // Bao gồm danh sách các ảnh phụ (Dùng ProductImageDTO nếu bạn muốn chi tiết hơn)
    private List<String> secondaryImages; 
}