package poly.edu.repository;
// sử lý các thao tác với giỏ hàng
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Cart;
import poly.edu.entity.CartItem;
import poly.edu.entity.Product;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCart(Cart cart);
    CartItem findByCartAndProduct(Cart cart, Product product);
    
    @Transactional
    void deleteByCartId(Integer cartId);
}