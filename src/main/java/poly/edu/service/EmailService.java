package poly.edu.service;

public interface EmailService {
    boolean sendContactEmail(String name, String fromEmail, String message, String toEmail);
}