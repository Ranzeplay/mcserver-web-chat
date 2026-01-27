package space.ranzeplay.MCServerWebChat.services;

import lombok.extern.slf4j.Slf4j;
import space.ranzeplay.MCServerWebChat.models.DatabaseConfig;
import space.ranzeplay.MCServerWebChat.models.DatabaseType;
import space.ranzeplay.MCServerWebChat.services.database.PostgresqlDatabaseService;
import space.ranzeplay.MCServerWebChat.services.database.SqliteDatabaseService;

/**
 * Factory service for database operations
 * Supports SQLite and PostgreSQL backends with automatic selection based on configuration
 */
@Slf4j
public class DatabaseService {
    private static DatabaseService instance;
    private final IDatabaseService databaseImpl;
    
    private DatabaseService() {
        DatabaseConfig config = ConfigService.getInstance().getDatabaseConfig();
        
        log.info("Initializing database service with type: {}", config.getType().getDisplayName());
        
        if (config.getType() == DatabaseType.POSTGRESQL) {
            log.info("Using PostgreSQL database at {}:{}/{}", 
                config.getHost(), config.getPort(), config.getDatabase());
            databaseImpl = new PostgresqlDatabaseService(config);
        } else {
            log.info("Using SQLite database at {}", config.getSqliteFilePath());
            databaseImpl = new SqliteDatabaseService(config);
        }
    }
    
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }
    
    // Delegate all methods to the implementation
    
    public void saveUser(String username, String hashedPassword) {
        databaseImpl.saveUser(username, hashedPassword);
    }
    
    public java.util.Map<String, String> loadAllUsers() {
        return databaseImpl.loadAllUsers();
    }
    
    public boolean userExists(String username) {
        return databaseImpl.userExists(username);
    }
    
    public String getUserHashedPassword(String username) {
        return databaseImpl.getUserHashedPassword(username);
    }
    
    public void saveChatMessage(String username, String message, String source, java.time.LocalDateTime timestamp) {
        databaseImpl.saveChatMessage(username, message, source, timestamp);
    }
    
    public java.util.List<MessageHistoryService.ChatMessage> loadRecentChatMessages(int limit) {
        return databaseImpl.loadRecentChatMessages(limit);
    }
    
    public void cleanupOldMessages(int keepCount) {
        databaseImpl.cleanupOldMessages(keepCount);
    }
    
    public void shutdown() {
        if (databaseImpl != null) {
            databaseImpl.shutdown();
        }
    }
}
