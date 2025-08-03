package space.ranzeplay.MCServerWebChat.fabric;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import space.ranzeplay.MCServerWebChat.Main;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import space.ranzeplay.MCServerWebChat.handlers.PlayerChatHandler;
import space.ranzeplay.MCServerWebChat.models.InGameChatMessage;

public final class MainFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Main.init(FabricLoader.getInstance().getConfigDir());
        
        // Register server shutdown event
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            Main.shutdown();
        });

        // Set the Minecraft server instance in the main class
        ServerLifecycleEvents.SERVER_STARTING.register(Main::setMinecraftServer);

        ServerMessageEvents.CHAT_MESSAGE.register((playerChatMessage, serverPlayer, bound) -> {
            var obj = new InGameChatMessage(serverPlayer, playerChatMessage.signedContent(), playerChatMessage.decoratedContent());
            PlayerChatHandler.handlePlayerChat(obj);
        });
    }
}
