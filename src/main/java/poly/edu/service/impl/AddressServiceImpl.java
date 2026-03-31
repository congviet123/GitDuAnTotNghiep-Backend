package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Address;
import poly.edu.entity.User;
import poly.edu.repository.AddressRepository;
import poly.edu.repository.UserRepository;
import poly.edu.service.AddressService;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepo;

    @Autowired
    private UserRepository userRepo;

    /**
     * [TÍNH NĂNG]: Lấy toàn bộ Sổ địa chỉ của một khách hàng
     */
    @Override
    public List<Address> findAllByUsername(String username) {
        return addressRepo.findByUser_Username(username);
    }

    /**
     * [TÍNH NĂNG]: Thêm mới một địa chỉ vào Sổ địa chỉ
     * Logic thông minh: 
     * - Nếu đây là địa chỉ đầu tiên khách thêm -> Tự động ép làm Mặc định.
     * - Nếu khách chủ động tích vào "Đặt làm mặc định" -> Tự động gỡ Mặc định của các địa chỉ cũ.
     */
    @Override
    @Transactional
    public Address create(String username, Address address) {
        User user = userRepo.findById(username).orElseThrow(() -> new RuntimeException("User not found"));
        address.setUser(user);

        // Lấy danh sách địa chỉ hiện tại của user
        List<Address> list = addressRepo.findByUser_Username(username);
        
        if (list.isEmpty()) {
            // Nếu danh sách rỗng (chưa có địa chỉ nào), bắt buộc set địa chỉ này làm mặc định
            address.setIsDefault(true);
        } else if (Boolean.TRUE.equals(address.getIsDefault())) {
            // Nếu user cố tình chọn địa chỉ mới này làm mặc định, xóa cờ mặc định của các địa chỉ cũ
            addressRepo.removeDefaultAddress(username);
        }

        return addressRepo.save(address);
    }

    /**
     * [TÍNH NĂNG]: Cập nhật thông tin địa chỉ đã có trong Sổ địa chỉ
     */
    @Override
    @Transactional
    public Address update(String username, Integer id, Address addressDetails) {
        Address existing = addressRepo.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        
        // Bảo mật: Xác minh xem người đang yêu cầu sửa có phải là chủ nhân của địa chỉ này không
        if (!existing.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Lỗi bảo mật: Không có quyền sửa địa chỉ của người khác!");
        }

        // Nếu khi sửa, khách hàng tích chọn "Đặt làm mặc định" -> Gỡ mặc định các địa chỉ khác
        if (Boolean.TRUE.equals(addressDetails.getIsDefault())) {
            addressRepo.removeDefaultAddress(username);
        }

        // Cập nhật các trường thông tin
        existing.setFullname(addressDetails.getFullname());
        existing.setPhone(addressDetails.getPhone());
        existing.setProvince(addressDetails.getProvince());
        existing.setDistrict(addressDetails.getDistrict());
        existing.setWard(addressDetails.getWard());
        existing.setAddressLine(addressDetails.getAddressLine());
        existing.setIsDefault(addressDetails.getIsDefault());

        return addressRepo.save(existing);
    }

    /**
     * [TÍNH NĂNG]: Xóa một địa chỉ khỏi Sổ
     */
    @Override
    @Transactional
    public void delete(String username, Integer id) {
        Address address = addressRepo.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        
        // Bảo mật: Không cho phép dùng Postman để xóa trộm địa chỉ của người khác
        if (!address.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Lỗi bảo mật: Không có quyền xóa địa chỉ của người khác!");
        }
        
        addressRepo.delete(address);
    }

    /**
     * [TÍNH NĂNG]: Lấy chi tiết 1 địa chỉ để hiển thị lên Form Sửa
     */
    @Override
    public Address findById(Integer id) {
        return addressRepo.findById(id).orElse(null);
    }
}