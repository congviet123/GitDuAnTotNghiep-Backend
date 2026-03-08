package poly.edu.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.multipart.MultipartFile;
import poly.edu.entity.News;
import poly.edu.entity.dto.NewsCreateDTO;
import poly.edu.entity.dto.NewsResponseDTO;
import poly.edu.entity.dto.NewsUpdateDTO;

public interface NewsService {

    Page<NewsResponseDTO> getAllNews(int page, int size, String sortDir, String sortBy, String searchKeyWord);

    Page<NewsResponseDTO> getAllNewsByCurrentUser(int page, int size, String sortDir, String sortBy, UserDetails userDetails);

    NewsResponseDTO getNewsById(Long id);

    NewsResponseDTO createNews(NewsCreateDTO news, MultipartFile newsImage, UserDetails userDetails) throws IOException;

    NewsResponseDTO updateNews(Long id, NewsUpdateDTO news, MultipartFile imageFile) throws IOException;

    void deleteNews(Long newsId);
}
