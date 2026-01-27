package poly.edu.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDTO {
    
    // Tên biến này phải khớp với JSON từ Vue gửi lên
    @NotBlank(message = "Mật khẩu hiện tại không được để trống.")
    private String currentPassword; 

    @NotBlank(message = "Mật khẩu mới không được để trống.")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự.")
    private String newPassword;
    
    @NotBlank(message = "Xác nhận mật khẩu không được để trống.")
    private String confirmPassword;
}