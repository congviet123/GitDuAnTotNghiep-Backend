package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

 
    @Query("SELECT r FROM Review r JOIN FETCH r.user u WHERE r.orderDetail.product.id = ?1 ORDER BY r.reviewDate DESC")
    List<Review> findByProductId(Integer productId);
    
}