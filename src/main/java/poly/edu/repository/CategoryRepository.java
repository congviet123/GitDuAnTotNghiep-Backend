package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // Kiểm tra xem tên danh mục đã tồn tại chưa (để validate khi thêm/sửa)
    boolean existsByName(String name);
}