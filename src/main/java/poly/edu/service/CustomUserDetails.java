// Đặt file này trong gói poly.edu.service (hoặc poly.edu.service.impl)
package poly.edu.service; 

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;
import java.io.Serializable; // <--- THÊM IMPORT NÀY

import poly.edu.entity.User; // Entity User của bạn

// TRIỂN KHAI UserDetails TRỰC TIẾP và implements Serializable
public class CustomUserDetails implements UserDetails, Serializable { // <--- THÊM implements Serializable

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // --- Phương thức Getter cần thiết cho Thymeleaf ---
    public String getFullname() {
        return user.getFullname();
    }
    
    public User getUserEntity() {
        return user;
    }
    
    // --- UserDetails Methods (Cần thiết cho Spring Security) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRole().getName().describeConstable().stream()
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled();
    }
}