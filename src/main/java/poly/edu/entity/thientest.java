package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.ToString;

import java.util.List;

public class thientest {
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
