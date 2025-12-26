package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.Role;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    // Phương thức cần thiết để tìm Role đã tồn tại trong DB theo tên
    Optional<Role> findByName(String name);
}