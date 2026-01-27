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
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired private UserRepository userRepository;

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
            
            // --- [CẬP NHẬT] LOGIC CHẶN VÀ THÔNG BÁO LỖI ---
            if (!user.getEnabled()) {
                // Ném lỗi với message chuẩn theo yêu cầu
                throw new OAuth2AuthenticationException(new OAuth2Error("account_disabled"), 
                        "Tài khoản của bạn đã tạm bị khóa, vui lòng liên hệ admin để mở tài khoản");
            }
            
            // Cập nhật tên nếu có thay đổi từ phía Google
            if (name != null && !name.equals(user.getFullname())) {
                user.setFullname(name);
                userRepository.save(user);
            }
        } else {
            // Nếu chưa có tài khoản -> Chặn đăng nhập (Logic cũ)
            // Mã lỗi "unregistered" sẽ được hứng ở SecurityConfig
            throw new OAuth2AuthenticationException(new OAuth2Error("unregistered"), "Email này chưa đăng ký tài khoản.");
        }

        // 3. Trả về user với quyền hạn lấy từ DB
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getName())),
                oauth2User.getAttributes(),
                "email"
        );
    }
}