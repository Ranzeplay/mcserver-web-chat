package space.ranzeplay.MCServerWebChat.handlers;

import org.jetbrains.annotations.NotNull;
import space.ranzeplay.MCServerWebChat.models.InGameChatMessage;
import space.ranzeplay.MCServerWebChat.services.ChatService;

public class PlayerChatHandler {
    public static void handlePlayerChat(@NotNull InGameChatMessage message) {
        ChatService.getInstance().broadcastGameMessage(message.getPlayer().getName().getString(), message.getRawMessage());
    }
}
