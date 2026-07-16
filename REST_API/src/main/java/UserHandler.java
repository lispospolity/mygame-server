import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class UserHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if (Helper.handle(exchange)) return;
        Boolean throttler = RateLimiting.block(exchange, "user");
        if (throttler == null) return; //server error check
        if (throttler) {
            Helper.error(exchange, 429, "Too Many Requests. Retry in ~60s"); //these 3 lines send it to rate limiting method and handle the result
            return;
        }
        if (exchange.getRequestMethod().equals("POST")) postMethod(exchange);
        if (exchange.getRequestMethod().equals("DELETE")) deleteMethod(exchange);
    }
    private static void postMethod(HttpExchange exchange) {
        try {
            Map msg = Helper.extractMessage(exchange);
            String name = (String) msg.get("username");
            String password = (String) msg.get("password");
            String email = (String) msg.get("email");
            if (name == null||password == null||email == null) {
                Helper.error(exchange, 403, "Missing one of the required prompts");
                return;
            }
            if (RateLimiting.antiSQLI(exchange, name, password)) {
                Helper.error(exchange, 403, "DON'T INJECT SQL (you are blocked for 2 minutes)");
                return;
            }
            Helper.respond(exchange, UserLogin.registerStep1(email, name, password));
        } catch (IOException e) {
            Debug.log(e.toString());
            Helper.error(exchange, 500, "Internal Server Error", e);
        }
    }
    private static void deleteMethod(HttpExchange exchange) {
        try {
            Map msg = Helper.extractMessage(exchange);
            String token = (String) msg.get("token");
            if (RateLimiting.antiSQLI(exchange, token)) {
                Helper.error(exchange, 403, "DON'T INJECT SQL (you are blocked for 2 minutes)");
                return;
            }
            Helper.respond(exchange, UserLogin.delUser(token));
        } catch (IOException e) {
            Debug.log(e.toString());
            Helper.error(exchange, 500, "Internal Server Error", e);
        }
    }
}
