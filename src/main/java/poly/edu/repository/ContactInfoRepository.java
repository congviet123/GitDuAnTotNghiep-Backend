package poly.edu.repository;

import poly.edu.entity.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactInfoRepository extends JpaRepository<ContactInfo, Integer> {
    
    // Query vẫn giữ nguyên vì dùng entity ContactInfo
    @Query("SELECT c FROM ContactInfo c ORDER BY c.id DESC LIMIT 1")
    ContactInfo findLatestContactInfo();
}