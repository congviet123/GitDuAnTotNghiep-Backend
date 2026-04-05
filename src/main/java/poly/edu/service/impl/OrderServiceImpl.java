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
import poly.edu.entity.Voucher;
import poly.edu.entity.dto.OrderCreateDTO;
import poly.edu.entity.dto.OrderListDTO;
import poly.edu.repository.CartRepository;
import poly.edu.repository.OrderDetailRepository;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.ProductRepository;
import poly.edu.repository.UserRepository;
import poly.edu.repository.VoucherRepository;
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
    @Autowired private VoucherRepository voucherRepository;
    
    //  Hàm chuyển đổi (Mapper) từ đối tượng Entity Order sang DTO (Data Transfer Object)
    // Giúp giảm dung lượng dữ liệu trả về cho Frontend và bảo mật thông tin nhạy cảm.
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

    //  Hàm hỗ trợ - Hoàn lại số lượng tồn kho cho các Sản phẩm trong Đơn hàng bị hủy/trả.
    private void restoreProductStock(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = detail.getProduct();
            BigDecimal restoredQty = product.getQuantity().add(detail.getQuantity());
            product.setQuantity(restoredQty);
            product.setAvailable(true); // Mở bán lại nếu trước đó bị hết hàng
            productRepository.save(product);
        }
    }
    
    // Hàm hỗ trợ - Kiểm tra xem đơn hàng đã giao quá 24h chưa (Quy định không được hoàn trả sau 24h).
    private void check24hReturnPolicy(Order order) {
        if (order.getDeliveryDate() == null) return; // Nếu chưa giao hoặc lỗi ngày thì bỏ qua

        long diffInMillies = new Date().getTime() - order.getDeliveryDate().getTime();
        long diffInHours = diffInMillies / (60 * 60 * 1000);
        
        if (diffInHours >= 24) {
            throw new RuntimeException("Đã quá 24h kể từ khi giao hàng. Không thể yêu cầu hoàn trả.");
        }
    }
    
    // ========== HÀM ĐẾM SỐ LẦN USER ĐÃ DÙNG VOUCHER (PRIVATE) ==========
    private int countUserVoucherUsagePrivate(String username, String voucherCode) {
        int count = 0;
        List<Order> userOrders = orderRepository.findByAccountUsernameOrderByCreateDateDesc(username);
        
        // ========== LOG DEBUG ==========
        System.out.println("========== KIỂM TRA SỐ LẦN DÙNG VOUCHER ==========");
        System.out.println("User: " + username);
        System.out.println("Voucher: " + voucherCode);
        System.out.println("Tổng số đơn của user: " + userOrders.size());
        // ================================
        
        for (Order order : userOrders) {
            System.out.println("  - Đơn #" + order.getId() + ": voucher=" + order.getVoucherCode() + ", status=" + order.getStatus());
            
            if (voucherCode.equals(order.getVoucherCode())) {
                String status = order.getStatus();
                // KHÔNG TÍNH CÁC ĐƠN ĐÃ HỦY
                if (!"CANCELLED".equals(status) && !"CANCELLED_REFUNDED".equals(status)) {
                    count++;
                    System.out.println("    -> TÍNH (lần thứ " + count + ")");
                } else {
                    System.out.println("    -> KHÔNG TÍNH (đơn đã hủy)");
                }
            }
        }
        System.out.println("Kết quả: User đã dùng " + count + " lần");
        return count;
    }
    // ========== KẾT THÚC ==========

    // =========================================================================
    // [JOB TỰ ĐỘNG]: TỰ ĐỘNG CHUYỂN TRẠNG THÁI "HOÀN TẤT ĐƠN" SAU 24H GIAO HÀNG
    // =========================================================================
    @Scheduled(fixedRate = 60000) // Chạy ngầm mỗi 60 giây (1 phút) một lần trên Server
    @Transactional
    public void autoCompleteDeliveredOrders() {
        // Tính mốc thời gian: Thời điểm hiện tại lùi lại 24 tiếng
        long cutoffMillis = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        Date cutoffDate = new Date(cutoffMillis);

        // Quét DB tìm các đơn có trạng thái DELIVERED (Đã giao) mà có ngày giao lâu hơn mốc cutoffDate
        List<Order> expiredOrders = orderRepository.findDeliveredOrdersOlderThan(cutoffDate);

        if (!expiredOrders.isEmpty()) {
            for (Order order : expiredOrders) {
                order.setStatus("COMPLETED"); // Chuyển trạng thái chốt đơn (Không cho trả hàng nữa)
                // Lưu vết log hệ thống vào phần Ghi chú
                order.setNotes((order.getNotes() == null ? "" : order.getNotes()) + " | [Hệ thống tự động chốt đơn sau 24h]");
            }
            orderRepository.saveAll(expiredOrders);
            System.out.println("🤖 Đã tự động chốt thành công " + expiredOrders.size() + " đơn hàng hết hạn đổi trả.");
        }
    }
    // =========================================================================

    //  Lọc đơn hàng cho trang Quản trị Admin
    @Override
    public List<Order> filterOrdersForAdmin(String status, String paymentMethod, LocalDateTime start, LocalDateTime end) {
        String statusParam = (status == null || status.equals("ALL")) ? null : status;
        String methodParam = (paymentMethod == null || paymentMethod.equals("ALL")) ? null : paymentMethod;
        return orderRepository.filterOrders(statusParam, methodParam, start, end);
    }
    
    //  Tính năng Khách hàng Đặt Hàng Mới (Checkout)
    @Override
    @Transactional
    public Order placeOrder(String username, OrderCreateDTO orderDTO) {
        User user = userRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));
        
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderDetail> details = new ArrayList<>();
        List<Integer> cartIdsToDelete = new ArrayList<>();


        // ========== TÍNH TỔNG TIỀN HÀNG ==========
