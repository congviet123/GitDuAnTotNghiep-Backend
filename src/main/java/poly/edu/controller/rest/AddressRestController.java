package poly.edu.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Address;
import poly.edu.service.AddressService;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/rest/addresses") // Đường dẫn API riêng cho địa chỉ
public class AddressRestController {

    @Autowired
    private AddressService addressService;

    // Helper: Lấy username của người đang đăng nhập
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }

    // 1. Lấy danh sách địa chỉ của tôi
    @GetMapping
    public ResponseEntity<?> getMyAddresses() {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập.");
        }
        return ResponseEntity.ok(addressService.findAllByUsername(username));
    }

    // 2. Thêm địa chỉ mới
    @PostMapping
    public ResponseEntity<?> createAddress(@RequestBody Address address) {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập.");
        }
        try {
            return ResponseEntity.ok(addressService.create(username, address));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Cập nhật địa chỉ
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody Address address) {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập.");
        }
        try {
            return ResponseEntity.ok(addressService.update(username, id, address));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Xóa địa chỉ
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập.");
        }
        try {
            addressService.delete(username, id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 5. Lấy chi tiết 1 địa chỉ (dùng khi sửa)
    @GetMapping("/{id}")
    public ResponseEntity<?> getAddressDetail(@PathVariable Long id) {
        String username = getCurrentUsername();
        if (username == null) return ResponseEntity.status(401).build();
        
        Address addr = addressService.findById(id);
        if (addr != null && addr.getUser().getUsername().equals(username)) {
            return ResponseEntity.ok(addr);
        }
        return ResponseEntity.notFound().build();
    }
}