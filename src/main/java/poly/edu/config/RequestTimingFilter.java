package poly.edu.config;

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
