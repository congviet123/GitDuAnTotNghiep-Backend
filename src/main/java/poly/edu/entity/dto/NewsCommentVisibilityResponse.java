package poly.edu.entity.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsCommentVisibilityResponse implements Serializable {

    private Long commentId;

    private boolean isVisiable;
}
