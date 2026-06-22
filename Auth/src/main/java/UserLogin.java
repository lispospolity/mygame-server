//hashing
import org.mindrot.jbcrypt.BCrypt;
//uuid
import java.net.http.WebSocket;
import java.util.UUID;
import java.sql.SQLException;

public class UserLogin {
    static final DB db;
    static {
        try {
            db = new DB();
        } catch (SQLException e) {
            String error = e.toString();
            Debug.Log(error);
            throw new RuntimeException(e);
        }
    }

    public static ServerResponse MkUser(String Name, String Password) throws SQLException {
        if (db.GetPassword(Name) != null) return new ServerResponse(false, "User already exists.", 200);
        String Hash = BCrypt.hashpw(Password, BCrypt.gensalt());
        db.AddUser(Name, Hash);
        Debug.Log("User "+Name+" succesfully made. (returned code 200)");
        return new ServerResponse(true, "User succesfully made.", 200);
    }
    public static ServerResponse DelUser(String token) {
        if (!db.ValidSession(token)) return new ServerResponse(false, "Token incorrect", 200);
        String Name = db.GetName(token);
        db.DelUser(Name);
        Debug.Log("User "+Name+" succesfully deleted (returned code 200)");
        db.LogOut(token);
        return new ServerResponse(true, "User "+Name+" succesfully deleted", 200);
    }
    public static LoginResponse LogIn(String Name, String Password) {
        if (db.GetPassword(Name) == null) return new LoginResponse(false, null, "Account does not exist.", 200);
        if (db.LoggedIn(Name)) return new LoginResponse(false, null, "User already logged in.", 200);
        if (BCrypt.checkpw(Password, db.GetPassword(Name))) {
            String token = UUID.randomUUID().toString();
            long time = System.currentTimeMillis();
            db.LogIn(Name, token, time);
            Debug.Log("User "+Name+" logged in. (returned code 200)");
            return new LoginResponse(true, token, "Account succesfully logged in.", 200);
        }
        return new LoginResponse(false, null, "Wrong password.", 200);
    }
    public static ServerResponse LogOut(String token) {
        if (!db.ValidSession(token)) return new ServerResponse(false, "Could not find player", 200);
        db.LogOut(token);
        Debug.Log("User logged out. (returned code 200)");
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
