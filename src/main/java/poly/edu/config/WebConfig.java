package poly.edu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        
        // 1. Lấy đường dẫn gốc của dự án (Ví dụ: D:\Project\...)
        String rootPath = System.getProperty("user.dir");
        
        // 2. Chuyển đổi dấu gạch chéo ngược (\) thành xuôi (/) để đúng chuẩn URL
        String cleanRootPath = rootPath.replace("\\", "/");
        
        // 3. Ghép chuỗi để trỏ vào thư mục src
        String uploadPath = cleanRootPath + "/src/main/resources/static/imgs/";

        // Log ra để bạn kiểm tra (Nó phải ra dạng: D:/Project.../src/.../imgs/)
        System.out.println(">>> PATH CHUẨN: file:///" + uploadPath);

        registry.addResourceHandler("/imgs/**")
                .addResourceLocations("file:///" + uploadPath) // Ưu tiên 1: Đọc từ source code (Dev)
                .addResourceLocations("classpath:/static/imgs/"); // Ưu tiên 2: Đọc từ file jar (Prod)
        
        // Cấu hình tài nguyên tĩnh khác
        registry.addResourceHandler("/static/**", "/css/**", "/js/**")
                .addResourceLocations("classpath:/static/", "classpath:/static/css/", "classpath:/static/js/");
    }
}