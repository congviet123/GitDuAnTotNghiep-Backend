package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Address;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    // Tìm danh sách địa chỉ theo username
    // Lưu ý: Trong Entity Address đặt tên biến là 'user' thì ở đây dùng 'User_Username'
    List<Address> findByUser_Username(String username);

    // Reset tất cả địa chỉ của user về không mặc định (khi user set 1 cái mới làm mặc định)
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.username = ?1")
    void removeDefaultAddress(String username);
}