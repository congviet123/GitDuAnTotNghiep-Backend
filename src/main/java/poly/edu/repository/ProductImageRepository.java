package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    // Tự động
}