package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Cart;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    List<Cart> findByAccountUsername(String username);

    Optional<Cart> findByAccountUsernameAndProductId(String username, Integer productId);

    void deleteAllByAccountUsername(String username);
}