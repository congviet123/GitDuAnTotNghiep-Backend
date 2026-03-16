package poly.edu.controller.rest;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import poly.edu.entity.dto.NewsCommentCreateDTO;
import poly.edu.entity.dto.NewsCommentResponseDTO;
import poly.edu.service.NewsCommentService;

@CrossOrigin("*")
@RestController
@RequestMapping("/rest/news-comments")
@RequiredArgsConstructor
public class NewsCommentRestController {

    private final NewsCommentService newsCommentService;

    @GetMapping("/news/{newsId}")
    public ResponseEntity<Page<NewsCommentResponseDTO>> getCommentsByNewsId(
            @PathVariable Long newsId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(newsCommentService.getCommentsByNewsId(newsId, page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/news/{newsId}/root")
    public ResponseEntity<Page<NewsCommentResponseDTO>> getRootComments(
            @PathVariable Long newsId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(newsCommentService.getRootComments(newsId, page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{parentId}/replies")
    public ResponseEntity<Page<NewsCommentResponseDTO>> getReplies(
            @PathVariable Long parentId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(newsCommentService.getReplies(parentId, page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createComment(@Valid @RequestBody NewsCommentCreateDTO comment,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(newsCommentService.createComment(comment, userDetails));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/toggle-visibility")
    public ResponseEntity<?> toggleVisiable(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(newsCommentService.toggleVisiable(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
