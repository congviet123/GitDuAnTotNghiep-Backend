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

    @Override
    public List<Address> findAllByUsername(String username) {
        return addressRepo.findByUser_Username(username);
    }

    @Override
    @Transactional
    public Address create(String username, Address address) {
        User user = userRepo.findById(username).orElseThrow(() -> new RuntimeException("User not found"));
        address.setUser(user);

        // Logic: Nếu đây là địa chỉ đầu tiên, set làm mặc định
        List<Address> list = addressRepo.findByUser_Username(username);
        if (list.isEmpty()) {
            address.setIsDefault(true);
        } else if (Boolean.TRUE.equals(address.getIsDefault())) {
            // Nếu user chọn cái mới là mặc định, bỏ mặc định các cái cũ
            addressRepo.removeDefaultAddress(username);
        }

        return addressRepo.save(address);
    }

    @Override
    @Transactional
    public Address update(String username, Long id, Address addressDetails) {
        Address existing = addressRepo.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        
        // Bảo mật: Kiểm tra xem địa chỉ này có đúng của user đang đăng nhập không
        if (!existing.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Không có quyền sửa địa chỉ này");
        }

        if (Boolean.TRUE.equals(addressDetails.getIsDefault())) {
            addressRepo.removeDefaultAddress(username);
        }

        existing.setFullname(addressDetails.getFullname());
        existing.setPhone(addressDetails.getPhone());
        existing.setProvince(addressDetails.getProvince());
        existing.setDistrict(addressDetails.getDistrict());
        existing.setWard(addressDetails.getWard());
        existing.setAddressLine(addressDetails.getAddressLine());
        existing.setIsDefault(addressDetails.getIsDefault());

        return addressRepo.save(existing);
    }

    @Override
    @Transactional
    public void delete(String username, Long id) {
        Address address = addressRepo.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        
        if (!address.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Không có quyền xóa địa chỉ này");
        }
        
        addressRepo.delete(address);
    }

    @Override
    public Address findById(Long id) {
        return addressRepo.findById(id).orElse(null);
    }
}