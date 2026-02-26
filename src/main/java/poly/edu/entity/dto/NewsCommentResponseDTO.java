package poly.edu.entity.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsCommentResponseDTO implements Serializable {

    Long id;

    Long newsId;
    
    Long parentId;

    String content;

    String author;

    boolean isVisiable;

    LocalDateTime createdDate;

    int replyCount;

    boolean hasMoreReplies;

    List<NewsCommentResponseDTO> replies;
}
