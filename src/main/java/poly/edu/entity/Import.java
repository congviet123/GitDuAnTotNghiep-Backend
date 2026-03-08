package poly.edu.entity;

import jakarta.persistence.*;
<<<<<<< HEAD
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
=======
import lombok.*;
>>>>>>> 26c4aa114effa03283d0371373d861a845cf6c72

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

<<<<<<< HEAD
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "Import") // Bảng Phiếu Nhập Hàng
@Data
@NoArgsConstructor
@AllArgsConstructor
=======
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Import")
>>>>>>> 26c4aa114effa03283d0371373d861a845cf6c72
public class Import {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

<<<<<<< HEAD
    // Thời gian nhập hàng
    @Column(name = "import_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime importDate;

    // ID của Nhà cung cấp (Lưu ID thay vì Object để giảm tải truy vấn)
    @Column(name = "supplier_id")
    private Integer supplierId;
=======
    @OneToMany(mappedBy = "importEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ImportDetail> details;
>>>>>>> 26c4aa114effa03283d0371373d861a845cf6c72

    // Tài khoản Admin thực hiện nhập hàng
    @Column(name = "account_username")
    private String accountUsername;

<<<<<<< HEAD
    // Tổng tiền của phiếu nhập
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    // Ghi chú thêm
    @Column(name = "notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;

    // Danh sách chi tiết phiếu nhập
    // Dùng @JsonManagedReference để khi trả về JSON cho VueJS không bị lỗi vòng lặp vô hạn
    @OneToMany(mappedBy = "importEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference 
    private List<ImportDetail> details;

    // Tự động gán thời gian hiện tại nếu khi lưu chưa set thời gian
    @PrePersist
    public void prePersist() {
        if (this.importDate == null) {
            this.importDate = LocalDateTime.now();
        }
    }
=======
    @Column(name = "supplier_id")
    private Integer supplierId;
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "notes")
    private String notes;

    @Column(name = "import_date")
    private LocalDateTime importDate;
>>>>>>> 26c4aa114effa03283d0371373d861a845cf6c72
}