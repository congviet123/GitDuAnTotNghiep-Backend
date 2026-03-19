package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.service.FileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:src/main/resources/static/imgs}")
    private String uploadDir;

    @Override
    public String storeFile(MultipartFile file) {
        try {
            // Tạo tên file ngẫu nhiên
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFileName);
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            
            // Tạo đường dẫn đầy đủ
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            
            // Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Lưu file
            Path targetLocation = uploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // *** QUAN TRỌNG: CHỈ TRẢ VỀ 1 LẦN /imsgs/ ***
            return "/imgs/" + newFileName;  // Không thêm dấu / thừa
            
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}