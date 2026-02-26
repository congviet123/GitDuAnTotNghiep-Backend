package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import poly.edu.entity.ContactInfo;
import poly.edu.repository.ContactInfoRepository;
import poly.edu.service.ContactInfoService;

@Service
public class ContactInfoServiceImpl implements ContactInfoService {
    
    @Autowired
    private ContactInfoRepository contactInfoRepository;
    
    @Override
    public ContactInfo getContactInfo() {
        ContactInfo contactInfo = contactInfoRepository.findLatestContactInfo();
        
        // Nếu chưa có dữ liệu trong DB (phòng trường hợp)
        if (contactInfo == null) {
            contactInfo = new ContactInfo();
            contactInfo.setAddress("QTSC 9 Building, Đ. Tô Ký, Tân Chánh Hiệp, Quận 12, TP.HCM");
            contactInfo.setPhone("0900 000 001 | 0987654321");
            contactInfo.setEmail("CongViet47@gmail.com");
            contactInfo.setMapUrl("https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3918.420663996833!2d106.62615171098252!3d10.855574789254332!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752b6c59ba4c97%3A0x535e784068f1558b!2zVHLGsOG7nW5nIENhbyDEkOG6s25nIFRlY2hub2xvZ3kgU2lnb24gU1R1!5e0!3m2!1svi!2s!4v1706692345678!5m2!1svi!2s");
            contactInfo = contactInfoRepository.save(contactInfo);
        }
        
        return contactInfo;
    }
    
    @Override
    public ContactInfo updateContactInfo(ContactInfo contactInfo) {
        // Lấy ID của bản ghi hiện tại để cập nhật
        ContactInfo existing = getContactInfo();
        if (existing != null) {
            contactInfo.setId(existing.getId());
        }
        return contactInfoRepository.save(contactInfo);
    }
}