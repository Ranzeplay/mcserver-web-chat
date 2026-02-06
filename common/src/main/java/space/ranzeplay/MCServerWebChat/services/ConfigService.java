package space.ranzeplay.MCServerWebChat.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import space.ranzeplay.MCServerWebChat.Main;
import space.ranzeplay.MCServerWebChat.models.DatabaseConfig;
import space.ranzeplay.MCServerWebChat.models.DatabaseType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class ConfigService {
    private static ConfigService instance;
    private final Gson gson = new Gson();
    private final Path configFile;
    private JsonObject config;

    private ConfigService() {
        Path configDir = Main.getRootConfigDir().resolve("mcserver-web-chat");
        this.configFile = configDir.resolve("config.json");
        loadConfig();
    }

    public static synchronized ConfigService getInstance() {
        if (instance == null) {
            instance = new ConfigService();
        }
        return instance;
    }

    private void loadConfig() {
        try {
            // Create config directory if it doesn't exist
            Files.createDirectories(configFile.getParent());
            
            if (Files.exists(configFile)) {
                String content = Files.readString(configFile);
                config = gson.fromJson(content, JsonObject.class);
                log.info("Loaded configuration from {}", configFile);
            } else {
                config = createDefaultConfig();
                saveConfig();
                log.info("Created default configuration at {}", configFile);
            }
        } catch (Exception e) {
            log.error("Failed to load configuration: {}", e.getMessage());
            config = createDefaultConfig();
        }
    }

    private JsonObject createDefaultConfig() {
        JsonObject defaultConfig = new JsonObject();
        defaultConfig.addProperty("language", "en-us");
        defaultConfig.addProperty("webPort", 8080);
        defaultConfig.addProperty("enableDebugLogging", false);
        defaultConfig.addProperty("baseUrl", "http://localhost:8080");
        defaultConfig.addProperty("websocketPath", "/ws");
        defaultConfig.addProperty("serverName", "MCServer Web Chat");
        defaultConfig.addProperty("enableCors", true);
        defaultConfig.addProperty("staticResourcePath", "/static");
        
        // BlueMap integration configuration
        defaultConfig.addProperty("enableBlueMapIntegration", true);
        
        // Database configuration
        JsonObject dbConfig = new JsonObject();
        dbConfig.addProperty("type", "sqlite"); // sqlite or postgresql
        dbConfig.addProperty("host", "localhost");
        dbConfig.addProperty("port", 5432);
        dbConfig.addProperty("database", "mcserver_web_chat");
        dbConfig.addProperty("username", "postgres");
        dbConfig.addProperty("password", "");
        dbConfig.addProperty("maxPoolSize", 10);
        dbConfig.addProperty("sqliteFilePath", "mcserver-web-chat.db");
        defaultConfig.add("database", dbConfig);
        
        return defaultConfig;
    }

    private void saveConfig() {
        try {
            String content = gson.toJson(config);
            Files.writeString(configFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.debug("Saved configuration to {}", configFile);
        } catch (IOException e) {
            log.error("Failed to save configuration: {}", e.getMessage());
        }
    }

    public String getLanguage() {
        return config.has("language") ? config.get("language").getAsString() : "en-us";
    }

    public void setLanguage(String language) {
        config.addProperty("language", language);
        saveConfig();
        
        // Update services with new language
        ChatService.getInstance().setServerLanguage(language);
        PlayerEventService.getInstance().setServerLanguage(language);
        
        log.info("Server language changed to: {}", language);
    }

    public int getWebPort() {
        return config.has("webPort") ? config.get("webPort").getAsInt() : 8080;
    }

    public void setWebPort(int port) {
        config.addProperty("webPort", port);
        saveConfig();
        log.info("Web port changed to: {}", port);
    }

    public String getBaseUrl() {
        return config.has("baseUrl") ? config.get("baseUrl").getAsString() : "http://localhost:8080";
    }

    public void setBaseUrl(String baseUrl) {
        config.addProperty("baseUrl", baseUrl);
        saveConfig();
        log.info("Base URL changed to: {}", baseUrl);
    }

    public String getWebsocketPath() {
        return config.has("websocketPath") ? config.get("websocketPath").getAsString() : "/ws";
    }

    public void setWebsocketPath(String path) {
        config.addProperty("websocketPath", path);
        saveConfig();
        log.info("WebSocket path changed to: {}", path);
    }

    public String getServerName() {
        return config.has("serverName") ? config.get("serverName").getAsString() : "MCServer Web Chat";
    }

    public void setServerName(String name) {
        config.addProperty("serverName", name);
        saveConfig();
        log.info("Server name changed to: {}", name);
    }

    public boolean isCorsEnabled() {
        return config.has("enableCors") ? config.get("enableCors").getAsBoolean() : true;
    }

    public void setCorsEnabled(boolean enabled) {
        config.addProperty("enableCors", enabled);
        saveConfig();
        log.info("CORS enabled changed to: {}", enabled);
    }

    public String getStaticResourcePath() {
        return config.has("staticResourcePath") ? config.get("staticResourcePath").getAsString() : "/static";
    }

    public void setStaticResourcePath(String path) {
        config.addProperty("staticResourcePath", path);
        saveConfig();
        log.info("Static resource path changed to: {}", path);
    }

    public boolean isDebugLoggingEnabled() {
        return config.has("enableDebugLogging") && config.get("enableDebugLogging").getAsBoolean();
    }
    
    public boolean isBlueMapIntegrationEnabled() {
        return config.has("enableBlueMapIntegration") ? config.get("enableBlueMapIntegration").getAsBoolean() : true;
    }
    
    public void setBlueMapIntegrationEnabled(boolean enabled) {
        config.addProperty("enableBlueMapIntegration", enabled);
        saveConfig();
        log.info("BlueMap integration changed to: {}", enabled);
    }
    
    public DatabaseConfig getDatabaseConfig() {
        if (!config.has("database")) {
            // Return default SQLite config if not configured
            return DatabaseConfig.builder().build();
        }
        
        JsonObject dbConfig = config.getAsJsonObject("database");
        DatabaseType type = DatabaseType.fromString(
            dbConfig.has("type") ? dbConfig.get("type").getAsString() : "sqlite"
        );
        
        DatabaseConfig.Builder builder = DatabaseConfig.builder()
            .type(type);
        
        if (type == DatabaseType.POSTGRESQL) {
            if (dbConfig.has("host")) builder.host(dbConfig.get("host").getAsString());
            if (dbConfig.has("port")) builder.port(dbConfig.get("port").getAsInt());
            if (dbConfig.has("database")) builder.database(dbConfig.get("database").getAsString());
            if (dbConfig.has("username")) builder.username(dbConfig.get("username").getAsString());
            if (dbConfig.has("password")) builder.password(dbConfig.get("password").getAsString());
            if (dbConfig.has("maxPoolSize")) builder.maxPoolSize(dbConfig.get("maxPoolSize").getAsInt());
        } else {
            if (dbConfig.has("sqliteFilePath")) {
                builder.sqliteFilePath(dbConfig.get("sqliteFilePath").getAsString());
            }
        }
        
        return builder.build();
    }
}