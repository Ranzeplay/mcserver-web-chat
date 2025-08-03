package space.ranzeplay.MCServerWebChat.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.ranzeplay.MCServerWebChat.services.ChatService;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "broadcastChatMessage", at = @At("HEAD"))
    private void onBroadcastChatMessage(PlayerChatMessage message, ServerPlayer sender, Component content, CallbackInfo ci) {
        // Extract the plain text content from the chat message
        String username = sender.getName().getString();
        String messageText = message.decoratedContent().getString();
        
        // Broadcast to web clients
        ChatService.getInstance().broadcastGameMessage(username, messageText);
    }
}