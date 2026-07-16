import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class AuthUserHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if (Helper.handle(exchange)) return;
        Boolean throttler = RateLimiting.block(exchange, "user");
        if (throttler == null) return; //server error check
        if (throttler) {
            Helper.error(exchange, 429, "Too Many Requests. Retry in ~60s"); //these 3 lines send it to rate limiting method and handle the result
            return;
        }
        if (exchange.getRequestMethod().equals("POST")) postMethod(exchange);
    }
    private static void postMethod(HttpExchange exchange) {
        try {
            Map msg = Helper.extractMessage(exchange);
            String code = (String) msg.get("code");
            String email = (String) msg.get("email");
            if (RateLimiting.antiSQLI(exchange, code)) {
                Helper.error(exchange, 403, "DON'T INJECT SQL (you are blocked for 2 minutes)");
                return;
            }
            Helper.respond(exchange, UserLogin.registerStep2(email, Integer.valueOf(code)));
        } catch (IOException e) {
            Debug.log(e.toString());
            Helper.error(exchange, 500, "Internal Server Error", e);
        }
    }
}
