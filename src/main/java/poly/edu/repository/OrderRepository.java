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
     * [TÍNH NĂNG]: Lấy lịch sử đơn hàng của khách hàng.
     * Sử dụng JOIN FETCH để tải luôn thông tin Account, tránh lỗi truy vấn chậm (LazyInitializationException).
     */
    @Query("SELECT o FROM Order o " + 
           "JOIN FETCH o.account u " + 
           "WHERE u.username = :username ORDER BY o.createDate DESC")
    List<Order> findByAccountUsernameOrderByCreateDateDesc(@Param("username") String username);
    
    /**
     * [TÍNH NĂNG]: Lấy chi tiết một đơn hàng cụ thể (Khi người dùng bấm "Chi tiết").
     * Kéo theo thông tin User, Chi tiết đơn hàng và Sản phẩm để hiển thị đầy đủ trên Modal.
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
     * [TÍNH NĂNG]: Lấy toàn bộ danh sách đơn hàng cho trang Quản trị (sắp xếp đơn mới nhất lên đầu).
     */
    @Query("SELECT o FROM Order o ORDER BY o.createDate DESC")
    List<Order> findAllOrdersSimple();

    /**
     * [TÍNH NĂNG]: Lọc đơn hàng nâng cao cho Admin (theo trạng thái, phương thức thanh toán, khoảng thời gian).
     * Dùng CONCAT để hỗ trợ tìm kiếm linh hoạt hơn.
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

    /**
     *  Tìm các đơn hàng Đã Giao Thành Công quá 24h để tự động chốt đơn.
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'DELIVERED' AND COALESCE(o.deliveryDate, o.createDate) <= :cutoffTime")
    List<Order> findDeliveredOrdersOlderThan(@Param("cutoffTime") Date cutoffTime);

    
    
    
    
    
    
    
 
    // =========================================================================
    // 3. PHƯƠNG THỨC BÁO CÁO & THỐNG KÊ (DASHBOARD)
    // =========================================================================
    
    /**
     * [TÍNH NĂNG]: Cập nhật cờ đánh dấu hóa đơn này đã được Admin in PDF.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.isPrinted = true WHERE o.id = :id")
    void markAsPrinted(@Param("id") Integer id);

    /**
     * [TÍNH NĂNG]: Tính tổng doanh thu theo từng tháng của một năm (chỉ tính các đơn COMPLETED).
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
    
    /**
     * [TÍNH NĂNG]: Tính tổng doanh thu theo tuần trong một tháng cụ thể.
     */
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
    // 4. DASHBOARD SẢN PHẨM & ĐƠN HÀNG NÂNG CAO
    // =========================================================================

    /**
     * [TÍNH NĂNG]: Lấy TOP 3 SẢN PHẨM BÁN CHẠY NHẤT.
     * Tính theo tổng Khối lượng (KG) đã bán trong các đơn COMPLETED hoặc DELIVERED.
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
     * [TÍNH NĂNG]: Đếm tổng số lượng các đơn hàng theo từng trạng thái (dùng JPQL).
     */
    @Query("""
    SELECT o.status, COUNT(o)
    FROM Order o
    GROUP BY o.status
    """)
    List<Object[]> getOrderStatistics();
    
    /**
     * [TÍNH NĂNG]: Biểu đồ thống kê số lượng đơn hàng Hoàn tất / Hủy hoàn tiền (dùng Native SQL).
     */
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

    /**
     * [TÍNH NĂNG]: Thống kê chi tiết sản phẩm bán chạy theo thời gian (Native SQL).
     */
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
}