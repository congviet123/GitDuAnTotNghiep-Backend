
package poly.edu.entity.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderListDTO {
    private Integer id;
    private Date createDate;
    private BigDecimal totalAmount;
    private String status;
    private String accountFullname; // Tên đầy đủ người đặt (cho Admin)
}