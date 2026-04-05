package poly.edu.controller.rest;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Supplier;
import poly.edu.service.SupplierService;


@RestController
@RequestMapping("/rest/api/suppliers")
@CrossOrigin("*")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    // ========== THÊM: Chỉ ADMIN và STAFF mới xem được danh sách NCC ==========
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping
    public List<Supplier> getAll() {
        return supplierService.findAll();
    }

    // ========== THÊM: Chỉ ADMIN và STAFF mới tạo được NCC ==========
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PostMapping
    public Supplier create(@RequestBody Supplier supplier) {
        return supplierService.save(supplier);
    }
    
    // ========== THÊM: Chỉ ADMIN và STAFF mới xóa được NCC ==========
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        supplierService.deleteById(id);
    }

    // ========== THÊM: Chỉ ADMIN và STAFF mới cập nhật được NCC ==========
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/{id}")
    public Supplier update(
            @PathVariable Integer id,
            @RequestBody Supplier supplier) {

        supplier.setId(id);
        return supplierService.save(supplier);
    }
    
    // ========== THÊM: Chỉ ADMIN và STAFF mới tìm kiếm được NCC ==========
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/search")
    public List<Supplier> search(@RequestParam(required = false) String keyword) {
        return supplierService.search(keyword);
    }
}