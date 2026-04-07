package poly.edu.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // THÊM: Import cho phân quyền
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.entity.Category;
import poly.edu.entity.Order;
import poly.edu.entity.Product;
import poly.edu.entity.Role;
import poly.edu.entity.User;
import poly.edu.entity.dto.UserListDTO;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.RoleRepository;
import poly.edu.service.CategoryService;
import poly.edu.service.OrderService;
import poly.edu.service.PdfService;
import poly.edu.service.ProductService;
import poly.edu.service.UserService;
import poly.edu.service.MailService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/rest/admin")
public class AdminRestController {

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private OrderService orderService;
    @Autowired private UserService userService;
    @Autowired private MailService mailService;
    @Autowired private OrderRepository orderRepository; 
    @Autowired private PdfService pdfService; 
    @Autowired private RoleRepository roleRepository; // Inject RoleRepository để lấy danh sách Role

    // ======================================================================
    // 1. QUẢN LÝ SẢN PHẨM
    // ======================================================================
    // THÊM: Chỉ Admin và Staff mới được quản lý sản phẩm
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "minQty", required = false) Double minQty,
            @RequestParam(value = "maxQty", required = false) Double maxQty,
            @RequestParam(value = "minDiscount", required = false) Integer minDiscount, 
            @RequestParam(value = "maxDiscount", required = false) Integer maxDiscount,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        try {
            Page<Product> productPage = productService.filterAdminProducts(
                    keyword, categoryId, minPrice, maxPrice, minQty, maxQty, 
                    minDiscount, maxDiscount, page, size
            );
            return ResponseEntity.ok(productPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi tải danh sách: " + e.getMessage());
        }
    }

    // THÊM: Chỉ Admin và Staff mới được xem chi tiết sản phẩm
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") Integer id) {
        return productService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // THÊM: Chỉ Admin và Staff mới được tạo sản phẩm
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PostMapping(value = "/products", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> createProduct(@RequestPart("product") Product product, @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            calculateSalePrice(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(product, imageFile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi thêm: " + e.getMessage());
        }
    }

    // THÊM: Chỉ Admin và Staff mới được cập nhật sản phẩm
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping(value = "/products/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> updateProduct(@PathVariable("id") Integer id, @RequestPart("product") Product product, @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        if (!id.equals(product.getId())) return ResponseEntity.badRequest().body("ID không khớp.");
        try {
            calculateSalePrice(product);
            return ResponseEntity.ok(productService.update(product, imageFile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // THÊM: Chỉ Admin và Staff mới được xóa sản phẩm
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") Integer id) {
        try {
            productService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xóa: " + e.getMessage());
        }
    }

    private void calculateSalePrice(Product p) {
        if (p.getOriginalPrice() != null) {
            int discount = (p.getDiscount() != null && p.getDiscount() >= 0) ? p.getDiscount() : 0;
            p.setDiscount(discount);
            BigDecimal original = p.getOriginalPrice();
            BigDecimal discountAmount = original.multiply(BigDecimal.valueOf(discount)).divide(BigDecimal.valueOf(100));
            p.setPrice(original.subtract(discountAmount)); 
        }
    }

    // ======================================================================
    // 2. QUẢN LÝ DANH MỤC 
    // ======================================================================
    // THÊM: Chỉ Admin và Staff mới được quản lý danh mục
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        try { return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(category)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable("id") Integer id, @RequestBody Category category) {
        try { category.setId(id); return ResponseEntity.ok(categoryService.update(category)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Integer id) {
        try { categoryService.delete(id); return ResponseEntity.ok().body("Xóa thành công: " + id); }
        catch (Exception e) { return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage()); }
    }

    // ======================================================================
    // 3. QUẢN LÝ NGƯỜI DÙNG 
    // ======================================================================
<<<<<<< HEAD
    
    //  API lấy danh sách User có nhận tham số tìm kiếm 
=======
    // THÊM: Chỉ Admin và Staff mới được quản lý người dùng
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
>>>>>>> 885c037581597d70fbab6d55a30f25b605ee48af
    @GetMapping("/users")
    public ResponseEntity<List<UserListDTO>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean status) {
        return ResponseEntity.ok(userService.findAllForAdminList(keyword, role, status));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/users/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable("username") String username) {
        try {
            User user = userService.findById(username);
            if (user == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(user);
        } catch (UsernameNotFoundException e) { return ResponseEntity.notFound().build(); }
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.create(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/users/{username}")
    public ResponseEntity<?> updateUser(@PathVariable("username") String username, @RequestBody User user) {
        if (!username.equals(user.getUsername())) {
            return ResponseEntity.badRequest().body("Username không khớp");
        }
        try {
            User updatedUser = userService.update(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable("username") String username) {
        try {
            userService.delete(username);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ======================================================================
    // 4. QUẢN LÝ ĐƠN HÀNG
    // ======================================================================
    // THÊM: Cho phép Admin, Staff và Shipper đều xem được đơn hàng
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SHIPPER')")
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrdersForAdmin(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod, 
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Order> orders = orderService.filterOrdersForAdmin(status, paymentMethod, startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    // THÊM: Cho phép Admin, Staff và Shipper đều xem chi tiết đơn hàng
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SHIPPER')")
    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrderDetail(@PathVariable("id") Integer id) {
        return orderService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // THÊM: Cho phép Admin, Staff và Shipper đều cập nhật trạng thái đơn hàng
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SHIPPER')")
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable("id") Integer orderId, @RequestBody Map<String, String> body) {
        try {
            String newStatus = body.get("status");
            String message = body.get("message");
            boolean sendEmail = Boolean.parseBoolean(body.get("sendEmail"));

            Order order = orderService.findById(orderId).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderId));
            order.setStatus(newStatus);
            Order updatedOrder = orderService.save(order);

            if (sendEmail && order.getAccount() != null && order.getAccount().getEmail() != null) {
                mailService.sendOrderUpdateEmail(order.getAccount().getEmail(), "Cập nhật đơn hàng #" + order.getId(), message, updatedOrder);
            }
            return ResponseEntity.ok(updatedOrder); 
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // --- API CỘNG LẠI TỒN KHO THỦ CÔNG CHO ĐƠN HOÀN TRẢ ---
    // THÊM: Chỉ Admin và Staff mới được cộng lại kho
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/orders/{id}/restock")
    public ResponseEntity<?> restockReturnedOrder(@PathVariable("id") Integer id) {
        try {
            orderService.restockReturnedOrder(id);
            return ResponseEntity.ok("Đã cộng số lượng sản phẩm vào kho thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // THÊM: Chỉ Admin và Staff mới được xóa đơn hàng
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable("id") Integer id) {
        try {
            Order order = orderService.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn"));
            if (List.of("COMPLETED", "HIDDEN", "CANCELLED", "CANCELLED_REFUNDED").contains(order.getStatus())) {
                orderService.delete(id); return ResponseEntity.ok().build();
            }
            return ResponseEntity.badRequest().body("Không thể xóa đơn đang xử lý.");
        } catch (Exception e) { return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage()); }
    }
    
    // --- API XUẤT HÓA ĐƠN PDF ---
    // THÊM: Cho phép Admin, Staff và Shipper đều xuất PDF đơn hàng
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SHIPPER')")
    @GetMapping("/orders/{id}/export-pdf")
    public ResponseEntity<byte[]> exportOrderPdf(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "A4") String paperSize 
    ) {
        Order order = orderService.findById(id).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();

        byte[] pdfBytes = pdfService.generateInvoice(order, paperSize);
        orderRepository.markAsPrinted(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=HoaDon_DH" + id + "_" + paperSize + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
    
    // --- API IN HÀNG LOẠT (BULK EXPORT) ---
    // THÊM: Chỉ Admin và Staff mới được in hàng loạt
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/orders/export-pdf/bulk")
    public ResponseEntity<byte[]> exportBulkOrders(
            @RequestParam List<Integer> ids, 
            @RequestParam(defaultValue = "A4") String paperSize
    ) {
        List<Order> orders = orderRepository.findAllById(ids);
        if (orders.isEmpty()) return ResponseEntity.notFound().build();

        byte[] pdfBytes = pdfService.generateBulkInvoices(orders, paperSize);

        for (Order order : orders) {
            if (!Boolean.TRUE.equals(order.getIsPrinted())) {
                orderRepository.markAsPrinted(order.getId());
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=HoaDon_TongHop_" + System.currentTimeMillis() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
    

    // ======================================================================
    // 5. BÁO CÁO THỐNG KÊ (GIỮ NGUYÊN)
    // ======================================================================
    // THÊM: Chỉ Admin và Staff mới xem được báo cáo thống kê
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/reports/revenue/{year}")
    public ResponseEntity<?> getRevenueReport(@PathVariable("year") Integer year) {
        try { return ResponseEntity.ok(orderService.getMonthlyRevenue(year)); }
        catch (Exception e) { return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); }
    }
}