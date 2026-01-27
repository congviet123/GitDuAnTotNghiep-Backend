package poly.edu.service;

import poly.edu.entity.User;
import poly.edu.entity.dto.ChangePasswordDTO;
import poly.edu.entity.dto.UserListDTO;
import poly.edu.entity.dto.UserRegistrationDTO;
import poly.edu.entity.dto.UserUpdateDTO;

import java.util.List;
import java.util.Optional;

public interface UserService {
    
    // --- Basic CRUD (Admin) ---
    List<User> findAll();
    
    // Returns User or null (not throwing exception immediately) to allow flexible controller logic
    User findById(String username); 
    
    // [ADDED] Needed for Google Login logic in Controller
    Optional<User> findByEmail(String email);

    User create(User user);
    User update(User user);
    void delete(String username);
    
    List<UserListDTO> findAllForAdminList();

    // --- Functional Methods (Client) ---
    
    User register(UserRegistrationDTO registrationDTO);

    User updateProfile(String username, UserUpdateDTO updateDTO);

    void changePassword(String username, ChangePasswordDTO passDto);
    
    void forgotPassword(String email);
    
    List<User> getAdmins();
}