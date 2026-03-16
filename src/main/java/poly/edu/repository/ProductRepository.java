package poly.edu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Product;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // =================================================================================
    // 1. CÁC HÀM CƠ BẢN DÀNH CHO ADMIN (Lấy tất cả, kể cả sản phẩm chưa set giá)
    // =================================================================================
    @Override
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c")
    List<Product> findAll();

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c",
           countQuery = "SELECT count(p) FROM Product p")
    Page<Product> findAllWithCategory(Pageable pageable);

    // =================================================================================
    // 2. CÁC HÀM DÀNH CHO KHÁCH HÀNG (Chỉ lấy sản phẩm có giá > 0 và đang có sẵn)
    // =================================================================================
    
    // LẤY SẢN PHẨM BÁN CHẠY THỰC TẾ (Dựa trên tổng số lượng bán từ đơn COMPLETED)
    @Query("SELECT od.product FROM OrderDetail od " +
           "JOIN od.order o " +
           "WHERE o.status = 'COMPLETED' " +
           "AND od.product.available = true " +
           "AND od.product.price > 0 " +
           "GROUP BY od.product " +
           "ORDER BY SUM(od.quantity) DESC")
    Page<Product> findTopSellingProducts(Pageable pageable);

    // Lấy sp bán chạy (Sắp xếp theo giá giảm dần - demo logic cũ, giữ lại để không lỗi code cũ)
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE p.available = true AND p.price > 0 ORDER BY p.price DESC",
           countQuery = "SELECT count(p) FROM Product p WHERE p.available = true AND p.price > 0")
    Page<Product> findAvailableOrderByPriceDesc(Pageable pageable);

    // Lấy sp mới nhất
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE p.available = true AND p.price > 0 ORDER BY p.createDate DESC",
           countQuery = "SELECT count(p) FROM Product p WHERE p.available = true AND p.price > 0")
    Page<Product> findAvailableOrderByCreateDateDesc(Pageable pageable);

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE p.available = true AND p.price > 0",
           countQuery = "SELECT count(p) FROM Product p WHERE p.available = true AND p.price > 0")
    Page<Product> findAvailable(Pageable pageable);
    
    // LẤY SẢN PHẨM GIẢM GIÁ (Cho mục "Giảm giá sốc" ở trang chủ)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c " +
           "WHERE p.available = true AND p.price > 0 AND p.discount > 0 " +
           "ORDER BY p.discount DESC")
    List<Product> findDiscountProducts(Pageable pageable);

    // HÀM TÌM KIẾM CHO CLIENT (Ưu tiên hiển thị giảm giá)
    @Query("SELECT p FROM Product p WHERE " +
           "p.available = true AND p.price > 0 " + 
           "AND (:keyword IS NULL OR p.name LIKE %:keyword%) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "ORDER BY p.discount DESC, p.createDate DESC") 
    Page<Product> searchForClient(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
    
    // =================================================================================
    // 3. HÀM TÌM KIẾM NÂNG CAO CHO ADMIN (Tìm tất cả)
    // =================================================================================
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR p.name LIKE %:keyword%) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:minQty IS NULL OR p.quantity >= :minQty) " +
           "AND (:maxQty IS NULL OR p.quantity <= :maxQty) " +
           "AND (:minDiscount IS NULL OR p.discount >= :minDiscount) " +
           "AND (:maxDiscount IS NULL OR p.discount <= :maxDiscount)")
    Page<Product> searchForAdmin(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minQty") BigDecimal minQty,
            @Param("maxQty") BigDecimal maxQty,
            @Param("minDiscount") Integer minDiscount, 
            @Param("maxDiscount") Integer maxDiscount, 
            Pageable pageable
    );
}