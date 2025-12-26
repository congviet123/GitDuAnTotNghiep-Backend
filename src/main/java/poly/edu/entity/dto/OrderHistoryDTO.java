package poly.edu.entity.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderHistoryDTO {
    private Integer id;
    private Date createDate;
    private String shippingAddress;
    private String status;
    private BigDecimal totalAmount;
    private String username;
    private List<OrderDetailDTO> details; // Dùng để hiển thị chi tiết khi cần
}