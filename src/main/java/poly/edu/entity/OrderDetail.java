package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // Bổ sung để khớp với DTO logic
import java.io.Serializable;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore; 

@Entity
@Table(name = "Order_Detail") 
@Data
@NoArgsConstructor // Bổ sung
@AllArgsConstructor // Bổ sung
public class OrderDetail implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer quantity;

    private BigDecimal price; 
    
    // Khóa ngoại tới Order: Giữ LAZY và @JsonIgnore để tránh vòng lặp
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore 
    private Order order;

    // ✅ SỬA LỖI: Buộc tải Product EAGERLY để tránh lỗi Serialization/Proxy khi trả về Order
    @ManyToOne(fetch = FetchType.EAGER) // Thay đổi từ LAZY sang EAGER
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}