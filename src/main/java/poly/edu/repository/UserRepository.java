package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.User;
import poly.edu.entity.dto.UserListDTO;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    @Query("SELECT new poly.edu.entity.dto.UserListDTO(" +
           "u.username, " +
           "u.fullname, " +
           "u.email, " +
           "'' , " +  
           "u.phone, " +
           "u.role.name, " +
           "u.enabled) " +
           "FROM User u")
    List<UserListDTO> findAllUserListDTO();

    @Query("SELECT u FROM User u WHERE u.role.name IN ('ROLE_ADMIN', 'ROLE_STAFF')")
    List<User> findAllAdmins();
    
    //  Lấy danh sách user theo role 
    @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    List<User> findByRole_Name(@Param("roleName") String roleName);


    //  Lọc danh sách User cho Admin với tham số keyword, role và status (enabled)
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:role IS NULL OR u.role.name = :role) " +
           "AND (:status IS NULL OR u.enabled = :status)")
    List<User> filterUsersForAdmin(@Param("keyword") String keyword, @Param("role") String role, @Param("status") Boolean status);
}