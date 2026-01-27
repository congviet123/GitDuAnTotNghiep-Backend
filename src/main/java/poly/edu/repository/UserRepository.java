package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poly.edu.entity.User;
import poly.edu.entity.dto.UserListDTO;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
    
    // Lấy danh sách User dưới dạng UserListDTO để hiển thị cho Admin
    @Query("SELECT new poly.edu.entity.dto.UserListDTO(u.username, u.fullname, u.email, u.address, u.phone, u.role.name, u.enabled) " +
            "FROM User u")
     List<UserListDTO> findAllUserListDTO();

 
    // Lấy danh sách tất cả Admin (có thể có nhiều tên role khác nhau cho Admin)
    @Query("SELECT u FROM User u WHERE u.role.name IN ('ADMIN', 'ROLE_ADMIN')")
    List<User> findAllAdmins();
}