package poly.edu.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.entity.Product;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    
    // ======================================================================
    // 1. CÁC HÀM CƠ BẢN (READ)
    // ======================================================================
    List<Product> findAll();
    Optional<Product> findById(Integer id);
    Page<Product> findAllPaged(int page, int size);

    // ======================================================================
    // 2. CRUD CÓ XỬ LÝ ẢNH (Dùng cho Controller)
    // ======================================================================
    Product create(Product product, MultipartFile imageFile) throws IOException;
    Product update(Product product, MultipartFile imageFile) throws IOException;

    // ======================================================================
    // 3. CRUD ĐƠN GIẢN (Tương thích ngược / Test)
    // ======================================================================
    Product create(Product product);
    Product update(Product product);
    void delete(Integer id);
    
    // ======================================================================
    // 4. LOGIC CLIENT (TRANG CHỦ)
    // ======================================================================
    List<Product> findDiscountProducts(int limit);
    List<Product> findBestSellers(int limit);
    List<Product> findNewProducts(int limit);
    
 
    // 5. TÌM KIẾM CHO CLIENT (Trang danh sách sản phẩm)
    // Hàm cũ (nếu còn dùng)
    List<Product> searchAndFilter(String keyword, String categoryName, Double minPrice, Double maxPrice);

    // Hàm mới (Hỗ trợ phân trang & categoryId)
    Page<Product> searchProductsClient(String keyword, Integer categoryId, 
                                       Double minPrice, Double maxPrice, 
                                       int page, int size);

    // 6.  ADMIN SEARCH (Tìm kiếm nâng cao)
    // Đã bổ sung tham số minDiscount và maxDiscount để khớp với ServiceImpl
    Page<Product> filterAdminProducts(
            String keyword, 
            Integer categoryId, 
            Double minPrice, Double maxPrice, 
            Double minQty, Double maxQty, 
            Integer minDiscount, Integer maxDiscount, // [QUAN TRỌNG] Thêm dòng này
            int page, int size
    );
}