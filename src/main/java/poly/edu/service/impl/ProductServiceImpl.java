package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Product;
import poly.edu.repository.ProductRepository;
import poly.edu.service.ProductService;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.Comparator; 

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    // --- CRUD CƠ BẢN ---
    // Các phương thức này không cần @Transactional nếu không truy cập Lazy-Loaded fields sau khi Session đóng

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findById(Integer id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional
    public Product create(Product product) {
        product.setCreateDate(new Date());
        if (product.getAvailable() == null) {
            product.setAvailable(true);
        }
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product update(Product product) {
        if (product.getId() == null || !productRepository.existsById(product.getId())) {
            throw new RuntimeException("Sản phẩm không tồn tại.");
        }
        return productRepository.save(product);
    }

    @Override
    public void delete(Integer id) {
        productRepository.deleteById(id);
    }

    // --- PAGED ---
    @Override
    public Page<Product> findAllPaged(int page, int size) {
        int p = Math.max(0, page);
        int s = Math.max(1, size);
        Pageable pageable = PageRequest.of(p, s);
        return productRepository.findAllWithCategory(pageable);
    }

    // --- LOGIC TRANG CHỦ (INDEX) ---
    // BỔ SUNG @Transactional để tránh LazyInitializationException khi truy cập Category hoặc các quan hệ khác

    @Override
    @Transactional // <--- ĐÃ BỔ SUNG
    public List<Product> findBestSellers(int limit) {
        if (limit <= 0) return List.of();
        Page<Product> page = productRepository.findAvailableOrderByPriceDesc(PageRequest.of(0, limit));
        return page.getContent();
    }

    @Override
    @Transactional // <--- ĐÃ BỔ SUNG
    public List<Product> findNewProducts(int limit) {
        if (limit <= 0) return List.of();
        Page<Product> page = productRepository.findAvailableOrderByCreateDateDesc(PageRequest.of(0, limit));
        return page.getContent();
    }
    
    @Override
    public List<Product> findDiscountProducts(int limit) {
        if (limit <= 0) return List.of();
        // Fetch a few more items to increase chance of finding 'odd id' products
        int fetchSize = Math.max(limit * 3, 50);
        Page<Product> page = productRepository.findAvailable(PageRequest.of(0, fetchSize));
        return page.getContent().stream()
                .filter(p -> p.getId() != null && p.getId() % 2 != 0)
                .limit(limit)
                .collect(Collectors.toList());
    }

    // --- LOGIC TÌM KIẾM & LỌC (SEARCH & FILTER) ---
    // BẮT BUỘC có @Transactional vì truy cập p.getCategory().getName() (Lazy field)
    
    @Override
    @Transactional // <--- ĐÃ BỔ SUNG (BẮT BUỘC)
    public List<Product> searchAndFilter(String keyword, String categoryName, Double minPrice, Double maxPrice) {
        // Use a limited fetch to avoid loading absolutely everything into memory
        int maxFetch = 1000;
        Page<Product> page = productRepository.findAllWithCategory(PageRequest.of(0, maxFetch));
        List<Product> products = page.getContent();

        // Logic lọc bằng Java Stream
        return products.stream()
            // 1. Lọc theo Từ khóa
            .filter(p -> keyword == null || keyword.isEmpty() || p.getName().toLowerCase().contains(keyword.toLowerCase()))
            
            // 2. Lọc theo Danh mục (Đảm bảo Category và Category Name không null)
            // Việc truy cập p.getCategory().getName() đã an toàn nhờ @Transactional
            .filter(p -> categoryName == null || categoryName.isEmpty() ||
                          (p.getCategory() != null && p.getCategory().getName() != null &&
                           p.getCategory().getName().equalsIgnoreCase(categoryName)))
            
            // 3. Lọc theo Khoảng Giá Min 
            .filter(p -> (minPrice == null || p.getPrice() == null) || p.getPrice().compareTo(BigDecimal.valueOf(minPrice)) >= 0)
            
            // 4. Lọc theo Khoảng Giá Max 
            .filter(p -> (maxPrice == null || p.getPrice() == null) || p.getPrice().compareTo(BigDecimal.valueOf(maxPrice)) <= 0)
            
            .collect(Collectors.toList());
    }
}