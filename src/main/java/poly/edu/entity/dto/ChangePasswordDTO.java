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
    
    // Mật khẩu hiện tại (dùng để xác thực)
    @NotBlank(message = "Mật khẩu hiện tại không được để trống.")
    private String currentPassword;

    // Mật khẩu mới
    @NotBlank(message = "Mật khẩu mới không được để trống.")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự.")
    private String newPassword;
    
    // Xác nhận mật khẩu mới (dùng để so sánh)
    @NotBlank(message = "Xác nhận mật khẩu không được để trống.")
    private String confirmPassword;
}