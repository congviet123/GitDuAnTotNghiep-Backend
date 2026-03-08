package poly.edu.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.entity.Address;

import java.util.List;

@Data
@AllArgsConstructor 
@NoArgsConstructor
public class UserListDTO {
    
    private String username;
    
    private String fullname;
    
    private String email;
    
    // --- CÁC TRƯỜNG CHO ADMIN QUẢN LÝ ---
    
    private String address;   // Giữ lại trường cũ để tránh lỗi với các Query hiện tại
    
    // Thêm danh sách địa chỉ để gửi về cho VueJS xử lý giao diện
    private List<Address> addresses; 
    
    private String phone;     // Để hiển thị Số điện thoại
    
    private String roleName;  // Tên quyền (ROLE_ADMIN, ROLE_STAFF...)
    
    private Boolean enabled;  // Để hiển thị trạng thái (Đang hoạt động hay Bị khóa)

    // Constructor tương thích ngược để đảm bảo không làm "sập" các tính năng hiện tại khi thêm các trường mới
    public UserListDTO(String username, String fullname, String email, String address, String phone, String roleName, Boolean enabled) {
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.roleName = roleName;
        this.enabled = enabled;
    }
}
