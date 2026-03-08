package poly.edu.controller.rest;

import java.util.List;

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

    @GetMapping
    public List<Supplier> getAll() {
        return supplierService.findAll();
    }

    @PostMapping
    public Supplier create(@RequestBody Supplier supplier) {
        return supplierService.save(supplier);
    }
    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        supplierService.deleteById(id);
    }

    @PutMapping("/{id}")
    public Supplier update(
            @PathVariable Integer id,
            @RequestBody Supplier supplier) {

        supplier.setId(id);
        return supplierService.save(supplier);
    }
    
   
    @GetMapping("/search")
    public List<Supplier> search(@RequestParam(required = false) String keyword) {
        return supplierService.search(keyword);
    }



}
