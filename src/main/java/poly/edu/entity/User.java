package poly.edu.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString; // [1] Import thêm cái này
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
// import poly.edu.entity.Order; 

@Entity
@Table(name = "Account")
@Data // @Data sinh ra toString(), equals(), hashCode()...
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    
    @Id
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String fullname;

    @Column(unique = true, nullable = false)
    private String email;

    private String address;
    private String phone;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    
    // Ngăn chặn toString() gọi sang Role -> Role gọi lại User -> Vòng lặp
    @ToString.Exclude 
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

   
    // Ngăn chặn toString() kích hoạt Hibernate tải toàn bộ danh sách Orders (LAZY loading)
    @JsonIgnore 
    @ToString.Exclude 
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> orders;
}