package space.ranzeplay.MCServerWebChat;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

@Slf4j
public final class Main {
    public static final String MOD_ID = "mcserver-web-chat";
    private static WebServer webServer;
    private static MinecraftServer minecraftServer;

    public static Path getRootConfigDir() {
        return ROOT_CONFIG_DIR;
    }

    private static Path ROOT_CONFIG_DIR;

    public static void init(Path rootConfigDir) {
        // Write common init code here.
        log.info("Initializing MC Web Chat mod...");

        ROOT_CONFIG_DIR = rootConfigDir;
        
        // Start the web server
        webServer = new WebServer();
        
        // Start the server in a separate thread to avoid blocking mod initialization
        Thread serverThread = new Thread(() -> {
            try {
                webServer.start();
            } catch (Exception e) {
                log.error("Failed to start MC Web Chat server: {}", e.getMessage(), e);
            }
        });
        serverThread.setDaemon(true);
        serverThread.setName("MC-Web-Chat-Server");
        serverThread.start();
        
        log.info("MC Web Chat mod initialized successfully!");
    }
    
    public static void shutdown() {
        if (webServer != null && webServer.isRunning()) {
            webServer.stop();
        }
    }

    public static void setMinecraftServer(MinecraftServer server) {
        if (webServer != null) {
           minecraftServer = server;
        }
    }

    public static MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }

    public static WebServer getWebServer() {
        return webServer;
    }
}
