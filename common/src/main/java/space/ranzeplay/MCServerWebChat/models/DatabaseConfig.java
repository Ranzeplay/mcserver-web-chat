package space.ranzeplay.MCServerWebChat.models;

import lombok.Getter;

/**
 * Database configuration model
 */
@Getter
public class DatabaseConfig {
    private final DatabaseType type;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final int maxPoolSize;
    private final String sqliteFilePath;
    
    private DatabaseConfig(Builder builder) {
        this.type = builder.type;
        this.host = builder.host;
        this.port = builder.port;
        this.database = builder.database;
        this.username = builder.username;
        this.password = builder.password;
        this.maxPoolSize = builder.maxPoolSize;
        this.sqliteFilePath = builder.sqliteFilePath;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private DatabaseType type = DatabaseType.SQLITE;
        private String host = "localhost";
        private int port = 5432;
        private String database = "mcserver_web_chat";
        private String username = "postgres";
        private String password = "";
        private int maxPoolSize = 10;
        private String sqliteFilePath = "mcserver-web-chat.db";
        
        public Builder type(DatabaseType type) {
            this.type = type;
            return this;
        }
        
        public Builder host(String host) {
            this.host = host;
            return this;
        }
        
        public Builder port(int port) {
            this.port = port;
            return this;
        }
        
        public Builder database(String database) {
            this.database = database;
            return this;
        }
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        
        public Builder maxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
            return this;
        }
        
        public Builder sqliteFilePath(String sqliteFilePath) {
            this.sqliteFilePath = sqliteFilePath;
            return this;
        }
        
        public DatabaseConfig build() {
            return new DatabaseConfig(this);
        }
    }
}
