package poly.edu.entity.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsCommentCreateDTO implements Serializable {
    
    @NotNull(message = "NewsId cannot be null")
    private Long newsId;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    private Long parentId;
}
