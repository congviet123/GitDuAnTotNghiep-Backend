package poly.edu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import poly.edu.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    // ✅ FIX LỖI SẢN PHẨM KHÔNG HIỂN THỊ: Sử dụng LEFT JOIN FETCH Category
    @Override
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c")
    List<Product> findAll();

    // Pageable variants with fetch join require a countQuery
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c",
           countQuery = "SELECT count(p) FROM Product p")
    Page<Product> findAllWithCategory(Pageable pageable);

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE p.available = true ORDER BY p.price DESC",
           countQuery = "SELECT count(p) FROM Product p WHERE p.available = true")
    Page<Product> findAvailableOrderByPriceDesc(Pageable pageable);

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE p.available = true ORDER BY p.createDate DESC",
           countQuery = "SELECT count(p) FROM Product p WHERE p.available = true")
    Page<Product> findAvailableOrderByCreateDateDesc(Pageable pageable);

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE p.available = true",
           countQuery = "SELECT count(p) FROM Product p WHERE p.available = true")
    Page<Product> findAvailable(Pageable pageable);
}