<<<<<<< HEAD

=======
>>>>>>> cdf00652cff9dd356633a7468373e132547e5d03
        // Duyệt qua từng sản phẩm mà khách đặt mua
        for (OrderCreateDTO.OrderItem item : orderDTO.getItems()) {
            
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm ID " + item.getProductId() + " không tồn tại."));
            
            BigDecimal buyQuantity = BigDecimal.valueOf(item.getQuantity());

            // Kiểm tra tồn kho có đủ không
            if (product.getQuantity().compareTo(buyQuantity) < 0) {
                throw new RuntimeException("Sản phẩm '" + product.getName() + "' không đủ số lượng.");
            }

            // TRỪ TỒN KHO THỰC TẾ TRONG DATABASE
            BigDecimal newStock = product.getQuantity().subtract(buyQuantity);
            product.setQuantity(newStock);
            if (newStock.compareTo(BigDecimal.ZERO) <= 0) product.setAvailable(false); // Hết hàng thì tự động tắt bán
            productRepository.save(product);

            // Cộng dồn thành tiền
            BigDecimal lineTotal = product.getPrice().multiply(buyQuantity);
            subtotal = subtotal.add(lineTotal);
            
            // Tạo chi tiết đơn hàng
            OrderDetail detail = new OrderDetail();
            detail.setQuantity(buyQuantity); 
            detail.setPrice(product.getPrice());
            detail.setProduct(product);
            details.add(detail);
            
            // Lưu lại ID giỏ hàng để lát nữa xóa (vì đã đặt mua xong)
            Optional<Cart> cartItem = cartRepository.findByUser_UsernameAndProduct_Id(username, item.getProductId());
            cartItem.ifPresent(cart -> cartIdsToDelete.add(cart.getId()));
        }


        // ==================== ÁP DỤNG VOUCHER ====================
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedVoucherCode = null;
        
        if (orderDTO.getVoucherCode() != null && !orderDTO.getVoucherCode().trim().isEmpty()) {
            String voucherCode = orderDTO.getVoucherCode().toUpperCase().trim();
            Optional<Voucher> voucherOpt = voucherRepository.findByCode(voucherCode);
            
            if (voucherOpt.isPresent()) {
                Voucher voucher = voucherOpt.get();
                LocalDateTime now = LocalDateTime.now();
                
                // Kiểm tra voucher còn hiệu lực
                boolean isValid = voucher.getActive() && 
                                  voucher.getStartDate() != null && voucher.getStartDate().isBefore(now) &&
                                  voucher.getEndDate() != null && voucher.getEndDate().isAfter(now) &&
                                  voucher.getVisibility(); // Chỉ voucher công khai mới áp dụng
                                  
                // Kiểm tra số lượng còn
                if (voucher.getQuantity() > 0 && voucher.getUsedCount() >= voucher.getQuantity()) {
                    isValid = false;
                    throw new RuntimeException("Voucher đã hết số lượng sử dụng!");
                }
                
                // ========== KIỂM TRA GIỚI HẠN MỖI NGƯỜI DÙNG ==========
                if (voucher.getPerUserLimit() != null && voucher.getPerUserLimit() > 0) {
                    int userUsageCount = countUserVoucherUsagePrivate(username, voucherCode);
                    System.out.println(">>> perUserLimit = " + voucher.getPerUserLimit() + ", userUsageCount = " + userUsageCount);
                    if (userUsageCount >= voucher.getPerUserLimit()) {
                        throw new RuntimeException("Bạn đã sử dụng voucher này " + userUsageCount + " lần. Giới hạn mỗi người chỉ " + voucher.getPerUserLimit() + " lần!");
                    }
                }
                // ========== KẾT THÚC ==========
                
                // Kiểm tra điều kiện đơn hàng tối thiểu
                if (voucher.getMinCondition().compareTo(subtotal) > 0) {
                    throw new RuntimeException("Đơn hàng tối thiểu " + formatPrice(voucher.getMinCondition()) + " mới được áp dụng!");
                }
                
                if (isValid) {
                    appliedVoucherCode = voucherCode;
                    
                    // Tính số tiền giảm
                    if (voucher.getDiscountPercent() != null && voucher.getDiscountPercent() > 0) {
                        // Giảm theo phần trăm
                        BigDecimal percent = BigDecimal.valueOf(voucher.getDiscountPercent());
                        discountAmount = subtotal.multiply(percent).divide(BigDecimal.valueOf(100));
                        
                        // Kiểm tra giảm tối đa
                        if (voucher.getMaxDiscountAmount() != null && voucher.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                            if (discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                                discountAmount = voucher.getMaxDiscountAmount();
                            }
                        }
                    } else if (voucher.getDiscountAmount() != null && voucher.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                        // Giảm theo số tiền cố định
                        discountAmount = voucher.getDiscountAmount();
                        if (discountAmount.compareTo(subtotal) > 0) {
                            discountAmount = subtotal;
                        }
                    }
                } else {
                    throw new RuntimeException("Voucher không hợp lệ hoặc đã hết hạn!");
                }
            } else {
                throw new RuntimeException("Mã voucher không tồn tại!");
            }
        }
        
        BigDecimal totalAmount = subtotal.subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) totalAmount = BigDecimal.ZERO;
        // =========================================================


        // Tạo Đơn hàng lưu DB
        Order order = new Order();
        order.setAccount(user);
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setNotes(orderDTO.getNotes());
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setStatus("PENDING");
        order.setVoucherCode(appliedVoucherCode); // Lưu mã voucher đã dùng
