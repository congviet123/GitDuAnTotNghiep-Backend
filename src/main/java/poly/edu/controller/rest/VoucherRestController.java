package poly.edu.controller.rest;

import poly.edu.entity.dto.VoucherDTO;
import poly.edu.entity.dto.VoucherEmailDTO;
import poly.edu.service.VoucherService;
import poly.edu.service.EmailService;
import poly.edu.repository.UserRepository;
import poly.edu.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@RestController
@RequestMapping("/rest/vouchers")
@CrossOrigin(origins = "*")
public class VoucherRestController {
    
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;
    
    // Lấy tất cả voucher - ADMIN và STAFF đều xem được
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping
    public ResponseEntity<List<VoucherDTO>> getAll() {
        return ResponseEntity.ok(voucherService.findAll());
    }
    
    // Lấy voucher đang hoạt động
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/active")
    public ResponseEntity<List<VoucherDTO>> getActive() {
        return ResponseEntity.ok(voucherService.findActiveVouchers());
    }
    
    // Lấy voucher công khai đang hoạt động (cho khách hàng) - PUBLIC
    @GetMapping("/public")
    public ResponseEntity<List<VoucherDTO>> getPublicActive() {
        return ResponseEntity.ok(voucherService.findPublicActiveVouchers());
    }
    
    // Lấy voucher theo mã
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/{code}")
    public ResponseEntity<VoucherDTO> getByCode(@PathVariable String code) {
        return voucherService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Kiểm tra voucher công khai (cho khách hàng áp dụng) - PUBLIC
    @GetMapping("/public/{code}")
    public ResponseEntity<?> checkPublicVoucher(@PathVariable String code) {
        java.util.Optional<VoucherDTO> voucherOpt = voucherService.findByCode(code.toUpperCase());
        
        if (voucherOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mã voucher không tồn tại!");
        }
        
        VoucherDTO voucher = voucherOpt.get();
        
        if (!"public".equals(voucher.getVisibility())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mã voucher không hợp lệ!");
        }
        
        if (!"published".equals(voucher.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Voucher chưa được kích hoạt!");
        }
        
        LocalDate today = LocalDate.now();
        if (today.isBefore(voucher.getStartDate())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Voucher có hiệu lực từ ngày " + voucher.getStartDate());
        }
        
        if (today.isAfter(voucher.getExpiryDate())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Voucher đã hết hạn!");
        }
        
        if (voucher.getUsageLimit() != null && voucher.getUsageLimit() > 0 
            && voucher.getUsedCount() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Voucher đã hết số lượng sử dụng!");
        }
        
        return ResponseEntity.ok(voucher);
    }
    
    // Gửi email voucher
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PostMapping("/send-email")
    public ResponseEntity<?> sendVoucherEmail(@RequestBody VoucherEmailDTO request) {
        try {
            VoucherDTO voucher = voucherService.findByCode(request.getVoucherCode())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher!"));
            
            String discountValue = "";
            if ("percentage".equals(voucher.getType())) {
                discountValue = voucher.getValue() + "%";
            } else {
                discountValue = formatCurrency(voucher.getValue());
            }
            
            List<String> targetEmails = new ArrayList<>();
            
            if ("all".equals(request.getSendType())) {
                List<User> users = userRepository.findByRole_Name("ROLE_USER");
                targetEmails = users.stream()
                        .map(User::getEmail)
                        .filter(email -> email != null && !email.isEmpty())
                        .collect(Collectors.toList());
            } else if ("single".equals(request.getSendType()) && request.getEmails() != null) {
                targetEmails = request.getEmails();
            }
            
            if (targetEmails.isEmpty()) {
                return ResponseEntity.badRequest().body("Không có email nào để gửi!");
            }
            
            int successCount = 0;
            for (String email : targetEmails) {
                try {
                    emailService.sendVoucherEmail(email, voucher.getCode(), voucher.getName(), discountValue);
                    successCount++;
                } catch (Exception e) {
                    System.err.println("Gửi email thất bại cho: " + email);
                }
            }
            
            return ResponseEntity.ok("Đã gửi thành công " + successCount + "/" + targetEmails.size() + " email!");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Kiểm tra mã tồn tại
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/check/{code}")
    public ResponseEntity<Boolean> checkCode(@PathVariable String code) {
        return ResponseEntity.ok(voucherService.existsByCode(code));
    }
    
    // Tạo voucher mới
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody VoucherDTO voucherDTO) {
        try {
            VoucherDTO created = voucherService.create(voucherDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Cập nhật voucher
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/{code}")
    public ResponseEntity<?> update(@PathVariable String code, @RequestBody VoucherDTO voucherDTO) {
        try {
            VoucherDTO updated = voucherService.update(code, voucherDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // ========== THÊM: Xóa voucher - Chỉ ADMIN và STAFF mới được xóa ==========
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        try {
            voucherService.delete(code);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Helper format tiền
    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0đ";
        return new java.text.DecimalFormat("#,###").format(value) + "đ";
    }
}