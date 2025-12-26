package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Review;
import poly.edu.entity.User;
import poly.edu.entity.dto.ReviewCreationDTO;
import poly.edu.repository.ProductRepository;
import poly.edu.repository.ReviewRepository;
import poly.edu.repository.UserRepository; 
import poly.edu.service.ReviewService;

import java.util.List;
import java.util.Date; 

@Service 
public class ReviewServiceImpl implements ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository; 

    @Override
    @Transactional(readOnly = true) 
    public List<Review> getReviewsByProductId(Integer productId) {
        
        List<Review> reviews = reviewRepository.findByProductId(productId);
        
        // BẮT BUỘC: Buộc tải đối tượng User để trường Transient hoạt động.
        // Đây là lớp bảo vệ cuối cùng chống lại lỗi Lazy Loading.
        reviews.forEach(review -> {
            if (review.getUser() != null) {
                // Kích hoạt Lazy Loading
                review.getUser().getFullname(); 
            }
        });
        
        return reviews;
    }
    
    @Override 
    @Transactional 
    public Review saveReview(String username, ReviewCreationDTO reviewDto) {
        
        // 1. Tìm User và Product (Đảm bảo tồn tại)
        User user = userRepository.findById(username)
                    .orElseThrow(() -> new RuntimeException("Lỗi bảo mật: Người dùng không tồn tại."));
        
        return productRepository.findById(reviewDto.getProductId())
                .map(product -> {
                    // 2. Tạo đối tượng Review mới
                    Review review = new Review();
                    review.setProduct(product);
                    review.setUser(user);
                    review.setComment(reviewDto.getComment());
                    review.setRating(reviewDto.getRating());
                    review.setReviewDate(new Date()); 
                    
                    // 3. Lưu Review
                    return reviewRepository.save(review);
                })
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại."));
    }
}