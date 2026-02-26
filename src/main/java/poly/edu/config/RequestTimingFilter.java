// Filter này sẽ được Spring Boot tự động phát hiện và áp dụng cho tất cả các request đến ứng dụng. 
// Nó sẽ đo thời gian bắt đầu và kết thúc của mỗi request, sau đó tính toán thời gian xử lý và log kết quả. 
// Nếu thời gian xử lý vượt quá ngưỡng đã định (200ms), nó sẽ log ở mức WARN để dễ dàng nhận biết các request chậm.
package poly.edu.config;
// Filter này sẽ log thời gian xử lý của mỗi request. Nếu request nào mất hơn 200ms, nó sẽ log ở mức WARN để dễ dàng phát hiện các request chậm.
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestTimingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestTimingFilter.class);
    private static final long WARN_THRESHOLD_MS = 200; // warn if request takes longer than this

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            String path = request.getMethod() + " " + request.getRequestURI();
            if (duration > WARN_THRESHOLD_MS) {
                logger.warn("Slow request: {} took {} ms", path, duration);
            } else {
                logger.debug("Request: {} took {} ms", path, duration);
            }
        }
    }
}
