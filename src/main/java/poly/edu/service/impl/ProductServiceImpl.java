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

    // Khai báo đường dẫn vật lý trên server để lưu trữ ảnh sản phẩm tải lên.
    // Ảnh sẽ được lưu vào thư mục target/classes/static/imgs khi ứng dụng chạy.
    private final Path fileStorageLocation = Paths.get("target/classes/static/imgs").toAbsolutePath().normalize();

    // Constructor tự động chạy khi khởi tạo Service này. 
    // Nó sẽ kiểm tra xem thư mục lưu ảnh đã tồn tại chưa, nếu chưa có sẽ tự động tạo thư mục.
    public ProductServiceImpl() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi: Không thể tạo thư mục lưu trữ ảnh.", ex);
        }
    }

    // ======================================================================
    // 1. TÌM KIẾM NÂNG CAO CHO ADMIN (Lọc chi tiết cho trang quản trị)
    // ======================================================================
    @Override
    public Page<Product> filterAdminProducts(String keyword, Integer categoryId, 
                                             Double minPrice, Double maxPrice, 
                                             Double minQty, Double maxQty, 
                                             Integer minDiscount, Integer maxDiscount, 
                                             int page, int size) {
        
        // Chuyển đổi dữ liệu từ Double (thường dùng ở Controller) sang BigDecimal 
        // để khớp với kiểu dữ liệu của tiền tệ trong Entity và Database, tránh sai số thập phân.
        BigDecimal minP = (minPrice != null) ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal maxP = (maxPrice != null) ? BigDecimal.valueOf(maxPrice) : null;
        BigDecimal minQ = (minQty != null) ? BigDecimal.valueOf(minQty) : null;
        BigDecimal maxQ = (maxQty != null) ? BigDecimal.valueOf(maxQty) : null;

        //  Nếu size truyền vào <= 0 (vd: muốn lấy tất cả), ta set size bằng mức tối đa của Integer.
        int effectiveSize = (size <= 0) ? Integer.MAX_VALUE : size;

        // Gọi xuống hàm tìm kiếm @Query đã định nghĩa trong ProductRepository
        return productRepository.searchForAdmin(
            keyword, categoryId, minP, maxP, minQ, maxQ, 
            minDiscount, maxDiscount, 
            PageRequest.of(page, effectiveSize) // Phân trang kết quả
        );
    }

    // ======================================================================
    // 2. TÌM KIẾM CHO CLIENT (Giao diện khách hàng mua sắm)
    // ======================================================================
    @Override
    public Page<Product> searchProductsClient(String keyword, Integer categoryId, 
                                              Double minPrice, Double maxPrice, 
                                              int page, int size) {
        //  Tương tự như trên, chuyển Double sang BigDecimal để tìm kiếm theo giá
        BigDecimal minP = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal maxP = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;
        
        //  Gọi Query tìm kiếm sản phẩm cho khách (chỉ lấy SP đang available = true và giá > 0)
        return productRepository.searchForClient(keyword, categoryId, minP, maxP, PageRequest.of(page, size));
    }

    // ======================================================================
    // 3. CRUD CƠ BẢN (Thêm, Sửa, Xóa, Lấy chi tiết)
    // ======================================================================

    @Override
    public List<Product> findAll() { 
        return productRepository.findAll(); 
    }

    @Override
    public Optional<Product> findById(Integer id) { 
        return productRepository.findById(id); 
    }

    // THÊM MỚI SẢN PHẨM KÈM THEO ẢNH
    @Override
    @Transactional // [GHI CHÚ]: Đảm bảo an toàn dữ liệu, nếu lỗi giữa chừng sẽ rollback lại toàn bộ
    public Product create(Product product, MultipartFile imageFile) throws IOException {
        
        // Nếu có file ảnh được upload lên, gọi hàm saveFile() để lưu file và gán tên file vào product
        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImage(saveFile(imageFile));
        }
        
        //  Nếu chưa set ngày tạo thì mặc định lấy ngày giờ hệ thống hiện tại
        if (product.getCreateDate() == null) {
            product.setCreateDate(new Date());
        } 

        //  Set các giá trị mặc định tránh lỗi NullPointerException khi insert vào DB
        if (product.getAvailable() == null) product.setAvailable(true); // Trạng thái: Đang bán
        if (product.getQuantity() == null) product.setQuantity(BigDecimal.ZERO); // Tồn kho: 0
        if (product.getIsLiquidation() == null) product.setIsLiquidation(false); // Hàng thanh lý: Không
        if (product.getImportPrice() == null) product.setImportPrice(BigDecimal.ZERO); // Giá nhập: 0
        if (product.getDiscount() == null) product.setDiscount(0); // Giảm giá: 0%
        
        return productRepository.save(product);
    }

    // CẬP NHẬT SẢN PHẨM
    @Override
    @Transactional
    public Product update(Product product, MultipartFile imageFile) throws IOException {
        if (product.getId() == null) throw new RuntimeException("ID không được để trống");
        
        //  Lấy sản phẩm cũ từ DB lên để đối chiếu thông tin
        Product existingProduct = findById(product.getId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + product.getId()));

        //  Nếu có chọn ảnh mới thì lưu ảnh mới, ngược lại thì giữ lại tên ảnh cũ
        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImage(saveFile(imageFile));
        } else {
            product.setImage(existingProduct.getImage());
        }

        //  Đảm bảo số lượng không bị null
        if (product.getQuantity() == null) product.setQuantity(BigDecimal.ZERO);
        
        //  Giữ nguyên ngày tạo ban đầu của sản phẩm cũ (không cho phép sửa ngày tạo lúc update)
        if (product.getCreateDate() == null) {
             product.setCreateDate(existingProduct.getCreateDate());
        }
        
        return productRepository.save(product);
    }

    // XÓA SẢN PHẨM
    @Override
    public void delete(Integer id) {
        Product product = findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Ràng buộc nghiệp vụ: Không cho phép xóa nếu sản phẩm vẫn còn tồn kho (>0)
        // Ngoại trừ trường hợp đó là hàng đang thanh lý (isLiquidation = true)
        if (product.getQuantity() != null && product.getQuantity().compareTo(BigDecimal.ZERO) > 0 
                && !Boolean.TRUE.equals(product.getIsLiquidation())) {
            throw new RuntimeException("Không thể xóa! Sản phẩm còn tồn kho: " + product.getQuantity() + " kg.");
        }

        try {
            productRepository.deleteById(id);
        } catch (Exception e) {
            // Bắt lỗi DataIntegrityViolationException (ví dụ: SP này đã có khách mua, nằm trong hóa đơn)
            throw new RuntimeException("Lỗi: Sản phẩm đã có đơn hàng liên quan.");
        }
    }

    // ======================================================================
    // 4. LOGIC KINH DOANH KHÁC (Tìm kiếm nâng cao, Lấy sản phẩm bán chạy, sản phẩm mới, sản phẩm giảm giá...)
    // ======================================================================

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchAndFilter(String keyword, String categoryName, Double minPrice, Double maxPrice) {
        // Lấy tạm 2000 sp để lọc bằng Java Stream API (Cách cũ, có thể cân nhắc dùng Query SQL để tối ưu hơn)
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

    // Lấy danh sách TOP sản phẩm bán chạy (Trang chủ)
    @Override
    public List<Product> findBestSellers(int limit) {
        // Lấy danh sách bán chạy THỰC TẾ thay vì lấy theo giá.
        // Hàm findTopSellingProducts sẽ đếm tổng số lượng bán được từ các hóa đơn COMPLETED (Thành công).
        return productRepository.findTopSellingProducts(PageRequest.of(0, limit)).getContent();
    }

    // Lấy danh sách sản phẩm mới nhất dựa trên ngày tạo (Trang chủ)
    @Override
    public List<Product> findNewProducts(int limit) {
        return productRepository.findAvailableOrderByCreateDateDesc(PageRequest.of(0, limit)).getContent();
    }

    // Lấy danh sách sản phẩm có % giảm giá (Trang chủ)
    @Override
    public List<Product> findDiscountProducts(int limit) {
        return productRepository.findDiscountProducts(PageRequest.of(0, limit));
    }

    // Các hàm nạp chồng (Overload) cho phép create/update không cần truyền file ảnh
    @Override
    public Product create(Product product) {
        try { return create(product, null); } catch (IOException e) { throw new RuntimeException(e); }
    }
    @Override
    public Product update(Product product) {
        try { return update(product, null); } catch (IOException e) { throw new RuntimeException(e); }
    }

    // Hàm hỗ trợ lưu file ảnh vật lý vào ổ cứng
    private String saveFile(MultipartFile file) throws IOException {
        // Tạo tên file ngẫu nhiên bằng cách gắn thêm thời gian (Timestamp) vào trước tên gốc 
        // để tránh trường hợp bị trùng tên file. VD: 1699999999_apple.jpg
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        
        // Copy file từ request của người dùng vào thư mục đích trên server
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return fileName; // Trả về tên file để lưu vào Database
    }
}