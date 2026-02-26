package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Table(name = "Contact_Info")
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
    
    @Column(name = "last_updated")
    private Date lastUpdated;
    
    @Column(name = "map_url", columnDefinition = "text")
    private String mapUrl;
    
    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        lastUpdated = new Date();
    }
}