package poly.edu.service;

import poly.edu.entity.Cart;
import java.math.BigDecimal;
import java.util.List;

public interface ShoppingCartService {
    
    // 1. Lấy danh sách giỏ hàng của User
    List<Cart> findAllByUsername(String username);

    // 2. Thêm sản phẩm vào giỏ (Nếu có rồi thì cộng dồn số lượng)
    Cart add(Integer productId, Double quantity, String username);
    
    // 3. Cập nhật số lượng (Dùng khi khách bấm +/- trong giỏ)
    Cart update(Integer cartId, Double quantity);
    
    // 4. Xóa một dòng trong giỏ
    void remove(Integer cartId);
    
    // 5. Xóa sạch giỏ hàng (Dùng sau khi Đặt hàng thành công hoặc nút Xóa hết)
    void clear(String username);
    
    // 6. Tính tổng tiền giỏ hàng (Backend tính để bảo mật giá)
    BigDecimal getTotalAmount(String username);
    
    // 7. Đếm tổng số lượng sản phẩm (Để hiển thị badge trên icon giỏ hàng)
    Double getTotalQuantity(String username);
}