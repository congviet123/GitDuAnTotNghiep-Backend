package poly.edu.service;

import org.springframework.web.multipart.MultipartFile;
import poly.edu.entity.Order;
import poly.edu.entity.dto.OrderCreateDTO;
import poly.edu.entity.dto.OrderListDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    

    // 1. PHƯƠNG THỨC DÀNH CHO KHÁCH HÀNG (CLIENT)
    
    /**
     * Tạo đơn hàng mới từ giỏ hàng.
     */
    Order placeOrder(String username, OrderCreateDTO orderDTO);

    /**
     * Lấy danh sách lịch sử đơn hàng của User (Dùng DTO rút gọn).
     */
    List<OrderListDTO> findOrdersByUsername(String username);
    
    /**
     * Tìm chi tiết một đơn hàng theo ID.
     */
    Optional<Order> findById(Integer orderId);

    /**
     * Khách hàng tự hủy đơn hàng (Khi đơn ở trạng thái Chờ xử lý/Xác nhận).
     */
    Order cancelOrder(String username, Integer orderId, String reason);

    // --- HỦY ĐƠN HÀNG ĐÃ THANH TOÁN (CẦN HOÀN TIỀN) ---
    /**
     * Khách hàng tự hủy đơn hàng đã thanh toán (Cần thông tin thẻ ngân hàng để hoàn tiền).
     */
    void cancelPaidOrder(String username, Integer orderId, String reason, 
                         String bankName, String accNo, String accName, MultipartFile qrFile);

    /**
     * Khách hàng gửi yêu cầu hoàn trả cơ bản.
     */
    Order requestReturn(String username, Integer orderId, String reason);

    /**
     * Khách hàng yêu cầu hoàn trả chuyên sâu (Kèm thông tin Bank, Ảnh QR và Ảnh minh chứng).
     */
    void requestReturnFull(String username, Integer orderId, 
                           String senderName, String senderPhone, String senderEmail,
                           String reason, String bankName, String accNo, String accName, 
                           MultipartFile qrFile, MultipartFile[] files);

    /**
     * Ẩn đơn hàng khỏi lịch sử hiển thị (Xóa mềm phía Client).
     */
    void hideOrder(String username, Integer orderId);

    // ========== THÊM VÀO ĐÂY ==========
    /**
     * Đếm số lần user đã sử dụng voucher (không tính đơn đã hủy)
     */
    int countUserVoucherUsage(String username, String voucherCode);
    // ========== KẾT THÚC THÊM ==========
    
    
    // 2. PHƯƠNG THỨC DÀNH CHO QUẢN TRỊ VIÊN (ADMIN)
    /**
     * Lấy tất cả đơn hàng (Entity đầy đủ).
     */
    List<Order> findAll(); 
    
    /**
     * Lưu thông tin đơn hàng.
     */
    Order save(Order order); 
    
    /**
     * Xóa đơn hàng theo ID (Xóa cứng trong Database).
     */
    void delete(Integer id);
    
    /**
     * Lấy danh sách tất cả đơn hàng (Dùng DTO rút gọn cho bảng quản trị).
     */
    List<OrderListDTO> findAllOrders(); 
    
    /**
     * Cập nhật trạng thái đơn hàng (Duyệt đơn, Giao hàng, Hoàn tất...).
     */
    Order updateStatus(Integer orderId, String newStatus); 

    // --- CỘNG LẠI TỒN KHO THỦ CÔNG ---
    /**
     * Cộng lại số lượng sản phẩm vào kho cho đơn hàng hoàn trả (Thao tác thủ công của Admin).
     */
    void restockReturnedOrder(Integer orderId);
    
    /**
     * Lấy dữ liệu doanh thu 12 tháng để vẽ biểu đồ.
     */
    List<Double> getMonthlyRevenue(Integer year);

    /**
     * Bộ lọc nâng cao dành cho Admin.
     * @param status Trạng thái đơn hàng (PENDING, DELIVERED...)
     * @param paymentMethod Hình thức thanh toán (CASH, TRANSFER...)
     * @param start Thời điểm bắt đầu
     * @param end Thời điểm kết thúc
     */
    List<Order> filterOrdersForAdmin(String status, String paymentMethod, LocalDateTime start, LocalDateTime end);
}