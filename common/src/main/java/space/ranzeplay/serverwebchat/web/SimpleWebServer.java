package space.ranzeplay.serverwebchat.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import space.ranzeplay.serverwebchat.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SimpleWebServer {
    private HttpServer server;
    private static final int PORT = 8080;
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    
    static {
        MIME_TYPES.put(".html", "text/html");
        MIME_TYPES.put(".css", "text/css");
        MIME_TYPES.put(".js", "application/javascript");
        MIME_TYPES.put(".json", "application/json");
        MIME_TYPES.put(".svg", "image/svg+xml");
        MIME_TYPES.put(".png", "image/png");
        MIME_TYPES.put(".ico", "image/x-icon");
    }
    
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", new StaticFileHandler());
            server.setExecutor(null);
            server.start();
            Constants.LOG.info("Web server started on port {}", PORT);
        } catch (IOException e) {
            Constants.LOG.error("Failed to start web server", e);
        }
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
            Constants.LOG.info("Web server stopped");
        }
    }
    
    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Default to index.html for root path
            if ("/".equals(path)) {
                path = "/index.html";
            }
            
            // Try to load the resource from the web directory
            String resourcePath = "/web" + path;
            InputStream resourceStream = getClass().getResourceAsStream(resourcePath);
            
            if (resourceStream != null) {
                try {
                    // Determine content type
                    String contentType = getContentType(path);
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    
                    // Read the resource
                    byte[] content = resourceStream.readAllBytes();
                    
                    // Send response
                    exchange.sendResponseHeaders(200, content.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(content);
                    }
                } finally {
                    resourceStream.close();
                }
            } else {
                // Resource not found
                String response = "File not found: " + path;
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        
        private String getContentType(String path) {
            int lastDot = path.lastIndexOf('.');
            if (lastDot != -1) {
                String extension = path.substring(lastDot);
                return MIME_TYPES.getOrDefault(extension, "application/octet-stream");
            }
            return "application/octet-stream";
        }
    }
}