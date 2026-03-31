package poly.edu.service;

import poly.edu.entity.Address;
import java.util.List;

public interface AddressService {
    
    /**
     * [TÍNH NĂNG 1]: Tìm danh sách Sổ địa chỉ
     * Lấy ra toàn bộ danh sách địa chỉ giao hàng thuộc về một tài khoản cụ thể.
     */
    List<Address> findAllByUsername(String username);

    /**
     * [TÍNH NĂNG 2]: Thêm địa chỉ mới
     * Tạo một địa chỉ mới và gắn nó vào tài khoản của người dùng.
     */
    Address create(String username, Address address);

    /**
     * [TÍNH NĂNG 3]: Cập nhật địa chỉ
     * Cho phép người dùng chỉnh sửa thông tin của một địa chỉ đã có.
     */
    Address update(String username, Integer id, Address address);

    /**
     * [TÍNH NĂNG 4]: Xóa địa chỉ
     * Xóa một địa chỉ khỏi Sổ địa chỉ của người dùng.
     */
    void delete(String username, Integer id);

    /**
     * [TÍNH NĂNG 5]: Lấy chi tiết một địa chỉ
     * Phục vụ cho việc lấy dữ liệu cũ đưa lên Form khi người dùng bấm nút "Sửa địa chỉ".
     */
    Address findById(Integer id);
}