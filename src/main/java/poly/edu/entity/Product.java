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
    
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;
    
    @Lob
    private String description;
    
    private String image;
    
    private Boolean available = true;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "create_date")
    private Date createDate = new Date();
    
    // Mối quan hệ: Many Products to One Category (GIỮ NGUYÊN)
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    // Ngắt vòng lặp: Product -> ProductImages
    @JsonIgnore // <--- ĐÃ THÊM
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> productImages;

    // Ngắt vòng lặp: Product -> OrderDetails
    @JsonIgnore // <--- ĐÃ THÊM
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;
}