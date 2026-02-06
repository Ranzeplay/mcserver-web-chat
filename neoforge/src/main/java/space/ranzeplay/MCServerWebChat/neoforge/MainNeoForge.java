package space.ranzeplay.MCServerWebChat.neoforge;

import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import space.ranzeplay.MCServerWebChat.Main;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.common.NeoForge;
import space.ranzeplay.MCServerWebChat.bluemap.BlueMapWebChatAddon;
import space.ranzeplay.MCServerWebChat.handlers.PlayerChatHandler;
import space.ranzeplay.MCServerWebChat.models.InGameChatMessage;

@Mod(Main.MOD_ID)
public final class MainNeoForge {
    public MainNeoForge() {
        // Run our common setup.
        Main.init(FMLPaths.CONFIGDIR.get());
        
        // Initialize BlueMap addon if BlueMap is available
        try {
            Class.forName("de.bluecolored.bluemap.api.BlueMapAPI");
            new BlueMapWebChatAddon();
        } catch (ClassNotFoundException e) {
            // BlueMap not available, skip integration
        }
        
        // Register shutdown event
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        // Register server starting event
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
    }
    
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        Main.shutdown();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Set the Minecraft server instance in the main class
        Main.setMinecraftServer(event.getServer());
    }

    @SubscribeEvent
    public void onPlayerChat(ServerChatEvent event) {
        var obj = new InGameChatMessage(event.getPlayer(), event.getRawText(), event.getMessage());
        PlayerChatHandler.handlePlayerChat(obj);
    }
}
