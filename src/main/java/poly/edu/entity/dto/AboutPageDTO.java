package poly.edu.entity.dto;

import java.util.List;

public class AboutPageDTO {
    
    // Banner section
    private String bannerTitle;
    private String bannerSubtitle;
    private String bannerImage;
    
    // About Us section
    private String introTitle;
    private String introText1;
    private String introText2;
    private String introImage;
    
    // Email Newsletter
    private String emailNewsletter;
    
    // Why Choose Us section
    private String whyChooseTitle;
    private String whyChooseSubtitle;
    private List<FeatureDTO> features;
    
    // Gallery images
    private List<String> galleryImages;
    
    // Partners
    private List<PartnerDTO> partners;
    
    // Constructors
    public AboutPageDTO() {
    }
    
    // Getters and Setters
    public String getBannerTitle() {
        return bannerTitle;
    }
    
    public void setBannerTitle(String bannerTitle) {
        this.bannerTitle = bannerTitle;
    }
    
    public String getBannerSubtitle() {
        return bannerSubtitle;
    }
    
    public void setBannerSubtitle(String bannerSubtitle) {
        this.bannerSubtitle = bannerSubtitle;
    }
    
    public String getBannerImage() {
        return bannerImage;
    }
    
    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }
    
    public String getIntroTitle() {
        return introTitle;
    }
    
    public void setIntroTitle(String introTitle) {
        this.introTitle = introTitle;
    }
    
    public String getIntroText1() {
        return introText1;
    }
    
    public void setIntroText1(String introText1) {
        this.introText1 = introText1;
    }
    
    public String getIntroText2() {
        return introText2;
    }
    
    public void setIntroText2(String introText2) {
        this.introText2 = introText2;
    }
    
    public String getIntroImage() {
        return introImage;
    }
    
    public void setIntroImage(String introImage) {
        this.introImage = introImage;
    }
    
    public String getEmailNewsletter() {
        return emailNewsletter;
    }
    
    public void setEmailNewsletter(String emailNewsletter) {
        this.emailNewsletter = emailNewsletter;
    }
    
    public String getWhyChooseTitle() {
        return whyChooseTitle;
    }
    
    public void setWhyChooseTitle(String whyChooseTitle) {
        this.whyChooseTitle = whyChooseTitle;
    }
    
    public String getWhyChooseSubtitle() {
        return whyChooseSubtitle;
    }
    
    public void setWhyChooseSubtitle(String whyChooseSubtitle) {
        this.whyChooseSubtitle = whyChooseSubtitle;
    }
    
    public List<FeatureDTO> getFeatures() {
        return features;
    }
    
    public void setFeatures(List<FeatureDTO> features) {
        this.features = features;
    }
    
    public List<String> getGalleryImages() {
        return galleryImages;
    }
    
    public void setGalleryImages(List<String> galleryImages) {
        this.galleryImages = galleryImages;
    }
    
    public List<PartnerDTO> getPartners() {
        return partners;
    }
    
    public void setPartners(List<PartnerDTO> partners) {
        this.partners = partners;
    }
    
    // Inner class FeatureDTO
    public static class FeatureDTO {
        private String title;
        private String description;
        private String icon;
        
        public FeatureDTO() {
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getIcon() {
            return icon;
        }
        
        public void setIcon(String icon) {
            this.icon = icon;
        }
    }
    
    // Inner class PartnerDTO
    public static class PartnerDTO {
        private String name;
        private String logo;
        
        public PartnerDTO() {
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getLogo() {
            return logo;
        }
        
        public void setLogo(String logo) {
            this.logo = logo;
        }
    }
}