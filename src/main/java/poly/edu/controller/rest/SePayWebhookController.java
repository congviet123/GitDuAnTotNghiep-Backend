package poly.edu.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Order;
import poly.edu.entity.dto.SePayWebhookDTO;
import poly.edu.repository.OrderRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CrossOrigin("*") // Cho phép Frontend (VueJS) gọi API này
@RestController
@RequestMapping("/rest/sepay")
public class SePayWebhookController {

    @Autowired
    private OrderRepository orderRepository;

    // API Key của bạn (Giữ bí mật ở Backend)
    private static final String SEPAY_API_KEY = "IADCEASR5BJBGOILSEVN6A83HGFWVEKRBXLUS0KDJYQDOIZLQVGCTFIOXM32KW0M";

    /**
     * API 1: Lấy thông tin ngân hàng của Shop
     * Frontend sẽ gọi vào đây để lấy số tài khoản thay vì viết cứng (hardcode)
     */
    @GetMapping("/bank-info")
    public ResponseEntity<?> getShopBankInfo() {
        Map<String, String> bankInfo = new HashMap<>();
        
        // Bạn sửa thông tin ngân hàng chính xác ở đây
        bankInfo.put("bankId", "TPB");             // Mã ngân hàng (MB, VCB, TPB...)
        bankInfo.put("accountNo", "55222422222");  // Số tài khoản
        bankInfo.put("accountName", "NGUYEN CONG VIET"); // Tên chủ tài khoản
        
        return ResponseEntity.ok(bankInfo);
    }

    /**
     * API 2: Nhận Webhook từ SePay (Tự động xác nhận thanh toán)
     */
    @PostMapping("/webhook")
    @Transactional
    public ResponseEntity<?> handleSePayWebhook(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody SePayWebhookDTO webhookData
    ) {
        // 1. KIỂM TRA BẢO MẬT (API KEY)
        if (authorization == null || !authorization.startsWith("Apikey ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String token = authorization.substring(7);
        if (!token.equals(SEPAY_API_KEY)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        // 2. XỬ LÝ DỮ LIỆU
        String orderIdStr = extractOrderId(webhookData.getContent());
        if (orderIdStr != null) {
            try {
                int orderId = Integer.parseInt(orderIdStr);
                Order order = orderRepository.findById(orderId).orElse(null);
                
                // Kiểm tra đơn hàng tồn tại & Số tiền chuyển khoản >= Tổng tiền đơn hàng
                if (order != null && webhookData.getTransferAmount() >= order.getTotalAmount().doubleValue()) {
                    // Chỉ cập nhật nếu đơn hàng chưa thanh toán để tránh trùng lặp
                    if (!"CONFIRMED".equals(order.getStatus()) && !"PAID".equals(order.getStatus())) {
                        order.setStatus("CONFIRMED"); // Hoặc trạng thái PAID tùy enum của bạn
                        orderRepository.save(order);
                        System.out.println("SePay Webhook: Đã thanh toán thành công đơn hàng #" + orderId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Luôn trả về thành công để SePay biết đã nhận tin
        return ResponseEntity.ok(new SePayWebhookDTO());
    }	

    // Hàm tách mã đơn hàng từ nội dung chuyển khoản (VD: "DH 123" -> "123")
    private String extractOrderId(String content) {
        if (content == null) return null;
        Pattern pattern = Pattern.compile("DH\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }
}