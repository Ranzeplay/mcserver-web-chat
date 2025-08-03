package space.ranzeplay.MCServerWebChat.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.MinecraftServer;
import space.ranzeplay.MCServerWebChat.Main;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

/**
 * Service for persisting data to the config/mcserver-web-chat directory
 */
@Slf4j
public class DataPersistenceService {
    private static DataPersistenceService instance;
    private final Gson gson;
    private final Path configDir;
    private final Path usersFile;
    private final Path chatHistoryFile;

    private DataPersistenceService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Create config directory relative to game directory
        this.configDir = getConfigDirectory();
        this.usersFile = configDir.resolve("users.json");
        this.chatHistoryFile = configDir.resolve("chat_history.json");
        
        createConfigDirectory();
    }

    private Path getConfigDirectory() {
        return Main.getRootConfigDir().resolve("mcserver-web-chat");
    }

    public static synchronized DataPersistenceService getInstance() {
        if (instance == null) {
            instance = new DataPersistenceService();
        }
        return instance;
    }

    private void createConfigDirectory() {
        try {
            Files.createDirectories(configDir);
            log.debug("Created config directory: {}", configDir);
        } catch (IOException e) {
            log.error("Failed to create config directory: {}", e.getMessage(), e);
        }
    }

    /**
     * Save user data to file
     */
    public void saveUsers(Map<String, String> users) {
        try (FileWriter writer = new FileWriter(usersFile.toFile())) {
            gson.toJson(users, writer);
            log.debug("Saved {} users to file", users.size());
        } catch (IOException e) {
            log.error("Failed to save users data: {}", e.getMessage(), e);
        }
    }

    /**
     * Load user data from file
     */
    public Map<String, String> loadUsers() {
        if (!Files.exists(usersFile)) {
            log.debug("Users file does not exist, returning empty map");
            return new ConcurrentHashMap<>();
        }

        try (FileReader reader = new FileReader(usersFile.toFile())) {
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> users = gson.fromJson(reader, type);
            Map<String, String> result = users != null ? new ConcurrentHashMap<>(users) : new ConcurrentHashMap<>();
            log.debug("Loaded {} users from file", result.size());
            return result;
        } catch (IOException | JsonSyntaxException e) {
            log.error("Failed to load users data: {}", e.getMessage(), e);
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Save chat history to file
     */
    public void saveChatHistory(List<MessageHistoryService.ChatMessage> messages) {
        try (FileWriter writer = new FileWriter(chatHistoryFile.toFile())) {
            gson.toJson(messages, writer);
            log.debug("Saved {} chat messages to file", messages.size());
        } catch (IOException e) {
            log.error("Failed to save chat history: {}", e.getMessage(), e);
        }
    }

    /**
     * Load chat history from file
     */
    public List<MessageHistoryService.ChatMessage> loadChatHistory() {
        if (!Files.exists(chatHistoryFile)) {
            log.debug("Chat history file does not exist, returning empty list");
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(chatHistoryFile.toFile())) {
            Type type = new TypeToken<List<MessageHistoryService.ChatMessage>>(){}.getType();
            List<MessageHistoryService.ChatMessage> messages = gson.fromJson(reader, type);
            List<MessageHistoryService.ChatMessage> result = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
            log.debug("Loaded {} chat messages from file", result.size());
            return result;
        } catch (IOException | JsonSyntaxException e) {
            log.error("Failed to load chat history: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get the config directory path
     */
    public Path getConfigDir() {
        return configDir;
    }
}