package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore; 

@Entity
@Table(name = "Orders") 
@Data
public class Order implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date")
    private Date createDate = new Date();

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    private String notes;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";
    
    // Mối quan hệ: Many Orders to One User
    // ✅ FIX LỖI: Sử dụng @JsonIgnore để bỏ qua việc serialize trường LAZY này, 
    // tránh lỗi ByteBuddyInterceptor (Hibernate Proxy).
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_username", nullable = false) 
    private User account;
    
    // Ngắt vòng lặp: Order -> OrderDetails (Giả định OrderDetail có @JsonIgnore trên trường Order)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;
}