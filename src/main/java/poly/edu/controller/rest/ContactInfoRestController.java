package poly.edu.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.ContactInfo;
import poly.edu.service.ContactInfoService;

@RestController
@RequestMapping("/rest/contact-info")  // GIỮ NGUYÊN endpoint
@CrossOrigin(origins = "*")
public class ContactInfoRestController {
    
    @Autowired
    private ContactInfoService contactInfoService;
    
    // GET: Lấy thông tin liên hệ
    @GetMapping
    public ResponseEntity<ContactInfo> getContactInfo() {
        ContactInfo contactInfo = contactInfoService.getContactInfo();
        return ResponseEntity.ok(contactInfo);
    }
    
    // PUT: Cập nhật thông tin liên hệ
    @PutMapping
    public ResponseEntity<ContactInfo> updateContactInfo(@RequestBody ContactInfo contactInfo) {
        ContactInfo updatedInfo = contactInfoService.updateContactInfo(contactInfo);
        return ResponseEntity.ok(updatedInfo);
    }
}