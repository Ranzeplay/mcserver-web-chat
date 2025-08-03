package space.ranzeplay.MCServerWebChat.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MessageHistoryService {
    private static MessageHistoryService instance;
    private final List<ChatMessage> messageHistory = new ArrayList<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Gson gson = new Gson();
    private final DataPersistenceService persistenceService;
    private static final int MAX_HISTORY_SIZE = 100;

    private MessageHistoryService() {
        this.persistenceService = DataPersistenceService.getInstance();
        loadHistory();
    }

    public static synchronized MessageHistoryService getInstance() {
        if (instance == null) {
            instance = new MessageHistoryService();
        }
        return instance;
    }

    public void addMessage(String username, String message, String source) {
        lock.writeLock().lock();
        try {
            ChatMessage chatMessage = new ChatMessage(username, message, source, LocalDateTime.now());
            messageHistory.add(chatMessage);
            
            // Keep only the last MAX_HISTORY_SIZE messages
            if (messageHistory.size() > MAX_HISTORY_SIZE) {
                messageHistory.remove(0);
            }
            
            // Save to persistent storage
            saveHistory();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void sendHistoryToClient(ChannelHandlerContext ctx) {
        lock.readLock().lock();
        try {
            JsonObject response = new JsonObject();
            response.addProperty("type", "history");
            
            JsonArray messages = new JsonArray();
            for (ChatMessage msg : messageHistory) {
                String formattedMessage = formatMessage(msg);
                messages.add(formattedMessage);
            }
            
            response.add("messages", messages);
            ctx.writeAndFlush(new TextWebSocketFrame(gson.toJson(response)));
        } finally {
            lock.readLock().unlock();
        }
    }

    private String formatMessage(ChatMessage msg) {
        String sourcePrefix = "game".equals(msg.source) ? "§a[Game]" : "§b[Web]";
        String timestamp = msg.timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
        return String.format("§7[%s] %s %s§f: %s", timestamp, sourcePrefix, msg.username, msg.message);
    }

    /**
     * Load message history from persistent storage
     */
    private void loadHistory() {
        lock.writeLock().lock();
        try {
            List<ChatMessage> loadedHistory = persistenceService.loadChatHistory();
            messageHistory.clear();
            messageHistory.addAll(loadedHistory);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Save message history to persistent storage
     */
    private void saveHistory() {
        // Called within write lock from addMessage, so no additional locking needed
        persistenceService.saveChatHistory(new ArrayList<>(messageHistory));
    }

    public static class ChatMessage {
        public final String username;
        public final String message;
        public final String source; // "game" or "web"
        public final LocalDateTime timestamp;

        public ChatMessage(String username, String message, String source, LocalDateTime timestamp) {
            this.username = username;
            this.message = message;
            this.source = source;
            this.timestamp = timestamp;
        }
    }
}