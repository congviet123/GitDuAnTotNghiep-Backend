package poly.edu.service;

import poly.edu.entity.Order;
import poly.edu.entity.dto.OrderCreateDTO;
import poly.edu.entity.dto.OrderListDTO;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    
    // --- PHƯƠNG THỨC KHÁCH HÀNG ---
    
    /**
     * Tạo đơn hàng từ giỏ hàng.
     */
    Order placeOrder(String username, OrderCreateDTO orderDTO);

    /**
     * Lấy lịch sử đơn hàng của một người dùng.
     */
    List<OrderListDTO> findOrdersByUsername(String username);
    
    /**
     * Lấy chi tiết đơn hàng theo ID.
     */
    Optional<Order> findById(Integer orderId);
    
    
    // --- PHƯƠNG THỨC QUẢN TRỊ (BỔ SUNG CẦN THIẾT) ---
    
    /**
     * Lấy danh sách TẤT CẢ đơn hàng (Dành cho Admin).
     */
    List<OrderListDTO> findAllOrders(); // PHƯƠNG THỨC BỔ SUNG
    
    /**
     * Cập nhật trạng thái của một đơn hàng.
     * @param orderId ID của đơn hàng cần cập nhật.
     * @param newStatus Trạng thái mới (ví dụ: "CONFIRMED", "SHIPPED").
     * @return Order đã được cập nhật.
     */
    Order updateStatus(Integer orderId, String newStatus); 
    
    
    List<Double> getMonthlyRevenue(Integer year);
}