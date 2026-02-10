package poly.edu.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Cart; 
import poly.edu.entity.Role;
import poly.edu.entity.User;
import poly.edu.entity.dto.*;
import poly.edu.repository.CartRepository; 
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
    @Autowired private CartRepository cartRepository; 
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MailService mailService;
    
    private static final String DEFAULT_USER_ROLE_NAME = "ROLE_USER"; 
    
    @Override
    public List<User> getAdmins() {
        return userRepository.findAllAdmins();
    }

    // =========================================================
    // 1. LOGIC QUÊN MẬT KHẨU
    // =========================================================
    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống."));
        
        String newRawPassword = UUID.randomUUID().toString().substring(0, 6);
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);

        String subject = "Cấp lại mật khẩu mới - Trái Cây Bay";
        
        String body = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px; background-color: #fdfdfd;'>"
                + "<h2 style='color: #007bff; text-align: center; border-bottom: 2px solid #007bff; padding-bottom: 10px;'>Yêu cầu cấp lại mật khẩu</h2>"
                + "<p>Xin chào <b>" + user.getFullname() + "</b>,</p>"
                + "<p>Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn.</p>"
                + "<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0;'>"
                + "<p style='margin: 0; color: #555;'>Mật khẩu mới của bạn là:</p>"
                + "<h1 style='color: #dc3545; letter-spacing: 5px; margin: 10px 0;'>" + newRawPassword + "</h1>"
                + "</div>"
                + "<p>Vì lý do bảo mật, vui lòng đăng nhập và đổi lại mật khẩu này ngay lập tức.</p>"
                + "<div style='text-align: center; margin-top: 30px; margin-bottom: 30px;'>"
                + "<a href='http://localhost:5173/login' style='background-color: #28a745; color: white; padding: 12px 25px; text-decoration: none; border-radius: 30px; font-weight: bold; font-size: 16px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>Đăng nhập ngay</a>"
                + "</div>"
                + "<hr style='border: 0; border-top: 1px solid #eee;'/>"
                + "<p style='font-size: 12px; color: #888; text-align: center;'>Email này được gửi tự động từ hệ thống Trái Cây Bay. Vui lòng không trả lời lại.</p>"
                + "</div>";

        mailService.sendEmail(email, subject, body);
    }

    // =========================================================
    // 2. LOGIC ADMIN & CRUD
    // =========================================================
    
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
        // 1. Kiểm tra Role
        if (user.getRole() == null || (user.getRole().getId() == null && user.getRole().getName() == null)) {
             throw new RuntimeException("Vui lòng chọn vai trò cho người dùng.");
        }
        
        Role existingRole = null;
        if(user.getRole().getId() != null) {
             existingRole = roleRepository.findById(user.getRole().getId()).orElse(null);
        }
        if(existingRole == null && user.getRole().getName() != null) {
             existingRole = roleRepository.findByName(user.getRole().getName()).orElse(null);
        }

        if(existingRole == null) {
             throw new RuntimeException("Vai trò không hợp lệ.");
        }
        user.setRole(existingRole);
        
        // 2. Kiểm tra trùng lặp
        if (userRepository.existsById(user.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng.");
        }
        
        // 3. Mã hóa mật khẩu
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // [SỬA LỖI]: Không setAddress vì đã tách bảng
        
        User savedUser = userRepository.save(user);
        createCartForUser(savedUser);

        return savedUser;
    }

    @Override
    @Transactional
    public User update(User user) {
        User existingUser = findById(user.getUsername());
        if (existingUser == null) {
             throw new RuntimeException("Không tìm thấy user: " + user.getUsername());
        }
        
        boolean oldStatus = existingUser.getEnabled();

        // 1. Cập nhật Role
        if (user.getRole() != null && user.getRole().getId() != null) {
            if (!user.getRole().getId().equals(existingUser.getRole().getId())) {
                Role newRole = roleRepository.findById(user.getRole().getId())
                        .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
                existingUser.setRole(newRole);
            }
        }
        
        // 2. Cập nhật thông tin cơ bản
        existingUser.setFullname(user.getFullname());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        
        // [SỬA LỖI]: Bỏ dòng setAddress(user.getAddress())
        
        // 3. Cập nhật trạng thái Khóa/Mở
        if (user.getEnabled() != null) {
            existingUser.setEnabled(user.getEnabled());
        }

        // 4. Cập nhật mật khẩu
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        User savedUser = userRepository.save(existingUser);

        // --- [LOGIC GỬI EMAIL KHÓA / MỞ KHÓA] ---
        try {
            if (oldStatus && !savedUser.getEnabled()) {
                String subject = "Thông báo KHÓA tài khoản - Trái Cây Bay";
                String body = "<div style='font-family: Arial; padding: 20px; border: 1px solid #f5c6cb; background-color: #f8d7da; border-radius: 5px;'>"
                        + "<h2 style='color: #721c24;'>Tài khoản của bạn đã bị tạm khóa</h2>"
                        + "<p>Xin chào <b>" + savedUser.getFullname() + "</b>,</p>"
                        + "<p>Tài khoản <b>" + savedUser.getUsername() + "</b> đã bị khóa bởi quản trị viên.</p>"
                        + "<p>Vui lòng liên hệ Admin để biết thêm chi tiết.</p>"
                        + "</div>";
                
                mailService.sendEmail(savedUser.getEmail(), subject, body);
            }
            else if (!oldStatus && savedUser.getEnabled()) {
                String subject = "Thông báo MỞ LẠI tài khoản - Trái Cây Bay";
                String body = "<div style='font-family: Arial; padding: 20px; border: 1px solid #c3e6cb; background-color: #d4edda; border-radius: 5px;'>"
                        + "<h2 style='color: #155724;'>Tài khoản của bạn đã được mở lại</h2>"
                        + "<p>Xin chào <b>" + savedUser.getFullname() + "</b>,</p>"
                        + "<p>Tin vui! Tài khoản <b>" + savedUser.getUsername() + "</b> của bạn đã được kích hoạt lại.</p>"
                        + "<p>Bạn có thể đăng nhập và mua sắm bình thường ngay bây giờ.</p>"
                        + "<hr>"
                        + "<div style='text-align: center; margin-top: 15px;'>"
                        + "<a href='http://localhost:5173/login' style='background: #28a745; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Đăng nhập ngay</a>"
                        + "</div>"
                        + "</div>";
                
                mailService.sendEmail(savedUser.getEmail(), subject, body);
            }
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail user update: " + e.getMessage());
        }
        
        return savedUser;
    }

    @Override
    @Transactional
    public void delete(String username) {
        User user = userRepository.findById(username).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (user.getOrders() != null && !user.getOrders().isEmpty()) {
            throw new RuntimeException("Không thể xóa tài khoản [" + username + "] vì đã có lịch sử Đơn hàng. Vui lòng KHÓA tài khoản thay thế.");
        }

        try {
            Cart cart = cartRepository.findByAccount_Username(username);
            if (cart != null) {
                cartRepository.delete(cart);
            }
            userRepository.deleteById(username);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException("Tài khoản dính líu dữ liệu khác. Không thể xóa, hãy KHÓA tài khoản.");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa: " + e.getMessage());
        }
    }

    // =========================================================
    // 3. LOGIC ĐĂNG KÝ & CẬP NHẬT PROFILE
    // =========================================================
    @Override
    @Transactional
    public User register(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsById(registrationDTO.getUsername())) throw new RuntimeException("Tên đăng nhập đã tồn tại.");
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) throw new RuntimeException("Email đã được sử dụng.");
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) throw new RuntimeException("Mật khẩu không khớp.");
        
        Role userRole = roleRepository.findByName(DEFAULT_USER_ROLE_NAME)
                                      .orElseThrow(() -> new RuntimeException("Lỗi cấu hình: Role mặc định không tồn tại."));
        
        User newUser = new User();
        // Copy các thuộc tính trùng tên (username, password, fullname, email...)
        BeanUtils.copyProperties(registrationDTO, newUser);
        
        newUser.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        newUser.setRole(userRole);
        newUser.setEnabled(true);
        if (newUser.getPhone() == null) newUser.setPhone("");
        
        User savedUser = userRepository.save(newUser);
        createCartForUser(savedUser);
        return savedUser;
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
        existingUser.setPhone(updateDTO.getPhone());
        
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordDTO passDto) {
        User existingUser = findById(username);
        if (existingUser == null) throw new RuntimeException("Không tìm thấy người dùng.");

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

    private void createCartForUser(User user) {
        try {
            if (cartRepository.findByAccount_Username(user.getUsername()) == null) {
                Cart cart = new Cart();
                cart.setAccount(user);
                cartRepository.save(cart);
            }
        } catch (Exception e) {
            System.err.println("Lỗi tạo Cart: " + e.getMessage());
        }
    }
}