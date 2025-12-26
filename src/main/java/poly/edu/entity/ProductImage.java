package poly.edu.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Product_Image")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "image_url", nullable = false)
    private String imageUrl;
    
    @Column(name = "is_main")
    private Boolean isMain = false; // Ảnh chính hay ảnh phụ
    
    // Mối quan hệ: Many ProductImages to One Product
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}