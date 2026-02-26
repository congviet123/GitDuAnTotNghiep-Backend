package poly.edu.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface NewsEngagementService {

    void incrementView(Long newsId);

    void toggleLike(Long newsId, UserDetails userDetails);

    void recordShare(Long newsId, String platform, UserDetails userDetails);
}
