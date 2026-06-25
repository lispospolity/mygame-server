//hashing
import org.mindrot.jbcrypt.BCrypt;
//uuid
import java.net.http.WebSocket;
import java.util.HashMap;
import java.util.UUID;
import java.sql.SQLException;

public class UserLogin {
    static final DB db;
    static {
        try {
            db = new DB();
        } catch (SQLException e) {
            String error = e.toString();
            Debug.log(error);
            throw new RuntimeException(e);
        }
    }

    public static ServerResponse mkUser(String name, String Password) {
        if (db.getPassword(name) != null) return new ServerResponse(false, "User already exists.", 200);
        String Hash = BCrypt.hashpw(Password, BCrypt.gensalt());
        db.addUser(name, Hash);
        Debug.log("User "+name+" succesfully made. (returned code 200)");
        return new ServerResponse(true, "User succesfully made.", 200);
    }
    public static ServerResponse delUser(String token) {
        if (!db.validSession(token)) return new ServerResponse(false, "Token incorrect", 200);
        String name = db.getName(token);
        db.delUser(name);
        Debug.log("User "+name+" succesfully deleted (returned code 200)");
        db.logOut(token);
        return new ServerResponse(true, "User "+name+" succesfully deleted", 200);
    }
    public static LoginResponse logIn(String name, String password) {
        LoginResponse lockResponse = LockOutAcc.handle(name);
        if (lockResponse != null) return lockResponse; //anti brute force()

        String dbHash = db.getPassword(name);
        if (dbHash == null) return new LoginResponse(false, null, "Account does not exist.", 200);
        if (db.loggedIn(name)) return new LoginResponse(false, null, "User already logged in.", 200);
        if (BCrypt.checkpw(password, dbHash)) {
            String token = UUID.randomUUID().toString();
            long time = System.currentTimeMillis();
            db.logIn(name, token, time);
            Debug.log("User "+name+" logged in. (returned code 200)");
            return new LoginResponse(true, token, "Account succesfully logged in.", 200);
        }
        LockOutAcc.nextWrong(name); //anti-brute force attempt counter
        return new LoginResponse(false, null, "Wrong password.", 200);
    }
    public static ServerResponse logOut(String token) {
        if (!db.validSession(token)) return new ServerResponse(false, "Could not find player", 200);
        db.logOut(token);
        Debug.log("User logged out. (returned code 200)");
        return new ServerResponse(true, "Terminated session", 200);
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
}
//TODO add email register and email log in compability