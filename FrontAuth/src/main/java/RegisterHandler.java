import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class RegisterHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if (Helper.handle(exchange, "POST")) return;
        try {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            String message = new String(exchange.getRequestBody().readAllBytes());
            Gson gson = new Gson();
            Map msg = gson.fromJson(message, Map.class);
            String name = msg.get("username").toString();
            String password = msg.get("password").toString();
            String response = gson.toJson(UserLogin.mkUser(name, password));
            byte[] byteresponse = response.getBytes();
            exchange.sendResponseHeaders(200, byteresponse.length);
            exchange.getResponseBody().write(byteresponse);
            exchange.close();
        } catch (Exception e) {
            Helper.Error(exchange, e);
        }
    }
}


