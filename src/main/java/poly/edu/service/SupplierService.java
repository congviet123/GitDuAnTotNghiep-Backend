package poly.edu.service;

import java.util.List;
import poly.edu.entity.Supplier;

public interface SupplierService {
    List<Supplier> findAll();
    Supplier save(Supplier supplier);
    void deleteById(Integer id);
    List<Supplier> search(String keyword);
}
