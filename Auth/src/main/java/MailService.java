import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.HashMap;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailService {
    static HashMap<String, UserLogin.UserCreds> awaitingAuth = UserLogin.awaitingAuthentication;
    static final Properties props = new Properties();
    static Session session = null;
    static ExecutorService emailPool;
    public MailService() {
        emailPool = Executors.newFixedThreadPool(2);
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                Env.read("Mail_Account"),
                                Env.read("Mail_Password")
                        );
                    }
                });
    }
    public static UserLogin.MailResponse handleMailAuthorization(String type, String email) {
        if (awaitingAuth.containsKey(email)) return new UserLogin.MailResponse(false, "Email already waiting for authorization", 0, 200);
        if (type.equals("REG")) {
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) return new UserLogin.MailResponse(false, "Inputted email seems to be invalid. Try again.", 0, 200);
            int code = generateCode();
            emailPool.submit(() -> sendRegMail(email, code));
            return new UserLogin.MailResponse(true, "Mail should arrive in your mailbox soon. Please check your spam folder too.", code, 200);
        }
        if (type.equals("LOG")) {
            int code = generateCode();
            emailPool.submit(() -> sendLogMail(email, code));
            return new UserLogin.MailResponse(true, "Mail should arrive in your mailbox soon. Please check your spam folder too.", code, 200);
        }
        return new UserLogin.MailResponse(false, "Something went wrong.", 0, 500);
    }
    private static void sendLogMail(String email, Integer code) {
        Message message = new MimeMessage(session);
        try {
            message.setFrom(
                    new InternetAddress(Env.read("Mail_Account"))
            );
            message.setRecipient(
                    Message.RecipientType.TO,
                    new InternetAddress(email)
            );
            message.setSubject("Verification code");
            message.setText("Your verification code: " + code);

            Transport.send(message);
        } catch (AddressException e) {
            Debug.log("Failed setting Mail address: "+e);
        } catch (MessagingException e) {
            Debug.log("Failed sending Mail message: "+e);
        }
    }
    private static void sendRegMail(String email, Integer code) {
        Message message = new MimeMessage(session);
        try {
            message.setFrom(
                    new InternetAddress(Env.read("Mail_Account"))
            );
            message.setRecipient(
                    Message.RecipientType.TO,
                    new InternetAddress(email)
            );
            message.setSubject("Verification code");
            message.setText("Your verification code: " + code);

            Transport.send(message);
        } catch (AddressException e) {
            Debug.log("Failed setting Mail address: "+e);
        } catch (MessagingException e) {
            Debug.log("Failed sending Mail message: "+e);
        }
    }
    private static Integer generateCode() {
        SecureRandom random = new SecureRandom();
        int min = 100_000_000;
        int max = 999_999_999;
        return random.nextInt(max) + min;
    }
}
