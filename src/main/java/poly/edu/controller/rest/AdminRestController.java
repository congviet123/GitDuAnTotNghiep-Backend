package poly.edu.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Category;
import poly.edu.entity.Order;
import poly.edu.entity.Product;
import poly.edu.entity.User;
import poly.edu.entity.dto.OrderListDTO;
import poly.edu.entity.dto.ReportRevenueDTO; // DTO cho báo cáo doanh thu
import poly.edu.entity.dto.StatusUpdateDTO;
import poly.edu.entity.dto.UserListDTO;
import poly.edu.service.CategoryService;
import poly.edu.service.OrderService;
import poly.edu.service.ProductService;
import poly.edu.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/rest/admin")
public class AdminRestController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService; 
    @Autowired
    private OrderService orderService; 
    @Autowired
    private UserService userService;

    // ----------------------------------------------------------------------
    // --- API QUẢN LÝ SẢN PHẨM & DANH MỤC ---
    // ----------------------------------------------------------------------

    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts(@RequestParam(value = "page", required = false) Integer page,
                                            @RequestParam(value = "size", required = false) Integer size) {
        try {
            if (page != null) {
                int p = page != null ? page : 0;
                int s = (size != null && size > 0) ? size : 50; // default size 50
                Page<Product> productPage = productService.findAllPaged(p, s);
                return ResponseEntity.ok(productPage);
            }
            // backward-compatible: return full list if no paging params provided
            return ResponseEntity.ok(productService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") Integer id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            Product createdProduct = productService.create(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") Integer id, @RequestBody Product product) {
        if (!id.equals(product.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); 
        }
        try {
            Product updatedProduct = productService.update(product);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); 
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Integer id) {
        try {
            productService.delete(id);
            return ResponseEntity.noContent().build(); 
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }
    
    // ----------------------------------------------------------------------
    // --- API QUẢN LÝ NGƯỜI DÙNG ---
    // ----------------------------------------------------------------------
    
    @GetMapping("/users")
    public List<UserListDTO> getAllUsers() {
        return userService.findAllForAdminList(); 
    }
    
    @GetMapping("/users/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable("username") String username) {
        try {
            User user = userService.findById(username);
            // Kích hoạt nạp Role (tránh lỗi Lazy Loading khi JSON parse)
            if (user.getRole() != null) user.getRole().getName(); 
            return ResponseEntity.ok(user);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.create(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    @PutMapping("/users/{username}")
    public ResponseEntity<User> updateUser(@PathVariable("username") String username, @RequestBody User user) {
        if (!username.equals(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            User updatedUser = userService.update(user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/users/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable("username") String username) {
        try {
            userService.delete(username);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ----------------------------------------------------------------------
    // --- API QUẢN LÝ ĐƠN HÀNG ---
    // ----------------------------------------------------------------------

    @GetMapping("/orders")
    public List<OrderListDTO> getAllOrdersForAdmin() { 
        return orderService.findAllOrders(); 
    }
    
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable("id") Integer orderId, 
                                               @RequestBody StatusUpdateDTO statusDto) {
        try {
            Order updatedOrder = orderService.updateStatus(orderId, statusDto.getStatus());
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    // ----------------------------------------------------------------------
    // --- API BÁO CÁO (DASHBOARD) - BỔ SUNG ---
    // ----------------------------------------------------------------------

    /**
     * API: GET /rest/admin/reports/revenue/{year}
     * Trả về mảng doanh thu 12 tháng của năm được chọn
     */
    @GetMapping("/reports/revenue/{year}")
    public ResponseEntity<List<Double>> getRevenueReport(@PathVariable("year") Integer year) {
        try {
            // Giả định OrderService có phương thức getMonthlyRevenue(year)
            // Nếu chưa có, bạn cần thêm vào OrderService
            List<Double> revenueData = orderService.getMonthlyRevenue(year);
            return ResponseEntity.ok(revenueData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}