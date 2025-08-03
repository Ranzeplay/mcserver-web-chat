package space.ranzeplay.MCServerWebChat.neoforge;

import space.ranzeplay.MCServerWebChat.Main;
import net.neoforged.fml.common.Mod;

@Mod(Main.MOD_ID)
public final class MainNeoForge {
    public MainNeoForge() {
        // Run our common setup.
        Main.init();
    }
}