<<<<<<< HEAD

=======
>>>>>>> cdf00652cff9dd356633a7468373e132547e5d03
        
        Order savedOrder = orderRepository.save(order);
        
        // Gắn ID đơn hàng vào từng chi tiết và lưu
        for (OrderDetail detail : details) {
            detail.setOrder(savedOrder);
            orderDetailRepository.save(detail);
        }
        
<<<<<<< HEAD

=======
>>>>>>> cdf00652cff9dd356633a7468373e132547e5d03
        // Cập nhật used_count của voucher
        if (appliedVoucherCode != null) {
            voucherRepository.findByCode(appliedVoucherCode).ifPresent(voucher -> {
                voucher.setUsedCount(voucher.getUsedCount() + 1);
                voucherRepository.save(voucher);
            });
        }
        
<<<<<<< HEAD

        // Xóa giỏ hàng
=======
        // Xóa giỏ hàng của user sau khi đặt hàng thành công
>>>>>>> cdf00652cff9dd356633a7468373e132547e5d03
        for (Integer cartId : cartIdsToDelete) {
            cartService.remove(cartId);
        }
        
        return savedOrder;
    }


    // ==================== HÀM HỖ TRỢ FORMAT TIỀN ====================
    private String formatPrice(BigDecimal price) {
        return new java.text.DecimalFormat("#,###").format(price) + "đ";
    }

    /// --- YÊU CẦU HOÀN TRẢ ĐƠN HÀNG ĐÃ GIAO (CÓ GỬI MAIL CHO ADMIN) ---
<<<<<<< HEAD

