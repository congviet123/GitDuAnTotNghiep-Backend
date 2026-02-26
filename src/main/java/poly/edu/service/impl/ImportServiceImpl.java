package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Import;
import poly.edu.entity.ImportDetail;
import poly.edu.entity.Product;
import poly.edu.entity.dto.ImportDTO;
import poly.edu.repository.ImportRepository;
import poly.edu.repository.ProductRepository;
import poly.edu.service.ImportService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ImportServiceImpl implements ImportService {

    @Autowired
    ImportRepository importRepo;

    @Autowired
    ProductRepository productRepo;   // 🔥 THÊM CÁI NÀY

    @Override
    public Import create(ImportDTO dto) {

        Import imp = new Import();
        imp.setImportDate(LocalDateTime.now());
        imp.setSupplierId(dto.getSupplierId());
        imp.setAccountUsername(dto.getAccountUsername());
        imp.setTotalAmount(dto.getTotalAmount());
        imp.setNotes(dto.getNotes());

        List<ImportDetail> details = dto.getDetails().stream().map(d -> {

            // 🔥 LẤY PRODUCT
        	Product product = productRepo.findById(d.getProductId())
        	        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        	BigDecimal currentQty = product.getQuantity() == null
        	        ? BigDecimal.ZERO
        	        : product.getQuantity();

        	BigDecimal importQty = BigDecimal.valueOf(d.getQuantity());

        	product.setQuantity(currentQty.add(importQty));

        	product.setImportPrice(d.getUnitPrice());

        	productRepo.save(product);

            // 🔥 TẠO DETAIL
            ImportDetail detail = new ImportDetail();
            detail.setProductId(d.getProductId());
            detail.setQuantity(d.getQuantity());
            detail.setUnitPrice(d.getUnitPrice());
            detail.setImportEntity(imp);

            return detail;

        }).collect(Collectors.toList());

        imp.setDetails(details);

        return importRepo.save(imp);
    }

    @Override
    public List<Import> search(String keyword, LocalDate start, LocalDate end) {
        return importRepo.search(
            keyword,
            start != null ? start.atStartOfDay() : null,
            end != null ? end.atTime(23, 59, 59) : null
        );
    }

    @Override
    public Import findById(Integer id) {
        return importRepo.findById(id).orElseThrow();
    }
}