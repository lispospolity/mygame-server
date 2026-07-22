//hashing
import org.mindrot.jbcrypt.BCrypt;
//uuid
import java.util.HashMap;
import java.util.UUID;
import java.sql.SQLException;

public class UserLogin {
    static final DB db;
    public static HashMap<String, UserCreds> awaitingAuthentication = new HashMap<String, UserCreds>();
    static {
        try {
            db = new DB();
        } catch (SQLException e) {
            String error = e.toString();
            Debug.log(error);
            throw new RuntimeException(e);
        }
    }
    public static String authenticatingType(String email) {
        if (!awaitingAuthentication.containsKey(email)) return null;
        return awaitingAuthentication.get(email).type();
    }
    public static ServerResponse registerStep1(String email, String name, String password) {
        try {
            if (db.isRegistered(name, email)) return new ServerResponse(false, "User already exists.", 200);
            ServerResponse credsLegal = CredentialsRules.credsLegal(name, password);
            if (!credsLegal.success()) return credsLegal;
            MailResponse mailResponse = MailService.handleMailAuthorization("REG", email);
            ServerResponse parsedMailRsp = new ServerResponse(mailResponse.success(), mailResponse.message(), mailResponse.code());
            if (!mailResponse.success()) return parsedMailRsp;
            awaitingAuthentication.put(email, new UserCreds("REG", name, BCrypt.hashpw(password, BCrypt.gensalt()), mailResponse.authCode(), System.currentTimeMillis()+300000));
            return parsedMailRsp;
        } catch (RuntimeException e) {
            Debug.log(e+"");
            return new ServerResponse(false, "Internal Server Error.", 500);
        }
    }
    public static ServerResponse registerStep2(String email, Integer code) {
        if (email == null) return new ServerResponse(false, "Please insert email", 200);
        try {
            if (!awaitingAuthentication.containsKey(email)) return new ServerResponse(false, "Couldn't find given email in pending requests, please try again", 200);
            if (code!=awaitingAuthentication.get(email).code()) return new ServerResponse(false, "Wrong code, please try again.", 200);
            if (awaitingAuthentication.get(email).expiresAt()<System.currentTimeMillis()) {
                awaitingAuthentication.remove(email);
                return new ServerResponse(false, "Code has expired.", 200);
            }
            String name = awaitingAuthentication.get(email).name();
            String hash = awaitingAuthentication.get(email).hash();
            awaitingAuthentication.remove(email);
            db.addUser(name, hash, email);
            Debug.log("User " + name + " succesfully registered.");
            return new ServerResponse(true, "User succesfully made.", 200);
        } catch (RuntimeException e) {
            Debug.log(e+"");
            return new ServerResponse(false, "Internal Server Error.", 500);
        }
    }
    public static ServerResponse delUser(String token) {
        try {
            if (!db.validSession(token)) return new ServerResponse(false, "Token incorrect", 200);
            String name = db.getName(token);
            db.delUser(name);
            Debug.log("User " + name + " succesfully deleted (returned code 200)");
            db.logOut(token);
            return new ServerResponse(true, "User " + name + " succesfully deleted", 200);
        } catch (RuntimeException e) {
            Debug.log(e + "");
            return new ServerResponse(false, "Internal Server Error.", 500);
        }
    }
    private static LoginResponse emailLogIn(String email) {
        if (!db.isRegistered(email, email)) return new LoginResponse(false, null, "User with mail "+email+" not registered.", 200);
        String name = db.getNameByMail(email);
        if (db.loggedIn(name)) return new LoginResponse(false, null, "User is already logged in", 200);
        MailResponse mailResponse = MailService.handleMailAuthorization("LOG", email);
        LoginResponse parsedMailRsp = new LoginResponse(mailResponse.success(), null, mailResponse.message(), mailResponse.code());
        if (!mailResponse.success()) return parsedMailRsp;
        awaitingAuthentication.put(email, new UserCreds("LOG", null, null, mailResponse.authCode(), System.currentTimeMillis()+300000));
        return parsedMailRsp;
    }
    public static LoginResponse emailLogInStep2(String email, Integer code) {
        if (email == null) return new LoginResponse(false, null, "Please insert the correct email.", 200);
        if (!awaitingAuthentication.containsKey(email)) return new LoginResponse(false, null, "Couldn't find given email in pending requests, please try again", 200);
        if (code!=awaitingAuthentication.get(email).code()) return new LoginResponse(false, null, "Wrong code, please try again.", 200);
        if (awaitingAuthentication.get(email).expiresAt()<System.currentTimeMillis()) {
            awaitingAuthentication.remove(email);
            return new LoginResponse(false, null, "Code has expired.", 200);
        }
        try {
            String name = db.getNameByMail(email);
            if (db.loggedIn(name)) return new LoginResponse(false, null, "User already logged in.", 200);
            String token = UUID.randomUUID().toString();
            long time = System.currentTimeMillis();
            db.logIn(name, token, time);
            Debug.log("User " + name + " logged in. (returned code 200)");
            awaitingAuthentication.remove(email);
            return new LoginResponse(true, token, "Account succesfully logged in.", 200);
        } catch (RuntimeException e) {
            Debug.log(e + "");
            return new LoginResponse(false, null, "Internal Server Error.", 500);
        }
    }
    public static LoginResponse logIn(String name, String password) {
        if (name.contains("@")) return emailLogIn(name);
        LoginResponse lockResponse = LockOutAcc.handle(name);
        if (lockResponse != null) return lockResponse; //anti brute force() account blocking
        try {
            String dbHash = db.getPassword(name);
            if (dbHash == null) return new LoginResponse(false, null, "Account does not exist.", 200);
            if (!BCrypt.checkpw(password, dbHash)) {
                LockOutAcc.nextWrong(name); //anti-brute force attempt counter
                return new LoginResponse(false, null, "Wrong password.", 200);
            }
            if (db.loggedIn(name)) return new LoginResponse(false, null, "User already logged in.", 200);
            String token = UUID.randomUUID().toString();
            long time = System.currentTimeMillis();
            db.logIn(name, token, time);
            Debug.log("User " + name + " logged in. (returned code 200)");
            return new LoginResponse(true, token, "Account succesfully logged in.", 200);
        } catch (RuntimeException e) {
            Debug.log(e + "");
            return new LoginResponse(false, null, "Internal Server Error.", 500);
        }
    }
    public static ServerResponse logOut(String token) {
        try {
            if (!db.validSession(token)) return new ServerResponse(false, "Could not find player", 200);
            db.logOut(token);
            Debug.log("User logged out. (returned code 200)");
            return new ServerResponse(true, "Terminated session", 200);
        } catch (RuntimeException e) {
        Debug.log(e + "");
        return new ServerResponse(false, "Internal Server Error.", 500);
        }
    }


    public record ServerResponse(
        Boolean success,
        String message,
        int code
    ) {}

    public record LoginResponse(
            Boolean success,
            String token,
            String message,
            int code
    ) {}

    public record MailResponse(
            Boolean success,
            String message,
            int authCode,
            int code
    ) {}

    public record UserCreds(
            String type,
            String name,
            String hash,
            int code,
            Long expiresAt
    ) {}
}
//TODO add email register and email log in compability