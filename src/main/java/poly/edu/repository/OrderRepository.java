package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.Order;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // =========================================================================
    // 1. PHƯƠNG THỨC DÀNH CHO KHÁCH HÀNG (CLIENT)
    // =========================================================================

    /**
     * Lấy lịch sử đơn hàng của người dùng cụ thể.
     * Sử dụng JOIN FETCH để tránh lỗi LazyInitializationException khi truy cập Account.
     */
    @Query("SELECT o FROM Order o " + 
           "JOIN FETCH o.account u " + 
           "WHERE u.username = :username ORDER BY o.createDate DESC")
    List<Order> findByAccountUsernameOrderByCreateDateDesc(@Param("username") String username);
    
    /**
     * Lấy chi tiết đơn hàng bao gồm: Thông tin khách hàng, Danh sách sản phẩm và Thông tin sản phẩm.
     */
    @Query("SELECT o FROM Order o " + 
           "JOIN FETCH o.account u " + 
           "JOIN FETCH o.orderDetails od " + 
           "JOIN FETCH od.product p " + 
           "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Integer id);
    
    // =========================================================================
    // 2. PHƯƠNG THỨC DÀNH CHO QUẢN TRỊ VIÊN (ADMIN)
    // =========================================================================

    /**
     * Lấy danh sách tất cả đơn hàng đơn giản nhất (mặc định khi chưa lọc).
     */
    @Query("SELECT o FROM Order o ORDER BY o.createDate DESC")
    List<Order> findAllOrdersSimple();

    /**
     * [BỘ LỌC TỔNG HỢP] Lọc đa năng cho Admin.
     * Đã cập nhật LIKE với CONCAT để đảm bảo tính tương thích cao nhất.
     */
    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:method IS NULL OR o.paymentMethod LIKE CONCAT('%', :method, '%')) AND " + 
           "(:start IS NULL OR o.createDate >= :start) AND " +
           "(:end IS NULL OR o.createDate <= :end) " +
           "ORDER BY o.createDate DESC")
    List<Order> filterOrders(
            @Param("status") String status, 
            @Param("method") String method, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);

    // =========================================================================
    // 3. PHƯƠNG THỨC BÁO CÁO & THỐNG KÊ
    // =========================================================================

    /**
     * Tính tổng doanh thu theo từng tháng của một năm (loại bỏ các đơn đã hủy).
     */
    @Query("SELECT MONTH(o.createDate), SUM(o.totalAmount) " +
           "FROM Order o " +
           "WHERE YEAR(o.createDate) = :year " +
           "AND o.status <> 'CANCELLED' " + 
           "GROUP BY MONTH(o.createDate)")
    List<Object[]> getMonthlyRevenue(@Param("year") Integer year);
    
    
    // kinh năng đánh dấu đã in
    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.isPrinted = true WHERE o.id = :id")
    void markAsPrinted(@Param("id") Integer id);
}