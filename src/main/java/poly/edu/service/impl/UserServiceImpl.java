package poly.edu.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private CartRepository cartRepository; 
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MailService mailService;
    
    private static final String DEFAULT_USER_ROLE_NAME = "ROLE_USER"; 
    
    // --- BỘ NHỚ TẠM CHO OTP ---
    // Key: Email | Value: Chuỗi chứa OTP và số lần đã nhập sai, ngăn cách bằng dấu gạch (VD: "123456_0")
    private static final ConcurrentHashMap<String, String> otpCache = new ConcurrentHashMap<>();

    @Override
    public List<User> getAdmins() {
        return userRepository.findAllAdmins();
    }

    // =========================================================
    // 1. LOGIC QUÊN MẬT KHẨU (CŨ - BẠN CÓ THỂ XÓA NẾU KHÔNG DÙNG NỮA)
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
        String body = "..."; // (Giữ nguyên chuỗi HTML cũ của bạn)
        mailService.sendEmail(email, subject, body);
    }

    // =========================================================
    // LẤY LẠI MẬT KHẨU BẰNG OTP (3 BƯỚC)
    // =========================================================

    // Bước 1: Tạo OTP và Gửi Mail
    @Override
    public void generateAndSendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email này chưa được đăng ký trong hệ thống."));

        // 1. Tạo mã OTP 6 số
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        // 2. Lưu vào cache kèm số lần nhập sai ban đầu là 0 (Định dạng: "MãOTP_SốLầnSai")
        otpCache.put(email, otp + "_0");

        // 3. Gửi Email (Giao diện HTML siêu đẹp)
        String subject = "Mã xác thực lấy lại mật khẩu - Trái Cây Bay";
        String body = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px; background-color: #fdfdfd;'>"
                + "<h2 style='color: #007bff; text-align: center; border-bottom: 2px solid #007bff; padding-bottom: 10px;'>Mã xác thực OTP</h2>"
                + "<p>Xin chào <b>" + user.getFullname() + "</b>,</p>"
                + "<p>Bạn vừa yêu cầu đặt lại mật khẩu tại hệ thống Trái Cây Bay.</p>"
                + "<p>Vui lòng nhập mã xác thực gồm 6 chữ số dưới đây để tiếp tục:</p>"
                + "<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0; border: 1px dashed #ccc;'>"
                + "<h1 style='color: #28a745; letter-spacing: 10px; margin: 10px 0; font-size: 36px;'>" + otp + "</h1>"
                + "</div>"
                + "<p style='color: #dc3545; font-size: 14px; font-weight: bold;'>⚠️ Tuyệt đối KHÔNG chia sẻ mã OTP này cho bất kỳ ai.</p>"
                + "<p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>"
                + "<hr style='border: 0; border-top: 1px solid #eee; margin-top: 30px;'/>"
                + "<p style='font-size: 12px; color: #888; text-align: center;'>Đội ngũ hỗ trợ Trái Cây Bay</p>"
                + "</div>";

        mailService.sendEmail(email, subject, body);
    }

    // Bước 2: Kiểm tra OTP và đếm số lần sai
    @Override
    public void verifyOtp(String email, String userInputOtp) {
        String cacheData = otpCache.get(email);
        
        if (cacheData == null) {
            throw new RuntimeException("Mã OTP đã hết hạn hoặc bạn chưa yêu cầu gửi mã.");
        }

        // Tách lấy mã OTP gốc và số lần sai hiện tại
        String[] parts = cacheData.split("_");
        String correctOtp = parts[0];
        int wrongAttempts = Integer.parseInt(parts[1]);

        if (correctOtp.equals(userInputOtp)) {
            // Nhập đúng -> Cập nhật lại trạng thái thành "verified" để cho phép đổi pass ở bước sau
            otpCache.put(email, "verified_0");
        } else {
            // Nhập sai -> Tăng biến đếm
            wrongAttempts++;
            if (wrongAttempts >= 3) {
                otpCache.remove(email); // Xóa luôn OTP
                throw new RuntimeException("Bạn đã nhập sai quá 3 lần. Mã OTP đã bị hủy, vui lòng yêu cầu gửi lại mã mới.");
            } else {
                otpCache.put(email, correctOtp + "_" + wrongAttempts); // Lưu lại số lần sai mới
                throw new RuntimeException("Mã OTP không chính xác. Bạn còn " + (3 - wrongAttempts) + " lần thử.");
            }
        }
    }

    // Bước 3: Đổi mật khẩu mới
    @Override
    @Transactional
    public void resetPasswordWithOtp(String email, String newPassword) {
        String cacheData = otpCache.get(email);
        
        // Chỉ cho phép đổi mật khẩu nếu trạng thái trong cache là "verified"
        if (cacheData == null || !cacheData.startsWith("verified")) {
            throw new RuntimeException("Bạn chưa xác thực mã OTP hoặc phiên làm việc đã hết hạn.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Đổi pass thành công thì dọn dẹp bộ nhớ đệm
        otpCache.remove(email);
    }


    // =========================================================
    // 2. LOGIC ADMIN & CRUD (Giữ nguyên)
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
        if (user.getRole() == null || (user.getRole().getId() == null && user.getRole().getName() == null)) {
             throw new RuntimeException("Vui lòng chọn vai trò cho người dùng.");
        }
        
        Role existingRole = null;
        if (user.getRole().getId() != null) {
             existingRole = roleRepository.findById(user.getRole().getId()).orElse(null);
        }
        if (existingRole == null && user.getRole().getName() != null) {
             existingRole = roleRepository.findByName(user.getRole().getName()).orElse(null);
        }

        if (existingRole == null) {
             throw new RuntimeException("Vai trò không hợp lệ.");
        }
        user.setRole(existingRole);
        
        if (userRepository.existsById(user.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng.");
        }
        
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
        
        boolean oldStatus = existingUser.getEnabled();

        if (user.getRole() != null && user.getRole().getId() != null) {
            if (!user.getRole().getId().equals(existingUser.getRole().getId())) {
                Role newRole = roleRepository.findById(user.getRole().getId())
                        .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
                existingUser.setRole(newRole);
            }
        }
        
        existingUser.setFullname(user.getFullname());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        
        if (user.getEnabled() != null) {
            existingUser.setEnabled(user.getEnabled());
        }

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        User savedUser = userRepository.save(existingUser);

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
<<<<<<< HEAD
            // [SỬA LỖI]: Xóa toàn bộ sản phẩm trong giỏ của user này trước khi xóa user
            // Sử dụng hàm đã định nghĩa trong CartRepository
            cartRepository.deleteAllByAccountUsername(username);
            
            // Xóa user
=======
            cartRepository.deleteAllByUser_Username(username);
>>>>>>> 590b36084399651b3a23ae0fa10bd74eb89ddac2
            userRepository.deleteById(username);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException("Tài khoản dính líu dữ liệu khác (Review, Comment...). Không thể xóa, hãy KHÓA tài khoản.");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa: " + e.getMessage());
        }
    }

    // =========================================================
    // 3. LOGIC ĐĂNG KÝ & CẬP NHẬT PROFILE (Giữ nguyên)
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
        BeanUtils.copyProperties(registrationDTO, newUser);
        
        newUser.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        newUser.setRole(userRole);
        newUser.setEnabled(true);
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
    
    @Override
    @Transactional
    public void setupNewPassword(String username, String newPassword) {
        User existingUser = findById(username);
        if (existingUser == null) throw new RuntimeException("Không tìm thấy người dùng.");
        
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(existingUser);
    }
}