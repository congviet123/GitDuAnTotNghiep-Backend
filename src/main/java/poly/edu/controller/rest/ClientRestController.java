package poly.edu.controller.rest;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import poly.edu.entity.*;
import poly.edu.entity.dto.*;
import poly.edu.repository.CartRepository;
import poly.edu.service.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/rest")
public class ClientRestController {

    @Autowired private UserService userService;
    @Autowired private ShoppingCartService cartService; // Đã sửa Service mới
    @Autowired private OrderService orderService;
    @Autowired private ProductService productService;
    @Autowired private ReviewService reviewService;
    @Autowired private CategoryService categoryService;
    
    @Autowired private CartRepository cartRepository; 

    // --- HELPER: LẤY USERNAME TỪ SECURITY CONTEXT ---
    private String validateAndGetUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null; 
        }

        Object principal = authentication.getPrincipal();
        
        // --- TRƯỜNG HỢP: ĐĂNG NHẬP GOOGLE ---
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            String email = oauth2User.getAttribute("email");

            Optional<User> existingUserOpt = userService.findByEmail(email);
            
            if (existingUserOpt.isEmpty()) {
                throw new RuntimeException("Tài khoản Google (" + email + ") chưa được đăng ký trong hệ thống.");
            }

            User currentUser = existingUserOpt.get();

            if (!authentication.getName().equals(currentUser.getUsername())) {
                UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                    currentUser.getUsername(), 
                    null, 
                    authentication.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }
            
            // Logic cũ check cart header có thể bỏ qua vì giờ cart tự tạo khi add
            return currentUser.getUsername();
        }
        
        return authentication.getName();
    }

    // ============================================================
    // 1. TÀI KHOẢN (PROFILE & AUTH) - GIỮ NGUYÊN
    // ============================================================

    @GetMapping("/account/profile")
    public ResponseEntity<?> getProfile() {
        try {
            String username = validateAndGetUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Phiên đăng nhập không hợp lệ.");
            
            User user = userService.findById(username);
            return user != null ? ResponseEntity.ok(user) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/account/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserUpdateDTO userDto) {
        try {
            String username = validateAndGetUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            
            userService.updateProfile(username, userDto);
            return ResponseEntity.ok("Cập nhật thành công!");
        } catch (Exception e) { 
            return ResponseEntity.badRequest().body(e.getMessage()); 
        }
    }

    @GetMapping("/client/profile")
    public ResponseEntity<?> getClientProfileShort() {
        try {
            String username = validateAndGetUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");

            User user = userService.findById(username);
            if (user == null) return ResponseEntity.notFound().build();

            Map<String, String> profile = new HashMap<>();
            profile.put("username", user.getUsername());
            profile.put("fullname", user.getFullname());
            profile.put("phone", user.getPhone());
            profile.put("email", user.getEmail());

            String displayAddress = "";
            List<Address> addresses = user.getAddresses();
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.stream()
                        .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                        .findFirst()
                        .orElse(addresses.get(0));
                displayAddress = addr.getFullAddress(); 
            }
            profile.put("address", displayAddress);

            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/account/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        try {
            if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
                return ResponseEntity.badRequest().body("Mật khẩu xác nhận không khớp.");
            }
            userService.register(registrationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký thành công!");
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PutMapping("/account/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO passDto) {
        try {
            String username = validateAndGetUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            
            userService.changePassword(username, passDto);
            return ResponseEntity.ok("Đổi mật khẩu thành công!");
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        try {
            userService.forgotPassword(email);
            return ResponseEntity.ok("Mật khẩu mới đã được gửi về email.");
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // ============================================================
    // 2. SẢN PHẨM & DANH MỤC - GIỮ NGUYÊN
    // ============================================================
    
    @GetMapping("/client/categories")
    public List<Category> getAllCategories() { return categoryService.findAll(); }

    @GetMapping("/client/products")
    public ResponseEntity<?> searchProducts(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer categoryId, 
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "12") int size) 
    {
        try {
            Page<Product> result = productService.searchProductsClient(keyword, categoryId, minPrice, maxPrice, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/client/products/{id}")
    public ResponseEntity<Product> getProductDetail(@PathVariable("id") Integer id) {
        return productService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/products/discount")
    public ResponseEntity<List<Product>> getDiscountProducts() {
        return ResponseEntity.ok(productService.findDiscountProducts(8));
    }

    @GetMapping("/client/products/bestsellers")
    public ResponseEntity<List<Product>> getBestSellers() {
        return ResponseEntity.ok(productService.findBestSellers(8));
    }

    @GetMapping("/client/products/new")
    public ResponseEntity<List<Product>> getNewProducts() {
        return ResponseEntity.ok(productService.findNewProducts(8));
    }

    @GetMapping("/client/products/{productId}/reviews")
    public List<Review> getProductReviews(@PathVariable Integer productId) { 
        return reviewService.getReviewsByProductId(productId); 
    }

    @PostMapping("/reviews")
    public ResponseEntity<?> postReview(@Valid @RequestBody ReviewCreationDTO reviewDto) {
        try {
            String username = validateAndGetUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập.");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.saveReview(username, reviewDto)); 
        } catch (Exception e) { 
            return ResponseEntity.badRequest().body(e.getMessage()); 
        }
    }

    // ============================================================
    // 3. GIỎ HÀNG (CART) - [ĐÃ SỬA CHO KHỚP VỚI CẤU TRÚC MỚI]
    // ============================================================

    // Thêm vào giỏ: Thay vì nhận DTO, ta nhận trực tiếp productId và quantity
    // Nếu Frontend vẫn gửi JSON {productId: 1, quantity: 2}, ta có thể dùng Map hoặc Class DTO nhỏ
    @PostMapping("/cart/add")
    public ResponseEntity<?> addItemToCart(@RequestBody Map<String, Object> payload) {
        try {
            String username = validateAndGetUsername(); 
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập để mua hàng.");
            }
            
            Integer productId = (Integer) payload.get("productId");
            // Xử lý quantity an toàn (vì JSON số có thể là Integer hoặc Double)
            Double quantity = Double.valueOf(payload.get("quantity").toString());

            cartService.add(productId, quantity, username);
            return ResponseEntity.ok(cartService.getTotalQuantity(username));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Lấy giỏ hàng: Trả về List<Cart> (Entity mới)
    @GetMapping("/cart")
    public ResponseEntity<?> getCartItems() {
        try {
            String username = validateAndGetUsername(); 
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
            
            return ResponseEntity.ok(cartService.findAllByUsername(username));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Cập nhật số lượng
    @PutMapping("/cart/{id}")
    public ResponseEntity<?> updateCartItem(@PathVariable("id") Integer id, @RequestParam("quantity") Double qty) {
        try {
            String username = validateAndGetUsername();
            cartService.update(id, qty);
            return ResponseEntity.ok(cartService.findAllByUsername(username)); // Trả về list mới để cập nhật UI
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa sản phẩm
    @DeleteMapping("/cart/{id}")
    public ResponseEntity<?> removeCartItem(@PathVariable("id") Integer id) {
        try {
            String username = validateAndGetUsername();
            cartService.remove(id);
            return ResponseEntity.ok(cartService.findAllByUsername(username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ============================================================
    // 4. ĐƠN HÀNG (ORDER) - GIỮ NGUYÊN
    // ============================================================

    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderCreateDTO orderDTO) {
        try {
            String username = validateAndGetUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập.");
            
            Order savedOrder = orderService.placeOrder(username, orderDTO);
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrderHistory() {
        try {
            String username = validateAndGetUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            
            return ResponseEntity.ok(orderService.findOrdersByUsername(username));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable("id") Integer id) {
        try {
            String username = validateAndGetUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            Order order = orderService.findById(id).orElse(null);
            
            if (order == null) return ResponseEntity.notFound().build();
            
            if (!order.getAccount().getUsername().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền xem đơn hàng này");
            }
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            String username = validateAndGetUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            orderService.cancelOrder(username, id, body.get("reason"));
            return ResponseEntity.ok("Đã hủy đơn hàng thành công.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/orders/{id}/return")
    public ResponseEntity<?> returnOrder(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            String username = validateAndGetUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            orderService.requestReturn(username, id, body.get("reason"));
            return ResponseEntity.ok("Yêu cầu hoàn trả đã được gửi đến Admin.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/orders/{id}/return-request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> requestReturnWithImages(
            @PathVariable("id") Integer id,
            @RequestParam("senderName") String senderName,
            @RequestParam("senderPhone") String senderPhone,
            @RequestParam("senderEmail") String senderEmail,
            @RequestParam("reason") String reason,
            @RequestParam("bankName") String bankName,
            @RequestParam("accNo") String accNo,
            @RequestParam("accName") String accName,
            @RequestParam(value = "qrFile", required = false) MultipartFile qrFile, 
            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        try {
            String username = validateAndGetUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            orderService.requestReturnFull(username, id, senderName, senderPhone, senderEmail, 
                                           reason, bankName, accNo, accName, qrFile, files);
            
            return ResponseEntity.ok("Đã gửi yêu cầu hoàn trả thành công.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Integer id) {
        try {
            String username = validateAndGetUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            orderService.hideOrder(username, id);
            return ResponseEntity.ok("Đã xóa đơn khỏi lịch sử.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/users/admins")
    public ResponseEntity<List<Map<String, String>>> getAdminList() {
        try {
            List<User> admins = userService.getAdmins();
            List<Map<String, String>> result = admins.stream().map(u -> {
                Map<String, String> map = new HashMap<>();
                map.put("username", u.getUsername());
                map.put("fullname", u.getFullname());
                map.put("email", u.getEmail());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}