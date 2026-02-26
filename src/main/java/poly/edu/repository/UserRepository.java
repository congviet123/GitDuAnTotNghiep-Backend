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
}