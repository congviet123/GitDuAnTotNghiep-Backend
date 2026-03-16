package poly.edu.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import poly.edu.entity.StaticPage;
import poly.edu.entity.dto.AboutPageDTO;
import poly.edu.repository.StaticPageRepository;
import poly.edu.service.AboutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AboutServiceImpl implements AboutService {
    
    @Autowired
    private StaticPageRepository staticPageRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String ABOUT_SLUG = "gioi-thieu";
    
    @Override
    @Transactional(readOnly = true)
    public AboutPageDTO getAboutPage() {
        StaticPage staticPage = staticPageRepository.findBySlug(ABOUT_SLUG)
                .orElseGet(() -> createDefaultAboutPage());
        
        try {
            return objectMapper.readValue(staticPage.getContent(), AboutPageDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error parsing about page content: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void saveAboutPage(AboutPageDTO aboutPageDTO) {
        StaticPage staticPage = staticPageRepository.findBySlug(ABOUT_SLUG)
                .orElse(new StaticPage());
        
        staticPage.setSlug(ABOUT_SLUG);
        staticPage.setTitle(aboutPageDTO.getBannerTitle());
        staticPage.setImageUrl(aboutPageDTO.getBannerImage());
        
        try {
            String content = objectMapper.writeValueAsString(aboutPageDTO);
            staticPage.setContent(content);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error converting about page to JSON: " + e.getMessage());
        }
        
        staticPageRepository.save(staticPage);
    }
    
    private StaticPage createDefaultAboutPage() {
        AboutPageDTO defaultAbout = new AboutPageDTO();
        
        // Banner defaults
        defaultAbout.setBannerTitle("Chào Mừng Bạn Đến Trái Cây Bay");
        defaultAbout.setBannerSubtitle("Nơi Cung Cấp Trái Cây Tươi Sạch - Fresh & Healthy");
        defaultAbout.setBannerImage("/imgs/bannerGioiThieu.jpg");
        
        // About Us defaults
        defaultAbout.setIntroTitle("Về Chúng Tôi");
        defaultAbout.setIntroText1("Công ty cổ phần thực phẩm dinh dưỡng Trái Cây Bay là đơn vị chuyên cung cấp các loại trái cây hoa quả, rau sạch nhập khẩu và nội địa chất lượng cao. Chúng tôi cam kết chất lượng sản phẩm sạch từ thiên nhiên, không hóa chất bảo quản.");
        defaultAbout.setIntroText2("Trái cây của chúng tôi sẽ mang đến cho khách hàng sự an tâm tuyệt đối. Chúng tôi mong muốn có cơ hội được phục vụ quý khách và góp phần phát triển ngành thực phẩm sạch tại Việt Nam.");
        defaultAbout.setIntroImage("/imgs/logoTraiCaybay.jpg");
        
        // Email Newsletter
        defaultAbout.setEmailNewsletter("Nhận thông tin cập nhật qua email về các ưu đãi đặc biệt.");
        
        // Why Choose Us
        defaultAbout.setWhyChooseTitle("Vì Sao Chọn Sản Phẩm Của Chúng Tôi");
        defaultAbout.setWhyChooseSubtitle("Cam kết mang đến cho khách hàng những sản phẩm chất lượng nhất, dịch vụ tốt nhất.");
        
        // Features
        List<AboutPageDTO.FeatureDTO> features = new ArrayList<>();
        features.add(createFeature("MIỄN PHÍ VẬN CHUYỂN", "Bán kính 2km", "bi-truck"));
        features.add(createFeature("HỖ TRỢ 24/7", "Hotline: 0987.654.321", "bi-headset"));
        features.add(createFeature("GIỜ LÀM VIỆC", "T2 - CN: Giờ hành chính", "bi-clock-history"));
        features.add(createFeature("ĐỔI TRẢ DỄ DÀNG", "Trong vòng 24h nếu lỗi", "bi-arrow-repeat"));
        features.add(createFeature("NHIỀU ƯU ĐÃI", "Quà tặng & Mã giảm giá", "bi-gift"));
        defaultAbout.setFeatures(features);
        
        // Gallery images
        List<String> galleryImages = new ArrayList<>();
        galleryImages.add("/imgs/Nho_Tim_Cardinal.jpg");
        galleryImages.add("/imgs/Tao_Rockit_New_Zealand.jpg");
        galleryImages.add("/imgs/Tao_Gala_My.jpg");
        galleryImages.add("/imgs/Nho_Tim_Cardinal.jpg");
        defaultAbout.setGalleryImages(galleryImages);
        
        // Partners
        List<AboutPageDTO.PartnerDTO> partners = new ArrayList<>();
        partners.add(createPartner("HIPSTER", "/imgs/no-image.png"));
        partners.add(createPartner("GOLDENGRID", "/imgs/no-image.png"));
        partners.add(createPartner("REAL ESTATE", "/imgs/no-image.png"));
        partners.add(createPartner("BALIRESORT", "/imgs/no-image.png"));
        defaultAbout.setPartners(partners);
        
        StaticPage staticPage = new StaticPage();
        staticPage.setSlug(ABOUT_SLUG);
        staticPage.setTitle(defaultAbout.getBannerTitle());
        staticPage.setImageUrl(defaultAbout.getBannerImage());
        
        try {
            staticPage.setContent(objectMapper.writeValueAsString(defaultAbout));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating default about page: " + e.getMessage());
        }
        
        return staticPageRepository.save(staticPage);
    }
    
    private AboutPageDTO.FeatureDTO createFeature(String title, String desc, String icon) {
        AboutPageDTO.FeatureDTO feature = new AboutPageDTO.FeatureDTO();
        feature.setTitle(title);
        feature.setDescription(desc);
        feature.setIcon(icon);
        return feature;
    }
    
    private AboutPageDTO.PartnerDTO createPartner(String name, String logo) {
        AboutPageDTO.PartnerDTO partner = new AboutPageDTO.PartnerDTO();
        partner.setName(name);
        partner.setLogo(logo);
        return partner;
    }
}