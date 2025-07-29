package space.ranzeplay.serverwebchat.web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import space.ranzeplay.serverwebchat.Constants;
import space.ranzeplay.serverwebchat.auth.AuthService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class WebServer {
    private HttpServer server;
    private AuthService authService;
    private static final int DEFAULT_PORT = 8080;
    
    public WebServer() {
        this.authService = new AuthService();
    }
    
    public void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api/login", new LoginHandler());
            server.setExecutor(null);
            server.start();
            Constants.LOG.info("Web server started on port {}", port);
        } catch (IOException e) {
            Constants.LOG.error("Failed to start web server", e);
        }
    }
    
    public void start() {
        start(DEFAULT_PORT);
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
            Constants.LOG.info("Web server stopped");
        }
    }
    
    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            
            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String response = authService.handleLogin(requestBody);
                int statusCode = response.contains("error") ? 400 : 200;
                sendResponse(exchange, statusCode, response);
            } catch (Exception e) {
                Constants.LOG.error("Error handling login request", e);
                sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
            }
        }
        
        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}