package poly.edu.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import poly.edu.entity.News;
import poly.edu.entity.NewsLike;
import poly.edu.entity.NewsShare;
import poly.edu.entity.NewsView;
import poly.edu.entity.User;
import poly.edu.repository.NewsLikeRepository;
import poly.edu.repository.NewsRepository;
import poly.edu.repository.NewsShareRepository;
import poly.edu.repository.NewsViewRepository;
import poly.edu.repository.UserRepository;
import poly.edu.service.NewsEngagementService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NewsEngagementServiceImpl implements NewsEngagementService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final NewsViewRepository newsViewRepository;
    private final NewsLikeRepository newsLikeRepository;
    private final NewsShareRepository newsShareRepository;

    @Override
    public void incrementView(Long newsId) {
        log.info("Initiating view count increment for news ID: {}", newsId);
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found with Id: " + newsId));
        NewsView newsView = NewsView.builder()
                .user(null)
                .news(news)
                .build();
        newsViewRepository.save(newsView);
        log.info("Successfully incremented view count for news ID: {}", newsId);
    }

    @Override
    public void toggleLike(Long newsId, UserDetails userDetails) {
        log.info("Attempting to toggle like on news ID: {}", newsId);
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found with Id: " + newsId));
        System.out.println("zzz");
        if (!newsLikeRepository.hasUserLikedNews(username, newsId)) {
            NewsLike newsLike = NewsLike.builder()
                    .user(user)
                    .news(news)
                    .build();
            newsLikeRepository.save(newsLike);
        } else {
            newsLikeRepository.deleteByUsernameAndNewsId(username, newsId);
        }
        log.info("Successfully toggled like for news ID: {}", newsId);
    }

    @Override
    public void recordShare(Long newsId, String platform, UserDetails userDetails) {
        log.info("Recording a new share for news ID: {}", newsId);
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found with Id: " + newsId));
        NewsShare newsShare = NewsShare.builder()
                .user(user)
                .news(news)
                .platform(platform)
                .build();
        newsShareRepository.save(newsShare);
        log.info("Successfully recorded share for news ID: {}", newsId);
    }

}
