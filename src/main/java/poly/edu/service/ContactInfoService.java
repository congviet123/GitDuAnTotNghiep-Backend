package poly.edu.service;

import poly.edu.entity.ContactInfo;

public interface ContactInfoService {
    
    // Lấy thông tin liên hệ
    ContactInfo getContactInfo();
    
    // Cập nhật thông tin liên hệ
    ContactInfo updateContactInfo(ContactInfo contactInfo);
}