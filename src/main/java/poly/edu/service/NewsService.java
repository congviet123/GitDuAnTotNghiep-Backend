package poly.edu.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;

import poly.edu.entity.News;
import poly.edu.entity.dto.NewsCreateDTO;
import poly.edu.entity.dto.NewsResponseDTO;
import poly.edu.entity.dto.NewsUpdateDTO;

public interface NewsService {

    Page<NewsResponseDTO> getAllNews(int page, int size, String sortDir, String sortBy, String searchKeyWord);

    NewsResponseDTO getNewsById(Long id);

    NewsResponseDTO createNews(NewsCreateDTO news, UserDetails userDetails);

    NewsResponseDTO updateNews(Long id, NewsUpdateDTO news);

    void deleteNews(Long newsId);
}
