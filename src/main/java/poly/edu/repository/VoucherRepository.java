package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.Voucher;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, String> {
    
    Optional<Voucher> findByCode(String code);
    boolean existsByCode(String code);
    
    @Query("SELECT v FROM Voucher v WHERE v.active = true AND v.startDate <= :now AND v.endDate >= :now")
    List<Voucher> findActiveVouchers(@Param("now") LocalDateTime now);
    
    // Lấy voucher công khai đang hoạt động (cho khách hàng áp dụng)
    @Query("SELECT v FROM Voucher v WHERE v.active = true AND v.visibility = true AND v.startDate <= :now AND v.endDate >= :now")
    List<Voucher> findPublicActiveVouchers(@Param("now") LocalDateTime now);
}