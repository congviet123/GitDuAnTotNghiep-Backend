package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.entity.Cart;
import poly.edu.entity.Order;
import poly.edu.entity.OrderDetail;
import poly.edu.entity.Product;
import poly.edu.entity.User;
import poly.edu.entity.dto.OrderCreateDTO;
import poly.edu.entity.dto.OrderListDTO;
import poly.edu.repository.CartRepository;
import poly.edu.repository.OrderDetailRepository;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.ProductRepository;
import poly.edu.repository.UserRepository;
import poly.edu.service.MailService;
import poly.edu.service.OrderService;
import poly.edu.service.ShoppingCartService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ShoppingCartService cartService;
    @Autowired private CartRepository cartRepository;
    @Autowired private MailService mailService;
    
    private OrderListDTO mapToDto(Order order) {
        OrderListDTO dto = new OrderListDTO();
        dto.setId(order.getId());
        dto.setCreateDate(order.getCreateDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        
        // --- TRUYỀN NGÀY GIAO HÀNG LÊN DTO ---
        dto.setDeliveryDate(order.getDeliveryDate());
        
        if (order.getAccount() != null) dto.setAccountFullname(order.getAccount().getFullname());
        return dto;
    }

    // --- HÀM HỖ TRỢ: CỘNG LẠI TỒN KHO ---
    private void restoreProductStock(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = detail.getProduct();
            BigDecimal restoredQty = product.getQuantity().add(detail.getQuantity());
            product.setQuantity(restoredQty);
            product.setAvailable(true); 
            productRepository.save(product);
        }
    }
    
    // --- HÀM HỖ TRỢ: KIỂM TRA QUÁ 24H HOÀN TRẢ ---
    private void check24hReturnPolicy(Order order) {
        if (order.getDeliveryDate() == null) return; // Nếu chưa giao hoặc lỗi ngày thì bỏ qua

        long diffInMillies = new Date().getTime() - order.getDeliveryDate().getTime();
        long diffInHours = diffInMillies / (60 * 60 * 1000);
        
        if (diffInHours >= 24) {
            throw new RuntimeException("Đã quá 24h kể từ khi giao hàng. Không thể yêu cầu hoàn trả.");
        }
    }

    // =========================================================================
    // TỰ ĐỘNG CHUYỂN TRẠNG THÁI "HOÀN TẤT ĐƠN" SAU 24H GIAO HÀNG
    // =========================================================================
    @Scheduled(fixedRate = 60000) // Chạy ngầm mỗi 60 giây (1 phút)
    @Transactional
    public void autoCompleteDeliveredOrders() {
        // Tính mốc thời gian: Hiện tại trừ đi 24 tiếng
        long cutoffMillis = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        Date cutoffDate = new Date(cutoffMillis);

        // Quét DB tìm các đơn DELIVERED đã giao lâu hơn mốc cutoffDate
        List<Order> expiredOrders = orderRepository.findDeliveredOrdersOlderThan(cutoffDate);

        if (!expiredOrders.isEmpty()) {
            for (Order order : expiredOrders) {
                order.setStatus("COMPLETED"); // Đổi trạng thái thành Hoàn tất
                // Lưu vết log hệ thống (tùy chọn)
                order.setNotes((order.getNotes() == null ? "" : order.getNotes()) + " | [Hệ thống tự động chốt đơn sau 24h]");
            }
            orderRepository.saveAll(expiredOrders);
            System.out.println("🤖 Đã tự động chốt thành công " + expiredOrders.size() + " đơn hàng hết hạn đổi trả.");
        }
    }
    // =========================================================================

    
    @Override
    public List<Order> filterOrdersForAdmin(String status, String paymentMethod, LocalDateTime start, LocalDateTime end) {
        String statusParam = (status == null || status.equals("ALL")) ? null : status;
        String methodParam = (paymentMethod == null || paymentMethod.equals("ALL")) ? null : paymentMethod;
        return orderRepository.filterOrders(statusParam, methodParam, start, end);
    }
    
    @Override
    @Transactional
    public Order placeOrder(String username, OrderCreateDTO orderDTO) {
        User user = userRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderDetail> details = new ArrayList<>();
        List<Integer> cartIdsToDelete = new ArrayList<>();

        for (OrderCreateDTO.OrderItem item : orderDTO.getItems()) {
            
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm ID " + item.getProductId() + " không tồn tại."));
            
            BigDecimal buyQuantity = BigDecimal.valueOf(item.getQuantity());

            if (product.getQuantity().compareTo(buyQuantity) < 0) {
                throw new RuntimeException("Sản phẩm '" + product.getName() + "' không đủ số lượng.");
            }

            // TRỪ TỒN KHO
            BigDecimal newStock = product.getQuantity().subtract(buyQuantity);
            product.setQuantity(newStock);
            if (newStock.compareTo(BigDecimal.ZERO) <= 0) product.setAvailable(false);
            productRepository.save(product);

            BigDecimal lineTotal = product.getPrice().multiply(buyQuantity);
            totalAmount = totalAmount.add(lineTotal);
            
            OrderDetail detail = new OrderDetail();
            detail.setQuantity(buyQuantity); 
            detail.setPrice(product.getPrice());
            detail.setProduct(product);
            details.add(detail);
            
            Optional<Cart> cartItem = cartRepository.findByUser_UsernameAndProduct_Id(username, item.getProductId());
            cartItem.ifPresent(cart -> cartIdsToDelete.add(cart.getId()));
        }

        Order order = new Order();
        order.setAccount(user);
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setNotes(orderDTO.getNotes());
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setStatus("PENDING");
        
        Order savedOrder = orderRepository.save(order);
        for (OrderDetail detail : details) {
            detail.setOrder(savedOrder);
            orderDetailRepository.save(detail);
        }
        
        for (Integer cartId : cartIdsToDelete) {
            cartService.remove(cartId);
        }
        
        return savedOrder;
    }

    /// --- YÊU CẦU HOÀN TRẢ ĐƠN HÀNG ĐÃ GIAO (CÓ GỬI MAIL CHO ADMIN) ---
    @Override
    @Transactional
    public void requestReturnFull(String username, Integer orderId, 
                                  String senderName, String senderPhone, String senderEmail,
                                  String reason, 
                                  String bankName, String accNo, String accName, 
                                  MultipartFile qrFile, MultipartFile[] files) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại."));

        if (!order.getAccount().getUsername().equals(username)) throw new RuntimeException("Lỗi quyền truy cập.");
        if (!"DELIVERED".equals(order.getStatus()) && !"COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ đơn hàng đã giao mới có thể hoàn trả.");
        }

        // --- CHECK LUẬT 24H ---
        check24hReturnPolicy(order);

        order.setStatus("RETURN_REQUESTED");
        String returnInfo = String.format(" [Yêu cầu trả: %s | Bank: %s-%s-%s | KH: %s-%s]", 
                                          reason, bankName, accNo, accName, senderName, senderPhone);
        order.setNotes((order.getNotes() == null ? "" : order.getNotes()) + returnInfo);
        orderRepository.save(order);

        StringBuilder productTable = new StringBuilder("<table style='width:100%; border-collapse: collapse; font-size: 14px;'>");
        productTable.append("<tr style='background: #f2f2f2;'><th>Sản phẩm</th><th>SL</th><th>Giá</th><th>Tổng</th></tr>");
        java.text.NumberFormat vnCurrency = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));

        for (OrderDetail d : order.getOrderDetails()) {
            productTable.append("<tr>")
                .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(d.getProduct().getName()).append("</td>")
                .append("<td style='border: 1px solid #ddd; padding: 8px; text-align: center;'>").append(d.getQuantity()).append("</td>")
                .append("<td style='border: 1px solid #ddd; padding: 8px; text-align: right;'>").append(vnCurrency.format(d.getPrice())).append("</td>")
                .append("<td style='border: 1px solid #ddd; padding: 8px; text-align: right;'>").append(vnCurrency.format(d.getPrice().multiply(d.getQuantity()))).append("</td>")
                .append("</tr>");
        }
        productTable.append("</table>");

        String qrImageHtml = (qrFile != null && !qrFile.isEmpty()) 
            ? "<td style='width: 150px; text-align: center; border-left: 1px dashed #ccc;'>"
              + "<div style='font-size: 12px; font-weight: bold;'>QR Của Khách</div>"
              + "<img src='cid:qrCodeImage' style='width: 120px; border: 1px solid #ddd;'>" + "</td>" : "";

        String subject = "YÊU CẦU HOÀN TRẢ ĐƠN HÀNG #" + orderId;
        String body = "<div style='font-family: Arial; max-width: 800px; color: #333;'>"
                    + "<h2 style='color: #d9534f;'>YÊU CẦU HOÀN TRẢ #" + orderId + "</h2>"
                    + "<p><strong>Khách hàng:</strong> " + senderName + " - " + senderPhone + "</p>"
                    + "<p><strong>Lý do:</strong> " + reason + "</p>"
                    + "<div style='background: #eefdfd; padding: 15px; border-radius: 5px; border: 1px solid #bce8f1;'>"
                    + "<table style='width: 100%;'><tr>"
                    + "<td><strong>Ngân hàng:</strong> " + bankName + "<br><strong>STK:</strong> " + accNo + "<br><strong>Chủ TK:</strong> " + accName + "</td>"
                    + qrImageHtml + "</tr></table></div>"
                    + "<h3>Chi tiết đơn hàng:</h3>" + productTable.toString() + "</div>";

        mailService.sendEmailWithReturnRequest(subject, body, qrFile, files); 
    }

    @Override
    @Transactional
    public Order cancelOrder(String username, Integer orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Đơn không tồn tại."));
        if (!order.getAccount().getUsername().equals(username)) throw new RuntimeException("Không có quyền.");
        if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) throw new RuntimeException("Không thể hủy.");
        
        order.setStatus("CANCELLED");
        order.setNotes((order.getNotes() == null ? "" : order.getNotes()) + " | Lý do hủy: " + reason);
        
        restoreProductStock(order);
        
        return orderRepository.save(order);
    }

    
    /// --- HỦY ĐƠN HÀNG ĐÃ THANH TOÁN & GỬI MAIL YÊU CẦU HOÀN TIỀN CHO ADMIN ---
    @Override
    @Transactional
    public void cancelPaidOrder(String username, Integer orderId, String reason, 
                                String bankName, String accNo, String accName, MultipartFile qrFile) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại."));

        if (!order.getAccount().getUsername().equals(username)) {
            throw new RuntimeException("Không có quyền truy cập.");
        }
        if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng đã được xử lý, không thể hủy.");
        }

        order.setStatus("CANCELLED"); 
        String refundInfo = String.format(" [Hủy & Hoàn tiền: %s | Bank: %s-%s-%s]", reason, bankName, accNo, accName);
        order.setNotes((order.getNotes() == null ? "" : order.getNotes()) + refundInfo);
        
        restoreProductStock(order);
        
        orderRepository.save(order);

        StringBuilder productTable = new StringBuilder("<table style='width:100%; border-collapse: collapse; font-size: 14px;'>");
        productTable.append("<tr style='background: #f2f2f2;'><th>Sản phẩm</th><th>SL</th><th>Giá</th><th>Tổng</th></tr>");
        java.text.NumberFormat vnCurrency = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));

        for (OrderDetail d : order.getOrderDetails()) {
            productTable.append("<tr>")
                .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(d.getProduct().getName()).append("</td>")
                .append("<td style='border: 1px solid #ddd; padding: 8px; text-align: center;'>").append(d.getQuantity()).append("</td>")
                .append("<td style='border: 1px solid #ddd; padding: 8px; text-align: right;'>").append(vnCurrency.format(d.getPrice())).append("</td>")
                .append("<td style='border: 1px solid #ddd; padding: 8px; text-align: right;'>").append(vnCurrency.format(d.getPrice().multiply(d.getQuantity()))).append("</td>")
                .append("</tr>");
        }
        productTable.append("</table>");

        String qrImageHtml = (qrFile != null && !qrFile.isEmpty()) 
            ? "<td style='width: 150px; text-align: center; border-left: 1px dashed #ccc;'>"
              + "<div style='font-size: 12px; font-weight: bold;'>QR Của Khách</div>"
              + "<img src='cid:qrCodeImage' style='width: 120px; border: 1px solid #ddd;'>" + "</td>" : "";

        String subject = "YÊU CẦU HỦY ĐƠN & HOÀN TIỀN #" + orderId;
        String body = "<div style='font-family: Arial; max-width: 800px; color: #333;'>"
                    + "<h2 style='color: #f0ad4e;'>YÊU CẦU HỦY & HOÀN TIỀN #" + orderId + "</h2>"
                    + "<p><strong>Khách hàng:</strong> " + order.getAccount().getFullname() + " - " + order.getAccount().getPhone() + "</p>"
                    + "<p><strong>Lý do hủy:</strong> " + reason + "</p>"
                    + "<div style='background: #fff3cd; padding: 15px; border-radius: 5px; border: 1px solid #ffeeba;'>"
                    + "<table style='width: 100%;'><tr>"
                    + "<td><strong>Ngân hàng:</strong> " + bankName + "<br><strong>STK:</strong> " + accNo + "<br><strong>Chủ TK:</strong> " + accName + "</td>"
                    + qrImageHtml + "</tr></table></div>"
                    + "<h3>Chi tiết đơn hàng:</h3>" + productTable.toString() + "</div>";

        mailService.sendEmailWithReturnRequest(subject, body, qrFile, null); 
    }

    @Override
    @Transactional
    public Order requestReturn(String username, Integer orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Đơn không tồn tại."));
        if (!"DELIVERED".equals(order.getStatus()) && !"COMPLETED".equals(order.getStatus())) throw new RuntimeException("Chưa giao xong.");
        
        // --- CHECK LUẬT 24H ---
        check24hReturnPolicy(order);

        order.setStatus("RETURN_REQUESTED");
        order.setNotes((order.getNotes() == null ? "" : order.getNotes()) + " | Yêu cầu trả: " + reason);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void hideOrder(String username, Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Đơn không tồn tại."));
        if (!order.getAccount().getUsername().equals(username)) throw new RuntimeException("Không có quyền.");
        order.setStatus("HIDDEN");
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true) 
    public List<OrderListDTO> findOrdersByUsername(String username) {
        return orderRepository.findByAccountUsernameOrderByCreateDateDesc(username)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Integer orderId) {
        return orderRepository.findByIdWithDetails(orderId);
    }

    @Override
    @Transactional
    public Order updateStatus(Integer orderId, String newStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Không thấy đơn."));
        
        String oldStatus = order.getStatus();
        String formattedNewStatus = newStatus.toUpperCase().trim();
        
        // CỘNG KHO
        boolean isOldStatusNormal = !oldStatus.equals("CANCELLED") && !oldStatus.equals("CANCELLED_REFUNDED");
        boolean isNewStatusCancelled = formattedNewStatus.equals("CANCELLED");
        
        if (isOldStatusNormal && isNewStatusCancelled) {
            restoreProductStock(order);
        }
        
        // --- SET NGÀY GIAO HÀNG ---
        if (!oldStatus.equals("DELIVERED") && formattedNewStatus.equals("DELIVERED")) {
            order.setDeliveryDate(new Date()); // Lưu giờ hiện tại khi Admin bấm giao thành công
        }
        
        order.setStatus(formattedNewStatus); 
        return orderRepository.save(order);
    }

    // --- HÀM HỖ TRỢ: CỘNG LẠI KHO CHO ĐƠN HÀNG TRẢ VỀ ĐÃ HOÀN TIỀN ---
    @Override
    @Transactional
    public void restockReturnedOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Đơn không tồn tại."));
        
        if (!"CANCELLED_REFUNDED".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể cộng kho thủ công cho đơn hàng đã hoàn tiền (CANCELLED_REFUNDED).");
        }

        String currentNotes = order.getNotes() != null ? order.getNotes() : "";
        if (currentNotes.contains("(Đã cộng lại kho)")) {
            throw new RuntimeException("Đơn hàng này đã được cộng lại kho trước đó rồi!");
        }

        restoreProductStock(order);

        order.setNotes(currentNotes + " (Đã cộng lại kho)");
        orderRepository.save(order);
    }

    @Override
    public List<Double> getMonthlyRevenue(Integer year) {
        List<Double> revenueList = new ArrayList<>(Collections.nCopies(12, 0.0));
        List<Object[]> results = orderRepository.getMonthlyRevenue(year);
        for (Object[] row : results) {
            int month = (int) row[0];
            double total = (double) row[1];
            if (month >= 1 && month <= 12) revenueList.set(month - 1, total);
        }
        return revenueList;
    }
    
    @Override public List<Order> findAll() { return orderRepository.findAll(); }
    @Override public Order save(Order order) { return orderRepository.save(order); }
    @Override public void delete(Integer id) { orderRepository.deleteById(id); }
    @Override public List<OrderListDTO> findAllOrders() { 
        return orderRepository.findAllOrdersSimple().stream().map(this::mapToDto).collect(Collectors.toList());
    }
}