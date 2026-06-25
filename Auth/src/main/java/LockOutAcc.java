import java.util.HashMap;

public class LockOutAcc {
    static HashMap<String, Long> blockAcc = new HashMap<String, Long>();
    static HashMap<String, Integer> attempts = new HashMap<String, Integer>();
    public static UserLogin.LoginResponse handle(String name) {
        if (!attempts.containsKey(name)) attempts.put(name, 0);
        if (attempts.get(name)>4) {
            Debug.log("Failed login from account "+name+". Blocked for 5 mins");
            blockAcc.put(name, System.currentTimeMillis()+300000);
            attempts.replace(name, 0);
        }
        if (blockAcc.containsKey(name)) {
            if (blockAcc.get(name) > System.currentTimeMillis()) return new UserLogin.LoginResponse(false, null, "Too many attempts.", 200);
        }
        return null;
    }
    public static void nextWrong(String name) {
        attempts.replace(name, attempts.get(name)+1);
    }
}
