import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;

public class LoginHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if (Helper.handle(exchange, "POST")) return;
        try {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            String message = new String(exchange.getRequestBody().readAllBytes());
            Gson gson = new Gson();
            Map msg = gson.fromJson(message, Map.class);
            String Name = msg.get("username").toString();
            String Password = msg.get("password").toString();
            String response = gson.toJson(UserLogin.LogIn(Name, Password));
            byte[] byteresponse = response.getBytes();
            exchange.sendResponseHeaders(200, byteresponse.length);
            exchange.getResponseBody().write(byteresponse);
            exchange.close();
        } catch (Exception e) {
            Helper.Error(exchange, e);
        }
    }
}
