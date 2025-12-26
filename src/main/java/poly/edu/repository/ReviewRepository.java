package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Cần import này
import poly.edu.entity.Review;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    
    /**
     * SỬA LỖI: Sử dụng JOIN FETCH để buộc tải (Eager Load) đối tượng User 
     * cùng lúc với Review. Điều này giúp Review.getReviewerFullname() hoạt động 
     * mà không gặp lỗi Lazy Loading.
     * * Đồng thời sắp xếp theo ngày đánh giá mới nhất (DESC).
     */
    @Query("SELECT r FROM Review r JOIN FETCH r.user u WHERE r.product.id = :productId ORDER BY r.reviewDate DESC")
    List<Review> findByProductId(Integer productId);
}