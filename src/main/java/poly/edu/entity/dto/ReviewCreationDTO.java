package poly.edu.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewCreationDTO {
    
    @NotNull(message = "Product ID không được để trống.")
    private Integer productId;

    @NotBlank(message = "Nội dung đánh giá không được để trống.")
    private String comment;

    @NotNull(message = "Số sao (rating) không được để trống.")
    @Min(value = 1, message = "Số sao phải lớn hơn hoặc bằng 1.")
    @Max(value = 5, message = "Số sao phải nhỏ hơn hoặc bằng 5.")
    private Integer rating;
}