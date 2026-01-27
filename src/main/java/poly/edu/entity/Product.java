package poly.edu.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal; 
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat; // [QUAN TRỌNG] Import thư viện này

@Entity
@Table(name = "Product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 150)
    private String name;
    
    // --- [LOGIC GIÁ] ---
    
    // 1. GIÁ BÁN THỰC TẾ (Final Price)
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;
    
    // 2. GIÁ GỐC (Original Price) 
    @Column(name = "original_price", precision = 18, scale = 2)
    private BigDecimal originalPrice;

    // 3. GIÁ NHẬP (Import Price)
    @Column(name = "import_price", precision = 18, scale = 2)
    private BigDecimal importPrice = BigDecimal.ZERO;

    // 4. PHẦN TRĂM GIẢM GIÁ 
    @Column(name = "discount")
    private Integer discount = 0;

    // -----------------------

    // Số lượng tồn kho (Kg)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO; 

    // Trạng thái thanh lý
    @Column(name = "is_liquidation")
    private Boolean isLiquidation = false;
    
    @Lob
    private String description;
    
    private String image;
    
    private Boolean available = true;
    
    // Thêm @JsonFormat để Spring Boot hiểu chuỗi "yyyy-MM-dd" từ VueJS gửi lên
    @Temporal(TemporalType.DATE)
    @Column(name = "create_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Ho_Chi_Minh")
    private Date createDate = new Date();
    
    // Quan hệ
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> productImages;

    @JsonIgnore
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;
}