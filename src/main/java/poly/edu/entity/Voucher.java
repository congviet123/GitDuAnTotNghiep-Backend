package poly.edu.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Voucher")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    
    @Id
    @Column(name = "code", length = 50)
    private String code;
    
    @Column(name = "name", length = 255, nullable = false)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "discount_percent")
    private Integer discountPercent = 0;
    
    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "max_discount_amount", precision = 18, scale = 2)
    private BigDecimal maxDiscountAmount = BigDecimal.ZERO;
    
    @Column(name = "min_condition", precision = 18, scale = 2)
    private BigDecimal minCondition = BigDecimal.ZERO;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "quantity")
    private Integer quantity = 0;
    
    @Column(name = "used_count")
    private Integer usedCount = 0;
    
    @Column(name = "per_user_limit")
    private Integer perUserLimit = 1;
    
    @Column(name = "visibility")
    private Boolean visibility = true;
    
    @Column(name = "active")
    private Boolean active = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}