package ib.finki.ukim.totp.services.implementation.global;

import ib.finki.ukim.totp.services.interfaces.global.IEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService implements IEmailService {
    private final JavaMailSender emailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @SneakyThrows
    @Override
    public void sendRegistrationEmail(String email, String token) {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String confirmationLink = baseUrl + "/confirm?token=" + token;

        String emailContent = "<p>Click the following link to confirm your account: <a href=\"" +
                confirmationLink + "\">Confirm Account here</a></p>";

        try {
            helper.setTo(email);
            helper.setSubject("Account Confirmation");
            helper.setText(emailContent, true);

            emailSender.send(message);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @SneakyThrows
    @Override
    public void generatePasswordEmail(String email, String password) {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false);

        String emailContent = "<p>Your password to finish the transaction is: " + password + "</p>";

        try {
            helper.setTo(email);
            helper.setSubject("Transaction confirmation");
            helper.setText(emailContent, true);

            emailSender.send(message);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
}
