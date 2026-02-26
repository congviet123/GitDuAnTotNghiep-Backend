package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Address") 
public class Address implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fullname", columnDefinition = "nvarchar(100)")
    private String fullname;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address_line", columnDefinition = "nvarchar(255)") // Ánh xạ rõ sang address_line
    private String addressLine;

    @Column(name = "province", columnDefinition = "nvarchar(100)")
    private String province;

    @Column(name = "district", columnDefinition = "nvarchar(100)")
    private String district;

    @Column(name = "ward", columnDefinition = "nvarchar(100)")
    private String ward;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    //Ngắt vòng lặp JSON gây lỗi 500
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username")
    private User user;

    public String getFullAddress() {
        return String.format("%s, %s, %s, %s", 
            addressLine != null ? addressLine : "", 
            ward != null ? ward : "", 
            district != null ? district : "", 
            province != null ? province : "");
    }
}