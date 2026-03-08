package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Cart;
import poly.edu.entity.Product;
import poly.edu.repository.CartRepository;
import poly.edu.repository.ProductRepository;
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

    @Override
    public List<Cart> findAllByUsername(String username) {
        return cartRepository.findByAccountUsername(username);
    }

    @Override
    @Transactional
    public Cart add(Integer productId, Double quantity, String username) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        Optional<Cart> existingCart =
                cartRepository.findByAccountUsernameAndProductId(username, productId);

        if (existingCart.isPresent()) {

            Cart cart = existingCart.get();
            Double newQty = cart.getQuantity() + quantity;

            BigDecimal newQtyBD = BigDecimal.valueOf(newQty);
            if (newQtyBD.compareTo(product.getQuantity()) > 0) {
                throw new RuntimeException("Số lượng trong kho chỉ còn lại "
                        + product.getQuantity());
            }

            cart.setQuantity(newQty);
            return cartRepository.save(cart);

        } else {

            BigDecimal reqQtyBD = BigDecimal.valueOf(quantity);
            if (reqQtyBD.compareTo(product.getQuantity()) > 0) {
                throw new RuntimeException("Số lượng trong kho chỉ còn lại "
                        + product.getQuantity());
            }

            Cart newCart = new Cart();
            newCart.setAccountUsername(username);
            newCart.setProductId(productId);
            newCart.setQuantity(quantity);

            return cartRepository.save(newCart);
        }
    }

    @Override
    @Transactional
    public Cart update(Integer cartId, Double quantity) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dòng giỏ hàng!"));

        Product product = productRepository.findById(cart.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        BigDecimal reqQtyBD = BigDecimal.valueOf(quantity);
        if (reqQtyBD.compareTo(product.getQuantity()) > 0) {
            throw new RuntimeException("Số lượng trong kho chỉ còn lại "
                    + product.getQuantity());
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
        cartRepository.deleteById(cartId);
    }

    @Override
    @Transactional
    public void clear(String username) {
        cartRepository.deleteAllByAccountUsername(username);
    }

    @Override
    public BigDecimal getTotalAmount(String username) {

        List<Cart> carts = cartRepository.findByAccountUsername(username);

        return carts.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow();
                    BigDecimal price = product.getPrice();
                    BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
                    return price.multiply(qty);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Double getTotalQuantity(String username) {

        List<Cart> carts = cartRepository.findByAccountUsername(username);

        return carts.stream()
                .mapToDouble(Cart::getQuantity)
                .sum();
    }
}