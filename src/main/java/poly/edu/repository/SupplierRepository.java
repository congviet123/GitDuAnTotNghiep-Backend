package poly.edu.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    // 🔥 Tìm theo tên nhà cung cấp HOẶC tên người liên hệ
    List<Supplier> findByNameContainingIgnoreCaseOrContactNameContainingIgnoreCase(
            String nameKeyword,
            String contactKeyword
    );

}