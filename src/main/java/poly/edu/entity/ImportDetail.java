package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "Import_Detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Lưu ID Sản phẩm
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    // Đổi từ Integer sang BigDecimal để đồng bộ với số lượng Kg của Product
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    // Giá nhập vào (Giá vốn)
    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    // Quan hệ ManyToOne với bảng Import (Phiếu nhập gốc)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_id")
    @JsonBackReference // Kết hợp với @JsonManagedReference ở Import.java để xử lý triệt để vòng lặp JSON
    private Import importEntity;
}