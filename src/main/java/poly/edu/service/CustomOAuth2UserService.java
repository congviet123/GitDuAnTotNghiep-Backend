package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import poly.edu.entity.User;
import poly.edu.repository.UserRepository;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired private UserRepository userRepository;

    //  BỘ NHỚ TẠM LƯU THÔNG TIN GOOGLE
    // Lưu trữ theo dạng: Key = tempToken, Value = Thông tin từ Google
    public static final Map<String, OAuth2User> pendingGoogleUsers = new ConcurrentHashMap<>();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Lấy thông tin từ Google
        OAuth2User oauth2User = super.loadUser(userRequest);
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        // 2. Kiểm tra trong Database
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            
            // --- LOGIC CHẶN VÀ THÔNG BÁO LỖI TÀI KHOẢN KHÓA ---
            if (!user.getEnabled()) {
                throw new OAuth2AuthenticationException(new OAuth2Error("account_disabled"), 
                        "Tài khoản của bạn đã tạm bị khóa, vui lòng liên hệ admin để mở tài khoản");
            }
            
            // Cập nhật tên nếu có thay đổi từ phía Google
            if (name != null && !name.equals(user.getFullname())) {
                user.setFullname(name);
                userRepository.save(user);
            }
        } else {
            // XỬ LÝ CHƯA ĐĂNG KÝ TÀI KHOẢN
            
            // Tạo 1 mã token ngẫu nhiên
            String tempToken = UUID.randomUUID().toString();
            
            // Lưu thông tin Google vào bộ nhớ tạm trong 1 khoảng thời gian ngắn
            pendingGoogleUsers.put(tempToken, oauth2User);
            
            // Ném lỗi kèm theo tempToken để truyền sang file SecurityConfig.
            // Mã lỗi sẽ có dạng: unregistered|mã-token-ngẫu-nhiên
            throw new OAuth2AuthenticationException(new OAuth2Error("unregistered|" + tempToken), "Email này chưa đăng ký tài khoản.");
        }

        // 3. Trả về user với quyền hạn lấy từ DB
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getName())),
                oauth2User.getAttributes(),
                "email"
        );
    }
}