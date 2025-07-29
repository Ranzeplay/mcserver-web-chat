package space.ranzeplay.serverwebchat.web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import space.ranzeplay.serverwebchat.Constants;
import space.ranzeplay.serverwebchat.auth.AuthService;
import space.ranzeplay.serverwebchat.config.WebServerConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class WebServer {
    private HttpServer server;
    private AuthService authService;
    private WebServerConfig config;
    
    public WebServer() {
        this.authService = new AuthService();
        this.config = WebServerConfig.load();
    }
    
    public void start() {
        if (!config.isEnabled()) {
            Constants.LOG.info("Web server is disabled in configuration");
            return;
        }
        
        try {
            server = HttpServer.create(new InetSocketAddress(config.getHost(), config.getPort()), 0);
            server.createContext("/api/login", new LoginHandler());
            server.setExecutor(null);
            server.start();
            Constants.LOG.info("Web server started on {}:{}", config.getHost(), config.getPort());
        } catch (IOException e) {
            Constants.LOG.error("Failed to start web server", e);
        }
    }
    
    public void start(int port) {
        config.setPort(port);
        start();
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
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendCorsHeaders(exchange);
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
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
        
        private void sendCorsHeaders(HttpExchange exchange) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        }
        
        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendCorsHeaders(exchange);
            
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}