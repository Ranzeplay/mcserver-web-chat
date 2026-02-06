package space.ranzeplay.MCServerWebChat.services.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import space.ranzeplay.MCServerWebChat.models.DatabaseConfig;
import space.ranzeplay.MCServerWebChat.services.IDatabaseService;
import space.ranzeplay.MCServerWebChat.services.MessageHistoryService;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PostgreSQL implementation of database operations with connection pooling
 * Uses HikariCP for efficient connection management
 * All queries use prepared statements to prevent SQL injection attacks
 */
@Slf4j
public class PostgresqlDatabaseService implements IDatabaseService {
    private final HikariDataSource dataSource;

    public PostgresqlDatabaseService(DatabaseConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s",
            config.getHost(), config.getPort(), config.getDatabase());
        
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        
        // Security and performance settings
        hikariConfig.setConnectionTimeout(30000); // 30 seconds
        hikariConfig.setIdleTimeout(600000); // 10 minutes
        hikariConfig.setMaxLifetime(1800000); // 30 minutes
        hikariConfig.setAutoCommit(true);
        
        this.dataSource = new HikariDataSource(hikariConfig);
        
        log.info("PostgreSQL connection pool initialized: {}:{}/{}", 
            config.getHost(), config.getPort(), config.getDatabase());
        
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            createTables();
            log.info("PostgreSQL database initialized successfully");
        } catch (SQLException e) {
            log.error("Failed to initialize PostgreSQL database: {}", e.getMessage(), e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void createTables() throws SQLException {
        // PostgreSQL uses SERIAL for auto-increment and TIMESTAMP for datetime
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(255) PRIMARY KEY,
                hashed_password TEXT NOT NULL,
                created_at TIMESTAMP NOT NULL
            )
        """;

        String createChatMessagesTable = """
            CREATE TABLE IF NOT EXISTS chat_messages (
                id SERIAL PRIMARY KEY,
                username VARCHAR(255) NOT NULL,
                message TEXT NOT NULL,
                source VARCHAR(50) NOT NULL,
                timestamp TIMESTAMP NOT NULL
            )
        """;
        
        // Create index for better query performance
        String createMessageIndexSql = """
            CREATE INDEX IF NOT EXISTS idx_chat_messages_timestamp 
            ON chat_messages(timestamp DESC)
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createUsersTable);
            stmt.execute(createChatMessagesTable);
            stmt.execute(createMessageIndexSql);
            log.debug("PostgreSQL database tables created/verified");
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // User operations - All use prepared statements to prevent SQL injection
    @Override
    public void saveUser(String username, String hashedPassword) {
        // PostgreSQL uses ON CONFLICT for upsert operations
        String sql = """
            INSERT INTO users (username, hashed_password, created_at) 
            VALUES (?, ?, ?) 
            ON CONFLICT (username) 
            DO UPDATE SET hashed_password = EXCLUDED.hashed_password
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            
            pstmt.executeUpdate();
            log.debug("Saved user: {}", username);
        } catch (SQLException e) {
            log.error("Failed to save user {}: {}", username, e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> loadAllUsers() {
        Map<String, String> users = new ConcurrentHashMap<>();
        String sql = "SELECT username, hashed_password FROM users";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.put(rs.getString("username"), rs.getString("hashed_password"));
            }
            
            log.debug("Loaded {} users from PostgreSQL database", users.size());
        } catch (SQLException e) {
            log.error("Failed to load users: {}", e.getMessage(), e);
        }
        
        return users;
    }

    @Override
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

    @Override
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

    // Chat message operations - All use prepared statements to prevent SQL injection
    @Override
    public void saveChatMessage(String username, String message, String source, LocalDateTime timestamp) {
        String sql = "INSERT INTO chat_messages (username, message, source, timestamp) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, message);
            pstmt.setString(3, source);
            pstmt.setTimestamp(4, Timestamp.valueOf(timestamp));
            
            pstmt.executeUpdate();
            log.debug("Saved chat message from {}: {}", username, message);
        } catch (SQLException e) {
            log.error("Failed to save chat message: {}", e.getMessage(), e);
        }
    }

    @Override
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
                    LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                    
                    messages.add(new MessageHistoryService.ChatMessage(username, message, source, timestamp));
                }
            }
            
            // Reverse the list since we want oldest first but queried newest first
            java.util.Collections.reverse(messages);
            log.debug("Loaded {} chat messages from PostgreSQL database", messages.size());
        } catch (SQLException e) {
            log.error("Failed to load chat messages: {}", e.getMessage(), e);
        }
        
        return messages;
    }

    @Override
    public void cleanupOldMessages(int keepCount) {
        // PostgreSQL supports subqueries in DELETE
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

    @Override
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("PostgreSQL connection pool closed");
        }
    }
}
