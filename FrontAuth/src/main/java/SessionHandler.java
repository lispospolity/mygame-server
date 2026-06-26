import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class SessionHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if (Helper.handle(exchange)) return; //HTTP method checking
        if (exchange.getRequestMethod().equals("POST")) postMethod(exchange);
        if (exchange.getRequestMethod().equals("DELETE")) deleteMethod(exchange);
    }
    private static void postMethod(HttpExchange exchange) {
        Boolean throttler = RateLimiting.block(exchange, "login");
        if (throttler == null) return; //server error check
        if (throttler) {
            Helper.error(exchange, 429, "Too Many Requests. Retry in ~60s"); //these 3 lines send it to rate limiting method and handle the result
            return;
        }
        try {
            Map msg = Helper.extractMessage(exchange);
            String name = (String) msg.get("username");
            String password = (String) msg.get("password");
            if (name == null||password == null) {
                Helper.error(exchange, 400, "Bad Request");
                return;
            }
            Helper.respond(exchange, UserLogin.logIn(name, password));
        } catch (IOException e) {
            Debug.log(e.toString());
            Helper.error(exchange, 500, "Internal Server Error", e);
        }
    }
    private static void deleteMethod(HttpExchange exchange) {
        Boolean throttler = RateLimiting.block(exchange, "logout");
        if (throttler == null) return; //server error check
        if (throttler) {
            Helper.error(exchange, 429, "Too Many Requests. Retry in ~60s"); //these 3 lines send it to rate limiting method and handle the result
            return;
        }
        try {
            Map msg = Helper.extractMessage(exchange);
            String token = (String) msg.get("token");
            if (token == null) {
                Helper.error(exchange, 400, "Bad Request");
                return;
            }
            Helper.respond(exchange, UserLogin.logOut(token));
        } catch (IOException e) {
            Debug.log(e.toString());
            Helper.error(exchange, 500, "Internal Server Error", e);
        }
    }
}
