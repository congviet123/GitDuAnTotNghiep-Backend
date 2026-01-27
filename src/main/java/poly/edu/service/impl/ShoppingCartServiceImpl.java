package poly.edu.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Cart;
import poly.edu.entity.CartItem;
import poly.edu.entity.Product;
import poly.edu.entity.User; // Hoặc Account tùy entity bạn đặt
import poly.edu.entity.dto.CartItemDTO;
import poly.edu.repository.CartItemRepository;
import poly.edu.repository.CartRepository;
import poly.edu.repository.ProductRepository;
import poly.edu.repository.UserRepository; // Hoặc AccountRepository
import poly.edu.service.ShoppingCartService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
// Bỏ @SessionScope vì chúng ta lưu thẳng xuống DB
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private UserRepository userRepository; // Dùng để tìm người dùng hiện tại
    @Autowired
    private HttpServletRequest request; // Dùng để lấy username từ session

    // Helper: Lấy người dùng đang đăng nhập
    private User getCurrentUser() {
        String username = request.getRemoteUser(); // Lấy từ Spring Security
        if (username == null) return null; // Chưa đăng nhập
        return userRepository.findById(username).orElse(null);
    }

 // Helper: Lấy giỏ hàng của người dùng hiện tại
    private Cart getCurrentCart() {
        User user = getCurrentUser(); 
        if (user == null) return null;
        
        Cart cart = cartRepository.findByAccount_Username(user.getUsername());
        
        if (cart == null) {
            // Nếu chưa có giỏ thì tạo mới
            cart = new Cart();
            cart.setAccount(user);
            
            cart = cartRepository.save(cart);
        }
        return cart;
    }

    
    
    @Override
    @Transactional
    public void add(Integer productId, Double quantity) {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("Vui lòng đăng nhập để mua hàng!");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        Cart cart = getCurrentCart();
        CartItem item = cartItemRepository.findByCartAndProduct(cart, product);

        // 1. Tính toán số lượng mới
        BigDecimal currentQty = (item != null) ? item.getQuantity() : BigDecimal.ZERO;
        BigDecimal addQty = BigDecimal.valueOf(quantity);
        BigDecimal newQty = currentQty.add(addQty);

        // 2. [CHECK TỒN KHO]
        if (newQty.compareTo(product.getQuantity()) > 0) {
            throw new RuntimeException("Số lượng trong kho chỉ còn lại " + product.getQuantity() + " kg. Xin lỗi vì sự bất tiện này, vui lòng đặt với số lượng thấp hơn.");
        }

        // 3. Lưu vào DB
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(addQty);
        } else {
            item.setQuantity(newQty);
        }
        cartItemRepository.save(item);
    }

    @Override
    @Transactional
    public void remove(Integer productId) {
        Cart cart = getCurrentCart();
        if (cart != null) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                CartItem item = cartItemRepository.findByCartAndProduct(cart, product);
                if (item != null) {
                    cartItemRepository.delete(item);
                }
            }
        }
    }

    @Override
    @Transactional
    public void update(Integer productId, Double quantity) {
        Cart cart = getCurrentCart();
        if (cart == null) return;

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // 1. [CHECK TỒN KHO]
        BigDecimal requestQty = BigDecimal.valueOf(quantity);
        if (requestQty.compareTo(product.getQuantity()) > 0) {
            throw new RuntimeException("Số lượng trong kho chỉ còn lại " + product.getQuantity() + " kg. Xin lỗi vì sự bất tiện này, vui lòng đặt với số lượng thấp hơn.");
        }

        // 2. Cập nhật DB
        CartItem item = cartItemRepository.findByCartAndProduct(cart, product);
        if (item != null) {
            if (quantity <= 0) {
                cartItemRepository.delete(item);
            } else {
                item.setQuantity(requestQty);
                cartItemRepository.save(item);
            }
        }
    }

    @Override
    @Transactional
    public void clear() {
        Cart cart = getCurrentCart();
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
        }
    }

    @Override
    @Transactional
    public void removeItems(List<Integer> productIds) {
        // Hàm này xóa nhiều sp, có thể gọi hàm remove nhiều lần
        if (productIds != null) {
            productIds.forEach(this::remove);
        }
    }

    @Override
    public List<CartItemDTO> getItems() {
        Cart cart = getCurrentCart();
        if (cart == null) return new ArrayList<>();

        // Lấy list từ DB và chuyển sang DTO để hiển thị
        List<CartItem> items = cartItemRepository.findByCart(cart);
        
        return items.stream().map(item -> {
            CartItemDTO dto = new CartItemDTO();
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
            dto.setPrice(item.getProduct().getPrice());
            dto.setImage(item.getProduct().getImage());
            dto.setQuantity(item.getQuantity().doubleValue()); // Chuyển BigDecimal sang Double cho DTO
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public BigDecimal getAmount() {
        List<CartItemDTO> items = getItems();
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int getCount() {
        List<CartItemDTO> items = getItems();
        return items.size();
    }

    @Override
    public double getTotalQuantity() {
        List<CartItemDTO> items = getItems();
        return items.stream().mapToDouble(CartItemDTO::getQuantity).sum();
    }
}