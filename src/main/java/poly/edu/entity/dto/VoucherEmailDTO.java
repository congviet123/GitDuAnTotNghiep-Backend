package poly.edu.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherEmailDTO {
    private String voucherCode;
    private String sendType; // "all" hoặc "single"
    private List<String> emails; // Danh sách email nếu chọn cá nhân
}