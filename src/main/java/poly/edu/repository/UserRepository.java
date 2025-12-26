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
    
    // 1. Find user by Email (Used for Google Login)
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByEmail(String email);

    // 2. Get simplified User list (Used for Admin User Management)
    // Note: Package 'poly.edu.entity.dto' must match where you created the DTO file
    @Query("SELECT new poly.edu.entity.dto.UserListDTO(u.username, u.fullname, u.email, u.role.name) " +
           "FROM User u " +
           "ORDER BY u.username ASC")
    List<UserListDTO> findAllUserListDTO();
}