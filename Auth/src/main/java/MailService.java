import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.HashMap;
import java.security.SecureRandom;
import java.util.Properties;

public class MailService {
    static HashMap<String, UserLogin.UserCreds> awaitingAuth = UserLogin.awaitingAuthentication;
    static final Properties props = new Properties();
    static Session session = null;
    public MailService() {
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
    public static UserLogin.MailResponse handleRegister(String email) {
        if (awaitingAuth.containsKey(email)) return new UserLogin.MailResponse(false, "Email waiting for authorization", 0, 200);
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) return new UserLogin.MailResponse(false, "Inputted email seems to be invalid. Try again.", 0, 200);
        int code = generateCode();
        if (!sendMail("REG", email, code)) return new UserLogin.MailResponse(false, "Something went wrong with sending a mail.", 0, 500);
        return new UserLogin.MailResponse(true, "Mail sent to a given address", code, 200);
    }
    private static Boolean sendMail(String type, String email, Integer code) {
        if (type.equals("REG")) return sendRegMail(email, code);
        //if (type.equals("LOG")) return sendLogMail(email, code);
        return false;
    }
    private static Boolean sendRegMail(String email, Integer code) {
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
            message.setText("Your code: " + code);

            Transport.send(message);
            return true;
        } catch (AddressException e) {
            Debug.log("Failed setting Mail address: "+e);
            return false;
        } catch (MessagingException e) {
            Debug.log("Failed sending Mail message: "+e);
            return false;
        }
    }
    private static Integer generateCode() {
        SecureRandom random = new SecureRandom();
        int min = 100_000_000;
        int max = 999_999_999;
        return random.nextInt(max) + min;
    }
}
