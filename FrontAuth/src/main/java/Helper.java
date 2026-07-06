import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Helper {

    static Gson gson = new Gson();

    public static boolean handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    public static Map extractMessage(HttpExchange exchange) throws IOException {
        String message = new String(exchange.getRequestBody().readAllBytes());
        return gson.fromJson(message, Map.class);
    }

    public static void respond(HttpExchange exchange, String toClient, Integer code) {
        if (code == 500) {
            error(exchange, code, toClient);
            return;
        }
        try {
            //System.out.println("responding with: "+toClient);
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            byte[] byteresponse = toClient.getBytes();
            exchange.sendResponseHeaders(200, byteresponse.length);
            exchange.getResponseBody().write(byteresponse);
            exchange.close();
        } catch (IOException e) {
            Debug.log(e.toString());
            error(exchange, 500, "Internal Server Error", e);
        }
    }
    public static void respond(HttpExchange ex, UserLogin.ServerResponse serverResponse) {
        String toClient = gson.toJson(serverResponse);
        respond(ex, toClient, serverResponse.code());
    }
    public static void respond(HttpExchange ex, UserLogin.LoginResponse loginResponse) {
        String toClient = gson.toJson(loginResponse);
        respond(ex, toClient, loginResponse.code());
    }

    public static void error(HttpExchange exchange, Integer code, String message, Exception e) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        String response = gson.toJson(error);

        Debug.log(e.toString());
        byte[] byting = response.getBytes();
        try {
            exchange.sendResponseHeaders(code, byting.length);
            exchange.getResponseBody().write(byting);
        } catch (IOException error2) {
            Debug.log("Error response failed as well: " + error2);
        } finally {
            exchange.close();
        }
    }
    public static void error(HttpExchange exchange, Integer code, String message) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        String response = gson.toJson(error);
        Debug.log("Responded to "+exchange.getRemoteAddress()+" with "+code+": "+message);
        byte[] byting = response.getBytes();
        try {
            exchange.sendResponseHeaders(code, byting.length);
            exchange.getResponseBody().write(byting);
        } catch (IOException error2) {
            Debug.log("Error response failed as well: " + error2);
        } finally {
            exchange.close();
        }
    }
}