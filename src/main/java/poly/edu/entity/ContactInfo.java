package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;
// import java.util.Date;  // Bỏ import này

@Entity
@Table(name = "Shop_Info")
@Data
public class ContactInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "address", columnDefinition = "nvarchar(255)", nullable = false)
    private String address;
    
    @Column(name = "phone", length = 50, nullable = false)
    private String phone;
    
    @Column(name = "email", length = 100, nullable = false)
    private String email;
    
    // COMMENT hoặc XÓA các dòng này
    // @Column(name = "last_updated")
    // private Date lastUpdated;
    
    @Column(name = "map_iframe", columnDefinition = "nvarchar(max)")
    private String mapIframe;
    
    // XÓA hoặc COMMENT method này
    // @PreUpdate
    // @PrePersist
    // protected void onUpdate() {
    //     lastUpdated = new Date();
    // }
}