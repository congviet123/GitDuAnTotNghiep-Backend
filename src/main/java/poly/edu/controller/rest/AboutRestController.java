package poly.edu.controller.rest;

import poly.edu.entity.dto.AboutPageDTO;
import poly.edu.service.AboutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest/about")
public class AboutRestController {
    
    @Autowired
    private AboutService aboutService;
    
    @GetMapping
    public ResponseEntity<AboutPageDTO> getAboutPage() {
        try {
            AboutPageDTO aboutPage = aboutService.getAboutPage();
            return ResponseEntity.ok(aboutPage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/admin")
    public ResponseEntity<?> updateAboutPage(@RequestBody AboutPageDTO aboutPageDTO) {
        try {
            aboutService.saveAboutPage(aboutPageDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error saving about page: " + e.getMessage());
        }
    }
}