package poly.edu.entity.dto;

import lombok.Data;

// DTO đơn giản để nhận trạng thái mới từ yêu cầu PUT
@Data
public class StatusUpdateDTO {
    private String status;
}