package poly.edu.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import poly.edu.entity.dto.ShareDTO;
import poly.edu.service.NewsEngagementService;

@CrossOrigin("*")
@RestController
@RequestMapping("/rest/news-engagement")
@RequiredArgsConstructor
public class NewsEngagementRestController {

    private final NewsEngagementService newsEngagementService;

    @PostMapping("/view/{newsId}")
    public ResponseEntity<?> incrementView(@PathVariable Long newsId) {
        try {
            newsEngagementService.incrementView(newsId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/like/{newsId}")
    public ResponseEntity<?> toggleLike(@PathVariable Long newsId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            newsEngagementService.toggleLike(newsId, userDetails);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/share/{newsId}")
    public ResponseEntity<?> recordShare(@PathVariable Long newsId, @RequestBody ShareDTO shareDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            newsEngagementService.recordShare(newsId, shareDTO, userDetails);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
