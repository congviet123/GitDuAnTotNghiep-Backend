package poly.edu.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDTO {
    private String code;
    private String name;
    private String description;
    private String type;        // "percentage" hoặc "fixed"
    private BigDecimal value;   // giá trị giảm
    private BigDecimal minOrderValue;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer perUserLimit;
    private String status;      // "draft" hoặc "published"
    private String visibility;  // "public" hoặc "private"
    private LocalDate createdAt;
}