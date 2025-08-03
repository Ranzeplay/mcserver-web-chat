package space.ranzeplay.MCServerWebChat.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

@AllArgsConstructor
@Getter
public class InGameChatMessage {
    private ServerPlayer player;
    private String rawMessage;
    private Component message;
}
