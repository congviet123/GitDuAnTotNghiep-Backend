package poly.edu.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDTO {
    
    // Tên đăng nhập
    @NotBlank(message = "Tên đăng nhập không được để trống.")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4 đến 50 ký tự.")
    private String username;

    // Họ và tên
    @NotBlank(message = "Họ và tên không được để trống.")
    private String fullname;

    // Email
    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email không đúng định dạng.")
    private String email;

    // Mật khẩu
    @NotBlank(message = "Mật khẩu không được để trống.")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự.")
    private String password;
    
    // Xác nhận mật khẩu (dùng để so sánh trong Service/Controller)
    @NotBlank(message = "Xác nhận mật khẩu không được để trống.")
    private String confirmPassword;
}