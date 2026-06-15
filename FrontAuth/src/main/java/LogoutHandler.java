import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;

public class LogoutHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        try {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            String message = new String(exchange.getRequestBody().readAllBytes());
            Gson gson = new Gson();
            Map msg = gson.fromJson(message, Map.class);
            String token = msg.get("token").toString();
            Websocket.getInstance().LogOut(token);
            String response = gson.toJson(UserLogin.LogOut(token));
            byte[] byteresponse = response.getBytes();
            exchange.sendResponseHeaders(200, byteresponse.length);
            exchange.getResponseBody().write(byteresponse);
            exchange.close();
        } catch (Exception e) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            String error = "Server API error";
            DebugLog.Log(e.toString());
            byte[] byting = error.getBytes();
            exchange.sendResponseHeaders(500, byting.length);
            exchange.getResponseBody().write(byting);
            exchange.close();
        }
    }
}
