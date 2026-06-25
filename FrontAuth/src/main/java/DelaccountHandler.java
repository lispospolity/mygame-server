import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class DelaccountHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if (Helper.handle(exchange, "DELETE")) return;
        try {
            Map msg = Helper.extractMessage(exchange);
            String token = (String) msg.get("token");
            if (token == null) {
                Helper.error(exchange, 400, "Bad Request");
                return;
            }
            Helper.respond(exchange, UserLogin.delUser(token));
        } catch (IOException e) {
            Debug.log(e.toString());
            Helper.error(exchange, 500, "Internal Server Error", e);
        }
    }
}
