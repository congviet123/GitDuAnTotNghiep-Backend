package poly.edu.service;

import poly.edu.entity.Review;
import poly.edu.entity.dto.ReviewCreationDTO; // Cần import DTO này
import java.util.List;

public interface ReviewService {
    
    List<Review> getReviewsByProductId(Integer productId);
    
    // BẮT BUỘC: Phương thức lưu đánh giá phải được khai báo
    Review saveReview(String username, ReviewCreationDTO reviewDto);
}