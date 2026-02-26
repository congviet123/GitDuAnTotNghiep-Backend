package poly.edu.entity.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NewsUpdateDTO implements Serializable {

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Title cannot be blank")
    private String content;

    private String image;
}
