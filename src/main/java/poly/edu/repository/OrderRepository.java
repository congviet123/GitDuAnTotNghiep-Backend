package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.Order;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // 1. Phương thức User List (Lịch sử đơn hàng): 
    // FIX LỖI AliasCollision: Đổi alias từ 'a' sang 'u' (User)
    @Query("SELECT o FROM Order o " + 
           "JOIN FETCH o.account u " + // ✅ Đã đổi alias sang 'u'
           "WHERE o.account.username = :username ORDER BY o.createDate DESC")
    List<Order> findByAccountUsernameOrderByCreateDateDesc(@Param("username") String username);
    
    // 2. Phương thức Chi tiết đơn hàng: 
    // FIX LỖI Nghiệp vụ: Đã thêm JOIN FETCH o.account (dùng alias 'u') để tải thông tin user
    @Query("SELECT o FROM Order o " + 
           "JOIN FETCH o.account u " +           // ✅ THÊM DÒNG NÀY để tải account/user
           "JOIN FETCH o.orderDetails od " + 
           "JOIN FETCH od.product p " + 
           "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Integer id);
    
    /**
     * ✅ PHƯƠNG THỨC ADMIN LIST: Dùng truy vấn đơn giản nhất
     */
    @Query("SELECT o FROM Order o ORDER BY o.createDate DESC")
    List<Order> findAllOrdersSimple(); // Dùng hàm này
    
    
 // Query tính tổng doanh thu theo từng tháng của một năm cụ thể
    // Trả về danh sách các mảng Object[] gồm: [Tháng, Tổng Tiền]
    // Chỉ tính các đơn hàng có trạng thái không phải là CANCELLED (Đã hủy)
    @Query("SELECT MONTH(o.createDate), SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE YEAR(o.createDate) = :year " +
            "AND o.status <> 'CANCELLED' " + 
            "GROUP BY MONTH(o.createDate)")
     List<Object[]> getMonthlyRevenue(@Param("year") Integer year);
 }
