package poly.edu.service;

import org.springframework.security.core.userdetails.UserDetails;
import poly.edu.entity.dto.ShareDTO;

public interface NewsEngagementService {

    void incrementView(Long newsId);

    void toggleLike(Long newsId, UserDetails userDetails);

    void recordShare(Long newsId, ShareDTO shareDTO, UserDetails userDetails);
}
