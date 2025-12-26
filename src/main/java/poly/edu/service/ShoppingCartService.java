package poly.edu.service;

import poly.edu.entity.dto.CartItemDTO;
import java.math.BigDecimal;
import java.util.List;

public interface ShoppingCartService {
    void add(Integer productId, Integer quantity);
    void remove(Integer productId);
    void update(Integer productId, Integer quantity);
    void clear();
 // BỔ SUNG: Khai báo phương thức xóa nhiều item
    void removeItems(List<Integer> productIds); 
    List<CartItemDTO> getItems();
    BigDecimal getAmount();
    
    // Tổng số item khác nhau (ví dụ: 2 loại)
    int getCount(); 

    // Tổng số lượng sản phẩm (ví dụ: 5 chiếc)
    int getTotalQuantity();
}