package space.ranzeplay.serverwebchat.config;

import space.ranzeplay.serverwebchat.Constants;
import space.ranzeplay.serverwebchat.util.SimpleJson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WebServerConfig {
    private static final String CONFIG_FILE = "config/serverwebchat-config.json";
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final boolean DEFAULT_ENABLED = true;
    
    private int port;
    private String host;
    private boolean enabled;
    
    public WebServerConfig() {
        this.port = DEFAULT_PORT;
        this.host = DEFAULT_HOST;
        this.enabled = DEFAULT_ENABLED;
    }
    
    // Getters and setters
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    // File operations
    public void save() {
        try {
            Path configDir = Paths.get("config");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("port", port);
            data.put("host", host);
            data.put("enabled", enabled);
            
            String json = SimpleJson.createObject(data);
            
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                writer.write(json);
            }
            Constants.LOG.info("Saved web server configuration");
        } catch (IOException e) {
            Constants.LOG.error("Failed to save web server configuration", e);
        }
    }
    
    public static WebServerConfig load() {
        try {
            File file = new File(CONFIG_FILE);
            if (!file.exists()) {
                WebServerConfig defaultConfig = new WebServerConfig();
                defaultConfig.save();
                return defaultConfig;
            }
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            }
            
            Map<String, String> data = SimpleJson.parseObject(content.toString());
            WebServerConfig config = new WebServerConfig();
            
            if (data.containsKey("port")) {
                try {
                    config.setPort(Integer.parseInt(data.get("port")));
                } catch (NumberFormatException e) {
                    Constants.LOG.warn("Invalid port in config, using default: {}", DEFAULT_PORT);
                }
            }
            
            if (data.containsKey("host")) {
                config.setHost(data.get("host"));
            }
            
            if (data.containsKey("enabled")) {
                config.setEnabled(Boolean.parseBoolean(data.get("enabled")));
            }
            
            Constants.LOG.info("Loaded web server configuration");
            return config;
        } catch (Exception e) {
            Constants.LOG.error("Failed to load web server configuration, using defaults", e);
            return new WebServerConfig();
        }
    }
}