package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Cart;
import poly.edu.entity.Product;
import poly.edu.entity.User;
import poly.edu.repository.CartRepository;
import poly.edu.repository.ProductRepository;
import poly.edu.repository.UserRepository;
import poly.edu.service.ShoppingCartService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Cart> findAllByUsername(String username) {
        return cartRepository.findByUser_Username(username);
    }

    @Override
    @Transactional
    public Cart add(Integer productId, Double quantity, String username) {
        // 1. Tìm sản phẩm trong DB
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        // 2. Tìm xem User đã có sản phẩm này trong giỏ chưa
        Optional<Cart> existingCart = cartRepository.findByUser_UsernameAndProduct_Id(username, productId);

        if (existingCart.isPresent()) {
            // --- TRƯỜNG HỢP CỘNG DỒN ---
            Cart cart = existingCart.get();
            Double currentQty = cart.getQuantity();
            Double newQty = currentQty + quantity;

            //  Check tồn kho
            BigDecimal newQtyBD = BigDecimal.valueOf(newQty);
            if (newQtyBD.compareTo(product.getQuantity()) > 0) {
                throw new RuntimeException("Số lượng trong kho chỉ còn lại " + product.getQuantity() + " kg. Vui lòng đặt ít hơn.");
            }

            cart.setQuantity(newQty);
            return cartRepository.save(cart);
        } else {
            // --- TRƯỜNG HỢP THÊM MỚI ---
            
            // Check tồn kho cho số lượng mới thêm
            BigDecimal reqQtyBD = BigDecimal.valueOf(quantity);
            if (reqQtyBD.compareTo(product.getQuantity()) > 0) {
                throw new RuntimeException("Số lượng trong kho chỉ còn lại " + product.getQuantity() + " kg.");
            }

            Cart newCart = new Cart();
            
            // Set User (Giả lập đối tượng User để Hibernate map khóa ngoại, không cần query lại DB)
            User user = new User();
            user.setUsername(username);
            newCart.setUser(user);

            newCart.setProduct(product);
            newCart.setQuantity(quantity);

            return cartRepository.save(newCart);
        }
    }

    @Override
    @Transactional
    public Cart update(Integer cartId, Double quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dòng giỏ hàng này!"));

        Product product = cart.getProduct();

        // [LOGIC GIỮ NGUYÊN] Check tồn kho khi cập nhật
        BigDecimal reqQtyBD = BigDecimal.valueOf(quantity);
        if (reqQtyBD.compareTo(product.getQuantity()) > 0) {
            throw new RuntimeException("Số lượng trong kho chỉ còn lại " + product.getQuantity() + " kg.");
        }

        if (quantity <= 0) {
            cartRepository.delete(cart);
            return null;
        } else {
            cart.setQuantity(quantity);
            return cartRepository.save(cart);
        }
    }

    @Override
    @Transactional
    public void remove(Integer cartId) {
        if (cartRepository.existsById(cartId)) {
            cartRepository.deleteById(cartId);
        }
    }

    @Override
    @Transactional
    public void clear(String username) {
        cartRepository.deleteAllByUser_Username(username);
    }

    @Override
    public BigDecimal getTotalAmount(String username) {
        List<Cart> carts = cartRepository.findByUser_Username(username);
        
        // Tính tổng tiền: Price * Quantity của từng dòng rồi cộng lại
        return carts.stream()
                .map(item -> {
                    BigDecimal price = item.getProduct().getPrice();
                    BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
                    return price.multiply(qty);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Double getTotalQuantity(String username) {
        List<Cart> carts = cartRepository.findByUser_Username(username);
        
        // Cộng tổng số lượng các món (để hiển thị badge giỏ hàng)
        return carts.stream()
                .mapToDouble(Cart::getQuantity)
                .sum();
    }
}