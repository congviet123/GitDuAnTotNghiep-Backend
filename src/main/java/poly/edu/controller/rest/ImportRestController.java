package poly.edu.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

    // 🔍 Tìm kiếm + load danh sách
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

    // ➕ Tạo phiếu nhập
    @PostMapping
    public Import create(@RequestBody ImportDTO dto) {
        return importService.create(dto);
    }

    // 👁 Chi tiết
    @GetMapping("/{id}")
    public Import detail(@PathVariable Integer id) {
        return importService.findById(id);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        importService.delete(id);
    }
}
