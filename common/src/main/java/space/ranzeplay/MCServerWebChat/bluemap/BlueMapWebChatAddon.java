package space.ranzeplay.MCServerWebChat.bluemap;

import de.bluecolored.bluemap.api.BlueMapAPI;
import lombok.extern.slf4j.Slf4j;
import space.ranzeplay.MCServerWebChat.services.BlueMapIntegrationService;
import space.ranzeplay.MCServerWebChat.services.ConfigService;

/**
 * BlueMap addon entrypoint for MCServer Web Chat.
 * This class is loaded by BlueMap's addon system and enables integration features.
 */
@Slf4j
public class BlueMapWebChatAddon {
    
    /**
     * Constructor called by BlueMap's addon loader.
     */
    public BlueMapWebChatAddon() {
        log.info("MCServer Web Chat BlueMap Addon initializing...");
        
        // Check if BlueMap integration is enabled in config
        if (!ConfigService.getInstance().isBlueMapIntegrationEnabled()) {
            log.info("BlueMap integration is disabled in configuration");
            return;
        }
        
        // Register with BlueMap API
        BlueMapAPI.onEnable(this::onBlueMapEnable);
        BlueMapAPI.onDisable(this::onBlueMapDisable);
        
        log.info("MCServer Web Chat BlueMap Addon initialized successfully!");
    }
    
    /**
     * Called when BlueMap API is enabled and ready to use.
     * 
     * @param blueMapAPI The BlueMap API instance
     */
    private void onBlueMapEnable(BlueMapAPI blueMapAPI) {
        log.info("BlueMap API enabled - registering web chat integration");
        
        // Notify the integration service that BlueMap is available
        BlueMapIntegrationService.getInstance().setBlueMapAPI(blueMapAPI);
        
        // Register web chat integration features
        registerWebIntegration(blueMapAPI);
    }
    
    /**
     * Called when BlueMap API is disabled.
     * 
     * @param blueMapAPI The BlueMap API instance
     */
    private void onBlueMapDisable(BlueMapAPI blueMapAPI) {
        log.info("BlueMap API disabled - unregistering web chat integration");
        
        // Notify the integration service
        BlueMapIntegrationService.getInstance().onBlueMapDisable();
        
        // Clean up our script from webroot
        try {
            var webRoot = blueMapAPI.getWebApp().getWebRoot();
            var scriptFile = webRoot.resolve("assets").resolve("mcserver-webchat").resolve("bluemap-webchat.js");
            
            if (java.nio.file.Files.exists(scriptFile)) {
                java.nio.file.Files.delete(scriptFile);
                log.debug("Removed web chat script from BlueMap webroot");
            }
            
            // Try to remove the directory if it's empty
            var scriptsDir = webRoot.resolve("assets").resolve("mcserver-webchat");
            if (java.nio.file.Files.exists(scriptsDir) && 
                java.nio.file.Files.list(scriptsDir).findAny().isEmpty()) {
                java.nio.file.Files.delete(scriptsDir);
                log.debug("Removed empty mcserver-webchat directory");
            }
        } catch (java.io.IOException e) {
            log.warn("Failed to clean up web chat script from BlueMap webroot", e);
        }
    }
    
    /**
     * Registers web integration features with BlueMap.
     * This allows the web chat UI to be accessible through BlueMap's web interface.
     * 
     * @param api The BlueMap API instance
     */
    private void registerWebIntegration(BlueMapAPI api) {
        try {
            // Get BlueMap's web app to integrate with
            var webApp = api.getWebApp();
            
            // Get the webroot path where we need to copy our script
            var webRoot = webApp.getWebRoot();
            var scriptsDir = webRoot.resolve("assets").resolve("mcserver-webchat");
            
            try {
                // Create directory for our scripts
                java.nio.file.Files.createDirectories(scriptsDir);
                
                // Load the JavaScript integration file from resources
                var scriptPath = "web/bluemap-webchat.js";
                var scriptStream = getClass().getClassLoader().getResourceAsStream(scriptPath);
                
                if (scriptStream != null) {
                    // Copy the script to BlueMap's webroot
                    var targetFile = scriptsDir.resolve("bluemap-webchat.js");
                    java.nio.file.Files.copy(scriptStream, targetFile, 
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    scriptStream.close();
                    
                    // Register the script with BlueMap's webapp
                    // The URL is relative to webroot
                    webApp.registerScript("assets/mcserver-webchat/bluemap-webchat.js");
                    
                    log.info("Web chat integration script registered with BlueMap");
                    log.info("Chat UI will be available in BlueMap's web interface");
                } else {
                    log.warn("Web chat integration script not found at: {}", scriptPath);
                }
            } catch (java.io.IOException e) {
                log.error("Failed to copy web chat script to BlueMap webroot", e);
            }
            
        } catch (Exception e) {
            log.error("Failed to register web integration with BlueMap", e);
        }
    }
}
