package poly.edu.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;

import poly.edu.entity.dto.NewsCommentCreateDTO;
import poly.edu.entity.dto.NewsCommentResponseDTO;
import poly.edu.entity.dto.NewsCommentVisibilityResponse;

public interface NewsCommentService {

    NewsCommentResponseDTO createComment(NewsCommentCreateDTO comment, UserDetails userDetails);

    NewsCommentVisibilityResponse toggleVisiable(Long commentId);

    Page<NewsCommentResponseDTO> getCommentsByNewsId(Long newsId, int page, int size);

    Page<NewsCommentResponseDTO> getRootComments(Long newsId, int page, int size);

    Page<NewsCommentResponseDTO> getReplies(Long parentId, int page, int size);
}
