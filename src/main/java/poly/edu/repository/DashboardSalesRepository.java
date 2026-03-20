package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Order;
import java.util.List;

@Repository
public interface DashboardSalesRepository extends JpaRepository<Order, Integer> {

    // Lấy doanh thu 12 tháng
    @Query(value = "SELECT MONTH(create_date) as m, SUM(total_amount) as total " +
                   "FROM Orders WHERE YEAR(create_date) = ?1 AND status = 'COMPLETED' " +
                   "GROUP BY MONTH(create_date)", nativeQuery = true)
    List<Object[]> getRevenueByYear(int year);

    // Top 5 sản phẩm bán chạy
    @Query(value = "SELECT TOP 5 p.name, SUM(od.quantity) as qty " +
                   "FROM Order_Detail od JOIN Product p ON od.product_id = p.id " +
                   "JOIN Orders o ON od.order_id = o.id " +
                   "WHERE o.status = 'COMPLETED' " +
                   "GROUP BY p.name ORDER BY qty DESC", nativeQuery = true)
    List<Object[]> getTopProducts();

    // Thống kê trạng thái đơn hàng
    @Query(value = "SELECT status, COUNT(*) FROM Orders GROUP BY status", nativeQuery = true)
    List<Object[]> getOrderStatusStats();
    
    
}