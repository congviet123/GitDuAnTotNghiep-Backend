package poly.edu.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Address;
import poly.edu.service.AddressService;

import java.util.List;

@CrossOrigin("*") // Cho phép Frontend (VueJS chạy ở port 5173) gọi API thoải mái mà không bị chặn lỗi CORS
@RestController
@RequestMapping("/rest/addresses") // Đường dẫn gốc cho module quản lý sổ địa chỉ
public class AddressRestController {

    @Autowired
    private AddressService addressService;

    /**
     * [HÀM HỖ TRỢ]: Lấy tên đăng nhập (username) của người dùng đang thao tác
     * Hoạt động: Chọc vào SecurityContext (Bộ nhớ bảo mật của Spring) để xem ai đang gọi API này.
     * Trả về: Tên username (nếu đã đăng nhập), hoặc null (nếu là khách vãng lai).
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }

    /**
     * [TÍNH NĂNG 1]: Lấy danh sách địa chỉ của tôi
     * Phục vụ cho Frontend: Hàm fetchSavedAddresses() trong Giỏ hàng sẽ gọi API này
     * để lấy danh sách địa chỉ, sau đó đổ dữ liệu vào các nút Radio cho khách chọn.
     */
    @GetMapping
    public ResponseEntity<?> getMyAddresses() {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để xem sổ địa chỉ.");
        }
        // Gọi Service tìm tất cả địa chỉ thuộc về username này
        return ResponseEntity.ok(addressService.findAllByUsername(username));
    }

    /**
     * [TÍNH NĂNG 2]: Thêm địa chỉ mới
     * Phục vụ cho Frontend: Khách hàng điền form thêm địa chỉ ở trang Profile cá nhân.
     */
    @PostMapping
    public ResponseEntity<?> createAddress(@RequestBody Address address) {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để thêm địa chỉ.");
        }
        try {
            // Gắn cứng username của người tạo vào địa chỉ này để bảo mật, tránh việc tạo hộ người khác
            return ResponseEntity.ok(addressService.create(username, address));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * [TÍNH NĂNG 3]: Cập nhật địa chỉ
     * [ĐÃ FIX LỖI]: Đổi kiểu dữ liệu của 'id' từ Long sang Integer cho khớp với SQL Server
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Integer id, @RequestBody Address address) {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để cập nhật địa chỉ.");
        }
        try {
            // Cập nhật địa chỉ dựa trên ID và bắt buộc phải thuộc sở hữu của username hiện tại
            return ResponseEntity.ok(addressService.update(username, id, address));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * [TÍNH NĂNG 4]: Xóa địa chỉ
     * [ĐÃ FIX LỖI]: Đổi kiểu dữ liệu của 'id' từ Long sang Integer cho khớp với SQL Server
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Integer id) {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để xóa địa chỉ.");
        }
        try {
            // Xóa địa chỉ dựa trên ID (Service sẽ tự kiểm tra xem địa chỉ này có đúng của user đó không)
            addressService.delete(username, id);
            return ResponseEntity.ok().build(); // Xóa thành công trả về mã 200 OK
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * [TÍNH NĂNG 5]: Lấy chi tiết 1 địa chỉ (Dùng khi khách bấm nút "Sửa" trên giao diện Profile)
     * [ĐÃ FIX LỖI]: Đổi kiểu dữ liệu của 'id' từ Long sang Integer cho khớp với SQL Server
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAddressDetail(@PathVariable Integer id) {
        String username = getCurrentUsername();
        if (username == null) return ResponseEntity.status(401).build();
        
        Address addr = addressService.findById(id);
        
        // Bảo mật: Nếu địa chỉ tồn tại và tên chủ sở hữu địa chỉ trùng với tên người đang đăng nhập thì mới trả về dữ liệu
        if (addr != null && addr.getUser() != null && addr.getUser().getUsername().equals(username)) {
            return ResponseEntity.ok(addr);
        }
        return ResponseEntity.notFound().build(); // Nếu không phải của mình hoặc không tồn tại thì trả về 404
    }
}