package poly.edu.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ImportDetailDTO {
    private Integer productId;
    private Integer quantity;
    private BigDecimal unitPrice;
}
