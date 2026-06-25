import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class RegisterHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if (Helper.handle(exchange, "POST")) return;
        try {
            Map msg = Helper.extractMessage(exchange);
            String name = (String) msg.get("username");
            String password = (String) msg.get("password");
            if (name == null||password == null) {
                Helper.error(exchange, 400, "Bad Request");
                return;
            }
            Helper.respond(exchange, UserLogin.mkUser(name, password));
        } catch (IOException e) {
            Debug.log(e.toString());
            Helper.error(exchange, 500, "Internal Server Error", e);
        }
    }
}


