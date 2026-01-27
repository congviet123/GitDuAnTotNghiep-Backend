package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import poly.edu.entity.User;
import poly.edu.service.UserService;
import org.springframework.security.core.userdetails.User.UserBuilder;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm user trong DB
        User user = userService.findById(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // 2. Build UserDetails cho Spring Security
        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(username);
        builder.password(user.getPassword());
        
   
        // Lấy trạng thái từ DB: Nếu user.getEnabled() là false (0) -> disabled = true
        builder.disabled(!user.getEnabled()); 
        // -------------------------
        
        // Set quyền hạn
        builder.authorities(user.getRole().getName()); 
        
        return builder.build();
    }
}