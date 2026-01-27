package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Orders") 
@Data
public class Order implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // --- [1. CÁC TRƯỜNG CƠ BẢN] ---
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
    
    @Column(name = "payment_method") 
    private String paymentMethod;

    @Column(name = "order_code", insertable = false, updatable = false)
    private String orderCode;

    // Tên người nhận hàng thực tế (Để in lên phiếu giao hàng cho Shipper)
    @Column(name = "recipient_name")
    private String recipientName;

    // SĐT người nhận thực tế (Để Shipper gọi)
    @Column(name = "recipient_phone")
    private String recipientPhone;

    // Phí giao hàng (Để hiển thị dòng phí ship trong PDF)
    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;

    // Cờ đánh dấu đã in hay chưa (Để hiện nút xanh/đỏ trên Admin VueJS)
    @Column(name = "is_printed")
    private Boolean isPrinted = false;

    // Thời điểm xuất hóa đơn gần nhất
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "export_date")
    private Date exportDate;
    
    // Mã Voucher (nếu có)
    @Column(name = "voucher_code")
    private String voucherCode;

    // --- [3. QUAN HỆ VỚI USER] ---
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "account_username", nullable = false)
    @JsonIgnoreProperties({"password", "orders", "roles", "hibernateLazyInitializer", "handler"}) 
    private User account;
    
    // --- [4. QUAN HỆ VỚI CHI TIẾT ĐƠN] ---
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"order", "hibernateLazyInitializer", "handler"})
    private List<OrderDetail> orderDetails;
}