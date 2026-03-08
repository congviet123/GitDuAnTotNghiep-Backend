package poly.edu.entity.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsResponseDTO implements Serializable {

    private Long id;

    private String title;

    private String content;

    private String image;

    private LocalDateTime createDate;

    private String authorName;

    private Integer likeCount;

    private Integer viewCount;

    private Integer shareCount;

    private String productLink;

    private boolean likedByCurrentUser;
}
