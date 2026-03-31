package poly.edu.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.entity.News;
import poly.edu.entity.User;
import poly.edu.entity.dto.NewsCreateDTO;
import poly.edu.entity.dto.NewsResponseDTO;
import poly.edu.entity.dto.NewsUpdateDTO;
import poly.edu.repository.NewsLikeRepository;
import poly.edu.repository.NewsRepository;
import poly.edu.repository.NewsShareRepository;
import poly.edu.repository.UserRepository;
import poly.edu.service.NewsService;

@Service
@Slf4j
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NewsLikeRepository newsLikeRepository;
    @Autowired
    private NewsShareRepository newsShareRepository;

    private final Path fileStorageLocation = Paths.get("target/classes/static/imgs").toAbsolutePath().normalize();

    public NewsServiceImpl() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi: Không thể tạo thư mục lưu trữ ảnh.", ex);
        }
    }

    @Override
    public Page<NewsResponseDTO> getAllNews(int page, int size, String sortDir, String sortBy, String searchKeyWord) {
        log.info("Fetching news: page={}, size={}, sortDir={}, sortBy={}, searchKeyword={}",
                page, size, sortDir, sortBy, searchKeyWord);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<News> pageNewsResponse;
        if (searchKeyWord != null && !searchKeyWord.isEmpty()) {
            pageNewsResponse = newsRepository.findByTitleContainingIgnoreCaseOrUser_FullnameContainingIgnoreCase(searchKeyWord, searchKeyWord,  pageable);
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
                .shareCount(n.getShares().size())
                .productLink(n.getProductLink())
                .build());
    }

    @Override
    public Page<NewsResponseDTO> getAllNewsByCurrentUser(int page, int size, String sortDir, String sortBy, UserDetails userDetails) {
        log.info("Fetching news: page={}, size={}, sortDir={}, sortBy={}",
                page, size, sortDir, sortBy);
        String currentUsername = userDetails.getUsername();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + currentUsername));
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<News> pageNewsResponse;

            pageNewsResponse = newsRepository.findLikedNewsByUsername(user.getUsername(), pageable);
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
                .shareCount(n.getShares().size())
                .productLink(n.getProductLink())
                .build());    }

    @Override
    public NewsResponseDTO getNewsById(Long id) {
        log.info("Fetching news with id: {}", id);
        News newsResponse = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));
        log.info("Fetching news successfully");
        return NewsResponseDTO.builder()
                .id(newsResponse.getId())
                .title(newsResponse.getTitle())
                .content(newsResponse.getContent())
                .image(newsResponse.getImage())
                .authorName(newsResponse.getUser().getFullname())
                .createDate(newsResponse.getCreateDate())
                .viewCount(newsResponse.getViewCount())
                .likeCount(newsResponse.getLikeCount())
                .likedByCurrentUser(newsLikeRepository.existsByNews_IdAndUser_Username(newsResponse.getId(),
                        newsResponse.getUser().getUsername()))
                .productLink(newsResponse.getProductLink())
                .shareCount(newsShareRepository.countNewsSharesByNews_Id(id))
                .build();
    }

    @Override
    public NewsResponseDTO createNews(NewsCreateDTO news, MultipartFile newsImage, UserDetails userDetails) throws IOException {
        log.info("Attempting to create a news with title: {}", news.getTitle());
        String currentUsername = userDetails.getUsername();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + currentUsername));
        News newsToSave = News.builder()
                .title(news.getTitle())
                .content(news.getContent())
                .image((newsImage != null && !newsImage.isEmpty()) ? saveFile(newsImage) : null)
                .productLink(news.getProductLink())
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
                .productLink(newsResponse.getProductLink())
                .build();
    }

    @Override
    public NewsResponseDTO updateNews(Long id, NewsUpdateDTO news, MultipartFile newsImage) throws IOException {
        log.info("Attempting to update news with id: {}", id);
        News existingNews = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));

        existingNews.setTitle(news.getTitle());
        existingNews.setContent(news.getContent());
        existingNews.setImage((newsImage != null && !newsImage.isEmpty()) ? saveFile(newsImage) : null);
        existingNews.setProductLink(news.getProductLink());

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
                .productLink(existingNews.getProductLink())
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

    private String saveFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }
}
