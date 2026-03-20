package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.OrderDetail;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    
    // Tìm dòng chi tiết đơn hàng thỏa mãn 3 điều kiện khắt khe để cho phép Đánh giá:
    // 1. Đúng người mua (username) và đúng sản phẩm (productId)
    // 2. Trạng thái hóa đơn phải là Đã giao xong (DELIVERED hoặc COMPLETED)
    // 3. Dòng mua hàng này CHƯA từng được đánh giá trước đó (Dùng NOT EXISTS an toàn hơn NOT IN)
    @Query("SELECT od FROM OrderDetail od " +
           "WHERE od.order.account.username = :username " +
           "AND od.product.id = :productId " +
           "AND od.order.status IN ('DELIVERED', 'COMPLETED', 'Giao hàng thành công') " +
           "AND NOT EXISTS (SELECT r FROM Review r WHERE r.orderDetail.id = od.id)")
    List<OrderDetail> findEligibleOrderDetailsForReview(@Param("username") String username, @Param("productId") Integer productId);
    
}