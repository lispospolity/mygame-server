import com.sun.net.httpserver.HttpExchange;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class RateLimiting {
    static HashMap<InetSocketAddress, Long> blockEverything = new HashMap<InetSocketAddress, Long>();
    static HashMap<InetSocketAddress, Long> blockLogIn = new HashMap<InetSocketAddress, Long>();
    static HashMap<InetSocketAddress, Long> blockLogOut = new HashMap<InetSocketAddress, Long>();
    static HashMap<InetSocketAddress, Long> blockUsers = new HashMap<InetSocketAddress, Long>();
    static HashMap<InetSocketAddress, requestCountAndOldest> loginrequests = new HashMap<InetSocketAddress, requestCountAndOldest>();
    static HashMap<InetSocketAddress, requestCountAndOldest> logoutrequests = new HashMap<InetSocketAddress, requestCountAndOldest>();
    static HashMap<InetSocketAddress, requestCountAndOldest> usersrequests = new HashMap<InetSocketAddress, requestCountAndOldest>();

    public static Boolean block(HttpExchange exchange, String type) {
        InetSocketAddress ip = exchange.getRemoteAddress();
        if (type.equals("login")) return throttleLogin(ip);
        if (type.equals("logout")) return throttleLogout(ip);
        if (type.equals("user")) return throttleUsers(ip);
        Helper.error(exchange, 500, "Internal Server Error (bad handling)");
        return null;
    }
    private static Boolean throttleLogin(InetSocketAddress ip) {
        if (blockLogIn.containsKey(ip)) {
            loginrequests.remove(ip);
            return blockLogIn.get(ip) > System.currentTimeMillis();
        }
        if (!loginrequests.containsKey(ip)) {
            loginrequests.put(ip, new requestCountAndOldest(1, System.currentTimeMillis()));
        } else {
            requestCountAndOldest map = loginrequests.get(ip);
            loginrequests.replace(ip, new requestCountAndOldest(map.requestCount()+1, map.oldest()));
        }
        if (loginrequests.get(ip).oldest() <= System.currentTimeMillis()-60000) {
            loginrequests.put(ip, new requestCountAndOldest(1, System.currentTimeMillis()));
        }
        if (loginrequests.get(ip).requestCount>=20) blockLogIn.put(ip, System.currentTimeMillis()+60000);
        return false;
    }
    private static Boolean throttleLogout(InetSocketAddress ip) {
        if (blockLogOut.containsKey(ip)) {
            logoutrequests.remove(ip);
            return blockLogOut.get(ip) > System.currentTimeMillis();
        }
        if (!logoutrequests.containsKey(ip)) {
            logoutrequests.put(ip, new requestCountAndOldest(1, System.currentTimeMillis()));
        } else {
            requestCountAndOldest map = logoutrequests.get(ip);
            logoutrequests.replace(ip, new requestCountAndOldest(map.requestCount()+1, map.oldest()));
        }
        if (logoutrequests.get(ip).oldest() <= System.currentTimeMillis()-60000) {
            logoutrequests.put(ip, new requestCountAndOldest(1, System.currentTimeMillis()));
        }
        if (logoutrequests.get(ip).requestCount>=50) blockLogOut.put(ip, System.currentTimeMillis()+3600000);
        return false;
    }
    private static Boolean throttleUsers(InetSocketAddress ip) {
        if (blockUsers.containsKey(ip)) {
            usersrequests.remove(ip);
            return blockUsers.get(ip) > System.currentTimeMillis();
        }
        if (!usersrequests.containsKey(ip)) {
            usersrequests.put(ip, new requestCountAndOldest(1, System.currentTimeMillis()));
        } else {
            requestCountAndOldest map = usersrequests.get(ip);
            usersrequests.replace(ip, new requestCountAndOldest(map.requestCount()+1, map.oldest()));
        }
        if (usersrequests.get(ip).oldest() <= System.currentTimeMillis()-3600000) {
            usersrequests.put(ip, new requestCountAndOldest(1, System.currentTimeMillis()));
        }
        if (usersrequests.get(ip).requestCount>=20) blockUsers.put(ip, System.currentTimeMillis()+60000);
        return false;
    }
    private record requestCountAndOldest(
            Integer requestCount,
            Long oldest
    ) {}
    public static Boolean antiSQLI(HttpExchange exchange, String prompt1, String prompt2) {
        InetSocketAddress ip = exchange.getRemoteAddress();
        if (blockEverything.containsKey(ip) && blockEverything.get(ip)>System.currentTimeMillis()) return true;
        if (prompt1.contains("OR 1=1") || prompt1.contains(";--")) {
            blockEverything.put(ip, System.currentTimeMillis()+120000);
            return true;
        }
        if (prompt2.contains("OR 1=1") || prompt2.contains(";--")) {
            blockEverything.put(ip, System.currentTimeMillis()+120000);
            return true;
        }
        return false;
    }
    public static Boolean antiSQLI(HttpExchange exchange, String prompt1) {
        InetSocketAddress ip = exchange.getRemoteAddress();
        if (blockEverything.containsKey(ip) && blockEverything.get(ip)>System.currentTimeMillis()) return true;
        if (prompt1.contains("OR 1=1") || prompt1.contains(";--")) {
            blockEverything.put(ip, System.currentTimeMillis()+120000);
            return true;
        }
        return false;
    }
}