package space.ranzeplay.MCServerWebChat.models;

/**
 * Database type enum for supported database backends
 */
public enum DatabaseType {
    SQLITE("SQLite"),
    POSTGRESQL("PostgreSQL");
    
    private final String displayName;
    
    DatabaseType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static DatabaseType fromString(String type) {
        if (type == null) {
            return SQLITE; // Default to SQLite
        }
        
        try {
            return DatabaseType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SQLITE; // Default to SQLite if invalid
        }
    }
}
