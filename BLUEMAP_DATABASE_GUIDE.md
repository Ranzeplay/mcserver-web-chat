# BlueMap Integration & Database Guide

This document provides detailed information about BlueMap integration and database configuration for MCServer Web Chat.

## BlueMap Integration

### Overview

MCServer Web Chat can optionally integrate with [BlueMap](https://github.com/BlueMap-Minecraft/BlueMap) to provide an enhanced experience where players can view the map and chat simultaneously.

### Features

- **Web Chat Widget**: Chat interface accessible directly from BlueMap's map viewer
- **Seamless Integration**: Chat widget overlays on top of the map interface
- **Toggle Visibility**: Easy toggle button to show/hide chat
- **Standalone Compatible**: Works perfectly fine without BlueMap installed

### Installation

#### As a Standalone Mod (Without BlueMap)

1. Download the mod JAR for your platform (Fabric or NeoForge)
2. Place in your `mods` folder
3. Start your server
4. Access web chat at `http://your-server:8080`

#### With BlueMap Integration

1. Install MCServer Web Chat as a standalone mod (steps above)
2. Install [BlueMap](https://github.com/BlueMap-Minecraft/BlueMap) if not already installed
3. Download the `mcserver-web-chat-bluemap-addon.jar`
4. Place the addon JAR in BlueMap's addons folder:
   ```
   config/bluemap/addons/mcserver-web-chat-bluemap-addon.jar
   ```
5. Restart your server
6. Open BlueMap in your browser
7. Look for the chat button (💬 Chat) in the corner
8. Click to toggle the chat widget

### How It Works

The BlueMap addon:
- Detects when BlueMap is present
- Injects a JavaScript integration script into BlueMap's web interface
- Creates a chat widget that overlays on the map
- Loads the chat UI in an iframe for security and isolation

### Configuration

The BlueMap integration uses the same web server as the standalone mod. Configure the port and other settings in `config/mcserver-web-chat/config.json`:

```json
{
  "webPort": 8080,
  "baseUrl": "http://your-server:8080"
}
```

## Database Configuration

### Supported Databases

MCServer Web Chat supports two database backends:

1. **SQLite** (default) - Simple, file-based, no setup required
2. **PostgreSQL** - Enterprise-grade, scalable, better for larger servers

### SQLite Configuration

SQLite is the default and requires no additional setup.

```json
{
  "database": {
    "type": "sqlite",
    "sqliteFilePath": "mcserver-web-chat.db"
  }
}
```

**Advantages:**
- No external database server required
- Zero configuration
- Automatic setup
- Perfect for small to medium servers

**Limitations:**
- Single file storage
- Limited concurrent write performance

### PostgreSQL Configuration

For larger servers or when you need better performance and scalability.

#### Prerequisites

1. Install PostgreSQL server
2. Create a database:
   ```sql
   CREATE DATABASE mcserver_web_chat;
   CREATE USER mcserver WITH PASSWORD 'secure_password';
   GRANT ALL PRIVILEGES ON DATABASE mcserver_web_chat TO mcserver;
   ```

#### Configuration

```json
{
  "database": {
    "type": "postgresql",
    "host": "localhost",
    "port": 5432,
    "database": "mcserver_web_chat",
    "username": "mcserver",
    "password": "secure_password",
    "maxPoolSize": 10
  }
}
```

**Advantages:**
- Better performance for high-traffic servers
- Support for multiple servers sharing one database
- Advanced features and monitoring
- Connection pooling with HikariCP
- Better concurrent access handling

**Configuration Options:**

- `host`: PostgreSQL server hostname
- `port`: PostgreSQL server port (default: 5432)
- `database`: Database name
- `username`: Database user
- `password`: Database password
- `maxPoolSize`: Maximum number of connections in the pool (default: 10)

### Security Features

Both database implementations include robust security measures:

#### SQL Injection Protection

All database queries use **prepared statements** to prevent SQL injection attacks:

```java
// Example: User input is safely parameterized
String sql = "SELECT * FROM users WHERE username = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setString(1, userInput); // Safe - input is escaped
```

#### Connection Security

**PostgreSQL:**
- HikariCP connection pooling with secure defaults
- Connection timeout: 30 seconds
- Idle timeout: 10 minutes
- Max lifetime: 30 minutes
- Prepared statement caching enabled

#### Password Storage

- User passwords are hashed with **BCrypt** before storage
- Passwords are never stored in plain text
- Database stores only the bcrypt hash

### Migration Between Databases

To migrate from SQLite to PostgreSQL:

1. Export your existing data (if needed)
2. Set up PostgreSQL and create the database
3. Update `config.json` with PostgreSQL settings
4. Restart the server
5. Tables will be automatically created

**Note**: Automatic data migration is not currently supported. If you have existing users/messages, you'll need to migrate them manually.

### Monitoring and Maintenance

#### PostgreSQL

Monitor your database with:

```sql
-- Check connection count
SELECT count(*) FROM pg_stat_activity WHERE datname = 'mcserver_web_chat';

-- Check table sizes
SELECT pg_size_pretty(pg_total_relation_size('chat_messages'));
SELECT pg_size_pretty(pg_total_relation_size('users'));
```

#### SQLite

Check database size:

```bash
ls -lh config/mcserver-web-chat/mcserver-web-chat.db
```

### Troubleshooting

#### PostgreSQL Connection Issues

**Error: "Connection refused"**
- Ensure PostgreSQL is running
- Check host and port are correct
- Verify PostgreSQL is listening on the correct interface
- Check firewall rules

**Error: "Authentication failed"**
- Verify username and password
- Check pg_hba.conf for authentication rules
- Ensure user has proper permissions

**Error: "Database does not exist"**
- Create the database first
- Ensure database name matches configuration

#### SQLite Issues

**Error: "Database is locked"**
- Another process is accessing the database
- Check for proper server shutdown
- Verify file permissions

### Best Practices

1. **Use SQLite for:**
   - Single server setups
   - Small to medium player counts (<100 concurrent)
   - Simplicity and ease of use

2. **Use PostgreSQL for:**
   - Multiple servers sharing data
   - Large player counts (>100 concurrent)
   - Need for advanced monitoring
   - Production environments with high availability requirements

3. **Security:**
   - Never commit database passwords to version control
   - Use strong, unique passwords for PostgreSQL
   - Restrict database access to localhost when possible
   - Regularly backup your database
   - Keep PostgreSQL updated with security patches

4. **Performance:**
   - Adjust `maxPoolSize` based on server load
   - Monitor connection usage
   - Configure PostgreSQL properly for your hardware
   - Use indexes for better query performance (already included)

### Database Schema

Both databases use the same schema:

```sql
-- Users table
CREATE TABLE users (
    username VARCHAR(255) PRIMARY KEY,
    hashed_password TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Chat messages table
CREATE TABLE chat_messages (
    id INTEGER/SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    source VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

-- Indexes for performance
CREATE INDEX idx_chat_messages_timestamp ON chat_messages(timestamp DESC);
```

## Support

For issues or questions:
- Check the [main README](README.md)
- Review [CONFIGURATION.md](CONFIGURATION.md)
- Open an issue on GitHub
