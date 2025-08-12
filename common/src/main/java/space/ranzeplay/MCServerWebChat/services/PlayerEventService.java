package space.ranzeplay.MCServerWebChat.services;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import space.ranzeplay.MCServerWebChat.Main;
import space.ranzeplay.MCServerWebChat.handlers.WebSocketHandler;
import space.ranzeplay.MCServerWebChat.services.I18nService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PlayerEventService {
    private static PlayerEventService instance;
    private String serverLanguage = "en-us"; // Default language, could be configurable

    private PlayerEventService() {}

    public static synchronized PlayerEventService getInstance() {
        if (instance == null) {
            instance = new PlayerEventService();
        }
        return instance;
    }

    public void setServerLanguage(String language) {
        this.serverLanguage = language;
        log.info("PlayerEventService language set to: {}", language);
    }

    public void onPlayerJoin(ServerPlayer player) {
        String username = player.getName().getString();
        log.info("Player {} joined the game", username);
        
        // Broadcast join notification to web clients
        WebSocketHandler.broadcastPlayerJoin(username);
        
        // Update player list for all clients
        List<String> playerList = getCurrentPlayerList();
        WebSocketHandler.broadcastPlayerListUpdate(playerList);
    }

    public void onPlayerLeave(ServerPlayer player) {
        String username = player.getName().getString();
        log.info("Player {} left the game", username);
        
        // Broadcast leave notification to web clients
        WebSocketHandler.broadcastPlayerLeave(username);
        
        // Update player list for all clients (after player removal)
        // We need to get the list after the player is removed, so we'll schedule it
        // for the next tick to ensure the player is properly removed from the list
        MinecraftServer server = Main.getMinecraftServer();
        if (server != null) {
            server.execute(() -> {
                List<String> playerList = getCurrentPlayerList();
                WebSocketHandler.broadcastPlayerListUpdate(playerList);
            });
        }
    }

    public List<String> getCurrentPlayerList() {
        List<String> playerNames = new ArrayList<>();
        MinecraftServer server = Main.getMinecraftServer();
        
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(player -> {
                playerNames.add(player.getName().getString());
            });
        }
        
        return playerNames;
    }

    public int getCurrentPlayerCount() {
        MinecraftServer server = Main.getMinecraftServer();
        return server != null ? server.getPlayerList().getPlayerCount() : 0;
    }
}