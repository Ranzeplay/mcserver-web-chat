package space.ranzeplay.MCServerWebChat.neoforge;

import space.ranzeplay.MCServerWebChat.Main;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Main.MOD_ID)
public final class MainNeoForge {
    public MainNeoForge() {
        // Run our common setup.
        Main.init();
        
        // Register shutdown event
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
    }
    
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        Main.shutdown();
    }
}
