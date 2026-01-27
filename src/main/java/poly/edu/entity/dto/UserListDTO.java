package poly.edu.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // Lombok sẽ tạo Constructor theo đúng thứ tự các biến bên dưới
@NoArgsConstructor
public class UserListDTO {
    
    private String username;
    
    private String fullname;
    
    private String email;
    
    // --- CÁC TRƯỜNG CHO ADMIN QUẢN LÝ ---
    
    private String address;   // Để hiển thị Địa chỉ
    
    private String phone;     // Để hiển thị Số điện thoại
    
    private String roleName;  // Tên quyền (ROLE_ADMIN, ROLE_STAFF...)
    
    private Boolean enabled;  // Để hiển thị trạng thái (Đang hoạt động hay Bị khóa)
}