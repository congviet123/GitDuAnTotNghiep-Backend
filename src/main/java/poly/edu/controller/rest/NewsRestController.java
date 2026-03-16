package poly.edu.controller.rest;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.entity.News;
import poly.edu.entity.dto.NewsCreateDTO;
import poly.edu.entity.dto.NewsResponseDTO;
import poly.edu.entity.dto.NewsUpdateDTO;
import poly.edu.service.NewsService;

@CrossOrigin("*")
@RestController
@RequestMapping("/rest/news")
@RequiredArgsConstructor
public class NewsRestController {

    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<Page<NewsResponseDTO>> getAllNews(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "search", required = false) String searchKeyWord) {
        return ResponseEntity.ok(newsService.getAllNews(page, size, sortDir, sortBy, searchKeyWord));
    }

    @GetMapping("/liked")
    public ResponseEntity<Page<NewsResponseDTO>> getAllNewsByCurrentUser(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(newsService.getAllNewsByCurrentUser(page, size, sortDir, sortBy, userDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNewsById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(newsService.getNewsById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createNews(
            @RequestPart("news") @Valid NewsCreateDTO news,
            @RequestPart(value = "newsImage", required = false) MultipartFile newsImage,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            return ResponseEntity.ok(newsService.createNews(news, newsImage, userDetails));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateNews(@Valid @PathVariable Long id, @RequestPart("news") @Valid NewsUpdateDTO news,
                                        @RequestPart(value = "newsImage", required = false) MultipartFile newsImage) {
        try {
            return ResponseEntity.ok(newsService.updateNews(id, news, newsImage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNews(@PathVariable Long id) {
        try {
            newsService.deleteNews(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
