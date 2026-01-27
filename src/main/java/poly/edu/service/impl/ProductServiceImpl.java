package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.entity.Product;
import poly.edu.repository.ProductRepository;
import poly.edu.service.ProductService;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Đường dẫn lưu ảnh: target/classes/static/imgs
    private final Path fileStorageLocation = Paths.get("target/classes/static/imgs").toAbsolutePath().normalize();

    public ProductServiceImpl() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi: Không thể tạo thư mục lưu trữ ảnh.", ex);
        }
    }

    // ======================================================================
    // 1. TÌM KIẾM NÂNG CAO CHO ADMIN (Đã thêm lọc theo Discount)
    // ======================================================================
    @Override
    public Page<Product> filterAdminProducts(String keyword, Integer categoryId, 
                                             Double minPrice, Double maxPrice, 
                                             Double minQty, Double maxQty, 
                                             Integer minDiscount, Integer maxDiscount, // [MỚI] Thêm tham số này
                                             int page, int size) {
        // Chuyển đổi Double sang BigDecimal
        BigDecimal minP = (minPrice != null) ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal maxP = (maxPrice != null) ? BigDecimal.valueOf(maxPrice) : null;
        BigDecimal minQ = (minQty != null) ? BigDecimal.valueOf(minQty) : null;
        BigDecimal maxQ = (maxQty != null) ? BigDecimal.valueOf(maxQty) : null;

        int effectiveSize = (size <= 0) ? Integer.MAX_VALUE : size;

        // Gọi Repository với đầy đủ tham số (bao gồm discount)
        return productRepository.searchForAdmin(
            keyword, categoryId, minP, maxP, minQ, maxQ, 
            minDiscount, maxDiscount, // [MỚI] Truyền xuống DB
            PageRequest.of(page, effectiveSize)
        );
    }

    // ======================================================================
    // 2. TÌM KIẾM CHO CLIENT
    // ======================================================================
    @Override
    public Page<Product> searchProductsClient(String keyword, Integer categoryId, 
                                              Double minPrice, Double maxPrice, 
                                              int page, int size) {
        BigDecimal minP = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal maxP = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;
        
        return productRepository.searchForClient(keyword, categoryId, minP, maxP, PageRequest.of(page, size));
    }

    // ======================================================================
    // 3. CRUD CƠ BẢN
    // ======================================================================

    @Override
    public List<Product> findAll() { return productRepository.findAll(); }

    @Override
    public Optional<Product> findById(Integer id) { return productRepository.findById(id); }

    // [CREATE]
    @Override
    @Transactional
    public Product create(Product product, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImage(saveFile(imageFile));
        }
        
        if (product.getCreateDate() == null) {
            product.setCreateDate(new Date());
        } 

        // Giá trị mặc định
        if (product.getAvailable() == null) product.setAvailable(true);
        if (product.getQuantity() == null) product.setQuantity(BigDecimal.ZERO);
        if (product.getIsLiquidation() == null) product.setIsLiquidation(false);
        if (product.getImportPrice() == null) product.setImportPrice(BigDecimal.ZERO);
        if (product.getDiscount() == null) product.setDiscount(0);
        
        return productRepository.save(product);
    }

    // [UPDATE]
    @Override
    @Transactional
    public Product update(Product product, MultipartFile imageFile) throws IOException {
        if (product.getId() == null) throw new RuntimeException("ID không được để trống");
        
        Product existingProduct = findById(product.getId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + product.getId()));

        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImage(saveFile(imageFile));
        } else {
            product.setImage(existingProduct.getImage());
        }

        if (product.getQuantity() == null) product.setQuantity(BigDecimal.ZERO);
        
        // Giữ nguyên ngày tạo nếu không chọn mới
        if (product.getCreateDate() == null) {
             product.setCreateDate(existingProduct.getCreateDate());
        }
        
        return productRepository.save(product);
    }

    // [DELETE]
    @Override
    public void delete(Integer id) {
        Product product = findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (product.getQuantity() != null && product.getQuantity().compareTo(BigDecimal.ZERO) > 0 
                && !Boolean.TRUE.equals(product.getIsLiquidation())) {
            throw new RuntimeException("Không thể xóa! Sản phẩm còn tồn kho: " + product.getQuantity() + " kg.");
        }

        try {
            productRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi: Sản phẩm đã có đơn hàng liên quan.");
        }
    }

    // ======================================================================
    // 4. LOGIC PHỤ & HELPERS
    // ======================================================================

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchAndFilter(String keyword, String categoryName, Double minPrice, Double maxPrice) {
        int maxFetch = 2000;
        List<Product> products = productRepository.findAllWithCategory(PageRequest.of(0, maxFetch)).getContent();
        return products.stream()
            .filter(p -> keyword == null || keyword.trim().isEmpty() || p.getName().toLowerCase().contains(keyword.trim().toLowerCase()))
            .collect(Collectors.toList());
    }

    @Override
    public Page<Product> findAllPaged(int page, int size) {
        return productRepository.findAllWithCategory(PageRequest.of(page, size));
    }

    @Override
    public List<Product> findBestSellers(int limit) {
        return productRepository.findAvailableOrderByPriceDesc(PageRequest.of(0, limit)).getContent();
    }

    @Override
    public List<Product> findNewProducts(int limit) {
        return productRepository.findAvailableOrderByCreateDateDesc(PageRequest.of(0, limit)).getContent();
    }

    @Override
    public List<Product> findDiscountProducts(int limit) {
        return productRepository.findDiscountProducts(PageRequest.of(0, limit));
    }

    // Overload methods
    @Override
    public Product create(Product product) {
        try { return create(product, null); } catch (IOException e) { throw new RuntimeException(e); }
    }
    @Override
    public Product update(Product product) {
        try { return update(product, null); } catch (IOException e) { throw new RuntimeException(e); }
    }

    private String saveFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }
}