package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.OrderDetail;
import poly.edu.entity.Review;
import poly.edu.entity.User;
import poly.edu.entity.dto.ReviewCreationDTO;
import poly.edu.repository.OrderDetailRepository;
import poly.edu.repository.ReviewRepository;
import poly.edu.repository.UserRepository; 
import poly.edu.service.ReviewService;

import java.util.List;
import java.util.Date; 

@Service 
public class ReviewServiceImpl implements ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private UserRepository userRepository; 
    
    // Dùng OrderDetailRepository thay vì ProductRepository
    @Autowired private OrderDetailRepository orderDetailRepository; 

    @Override
    @Transactional(readOnly = true) 
    public List<Review> getReviewsByProductId(Integer productId) {
        
        List<Review> reviews = reviewRepository.findByProductId(productId);
        
        // Buộc tải đối tượng User để trường Transient hoạt động.
        // Đây là lớp bảo vệ cuối cùng chống lại lỗi Lazy Loading. (Giữ nguyên logic của bạn)
        reviews.forEach(review -> {
            if (review.getUser() != null) {
                // Kích hoạt Lazy Loading
                review.getUser().getFullname(); 
            }
        });
        
        return reviews;
    }

 
    @Transactional(readOnly = true)
    public boolean canReview(String username, Integer productId) {
        List<OrderDetail> eligibles = orderDetailRepository.findEligibleOrderDetailsForReview(username, productId);
        return !eligibles.isEmpty();
    }
    
    @Override 
    @Transactional 
    public Review saveReview(String username, ReviewCreationDTO reviewDto) {
        
        // 1. Tìm User (Đảm bảo tồn tại)
        User user = userRepository.findById(username)
                    .orElseThrow(() -> new RuntimeException("Lỗi bảo mật: Người dùng không tồn tại."));
        
        // 2. Kiểm tra xem khách hàng này có đơn hàng hợp lệ để đánh giá không
        List<OrderDetail> eligibles = orderDetailRepository.findEligibleOrderDetailsForReview(username, reviewDto.getProductId());
        
        if (eligibles.isEmpty()) {
            throw new RuntimeException("Bạn chỉ được đánh giá khi đã mua, nhận hàng thành công, và chưa đánh giá lượt mua này!");
        }

        // 3. Lấy lượt mua đầu tiên hợp lệ để gắn vào Review
        OrderDetail validOrderDetail = eligibles.get(0);

        // 4. Tạo và lưu Review
        Review review = new Review();
        review.setUser(user);
        
        //  Gắn Review vào Chi tiết đơn hàng, KHÔNG gắn vào Product nữa
        review.setOrderDetail(validOrderDetail); 
        
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        review.setReviewDate(new Date()); 
        
        // 5. Sử dụng Try-Catch để lọc sạch lỗi từ Database Trigger
        try {
            return reviewRepository.save(review);
        } catch (Exception e) {
            // Lấy nguyên nhân gốc rễ của lỗi
            String errorMessage = e.getMessage();
            Throwable rootCause = e.getCause();
            while (rootCause != null) {
                errorMessage = rootCause.getMessage();
                rootCause = rootCause.getCause();
            }
            
            // Nếu lỗi chứa câu thông báo từ Trigger của SQL
            if (errorMessage != null && errorMessage.contains("Đơn hàng chưa hoàn thành")) {
                throw new RuntimeException("Đơn hàng chưa hoàn thành, bạn chưa thể đánh giá sản phẩm này!");
            }
            
            // Nếu là một lỗi SQL khác
            throw new RuntimeException("Lỗi khi gửi đánh giá: " + (errorMessage != null ? errorMessage : "Vui lòng thử lại sau."));
        }
    }
}