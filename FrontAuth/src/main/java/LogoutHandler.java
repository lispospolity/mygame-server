import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;

public class LogoutHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if (Helper.handle(exchange, "DELETE")) return;
        try {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            String message = new String(exchange.getRequestBody().readAllBytes());
            Gson gson = new Gson();
            Map msg = gson.fromJson(message, Map.class);
            String token = msg.get("token").toString();
            String response = gson.toJson(UserLogin.LogOut(token));
            Websocket.getInstance().LogOut(token);
            byte[] byteresponse = response.getBytes();
            exchange.sendResponseHeaders(200, byteresponse.length);
            exchange.getResponseBody().write(byteresponse);
            exchange.close();
        } catch (Exception e) {
            Helper.Error(exchange, e);
        }
    }
}