=======
>>>>>>> cdf00652cff9dd356633a7468373e132547e5d03
    ///  Khách hàng yêu cầu hoàn trả đơn (Có gắn ảnh, QR code và gửi email thông báo cho Admin)
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
        
        // Ràng buộc chỉ đơn đã giao hoặc hoàn tất mới được trả
        if (!"DELIVERED".equals(order.getStatus()) && !"COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ đơn hàng đã giao mới có thể hoàn trả.");
        }

        // --- CHECK LUẬT 24H ---
        check24hReturnPolicy(order);

        // Chuyển trạng thái sang Yêu cầu hoàn trả
        order.setStatus("RETURN_REQUESTED");
        String returnInfo = String.format(" [Yêu cầu trả: %s | Bank: %s-%s-%s | KH: %s-%s]", 
                                          reason, bankName, accNo, accName, senderName, senderPhone);
        order.setNotes((order.getNotes() == null ? "" : order.getNotes()) + returnInfo);
        orderRepository.save(order);

        // Xây dựng nội dung Email (Bảng HTML hiển thị sản phẩm trả về)
        StringBuilder productTable = new StringBuilder("<table style='width:100%; border-collapse: collapse; font-size: 14px;'>");
        productTable.append("<tr style='background: #f2f2f2;'><th>Sản phẩm</th><th>SL</th><th>Giá</th><th>Tổng</th>");
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

    // Hủy đơn hàng THÔNG THƯỜNG (Thanh toán COD)
    @Override
    @Transactional
    public Order cancelOrder(String username, Integer orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Đơn không tồn tại."));
        if (!order.getAccount().getUsername().equals(username)) throw new RuntimeException("Không có quyền.");
        if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) throw new RuntimeException("Không thể hủy.");
        
        // Với đơn COD, khi khách hủy sẽ chuyển ngay sang Hủy Thành Công (CANCELLED)
        order.setStatus("CANCELLED");
        order.setNotes((order.getNotes() == null ? "" : order.getNotes()) + " | Lý do hủy: " + reason);
        
        restoreProductStock(order); // Trả lại tồn kho
        
        return orderRepository.save(order);
    }

    
    /// HỦY ĐƠN HÀNG ĐÃ THANH TOÁN (Bank) - Cần xin số tài khoản và gửi mail Yêu Cầu Hủy (Không hủy ngay lập tức)
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

        // Với đơn Bank, khi khách hủy phải đổi trạng thái thành Yêu cầu Hủy, chứ không Hủy luôn (để chờ Admin hoàn tiền)
        // [QUAN TRỌNG]: TÔI ĐÃ SỬA LẠI THÀNH CANCEL_REQUESTED THAY VÌ CANCELLED
        order.setStatus("CANCEL_REQUESTED"); 
        String refundInfo = String.format(" [Hủy & Hoàn tiền: %s | Bank: %s-%s-%s]", reason, bankName, accNo, accName);
        order.setNotes((order.getNotes() == null ? "" : order.getNotes()) + refundInfo);
        
        //  Chưa restore tồn kho ở đây, vì đơn mới chỉ là "yêu cầu hủy". Khi nào Admin duyệt thì mới cộng kho!
        orderRepository.save(order);

        // Gửi mail thông báo cho Admin
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

    //  Khách hàng yêu cầu trả hàng thông thường (Không gửi ảnh qua form rút gọn)
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

    //Khách hàng Xóa Lịch sử mua hàng (Thực chất là Ẩn đi đối với User)
    @Override
    @Transactional
    public void hideOrder(String username, Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Đơn không tồn tại."));
        if (!order.getAccount().getUsername().equals(username)) throw new RuntimeException("Không có quyền.");
        
        //  CHẶN XÓA ĐƠN HÀNG ĐANG XỬ LÝ / YÊU CẦU HỦY
        // Chỉ cho phép User xóa lịch sử các đơn hàng đã Hoàn Tất hoặc Đã Hủy Thành Công
        if (!"COMPLETED".equals(order.getStatus()) && 
            !"CANCELLED".equals(order.getStatus()) && 
            !"CANCELLED_REFUNDED".equals(order.getStatus())) {
            throw new RuntimeException("Bảo mật: Không được phép xóa đơn hàng đang trong trạng thái xử lý hoặc chờ duyệt hủy!");
        }

        order.setStatus("HIDDEN");
        orderRepository.save(order);
    }

    // Lấy lịch sử đơn hàng của 1 user
    @Override
    @Transactional(readOnly = true) 
    public List<OrderListDTO> findOrdersByUsername(String username) {
        return orderRepository.findByAccountUsernameOrderByCreateDateDesc(username)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // Lấy chi tiết đơn
    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Integer orderId) {
        return orderRepository.findByIdWithDetails(orderId);
    }

    //  Admin đổi trạng thái đơn hàng
    @Override
    @Transactional
    public Order updateStatus(Integer orderId, String newStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Không thấy đơn."));
        
        String oldStatus = order.getStatus();
        String formattedNewStatus = newStatus.toUpperCase().trim();
        
        // NẾU ADMIN DUYỆT HỦY (Chuyển sang CANCELLED) THÌ MỚI CỘNG LẠI TỒN KHO
        boolean isOldStatusNormal = !oldStatus.equals("CANCELLED") && !oldStatus.equals("CANCELLED_REFUNDED");
        boolean isNewStatusCancelled = formattedNewStatus.equals("CANCELLED");
        
        if (isOldStatusNormal && isNewStatusCancelled) {
            restoreProductStock(order);
        }
        
        // --- SET NGÀY GIAO HÀNG (Mốc đếm ngược 24h) ---
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

    // Báo cáo doanh thu tháng
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

    
    // ========== IMPLEMENT METHOD TỪ INTERFACE ==========
    @Override
    public int countUserVoucherUsage(String username, String voucherCode) {
        return countUserVoucherUsagePrivate(username, voucherCode);
    }
    // ========== KẾT THÚC ==========

<<<<<<< HEAD

=======
>>>>>>> cdf00652cff9dd356633a7468373e132547e5d03
}