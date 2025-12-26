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
        User user = userService.findById(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(username);
        builder.password(user.getPassword());
        
        // [QUAN TRỌNG] DB lưu "ROLE_ADMIN" -> dùng authorities để giữ nguyên chuỗi này
        // Nếu dùng roles() nó sẽ thành ROLE_ROLE_ADMIN (Sai)
        builder.authorities(user.getRole().getName()); 
        
        return builder.build();
    }
}