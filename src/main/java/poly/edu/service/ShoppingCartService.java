package poly.edu.service;

import poly.edu.entity.dto.CartItemDTO;
import java.math.BigDecimal;
import java.util.List;

public interface ShoppingCartService {
   
    void add(Integer productId, Double quantity);
    
    void remove(Integer productId);
    
 
    void update(Integer productId, Double quantity);
    
    void clear();
    
    // Xóa nhiều item (dùng sau khi thanh toán)
    void removeItems(List<Integer> productIds); 
    
    List<CartItemDTO> getItems();
    BigDecimal getAmount();
    
    int getCount(); 

    
    double getTotalQuantity();
}