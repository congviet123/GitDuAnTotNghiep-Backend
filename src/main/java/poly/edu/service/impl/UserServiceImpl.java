package poly.edu.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Role;
import poly.edu.entity.User;
import poly.edu.entity.dto.*;
import poly.edu.repository.RoleRepository;
import poly.edu.repository.UserRepository;
import poly.edu.service.MailService;
import poly.edu.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MailService mailService;

    private static final String DEFAULT_USER_ROLE = "ROLE_USER"; 
    // 1. LOGIC QUÊN MẬT KHẨU (Gửi HTML Mail)
    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống."));
        
        // Sinh mật khẩu ngẫu nhiên 6 ký tự
        String newRawPassword = UUID.randomUUID().toString().substring(0, 6);
        
        // Lưu mật khẩu mã hóa vào DB
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);

        // Gửi mật khẩu gốc qua Email (HTML)
        String subject = "Cấp lại mật khẩu - Website Trái Cây";
        String body = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>"
                + "<h2 style='color: #2e8b57;'>Xin chào " + user.getFullname() + ",</h2>"
                + "<p>Chúng tôi nhận được yêu cầu cấp lại mật khẩu cho tài khoản của bạn.</p>"
                + "<p>Mật khẩu mới của bạn là: <b style='color: #e74c3c; font-size: 20px; letter-spacing: 2px;'>" + newRawPassword + "</b></p>"
                + "<p>Vui lòng đăng nhập và đổi lại mật khẩu ngay lập tức để bảo mật tài khoản.</p>"
                + "<hr/>"
                + "<p style='font-size: 12px; color: #666;'>Đây là email tự động, vui lòng không trả lời.</p>"
                + "</div>";

        mailService.sendEmail(email, subject, body);
    }

 
    
    // 2. LOGIC ADMIN & CRUD
    @Override
    @Transactional(readOnly = true)
    public List<UserListDTO> findAllForAdminList() {
        return userRepository.findAllUserListDTO();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(String username) {
        return userRepository.findById(username).orElse(null);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public User create(User user) {
        if (user.getRole() == null || user.getRole().getName() == null || user.getRole().getName().trim().isEmpty()) {
            user.setRole(new Role());
            user.getRole().setName(DEFAULT_USER_ROLE); 
        }
        
        String roleNameFromForm = user.getRole().getName().toUpperCase().trim();
        Role existingRole = roleRepository.findByName(roleNameFromForm)
                .orElseThrow(() -> new RuntimeException("Vai trò không hợp lệ: " + roleNameFromForm));
        
        user.setRole(existingRole);
        
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(User user) {
        User existingUser = findById(user.getUsername());
        if (existingUser == null) {
             throw new RuntimeException("Không tìm thấy user: " + user.getUsername());
        }
        
        if (user.getRole() != null && user.getRole().getName() != null && !user.getRole().getName().trim().isEmpty()) {
            String roleNameFromForm = user.getRole().getName().toUpperCase().trim();
            Role existingRole = roleRepository.findByName(roleNameFromForm)
                    .orElseThrow(() -> new RuntimeException("Vai trò không hợp lệ: " + roleNameFromForm));
            existingUser.setRole(existingRole);
        }
        
        existingUser.setFullname(user.getFullname());
        existingUser.setEmail(user.getEmail());
        existingUser.setAddress(user.getAddress());
        existingUser.setPhone(user.getPhone());
        
        return userRepository.save(existingUser);
    }

    @Override
    public void delete(String username) {
        userRepository.deleteById(username);
    }

    // =========================================================
    // 3. LOGIC CLIENT
    // =========================================================
    @Override
    @Transactional
    public User register(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsById(registrationDTO.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại.");
        }
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng.");
        }
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu và Xác nhận mật khẩu không khớp.");
        }
        
        Role userRole = roleRepository.findByName(DEFAULT_USER_ROLE)
                        .orElseThrow(() -> new RuntimeException("Lỗi cấu hình: Role không tồn tại."));
        
        User newUser = new User();
        BeanUtils.copyProperties(registrationDTO, newUser);
        newUser.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        newUser.setRole(userRole);
        newUser.setEnabled(true);
        if (newUser.getAddress() == null) newUser.setAddress("");
        if (newUser.getPhone() == null) newUser.setPhone("");
        
        return userRepository.save(newUser);
    }

    @Override
    @Transactional
    public User updateProfile(String username, UserUpdateDTO updateDTO) {
         User existingUser = findById(username);
        if (existingUser == null) throw new RuntimeException("User not found");
        
        if (!existingUser.getEmail().equals(updateDTO.getEmail())) {
             if (userRepository.findByEmail(updateDTO.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã được sử dụng bởi tài khoản khác.");
            }
        }
        
        existingUser.setFullname(updateDTO.getFullname());
        existingUser.setEmail(updateDTO.getEmail());
        existingUser.setAddress(updateDTO.getAddress());
        existingUser.setPhone(updateDTO.getPhone());
        
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordDTO passDto) {
         User existingUser = findById(username);
        if (existingUser == null) throw new RuntimeException("User not found");
        
        if (!passwordEncoder.matches(passDto.getCurrentPassword(), existingUser.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác.");
        }
        
        if (passwordEncoder.matches(passDto.getNewPassword(), existingUser.getPassword())) {
            throw new RuntimeException("Mật khẩu mới không được giống mật khẩu cũ.");
        }

        if (!passDto.getNewPassword().equals(passDto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp.");
        }
        
        existingUser.setPassword(passwordEncoder.encode(passDto.getNewPassword()));
        userRepository.save(existingUser);
    }
}