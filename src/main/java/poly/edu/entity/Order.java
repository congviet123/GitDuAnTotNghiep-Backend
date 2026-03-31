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

    // =========================================================================
    // 1. CÁC TRƯỜNG THÔNG TIN CƠ BẢN
    // =========================================================================
    
    // Ngày giờ tạo đơn hàng (Mặc định lấy giờ hệ thống lúc tạo)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date")
    private Date createDate = new Date();

    // Địa chỉ giao hàng (Thiết lập length = 500 để khớp với bảng SQL NVARCHAR(500))
    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Column(name = "notes", columnDefinition = "NVARCHAR(MAX)", length = 100000)
    private String notes;

    // Tổng tiền khách phải thanh toán
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    // Trạng thái đơn hàng (VD: PENDING, COMPLETED...)
    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING";
    
    // Hình thức thanh toán (VD: COD, BANK)
    @Column(name = "payment_method", length = 50) 
    private String paymentMethod;

    // Mã đơn hàng tự động sinh ra trong SQL Server (VD: DH000001)
    // insertable và updatable = false vì cột này do DB tự tính toán
    @Column(name = "order_code", insertable = false, updatable = false, length = 20)
    private String orderCode;

    // =========================================================================
    // 2. THÔNG TIN GIAO HÀNG & IN ẤN
    // =========================================================================
    
    // Tên người nhận hàng thực tế (Để in lên phiếu giao hàng cho Shipper)
    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    // Số điện thoại người nhận thực tế (Để Shipper gọi)
    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    // Phí giao hàng (Để tính toán và hiển thị dòng phí ship trong PDF)
    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;

    // Cờ đánh dấu đã in hóa đơn hay chưa (Dùng cho giao diện Admin)
    @Column(name = "is_printed")
    private Boolean isPrinted = false;

    // Thời điểm Admin bấm xuất hóa đơn PDF gần nhất
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "export_date")
    private Date exportDate;
    
    // Thời điểm giao hàng thành công (Dùng làm mốc đếm ngược 24h hỗ trợ đổi trả)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "delivery_date")
    private Date deliveryDate;
    
    // Mã Voucher khách đã sử dụng cho đơn này (nếu có)
    @Column(name = "voucher_code", length = 20)
    private String voucherCode;

    // =========================================================================
    // 3. CÁC MỐI QUAN HỆ (RELATIONSHIPS)
    // =========================================================================
    
    // Liên kết với bảng Account (Người đặt đơn hàng này)
    // Dùng EAGER để luôn tải thông tin người dùng kèm theo đơn hàng
    // JsonIgnoreProperties giúp chặn vòng lặp JSON vô hạn và ẩn password khi trả về API
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "account_username", nullable = false)
    @JsonIgnoreProperties({"password", "orders", "roles", "hibernateLazyInitializer", "handler"}) 
    private User account;
    
    // Liên kết với bảng Order_Detail (Danh sách các sản phẩm trong đơn)
    // CascadeType.ALL: Xóa đơn hàng thì tự động xóa luôn các chi tiết bên trong
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"order", "hibernateLazyInitializer", "handler"})
    private List<OrderDetail> orderDetails;
}