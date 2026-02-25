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
    // 2. Trạng thái hóa đơn phải là Đã giao xong (Giao hàng thành công / COMPLETED / DELIVERED)
    // 3. Dòng mua hàng này CHƯA từng được đánh giá trước đó
    @Query("SELECT od FROM OrderDetail od " +
           "WHERE od.order.account.username = :username " +
           "AND od.product.id = :productId " +
           "AND (od.order.status = 'Giao hàng thành công' OR od.order.status = 'COMPLETED' OR od.order.status = 'DELIVERED') " +
           "AND od.id NOT IN (SELECT r.orderDetail.id FROM Review r)")
    List<OrderDetail> findEligibleOrderDetailsForReview(@Param("username") String username, @Param("productId") Integer productId);

}