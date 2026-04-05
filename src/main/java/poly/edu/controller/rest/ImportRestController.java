package poly.edu.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Import;
import poly.edu.entity.dto.ImportDTO;
import poly.edu.service.ImportService;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/rest/api/imports")
@CrossOrigin("*")
public class ImportRestController {

    @Autowired
    ImportService importService;

    // ========== THÊM: Chỉ ADMIN và STAFF mới tìm kiếm được phiếu nhập ==========
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping
    public List<Import> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return importService.search(keyword, startDate, endDate);
    }

    // ========== THÊM: Chỉ ADMIN và STAFF mới tạo được phiếu nhập ==========
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PostMapping
    public Import create(@RequestBody ImportDTO dto) {
        return importService.create(dto);
    }

    // ========== THÊM: Chỉ ADMIN và STAFF mới xem chi tiết phiếu nhập ==========
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/{id}")
    public Import detail(@PathVariable Integer id) {
        return importService.findById(id);
    }
    
    // ========== THÊM: Chỉ ADMIN và STAFF mới xóa được phiếu nhập ==========
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        importService.delete(id);
    }
}