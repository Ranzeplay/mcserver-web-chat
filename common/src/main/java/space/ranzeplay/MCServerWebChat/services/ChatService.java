package space.ranzeplay.MCServerWebChat.services;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import space.ranzeplay.MCServerWebChat.Main;
import space.ranzeplay.MCServerWebChat.handlers.WebSocketHandler;
import space.ranzeplay.MCServerWebChat.services.I18nService;

@Slf4j
public class ChatService {
    private static ChatService instance;
    private String serverLanguage = "en-us"; // Default language, could be configurable

    private ChatService() {}

    public static synchronized ChatService getInstance() {
        if (instance == null) {
            instance = new ChatService();
        }
        return instance;
    }

    public void setServerLanguage(String language) {
        this.serverLanguage = language;
        log.info("Server language set to: {}", language);
    }

    public void sendOTPToPlayer(String username, String otp) {
        MinecraftServer server = Main.getMinecraftServer();
        if (server != null) {
            ServerPlayer player = server.getPlayerList().getPlayerByName(username);
            if (player != null) {
                I18nService i18n = I18nService.getInstance();
                String otpMessage = i18n.getMessage("otp.message", serverLanguage, otp);
                
                Component message = Component.literal("[Web Chat] " + otpMessage).withStyle(ChatFormatting.YELLOW);
                player.sendSystemMessage(message);
                log.info("Sent OTP to player {} in language {}", username, serverLanguage);
            } else {
                log.warn("Player {} not found when trying to send OTP", username);
            }
        } else {
            log.warn("Minecraft server not available when trying to send OTP to {}", username);
        }
    }

    public void broadcastWebMessage(String username, String message, String messageId) {
        // Broadcast to in-game players
        MinecraftServer server = Main.getMinecraftServer();
        if (server != null) {
            Component chatMessage = Component.empty()
                    .append(Component.literal("<"))
                    .append(Component.literal(username).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
                    .append(Component.literal("> "))
                    .append(Component.literal(message));
            server.getPlayerList().broadcastSystemMessage(chatMessage, false);
            log.debug("Broadcasted web message from {} to in-game players", username);
        }
        
        // Also broadcast to other web clients with message ID
        WebSocketHandler.broadcastToWebClients("§b[Web] " + username + "§f: " + message, messageId);
        log.debug("Broadcasted web message from {} to web clients", username);
        
        // Store in history
        MessageHistoryService.getInstance().addMessage(username, message, "web");
    }

    // Overloaded method for backward compatibility
    public void broadcastWebMessage(String username, String message) {
        broadcastWebMessage(username, message, null);
    }

    public void broadcastGameMessage(String username, String message) {
        // Broadcast to web clients
        WebSocketHandler.broadcastToWebClients("§a[Game] " + username + "§f: " + message);
        log.debug("Broadcasted game message from {} to web clients", username);
        
        // Store in history
        MessageHistoryService.getInstance().addMessage(username, message, "game");
    }
}