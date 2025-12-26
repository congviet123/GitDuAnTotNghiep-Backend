package poly.edu.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // Tạo Constructor có đầy đủ tham số (Quan trọng cho câu Query trên)
@NoArgsConstructor
public class UserListDTO {
    private String username;
    private String fullname;
    private String email;
    private String roleName; // Tương ứng với u.role.name
}