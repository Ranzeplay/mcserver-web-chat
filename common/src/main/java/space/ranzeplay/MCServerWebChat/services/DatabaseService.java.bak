package space.ranzeplay.MCServerWebChat.services;

import lombok.extern.slf4j.Slf4j;
import space.ranzeplay.MCServerWebChat.Main;

import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for SQLite database operations for users and chat messages
 */
@Slf4j
public class DatabaseService {
    private static DatabaseService instance;
    private final String databaseUrl;
    private static final String DB_FILE_NAME = "mcserver-web-chat.db";

    private DatabaseService() {
        Path configDir = Main.getRootConfigDir().resolve("mcserver-web-chat");
        Path dbPath = configDir.resolve(DB_FILE_NAME);
        this.databaseUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        
        log.info("Database will be located at: {}", dbPath.toAbsolutePath());
        initializeDatabase();
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            // Create database directory if it doesn't exist
            createConfigDirectory();
            
            // Create tables
            createTables();
            log.info("Database initialized successfully");
        } catch (SQLException e) {
            log.error("Failed to initialize database: {}", e.getMessage(), e);
        }
    }

    private void createConfigDirectory() {
        try {
            Path configDir = Main.getRootConfigDir().resolve("mcserver-web-chat");
            java.nio.file.Files.createDirectories(configDir);
            log.debug("Created config directory: {}", configDir);
        } catch (Exception e) {
            log.error("Failed to create config directory: {}", e.getMessage(), e);
        }
    }

    private void createTables() throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                hashed_password TEXT NOT NULL,
                created_at TEXT NOT NULL
            )
        """;

        String createChatMessagesTable = """
            CREATE TABLE IF NOT EXISTS chat_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                message TEXT NOT NULL,
                source TEXT NOT NULL,
                timestamp TEXT NOT NULL
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createUsersTable);
            stmt.execute(createChatMessagesTable);
            log.debug("Database tables created/verified");
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    // User operations
    public void saveUser(String username, String hashedPassword) {
        String sql = "INSERT OR REPLACE INTO users (username, hashed_password, created_at) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            pstmt.executeUpdate();
            log.debug("Saved user: {}", username);
        } catch (SQLException e) {
            log.error("Failed to save user {}: {}", username, e.getMessage(), e);
        }
    }

    public Map<String, String> loadAllUsers() {
        Map<String, String> users = new ConcurrentHashMap<>();
        String sql = "SELECT username, hashed_password FROM users";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.put(rs.getString("username"), rs.getString("hashed_password"));
            }
            
            log.debug("Loaded {} users from database", users.size());
        } catch (SQLException e) {
            log.error("Failed to load users: {}", e.getMessage(), e);
        }
        
        return users;
    }

    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("Failed to check if user exists {}: {}", username, e.getMessage(), e);
            return false;
        }
    }

    public String getUserHashedPassword(String username) {
        String sql = "SELECT hashed_password FROM users WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("hashed_password");
                }
            }
        } catch (SQLException e) {
            log.error("Failed to get hashed password for user {}: {}", username, e.getMessage(), e);
        }
        
        return null;
    }

    // Chat message operations
    public void saveChatMessage(String username, String message, String source, LocalDateTime timestamp) {
        String sql = "INSERT INTO chat_messages (username, message, source, timestamp) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, message);
            pstmt.setString(3, source);
            pstmt.setString(4, timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            pstmt.executeUpdate();
            log.debug("Saved chat message from {}: {}", username, message);
        } catch (SQLException e) {
            log.error("Failed to save chat message: {}", e.getMessage(), e);
        }
    }

    public List<MessageHistoryService.ChatMessage> loadRecentChatMessages(int limit) {
        List<MessageHistoryService.ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT username, message, source, timestamp FROM chat_messages ORDER BY id DESC LIMIT ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    String message = rs.getString("message");
                    String source = rs.getString("source");
                    LocalDateTime timestamp = LocalDateTime.parse(rs.getString("timestamp"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    
                    messages.add(new MessageHistoryService.ChatMessage(username, message, source, timestamp));
                }
            }
            
            // Reverse the list since we want oldest first but queried newest first
            java.util.Collections.reverse(messages);
            log.debug("Loaded {} chat messages from database", messages.size());
        } catch (SQLException e) {
            log.error("Failed to load chat messages: {}", e.getMessage(), e);
        }
        
        return messages;
    }

    public void cleanupOldMessages(int keepCount) {
        String sql = """
            DELETE FROM chat_messages 
            WHERE id NOT IN (
                SELECT id FROM chat_messages 
                ORDER BY id DESC 
                LIMIT ?
            )
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, keepCount);
            
            int deletedCount = pstmt.executeUpdate();
            if (deletedCount > 0) {
                log.debug("Cleaned up {} old chat messages", deletedCount);
            }
        } catch (SQLException e) {
            log.error("Failed to cleanup old messages: {}", e.getMessage(), e);
        }
    }
}