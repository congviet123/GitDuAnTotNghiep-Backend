package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
@Entity
@Table(name = "Role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id; 

    @Column(name = "Name", unique = true, nullable = false)
    private String name;

    // [QUAN TRỌNG] Ngắt vòng lặp vô tận (StackOverflow) khi login
    @JsonIgnore 
    @ToString.Exclude 
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private List<User> users; 
}