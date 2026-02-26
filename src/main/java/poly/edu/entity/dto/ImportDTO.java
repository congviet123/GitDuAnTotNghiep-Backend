package poly.edu.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ImportDTO {

    private Integer supplierId;
    private String accountUsername;
    private BigDecimal totalAmount;
    private String notes;
    private List<ImportDetailDTO> details;
}
