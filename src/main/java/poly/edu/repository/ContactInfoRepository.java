package poly.edu.repository;

import poly.edu.entity.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactInfoRepository extends JpaRepository<ContactInfo, Integer> {
    
    // Lấy thông tin liên hệ (chỉ có 1 dòng duy nhất)
    @Query("SELECT c FROM ContactInfo c ORDER BY c.id DESC LIMIT 1")
    ContactInfo findLatestContactInfo();
}