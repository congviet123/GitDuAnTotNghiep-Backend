package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Cart;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    // 1. Lấy danh sách tất cả sản phẩm trong giỏ của một User
    // (Vì bảng Cart gộp nên 1 user sẽ có nhiều dòng -> Dùng List)
    List<Cart> findByUser_Username(String username);

    // 2. Tìm một sản phẩm cụ thể trong giỏ hàng của User
    // (Dùng để kiểm tra xem sản phẩm đã có chưa -> Nếu có thì cộng dồn số lượng)
    Optional<Cart> findByUser_UsernameAndProduct_Id(String username, Integer productId);

    // 3. Xóa sạch giỏ hàng của User (Dùng khi đặt hàng thành công hoặc nhấn nút "Xóa hết")
    void deleteAllByUser_Username(String username);
}