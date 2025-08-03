package space.ranzeplay.MCServerWebChat.fabric;

import space.ranzeplay.MCServerWebChat.Main;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class MainFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Main.init();
        
        // Register server shutdown event
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            Main.shutdown();
        });
    }
}
