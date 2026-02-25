	package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Import")
@Data
public class Import {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "import_date")
    private LocalDateTime importDate;

    @Column(name = "supplier_id")
    private Integer supplierId;

    @Column(name = "account_username")
    private String accountUsername;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    private String notes;

    @OneToMany(mappedBy = "importEntity", cascade = CascadeType.ALL)
    private List<ImportDetail> details;
}
