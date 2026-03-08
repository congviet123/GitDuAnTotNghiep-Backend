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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private CartRepository cartRepository; 
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MailService mailService;
    
    private static final String DEFAULT_USER_ROLE_NAME = "ROLE_USER"; 
    
    // Bộ nhớ tạm lưu trữ OTP trên RAM của Server.
    // Cấu trúc: Key là Email (String) -> Value là chuỗi "MãOTP_SốLầnSai" (Ví dụ: "123456_0")
    // Dùng ConcurrentHashMap để an toàn khi có nhiều người dùng cùng lúc (Thread-safe)
    private static final ConcurrentHashMap<String, String> otpCache = new ConcurrentHashMap<>();

    @Override
    public List<User> getAdmins() {
        return userRepository.findAllAdmins();
    }

    // =========================================================
    // 1. NHÓM CHỨC NĂNG QUÊN MẬT KHẨU (BẰNG MÃ OTP 6 SỐ)
    // =========================================================

    // Bước 1 - Sinh mã OTP ngẫu nhiên và gửi về Email khách hàng
    @Override
    public void generateAndSendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email này chưa được đăng ký trong hệ thống."));

        // Tạo mã OTP 6 số (từ 000000 đến 999999)
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        // Lưu vào cache, mặc định số lần nhập sai ban đầu là 0
        otpCache.put(email, otp + "_0");

        // Giao diện HTML của Email chứa mã OTP
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

    // Bước 2 - Xác thực mã OTP mà người dùng nhập vào
    @Override
    public void verifyOtp(String email, String userInputOtp) {
        String cacheData = otpCache.get(email);
        
        if (cacheData == null) {
            throw new RuntimeException("Mã OTP đã hết hạn hoặc bạn chưa yêu cầu gửi mã.");
        }

        // Tách chuỗi để lấy OTP gốc và số lần đã nhập sai
        String[] parts = cacheData.split("_");
        String correctOtp = parts[0];
        int wrongAttempts = Integer.parseInt(parts[1]);

        if (correctOtp.equals(userInputOtp)) {
            // [GHI CHÚ]: Nếu đúng OTP -> Gắn cờ "verified" để cho phép bước đổi mật khẩu
            otpCache.put(email, "verified_0");
        } else {
            // [GHI CHÚ]: Nếu sai OTP -> Tăng số lần sai lên 1. Quá 3 lần sẽ hủy OTP.
            wrongAttempts++;
            if (wrongAttempts >= 3) {
                otpCache.remove(email); 
                throw new RuntimeException("Bạn đã nhập sai quá 3 lần. Mã OTP đã bị hủy, vui lòng yêu cầu gửi lại mã mới.");
            } else {
                otpCache.put(email, correctOtp + "_" + wrongAttempts); 
                throw new RuntimeException("Mã OTP không chính xác. Bạn còn " + (3 - wrongAttempts) + " lần thử.");
            }
        }
    }

    // Bước 3 - Cập nhật mật khẩu mới vào Database sau khi OTP đã hợp lệ
    @Override
    @Transactional
    public void resetPasswordWithOtp(String email, String newPassword) {
        String cacheData = otpCache.get(email);
        
        // Kiểm tra xem email này đã vượt qua bước "verifyOtp" chưa (phải có chữ "verified")
        if (cacheData == null || !cacheData.startsWith("verified")) {
            throw new RuntimeException("Bạn chưa xác thực mã OTP hoặc phiên làm việc đã hết hạn.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // [GHI CHÚ]: Đổi pass thành công thì phải xóa rác trong bộ nhớ đệm
        otpCache.remove(email);
    }


    // =========================================================
    // 2. NHÓM CHỨC NĂNG DÀNH CHO ADMIN (QUẢN LÝ TÀI KHOẢN)
    // =========================================================
    
    // Lấy danh sách User kèm theo Sổ địa chỉ để hiển thị lên bảng quản trị (VueJS)
    @Override
    @Transactional(readOnly = true)
    public List<UserListDTO> findAllForAdminList() {
        List<User> users = userRepository.findAll();
        List<UserListDTO> dtoList = new ArrayList<>();
        
        for (User u : users) {
            UserListDTO dto = new UserListDTO();
            dto.setUsername(u.getUsername());
            dto.setFullname(u.getFullname());
            dto.setEmail(u.getEmail());
            dto.setPhone(u.getPhone());
            dto.setRoleName(u.getRole() != null ? u.getRole().getName() : "");
            dto.setEnabled(u.getEnabled());
            
            // Ép mảng địa chỉ vào DTO để VueJS có dữ liệu hiển thị (Cột "Địa chỉ mặc định")
            dto.setAddresses(u.getAddresses());
            
            dtoList.add(dto);
        }
        
        return dtoList;
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

    // Hàm tạo User mới từ giao diện Admin
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
        
        // Mã hóa mật khẩu trước khi lưu xuống DB
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        return userRepository.save(user);
    }

    // Hàm cập nhật User từ giao diện Admin (Kèm chức năng tự động gửi Mail báo khóa/mở khóa)
    @Override
    @Transactional
    public User update(User user) {
        User existingUser = findById(user.getUsername());
        if (existingUser == null) {
             throw new RuntimeException("Không tìm thấy user: " + user.getUsername());
        }
        
        // Lưu lại trạng thái cũ để biết Admin vừa "Khóa" hay "Mở khóa"
        boolean oldStatus = existingUser.getEnabled();

        // Cập nhật Quyền (Role)
        if (user.getRole() != null && user.getRole().getId() != null) {
            if (!user.getRole().getId().equals(existingUser.getRole().getId())) {
                Role newRole = roleRepository.findById(user.getRole().getId())
                        .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
                existingUser.setRole(newRole);
            }
        }
        
        // Cập nhật thông tin cá nhân
        existingUser.setFullname(user.getFullname());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        
        // Cập nhật Trạng thái hoạt động (Khóa / Mở)
        if (user.getEnabled() != null) {
            existingUser.setEnabled(user.getEnabled());
        }

        // Cập nhật Mật khẩu (nếu Admin có nhập pass mới)
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        User savedUser = userRepository.save(existingUser);

        // Logic gửi Email khi tài khoản bị thay đổi trạng thái
        try {
            if (oldStatus && !savedUser.getEnabled()) {
                // Đang hoạt động -> Bị khóa
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
                // Đang bị khóa -> Được mở lại
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

    // Hàm xóa người dùng (Chặn xóa nếu người dùng đã có lịch sử Đơn hàng)
    @Override
    @Transactional
    public void delete(String username) {
        User user = userRepository.findById(username).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (user.getOrders() != null && !user.getOrders().isEmpty()) {
            throw new RuntimeException("Không thể xóa tài khoản [" + username + "] vì đã có lịch sử Đơn hàng. Vui lòng KHÓA tài khoản thay thế.");
        }

        try {
            cartRepository.deleteAllByUser_Username(username); // Xóa giỏ hàng trước
            userRepository.deleteById(username); // Sau đó mới xóa User
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException("Tài khoản dính líu dữ liệu khác (Review, Comment...). Không thể xóa, hãy KHÓA tài khoản.");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa: " + e.getMessage());
        }
    }


    // =========================================================
    // 3. NHÓM CHỨC NĂNG DÀNH CHO KHÁCH HÀNG (CLIENT)
    // =========================================================
    
    // Đăng ký tài khoản mới trên Website
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

    // Cập nhật hồ sơ (Profile) của khách hàng
    @Override
    @Transactional
    public User updateProfile(String username, UserUpdateDTO updateDTO) {
        User existingUser = findById(username);
        if (existingUser == null) throw new RuntimeException("User not found");
        
        // Kiểm tra xem email mới muốn đổi có bị trùng với người khác không
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

    // Đổi mật khẩu an toàn (Yêu cầu nhập mật khẩu cũ)
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
    
    // Thiết lập mật khẩu cho tài khoản đăng nhập bằng Google lần đầu
    @Override
    @Transactional
    public void setupNewPassword(String username, String newPassword) {
        User existingUser = findById(username);
        if (existingUser == null) throw new RuntimeException("Không tìm thấy người dùng.");
        
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(existingUser);
    }
}