package space.ranzeplay.MCServerWebChat.services;

import lombok.extern.slf4j.Slf4j;
import space.ranzeplay.MCServerWebChat.Main;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class I18nService {
    private static I18nService instance;
    private final Map<String, Properties> languages = new HashMap<>();
    private final String defaultLanguage = "en-us";

    private I18nService() {
        loadLanguages();
    }

    public static synchronized I18nService getInstance() {
        if (instance == null) {
            instance = new I18nService();
        }
        return instance;
    }

    private void loadLanguages() {
        loadLanguage("en-us");
        loadLanguage("zh-cn");
    }

    private void loadLanguage(String languageCode) {
        Properties props = new Properties();
        
        // Try to load from external config directory first
        Path configPath = Main.getRootConfigDir().resolve("mcserver-web-chat").resolve("lang").resolve(languageCode + ".properties");
        if (Files.exists(configPath)) {
            try (InputStream is = Files.newInputStream(configPath)) {
                props.load(is);
                languages.put(languageCode, props);
                log.info("Loaded language {} from external config", languageCode);
                return;
            } catch (IOException e) {
                log.warn("Failed to load language {} from external config: {}", languageCode, e.getMessage());
            }
        }
        
        // Fall back to built-in resources
        try (InputStream is = getClass().getResourceAsStream("/lang/" + languageCode + ".properties")) {
            if (is != null) {
                props.load(is);
                languages.put(languageCode, props);
                log.info("Loaded built-in language {}", languageCode);
            } else {
                log.warn("Language file not found for {}", languageCode);
                // Create default messages for this language
                createDefaultMessages(languageCode, props);
                languages.put(languageCode, props);
            }
        } catch (IOException e) {
            log.error("Failed to load language {}: {}", languageCode, e.getMessage());
            // Create default messages as fallback
            createDefaultMessages(languageCode, props);
            languages.put(languageCode, props);
        }
    }

    private void createDefaultMessages(String languageCode, Properties props) {
        if ("zh-cn".equals(languageCode)) {
            // Chinese messages
            props.setProperty("otp.message", "您的网页聊天验证码是: %s。请在网页端输入此验证码以完成注册。");
            props.setProperty("player.join", "%s 加入了游戏");
            props.setProperty("player.leave", "%s 离开了游戏");
            props.setProperty("web.prefix", "[网页]");
        } else {
            // English messages (default)
            props.setProperty("otp.message", "Your web chat OTP code is: %s. Enter this code in the web browser to complete registration.");
            props.setProperty("player.join", "%s joined the game");
            props.setProperty("player.leave", "%s left the game");
            props.setProperty("web.prefix", "[Web]");
        }
    }

    public String getMessage(String key, String languageCode) {
        Properties props = languages.get(languageCode);
        if (props == null) {
            props = languages.get(defaultLanguage);
        }
        
        if (props != null && props.containsKey(key)) {
            return props.getProperty(key);
        }
        
        // Fallback to key if message not found
        log.warn("Message key '{}' not found for language '{}'", key, languageCode);
        return key;
    }

    public String getMessage(String key, String languageCode, Object... args) {
        String message = getMessage(key, languageCode);
        try {
            return String.format(message, args);
        } catch (Exception e) {
            log.warn("Failed to format message '{}' with args: {}", message, e.getMessage());
            return message;
        }
    }

    public String getMessage(String key) {
        return getMessage(key, defaultLanguage);
    }

    public String getMessage(String key, Object... args) {
        return getMessage(key, defaultLanguage, args);
    }
}