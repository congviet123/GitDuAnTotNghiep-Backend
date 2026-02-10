package poly.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Cart") // Map đúng với tên bảng Cart trong SQL
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

	// Map với bảng User
    // Vì 1 User có thể thêm nhiều sản phẩm vào giỏ (tạo ra nhiều dòng trong bảng Cart)
    @ManyToOne 
    @JoinColumn(name = "username") // Map với cột username trong DB
    private User user; 

    // Map với bảng Product
    @ManyToOne
    @JoinColumn(name = "product_id") // Map với cột product_id trong DB
    private Product product;

    // Số lượng sản phẩm
    @Column(name = "quantity")
    private Double quantity; // Dùng Double vì trong SQL là DECIMAL(10,2)

    //  Ngày thêm vào giỏ
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date")
    private Date createDate = new Date();
}