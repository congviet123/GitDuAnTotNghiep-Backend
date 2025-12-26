package poly.edu.entity.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReportRevenueDTO {
    private String name; // Tên (ví dụ: Tên Category, Tên Month)
    private Long count; // Số lượng đơn hàng/sản phẩm
    private BigDecimal sum; // Tổng doanh thu
}