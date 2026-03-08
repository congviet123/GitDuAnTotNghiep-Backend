package poly.edu.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import poly.edu.entity.News;
import poly.edu.entity.NewsComment;
import poly.edu.entity.User;
import poly.edu.entity.dto.NewsCommentCreateDTO;
import poly.edu.entity.dto.NewsCommentResponseDTO;
import poly.edu.entity.dto.NewsCommentVisibilityResponse;
import poly.edu.repository.NewsCommentRepository;
import poly.edu.repository.NewsRepository;
import poly.edu.repository.UserRepository;
import poly.edu.service.NewsCommentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsCommentServiceImpl implements NewsCommentService {

    private final NewsCommentRepository newsCommentRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NewsCommentResponseDTO createComment(NewsCommentCreateDTO comment, UserDetails userDetails) {
        log.info("Creating comment for newsId: {} by user: {}",
                comment.getNewsId(), userDetails.getUsername());
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        News news = newsRepository.findById(comment.getNewsId())
                .orElseThrow(() -> new RuntimeException("News not found"));
        NewsComment parent = null;
        if (comment.getParentId() != null) {
            parent = newsCommentRepository.findById(comment.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));

            if (!parent.getNews().getId().equals(news.getId())) {
                throw new RuntimeException("Parent comment does not belong to this news");
            }
        }
        NewsComment newsComment = NewsComment.builder()
                .content(comment.getContent())
                .news(news)
                .user(user)
                .parent(parent)
                .build();
        newsComment = newsCommentRepository.save(newsComment);
        log.info("Successfully created comment with id: {}", newsComment.getId());
        return NewsCommentResponseDTO.builder()
                .id(newsComment.getId())
                .newsId(newsComment.getNews().getId())
                .author(newsComment.getUser().getFullname())
                .parentId(parent != null ? parent.getId() : null)
                .content(newsComment.getContent())
                .isVisiable(newsComment.getIsVisible())
                .createdDate(newsComment.getCreateDate())
                .replyCount(0)
                .hasMoreReplies(false)
                .replies(new ArrayList<>())
                .build();
    }

    @Override
    @Transactional
    public NewsCommentVisibilityResponse toggleVisiable(Long commentId) {
        log.info("Attemping to toggle visiable comment with commentId: {}", commentId);
        NewsComment newsComment = newsCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with commentId: " + commentId));
        newsComment.setIsVisible(!newsComment.getIsVisible());
        newsComment = newsCommentRepository.save(newsComment);
        log.info("Successfully toggled visiable comment");
        return NewsCommentVisibilityResponse.builder()
                .commentId(newsComment.getId())
                .isVisiable(newsComment.getIsVisible())
                .build();
    }

    @Override
    public List<NewsCommentResponseDTO> getRootComments(Long newsId, int offset, int limit) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRootComments'");
    }

    @Override
    public List<NewsCommentResponseDTO> getReplies(Long parentId, int offset, int limit) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getReplies'");
    }

}
