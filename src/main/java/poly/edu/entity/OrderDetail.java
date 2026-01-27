package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal; // Sử dụng BigDecimal để lưu tiền tệ và số lượng chuẩn xác hơn
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Order_Detail") 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //  Đổi từ Integer sang BigDecimal để lưu số lẻ (ví dụ: 1.5 kg)
    // precision=10, scale=2 nghĩa là lưu tối đa 10 chữ số, trong đó 2 số sau dấu phẩy
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price; 
    
    // Khóa ngoại tới Order
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore 
    private Order order;

    // Khóa ngoại tới Product
    // EAGER là hợp lý ở đây vì khi xem chi tiết đơn hàng, ta luôn cần biết đó là sản phẩm gì
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}