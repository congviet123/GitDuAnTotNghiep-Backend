package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import poly.edu.entity.Product;
import poly.edu.entity.dto.CartItemDTO;
import poly.edu.repository.ProductRepository;
import poly.edu.service.ShoppingCartService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ProductRepository productRepository;

    private final Map<Integer, CartItemDTO> cart = new HashMap<>();

    // --- Phương thức CRUD trong giỏ hàng ---

    @Override
    public void add(Integer productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + productId));

        CartItemDTO item = cart.get(productId);
        
        if (item == null) {
            item = new CartItemDTO();
            item.setProductId(productId);
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(quantity);
            item.setImage(product.getImage());
            
            cart.put(productId, item);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
        }
    }

    @Override
    public void remove(Integer productId) {
        cart.remove(productId);
    }

    @Override
    public void update(Integer productId, Integer quantity) {
        CartItemDTO item = cart.get(productId);
        if (item != null) {
            if (quantity == null || quantity <= 0) {
                remove(productId);
            } else {
                item.setQuantity(quantity);
            }
        }
    }

    @Override
    public void clear() {
        cart.clear();
    }

    /**
     * BỔ SUNG: Xóa một danh sách sản phẩm khỏi giỏ hàng (Sau khi đặt hàng thành công)
     * Phương thức này cần được thêm vào ShoppingCartService.java interface
     */
    public void removeItems(List<Integer> productIds) {
        productIds.forEach(cart::remove);
    }


    // --- Phương thức truy vấn ---

    @Override
    public List<CartItemDTO> getItems() {
        return new ArrayList<>(cart.values());
    }

    @Override
    public BigDecimal getAmount() {
        return cart.values().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int getCount() {
        return cart.size();
    }
    
    @Override
    public int getTotalQuantity() {
        return cart.values().stream().mapToInt(CartItemDTO::getQuantity).sum();
    }
}