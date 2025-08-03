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

    @Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"))
    private void onBroadcastChatMessage(PlayerChatMessage message, ServerPlayer sender, Component content, CallbackInfo ci) {
        try {
            // Extract the plain text content from the chat message
            String username = sender.getName().getString();
            String messageText = message.decoratedContent().getString();
            
            // Broadcast to web clients
            ChatService.getInstance().broadcastGameMessage(username, messageText);
        } catch (Exception e) {
            // Don't let mixin errors crash the game
            System.err.println("MC Web Chat: Error in chat mixin: " + e.getMessage());
            e.printStackTrace();
        }
    }
}