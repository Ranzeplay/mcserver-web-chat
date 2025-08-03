package space.ranzeplay.MCServerWebChat.services;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import space.ranzeplay.MCServerWebChat.Main;
import space.ranzeplay.MCServerWebChat.handlers.WebSocketHandler;

public class ChatService {
    private static ChatService instance;

    private ChatService() {}

    public static synchronized ChatService getInstance() {
        if (instance == null) {
            instance = new ChatService();
        }
        return instance;
    }

    public void sendOTPToPlayer(String username, String otp) {
        MinecraftServer server = Main.getMinecraftServer();
        if (server != null) {
            ServerPlayer player = server.getPlayerList().getPlayerByName(username);
            if (player != null) {
                Component message = Component.literal("§6[Web Chat] Your OTP code is: §e" + otp + "§6. Enter this code in the web browser to complete registration.");
                player.sendSystemMessage(message);
            }
        }
    }

    public void broadcastWebMessage(String username, String message) {
        // Broadcast to in-game players
        MinecraftServer server = Main.getMinecraftServer();
        if (server != null) {
            Component chatMessage = Component.literal("§b[Web] " + username + "§f: " + message);
            server.getPlayerList().broadcastSystemMessage(chatMessage, false);
        }
        
        // Also broadcast to other web clients
        WebSocketHandler.broadcastToWebClients("§b[Web] " + username + "§f: " + message);
    }

    public void broadcastGameMessage(String username, String message) {
        // Broadcast to web clients
        WebSocketHandler.broadcastToWebClients("§a[Game] " + username + "§f: " + message);
        
        // Store in history
        MessageHistoryService.getInstance().addMessage(username, message, "game");
    }
}