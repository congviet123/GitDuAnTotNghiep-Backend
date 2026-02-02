package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.ToString;

import java.util.List;

public class thientest1 {
    @Id
    @Column(length = 50)
    private String username;

    // Chỉ cho phép ghi (Write) khi tạo mới/đổi pass.
    // Không bao giờ trả về Password trong JSON khi xem danh sách (Read).
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, columnDefinition = "nvarchar(100)") // Hỗ trợ lưu tên tiếng Việt có dấu
    private String fullname;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    // --- CÁC TRƯỜNG PHỤC VỤ QUẢN LÝ NGƯỜI DÙNG] ---

    @Column(columnDefinition = "nvarchar(255)") // Hỗ trợ lưu địa chỉ tiếng Việt
    private String address;

    @Column(length = 20)
    private String phone;


    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true; // Mặc định khi tạo mới là Cho phép hoạt động


    // --- MAPPING ROLE (QUYỀN HẠN) ---
    // Đây là trường quan trọng để Admin chọn quyền (Admin, Staff, Shipper, User) khi tạo mới
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER) // EAGER: Lấy user là biết ngay quyền gì
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // --- MAPPING ORDERS ---
    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> orders;
}
