package poly.edu.service;

import poly.edu.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ProductService {
    List<Product> findAll();
    Optional<Product> findById(Integer id);
    Product create(Product product);
    Product update(Product product);
    void delete(Integer id);
    
    // Paged variant to avoid returning very large lists
    org.springframework.data.domain.Page<Product> findAllPaged(int page, int size);
    
    // --- BỔ SUNG CHO TRANG CHỦ (INDEX) ---
    List<Product> findDiscountProducts(int limit);
    List<Product> findBestSellers(int limit);
    List<Product> findNewProducts(int limit);
    
    // --- BỔ SUNG CHO TRANG DANH SÁCH (PRODUCTS) ---
    List<Product> searchAndFilter(String keyword, String categoryName, Double minPrice, Double maxPrice);
}