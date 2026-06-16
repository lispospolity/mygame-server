import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class Helper {
    public static boolean handle(HttpExchange exchange, String WantedMethod) throws IOException {
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        if (!exchange.getRequestMethod().equals(WantedMethod)) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            String error = "Method not allowed";
            byte[] byting = error.getBytes();
            exchange.sendResponseHeaders(405, byting.length);
            exchange.getResponseBody().write(byting);
            exchange.close();
            return true;
        }
        return false;
    }
    public static void Error(HttpExchange exchange, Exception e) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        String error = "Server API error";
        DebugLog.Log(e.toString());
        byte[] byting = error.getBytes();
        exchange.sendResponseHeaders(500, byting.length);
        exchange.getResponseBody().write(byting);
        exchange.close();
    }
}
