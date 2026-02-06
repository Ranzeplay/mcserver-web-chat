package space.ranzeplay.MCServerWebChat.services;

import space.ranzeplay.MCServerWebChat.services.MessageHistoryService.ChatMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface for database operations supporting multiple database backends
 */
public interface IDatabaseService {
    
    /**
     * Save a user to the database
     * @param username The username (input is validated to prevent SQL injection)
     * @param hashedPassword The bcrypt hashed password
     */
    void saveUser(String username, String hashedPassword);
    
    /**
     * Load all users from the database
     * @return Map of username to hashed password
     */
    Map<String, String> loadAllUsers();
    
    /**
     * Check if a user exists in the database
     * @param username The username to check (input is validated)
     * @return true if the user exists
     */
    boolean userExists(String username);
    
    /**
     * Get the hashed password for a user
     * @param username The username (input is validated)
     * @return The hashed password or null if user doesn't exist
     */
    String getUserHashedPassword(String username);
    
    /**
     * Save a chat message to the database
     * @param username The username (input is sanitized)
     * @param message The message content (input is sanitized)
     * @param source The message source (web or game)
     * @param timestamp The timestamp of the message
     */
    void saveChatMessage(String username, String message, String source, LocalDateTime timestamp);
    
    /**
     * Load recent chat messages from the database
     * @param limit The maximum number of messages to load
     * @return List of chat messages ordered by timestamp
     */
    List<ChatMessage> loadRecentChatMessages(int limit);
    
    /**
     * Delete old chat messages, keeping only the most recent ones
     * @param keepCount The number of messages to keep
     */
    void cleanupOldMessages(int keepCount);
    
    /**
     * Close database connections and cleanup resources
     */
    void shutdown();
}
