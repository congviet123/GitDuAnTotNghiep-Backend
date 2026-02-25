package poly.edu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
	List<Supplier> findByNameContainingIgnoreCase(String keyword);

}

