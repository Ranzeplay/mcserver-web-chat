package space.ranzeplay.MCServerWebChat.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import space.ranzeplay.MCServerWebChat.Main;

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

    public boolean isDebugLoggingEnabled() {
        return config.has("enableDebugLogging") && config.get("enableDebugLogging").getAsBoolean();
    }
}