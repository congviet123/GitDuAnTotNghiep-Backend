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
import poly.edu.repository.UserRepository;
import poly.edu.service.ImportService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional // Đảm bảo nếu bị lỗi giữa chừng (vd mất mạng), phiếu nhập và số lượng kho sẽ được Rollback lại trạng thái cũ, không bị sai lệch số liệu.
public class ImportServiceImpl implements ImportService {

    @Autowired
    ImportRepository importRepo;

    @Autowired
    ProductRepository productRepo;
<<<<<<< HEAD
=======

    @Autowired
    UserRepository userRepo;
>>>>>>> 26c4aa114effa03283d0371373d861a845cf6c72

    @Override
    public Import create(ImportDTO dto) {

        Import imp = new Import();

        // lấy ngày nhập
        LocalDate importDate = dto.getImportDate() != null
                ? dto.getImportDate()
                : LocalDate.now();

        imp.setImportDate(importDate.atStartOfDay());
        imp.setSupplierId(dto.getSupplierId());
        imp.setAccountUsername(dto.getAccountUsername());
        imp.setTotalAmount(dto.getTotalAmount());
        imp.setNotes(dto.getNotes());

        List<ImportDetail> details = dto.getDetails().stream().map(d -> {

<<<<<<< HEAD
            // 1. LẤY SẢN PHẨM TỪ DATABASE LÊN ĐỂ CHUẨN BỊ CẬP NHẬT
            Product product = productRepo.findById(d.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            // Nếu tồn kho cũ bị null thì gán tạm bằng 0
            BigDecimal currentQty = product.getQuantity() == null
                ? BigDecimal.ZERO
                : product.getQuantity();

            // Chuyển đổi an toàn sang BigDecimal (tránh lỗi nếu DTO gửi từ VueJS lên là Integer/Double)
            BigDecimal importQty = new BigDecimal(String.valueOf(d.getQuantity()));

            // =========================================================
            //  ĐỒNG BỘ SANG BẢNG SẢN PHẨM
            // =========================================================
            
            // a. Cộng dồn số lượng tồn kho (Tồn cũ + Nhập mới)
            product.setQuantity(currentQty.add(importQty));

            // b. Cập nhật Giá nhập (Vốn) mới nhất
            product.setImportPrice(d.getUnitPrice());

            // c. Cập nhật Ngày nhập kho (Sử dụng cột createDate như đã chốt)
            product.setCreateDate(new Date()); 

            // Lưu sản phẩm đã được cập nhật vào Database
            productRepo.save(product);

            // =========================================================
            // 2. TẠO CHI TIẾT PHIẾU NHẬP
            // =========================================================
=======
            Product product = productRepo.findById(d.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            BigDecimal currentQty = product.getQuantity() == null
                    ? BigDecimal.ZERO
                    : product.getQuantity();

            BigDecimal importQty = BigDecimal.valueOf(d.getQuantity());

            // cộng tồn kho
            product.setQuantity(currentQty.add(importQty));

            // cập nhật giá nhập
            product.setImportPrice(d.getUnitPrice());

            // cập nhật ngày nhập kho mới nhất
            if (product.getLastImportDate() == null ||
                    importDate.isAfter(product.getLastImportDate())) {

                product.setLastImportDate(importDate);
            }

            productRepo.save(product);

>>>>>>> 26c4aa114effa03283d0371373d861a845cf6c72
            ImportDetail detail = new ImportDetail();
            detail.setProductId(d.getProductId());
            detail.setQuantity(importQty); // Gán bằng BigDecimal chuẩn xác
            detail.setUnitPrice(d.getUnitPrice());
            detail.setImportEntity(imp);

            return detail;

        }).collect(Collectors.toList());

        imp.setDetails(details);

        return importRepo.save(imp);
    }

    @Override
    public List<Import> search(String keyword, LocalDate start, LocalDate end) {

        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }

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

    @Override
    public void delete(Integer id) {

        Import imp = importRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));

        // hoàn lại tồn kho
        for (ImportDetail detail : imp.getDetails()) {

            Product product = productRepo.findById(detail.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            BigDecimal currentQty = product.getQuantity() == null
                    ? BigDecimal.ZERO
                    : product.getQuantity();

            BigDecimal importQty = BigDecimal.valueOf(detail.getQuantity());

            product.setQuantity(currentQty.subtract(importQty));

            productRepo.save(product);
        }

        imp.getDetails().clear();

        importRepo.delete(imp);
    }
}