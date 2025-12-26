package poly.edu.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import poly.edu.service.MailService;

import java.io.UnsupportedEncodingException;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override

    // Khi nào chạy ổn định rồi thì mở lại để web mượt hơn
    public void sendEmail(String to, String subject, String body) {
        try {
            System.out.println("Đang bắt đầu gửi mail tới: " + to); // Log kiểm tra
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, "Trái Cây Nhập Khẩu"));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            System.out.println(">>> GỬI MAIL THÀNH CÔNG: " + to);

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            System.err.println(">>> LỖI GỬI MAIL: " + e.getMessage());
            // Ném lỗi ra ngoài để Controller bắt được và báo về Frontend
            throw new RuntimeException("Lỗi kết nối Gmail: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi không xác định khi gửi mail");
        }
    }
}