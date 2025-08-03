package space.ranzeplay.MCServerWebChat.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

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
        // Try to get the minecraft server directory
        try {
            net.minecraft.server.MinecraftServer server = space.ranzeplay.MCServerWebChat.Main.getMinecraftServer();
            if (server != null) {
                java.io.File serverDir = server.getFile(".");
                return serverDir.toPath().resolve("config").resolve("mcserver-web-chat");
            }
        } catch (Exception e) {
            // Fallback if minecraft server is not available
        }
        
        // Fallback to current working directory + config
        return Paths.get("config", "mcserver-web-chat");
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
        } catch (IOException e) {
            System.err.println("Failed to create config directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save user data to file
     */
    public void saveUsers(Map<String, String> users) {
        try (FileWriter writer = new FileWriter(usersFile.toFile())) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            System.err.println("Failed to save users data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load user data from file
     */
    public Map<String, String> loadUsers() {
        if (!Files.exists(usersFile)) {
            return new ConcurrentHashMap<>();
        }

        try (FileReader reader = new FileReader(usersFile.toFile())) {
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> users = gson.fromJson(reader, type);
            return users != null ? new ConcurrentHashMap<>(users) : new ConcurrentHashMap<>();
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Failed to load users data: " + e.getMessage());
            e.printStackTrace();
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Save chat history to file
     */
    public void saveChatHistory(List<MessageHistoryService.ChatMessage> messages) {
        try (FileWriter writer = new FileWriter(chatHistoryFile.toFile())) {
            gson.toJson(messages, writer);
        } catch (IOException e) {
            System.err.println("Failed to save chat history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load chat history from file
     */
    public List<MessageHistoryService.ChatMessage> loadChatHistory() {
        if (!Files.exists(chatHistoryFile)) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(chatHistoryFile.toFile())) {
            Type type = new TypeToken<List<MessageHistoryService.ChatMessage>>(){}.getType();
            List<MessageHistoryService.ChatMessage> messages = gson.fromJson(reader, type);
            return messages != null ? new ArrayList<>(messages) : new ArrayList<>();
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Failed to load chat history: " + e.getMessage());
            e.printStackTrace();
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