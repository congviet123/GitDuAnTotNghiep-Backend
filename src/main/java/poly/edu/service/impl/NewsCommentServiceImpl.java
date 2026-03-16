package poly.edu.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Page<NewsCommentResponseDTO> getRootComments(Long newsId, int page, int size) {
        log.info("Attempting to get root comments for newsId: {}", newsId);
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsComment> newsComments = newsCommentRepository.findByNewsIdAndParentIsNullAndIsVisibleTrue(newsId,
                pageable);
        log.info("Successfully retrieved root comments for newsId");
        return newsComments.map(n -> NewsCommentResponseDTO.builder()
                .id(n.getId())
                .newsId(newsId)
                .author(n.getUser().getFullname())
                .parentId(null)
                .content(n.getContent())
                .isVisiable(n.getIsVisible())
                .createdDate(n.getCreateDate())
                .build());
    }

    @Override
    public Page<NewsCommentResponseDTO> getReplies(Long parentId, int page, int size) {
        log.info("Attempting to get relies comments for parentId: {}", parentId);
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsComment> newsComments = newsCommentRepository.findByParentIdAndIsVisibleTrue(parentId, pageable);
        log.info("Successfully retrieved replies comments for parentId");
        return newsComments.map(n -> NewsCommentResponseDTO.builder()
                .id(n.getId())
                .newsId(n.getNews().getId())
                .author(n.getUser().getFullname())
                .parentId(parentId)
                .content(n.getContent())
                .isVisiable(n.getIsVisible())
                .createdDate(n.getCreateDate())
                .build());

    }

    @Override
    public Page<NewsCommentResponseDTO> getCommentsByNewsId(Long newsId, int page, int size) {
        log.info("Attempting to get comments for newsId: {}", newsId);
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsComment> newsComments = newsCommentRepository.findAllByNewsId(newsId,
                pageable);
        log.info("Successfully retrieved comments for newsId");
        return newsComments.map(n -> NewsCommentResponseDTO.builder()
                .id(n.getId())
                .newsId(newsId)
                .author(n.getUser().getFullname())
                .parentId(null)
                .content(n.getContent())
                .isVisiable(n.getIsVisible())
                .createdDate(n.getCreateDate())
                .build());
    }
}
