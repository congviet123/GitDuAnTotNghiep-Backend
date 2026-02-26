package poly.edu.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

import poly.edu.entity.dto.NewsCommentCreateDTO;
import poly.edu.entity.dto.NewsCommentResponseDTO;
import poly.edu.entity.dto.NewsCommentVisibilityResponse;

public interface NewsCommentService {

    NewsCommentResponseDTO createComment(NewsCommentCreateDTO comment, UserDetails userDetails);

    NewsCommentVisibilityResponse toggleVisiable(Long commentId);

    List<NewsCommentResponseDTO> getRootComments(Long newsId, int offset, int limit);

    List<NewsCommentResponseDTO> getReplies(Long parentId, int offset, int limit);
}
