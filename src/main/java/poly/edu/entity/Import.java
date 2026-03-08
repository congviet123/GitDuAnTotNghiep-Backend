package poly.edu.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Import")
public class Import {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "importEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ImportDetail> details;

    @Column(name = "account_username")
    private String accountUsername;

    @Column(name = "supplier_id")
    private Integer supplierId;
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "notes")
    private String notes;

    @Column(name = "import_date")
    private LocalDateTime importDate;
}