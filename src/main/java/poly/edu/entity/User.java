package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "Account") // Map với bảng Account trong DB
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    
    @Id
    @Column(length = 50)
    private String username;

    // Chỉ ghi (Write), không trả về khi đọc (Read) để bảo mật
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, columnDefinition = "nvarchar(100)")
    private String fullname;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true; 

    // --- MAPPING ROLE (N - 1) ---
    @ToString.Exclude 
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false) 
    private Role role;

    // --- MAPPING ADDRESS (1 - N) ---
    // Một User có nhiều địa chỉ. "user" là tên biến trong class Address
    @JsonIgnore 
    @ToString.Exclude
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Address> addresses;

    // --- MAPPING ORDERS (1 - N) ---
    @JsonIgnore 
    @ToString.Exclude 
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> orders;
}