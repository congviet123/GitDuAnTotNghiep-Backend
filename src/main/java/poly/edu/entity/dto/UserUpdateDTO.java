package poly.edu.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    
    // Username (thường là trường readonly, dùng để tìm kiếm User)
    @NotBlank
    private String username;

    // Các trường được phép cập nhật
    @NotBlank(message = "Họ và tên không được để trống.")
    private String fullname;
    
    private String address;
    private String phone;
    
    // Email (Có thể để readonly hoặc cho phép thay đổi nếu có xác thực email)
    @NotBlank
    private String email; 
}