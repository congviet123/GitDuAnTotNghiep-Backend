
// Bộ điều khiển để phục vụ hình ảnh từ classpath hoặc hệ thống tập tin với cơ chế bộ nhớ đệm và dự phòng.
package poly.edu.controller.rest;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Base64;

@RestController
@RequestMapping("/rest/imgs")
public class ImageController {

    // Return image bytes if present in classpath:/static/imgs/ or file:src/main/resources/static/imgs/;
    // otherwise return a tiny 1x1 transparent PNG as placeholder to avoid 404s.
    @GetMapping("/{filename:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws IOException {
        // 1) Try classpath
        Resource classpathRes = new ClassPathResource("static/imgs/" + filename);
        if (classpathRes.exists()) {
            try (InputStream is = classpathRes.getInputStream()) {
                byte[] bytes = StreamUtils.copyToByteArray(is);
                HttpHeaders headers = new HttpHeaders();
                headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).getHeaderValue());
                headers.setContentType(detectMediaType(filename));
                return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
            }
        }

        // 2) Try filesystem (development)
        File fs = new File("src/main/resources/static/imgs/" + filename);
        if (fs.exists() && fs.isFile()) {
            byte[] bytes = Files.readAllBytes(fs.toPath());
            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).getHeaderValue());
            headers.setContentType(detectMediaType(filename));
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        }

        // 3) Fallback: tiny 1x1 transparent PNG
        byte[] png = TRANSPARENT_PNG;
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).getHeaderValue());
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(png, headers, HttpStatus.OK);
    }

    private MediaType detectMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    // 1x1 transparent PNG (Base64-decoded)
    private static final byte[] TRANSPARENT_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQ" +
            "ImWNgYAAAAAMAAWgmWQ0AAAAAElFTkSuQmCC"
    );
}