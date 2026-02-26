package poly.edu.service.impl;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import poly.edu.entity.News;
import poly.edu.entity.User;
import poly.edu.entity.dto.NewsCreateDTO;
import poly.edu.entity.dto.NewsResponseDTO;
import poly.edu.entity.dto.NewsUpdateDTO;
import poly.edu.repository.NewsRepository;
import poly.edu.repository.UserRepository;
import poly.edu.service.NewsService;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;

    @Override
    public Page<NewsResponseDTO> getAllNews(int page, int size, String sortDir, String sortBy, String searchKeyWord) {
        log.info("Fetching news: page={}, size={}, sortDir={}, sortBy={}, searchKeyword={}",
                page, size, sortDir, sortBy, searchKeyWord);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<News> pageNewsResponse;
        if (searchKeyWord != null && !searchKeyWord.isEmpty()) {
            pageNewsResponse = newsRepository.searchdByTitle(searchKeyWord, pageable);
        } else {
            pageNewsResponse = newsRepository.findAll(pageable);
        }
        log.info("Fetching news successfull");
        return pageNewsResponse.map(n -> NewsResponseDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .image(n.getImage())
                .authorName(n.getUser().getFullname())
                .createDate(n.getCreateDate())
                .viewCount(n.getViewCount())
                .likeCount(n.getLikeCount())
                .build());
    }

    @Override
    public NewsResponseDTO getNewsById(Long id) {
        log.info("Fetching news with id: {}", id);
        News newsResponse = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));
        log.info("Fetchingn news successfully");
        return NewsResponseDTO.builder()
                .id(newsResponse.getId())
                .title(newsResponse.getTitle())
                .content(newsResponse.getContent())
                .image(newsResponse.getImage())
                .authorName(newsResponse.getUser().getFullname())
                .createDate(newsResponse.getCreateDate())
                .viewCount(newsResponse.getViewCount())
                .likeCount(newsResponse.getLikeCount())
                .build();
    }

    @Override
    public NewsResponseDTO createNews(NewsCreateDTO news, UserDetails userDetails) {
        log.info("Attempting to create a news with title: {}", news.getTitle());
        String currentUsername = userDetails.getUsername();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + currentUsername));
        News newsToSave = News.builder()
                .title(news.getTitle())
                .content(news.getContent())
                .image(news.getImage())
                .user(user)
                .build();
        News newsResponse = newsRepository.save(newsToSave);
        log.info("News created successfully with title: {}", news.getTitle());
        return NewsResponseDTO.builder()
                .id(newsResponse.getId())
                .title(newsResponse.getTitle())
                .content(newsResponse.getContent())
                .image(newsResponse.getImage())
                .authorName(newsResponse.getUser().getFullname())
                .createDate(newsResponse.getCreateDate())
                .viewCount(newsResponse.getViewCount())
                .likeCount(newsResponse.getLikeCount())
                .build();
    }

    @Override
    public NewsResponseDTO updateNews(Long id, NewsUpdateDTO news) {
        log.info("Attempting to update news with id: {}", id);
        News existingNews = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));

        existingNews.setTitle(news.getTitle());
        existingNews.setContent(news.getContent());
        existingNews.setImage(news.getImage());

        existingNews = newsRepository.save(existingNews);
        log.info("News updated successfully with id: {}", id);
        return NewsResponseDTO.builder()
                .id(existingNews.getId())
                .title(existingNews.getTitle())
                .content(existingNews.getContent())
                .image(existingNews.getImage())
                .authorName(existingNews.getUser().getFullname())
                .createDate(existingNews.getCreateDate())
                .viewCount(existingNews.getViewCount())
                .likeCount(existingNews.getLikeCount())
                .build();
    }

    @Override
    public void deleteNews(Long newsId) {
        log.info("Attempting to delete news with id: {}", newsId);
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + newsId));
        newsRepository.delete(news);
        log.info("News deleted successfully with id: {}", newsId);
    }

}
