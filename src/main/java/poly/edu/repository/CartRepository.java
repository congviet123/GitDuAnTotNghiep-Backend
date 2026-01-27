package poly.edu.repository;
// sử lý các thao tác với giỏ hàng
import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.Cart;
import poly.edu.entity.User; // Hoặc Account

public interface CartRepository extends JpaRepository<Cart, Integer> {
	Cart findByAccount_Username(String username);
}