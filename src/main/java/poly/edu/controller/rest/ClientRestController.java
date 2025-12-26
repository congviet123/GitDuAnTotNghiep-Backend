package poly.edu.controller.rest;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Category;
import poly.edu.entity.Order;
import poly.edu.entity.Product;
import poly.edu.entity.Review;
import poly.edu.entity.User;
import poly.edu.entity.dto.*;
import poly.edu.service.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rest")
public class ClientRestController {

    @Autowired private UserService userService;
    @Autowired private ShoppingCartService cartService;
    @Autowired private OrderService orderService;
    @Autowired private ProductService productService; 
    @Autowired private ReviewService reviewService;
    @Autowired private CategoryService categoryService;

    // --- Helper Method: Lấy username/email của người đang đăng nhập ---
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // 1. Nếu là Login Google
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            return oauth2User.getAttribute("email"); 
        }

        // 2. Nếu là Login thường
        return authentication.getName();
    }
    
    
    // --- [SỬA QUAN TRỌNG] API Profile ---
    @GetMapping("/account/profile")
    public ResponseEntity<?> getProfile() {
        try {
            String usernameOrEmail = getCurrentUsername();
            if (usernameOrEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Phiên đăng nhập không hợp lệ.");
            }

            // Ưu tiên 1: Tìm theo username (Login thường)
            User user = userService.findById(usernameOrEmail);
            
            // Ưu tiên 2: Tìm theo email (Login Google)
            if (user == null) {
                Optional<User> userByEmail = userService.findByEmail(usernameOrEmail);
                if (userByEmail.isPresent()) {
                    user = userByEmail.get();
                }
            }

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found: " + usernameOrEmail);
            }

            // Trả về cả ROLE để Frontend (VueJS) check quyền Admin
            // DTO phải có trường 'role' (Xem lại UserUpdateDTO hoặc tạo DTO mới)
            // Nếu UserUpdateDTO chưa có role, bạn có thể trả về User entity trực tiếp (nhưng nhớ @JsonIgnore password)
            return ResponseEntity.ok(user); 

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        }
    }

	// --- API CHO KHÁCH HÀNG (CLIENT) ---
    // (Phần còn lại của file này logic đã đúng, bạn giữ nguyên code cũ)
    
    @GetMapping("/client/categories") 
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }
    
    @GetMapping("/client/products")
    public ResponseEntity<?> searchProducts(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String categoryName,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(value = "page", required = false) Integer page,
        @RequestParam(value = "size", required = false) Integer size,
        @RequestParam(value = "all", required = false) Boolean all) 
    {
        try {
            int p = (page != null) ? Math.max(0, page) : 0;
            int s = (size != null && size > 0) ? size : 20;

            boolean hasFilter = (keyword != null && !keyword.isEmpty()) ||
                    (categoryName != null && !categoryName.isEmpty()) ||
                    minPrice != null || maxPrice != null;

            if (Boolean.TRUE.equals(all)) {
                List<Product> results = productService.searchAndFilter(keyword, categoryName, minPrice, maxPrice);
                return ResponseEntity.ok(results);
            }

            if (hasFilter) {
                List<Product> filtered = productService.searchAndFilter(keyword, categoryName, minPrice, maxPrice);
                int start = Math.min((int)PageRequest.of(p, s).getOffset(), filtered.size());
                int end = Math.min((start + s), filtered.size());
                Page<Product> pageResult = new PageImpl<>(filtered.subList(start, end), PageRequest.of(p, s), filtered.size());
                return ResponseEntity.ok(pageResult);
            } else {
                Page<Product> pageResult = productService.findAllPaged(p, s);
                return ResponseEntity.ok(pageResult);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }
    
    @GetMapping("/client/products/{id}")
    public ResponseEntity<Product> getProductDetail(@PathVariable("id") Integer id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/products/discount")
    public List<Product> getDiscountProducts() { return productService.findDiscountProducts(8); }
    
    @GetMapping("/client/products/bestsellers")
    public List<Product> getBestSellers() { return productService.findBestSellers(8); }
    
    @GetMapping("/client/products/new")
    public List<Product> getNewProducts() { return productService.findNewProducts(8); }
    
    @GetMapping("/client/products/{productId}/reviews")
    public List<Review> getProductReviews(@PathVariable Integer productId) {
        return reviewService.getReviewsByProductId(productId);
    }
    
    // --- API CẦN ĐĂNG NHẬP (USER) ---

    @PostMapping("/reviews")
    public ResponseEntity<?> postReview(@Valid @RequestBody ReviewCreationDTO reviewDto) {
        String username = getCurrentUsername(); 
        if (username == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập.");
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.saveReview(username, reviewDto));
        } catch (Exception e) {
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/account/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserUpdateDTO userDto) {
        try {
            String username = getCurrentUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            
            userService.updateProfile(username, userDto);
            return ResponseEntity.ok("Cập nhật thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/account/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO passDto) {
        try {
            String username = getCurrentUsername();
            if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            userService.changePassword(username, passDto);
            return ResponseEntity.ok("Đổi mật khẩu thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        try {
            userService.forgotPassword(email);
            return ResponseEntity.ok("Mật khẩu mới đã được gửi về email. Vui lòng kiểm tra (cả hộp thư rác).");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- CART & ORDER ---
    @PostMapping("/cart/add")
    public ResponseEntity<Integer> addItemToCart(@RequestBody CartItemDTO itemDto) {
        cartService.add(itemDto.getProductId(), itemDto.getQuantity());
        return ResponseEntity.ok(cartService.getTotalQuantity());
    }

    @GetMapping("/cart")
    public ResponseEntity<List<CartItemDTO>> getCartItems() {
        return ResponseEntity.ok(cartService.getItems());
    }
    
    @PutMapping("/cart/{id}")
    public ResponseEntity<List<CartItemDTO>> updateCartItem(@PathVariable("id") Integer id, @RequestParam("quantity") Integer qty) {
        cartService.update(id, qty);
        return ResponseEntity.ok(cartService.getItems());
    }
    
    @DeleteMapping("/cart/{id}")
    public ResponseEntity<List<CartItemDTO>> removeCartItem(@PathVariable("id") Integer id) {
        cartService.remove(id);
        return ResponseEntity.ok(cartService.getItems());
    }
    
    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderCreateDTO orderDTO) {
        String username = getCurrentUsername();
        if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập.");
        try {
            orderService.placeOrder(username, orderDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("Đặt hàng thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrderHistory() {
        String username = getCurrentUsername();
        if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(orderService.findOrdersByUsername(username)); 
    }
    
    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrderDetail(@PathVariable("id") Integer id) {
        return orderService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}