package poly.edu.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import poly.edu.entity.Supplier;
import poly.edu.repository.SupplierRepository;
import poly.edu.service.SupplierService;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    @Override
    public Supplier save(Supplier supplier) {
        return supplierRepository.save(supplier);
    }
    @Override
    public void deleteById(Integer id) {
        supplierRepository.deleteById(id);
    }
    
    @Override
    public List<Supplier> search(String keyword) {
        return supplierRepository.findByNameContainingIgnoreCase(keyword);
    }

}
