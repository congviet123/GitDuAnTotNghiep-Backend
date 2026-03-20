package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Order;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    // --- TÌM CÁC ĐƠN HÀNG ĐÃ GIAO THÀNH CÔNG QUÁ 24H ---
    @Query("SELECT o FROM Order o WHERE o.status = 'DELIVERED' AND o.deliveryDate <= :cutoffTime")
    List<Order> findDeliveredOrdersOlderThan(@Param("cutoffTime") Date cutoffTime);

    // =========================================================================
    // 3. PHƯƠNG THỨC BÁO CÁO & THỐNG KÊ
    // =========================================================================
    
    
    // code mới của tuyến làm doshboard
    
    
    /**
     * Tính tổng doanh thu theo từng tháng của một năm (chỉ tính đơn đã hoàn tất).
     */
    @Query(value = """
    		SELECT MONTH(o.create_date) AS month,
    		       SUM(o.total_amount) AS totalRevenue
    		FROM Orders o
    		WHERE o.status IN ('COMPLETED')
    		AND YEAR(o.create_date) = :year
    		GROUP BY MONTH(o.create_date)
    		ORDER BY MONTH(o.create_date)
    		""", nativeQuery = true)
    		List<Object[]> getMonthlyRevenue(@Param("year") int year);
    
    
    // tổng doanh thu theo tháng
    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.isPrinted = true WHERE o.id = :id")
    void markAsPrinted(@Param("id") Integer id);
    
    
    
    @Query(value = """
    		SELECT DATEPART(WEEK, o.create_date) - 
    		       DATEPART(WEEK, DATEFROMPARTS(:year,:month,1)) + 1 AS week,
    		       SUM(o.total_amount) AS revenue
    		FROM Orders o
    		WHERE o.status IN ('COMPLETED')
    		AND YEAR(o.create_date) = :year
    		AND MONTH(o.create_date) = :month
    		GROUP BY DATEPART(WEEK, o.create_date)
    		ORDER BY week
    		""", nativeQuery = true)
    		List<Object[]> getWeeklyRevenue(
    		        @Param("year") int year,
    		        @Param("month") int month
    		);
    		
    		// =========================================================================
    		// 4. DASHBOARD SẢN PHẨM & ĐƠN HÀNG
    		// =========================================================================

    		/**
    		 * TOP 3 SẢN PHẨM BÁN CHẠY
    		 * Tính theo tổng KG đã bán trong các đơn COMPLETED hoặc DELIVERED
    		 */
    		@Query(value = """
    				SELECT p.name,
    				       SUM(od.quantity) AS totalKg
    				FROM Order_Detail od
    				JOIN Orders o ON od.order_id = o.id
    				JOIN Product p ON od.product_id = p.id
    				WHERE o.status IN ('COMPLETED','DELIVERED')
    				AND YEAR(o.create_date) = :year
    				AND (
    				        :month = 'all'
    				        OR MONTH(o.create_date) = TRY_CAST(:month AS INT)
    				    )
    				GROUP BY p.id, p.name
    				ORDER BY totalKg DESC
    				""", nativeQuery = true)
    				List<Object[]> getTopSellingProducts(
    				        @Param("year") int year,
    				        @Param("month") String month
    				);
    		/**
    		 * TOP 3 SẢN PHẨM TỒN KHO NHIỀU NHẤT
    		 * Lấy từ bảng Product
    		 */
    		

    		/**
    		 * THỐNG KÊ ĐƠN HÀNG
    		 * Đếm số đơn COMPLETED / CANCELLED
    		 */
    		@Query("""
    		SELECT o.status, COUNT(o)
    		FROM Order o
    		GROUP BY o.status
    		""")
    		List<Object[]> getOrderStatistics();
    		
    		
    		@Query(value = """
    				SELECT p.name,
    				       SUM(od.quantity)
    				FROM order_details od
    				JOIN orders o ON od.order_id = o.id
    				JOIN products p ON od.product_id = p.id
    				WHERE YEAR(o.create_date) = :year
    				AND (:month = 'all' OR MONTH(o.create_date) = :month)
    				GROUP BY p.name
    				ORDER BY SUM(od.quantity) DESC
    				""", nativeQuery = true)
    				List<Object[]> getTopSellingProductsByTime(
    				        @Param("year") int year,
    				        @Param("month") String month
    				);
    			
    				@Query(value = """
    						SELECT 
    						    status,
    						    COUNT(*) as total
    						FROM Orders
    						WHERE YEAR(create_date) = :year
    						AND (
    						        :month = 'all'
    						        OR MONTH(create_date) = TRY_CAST(:month AS INT)
    						    )
    						AND status IN ('COMPLETED','CANCELLED_REFUNDED')
    						GROUP BY status
    						""", nativeQuery = true)
    						List<Object[]> getOrderStatistics(int year, String month);
}