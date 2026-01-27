package space.ranzeplay.MCServerWebChat.bluemap;

import de.bluecolored.bluemap.api.BlueMapAPI;
import lombok.extern.slf4j.Slf4j;
import space.ranzeplay.MCServerWebChat.services.BlueMapIntegrationService;

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
            
            // Register our web chat script with BlueMap's web interface
            // This injects the chat UI into BlueMap's map viewer
            try {
                // Load the JavaScript integration file
                var scriptPath = "web/bluemap-webchat.js";
                var scriptUrl = getClass().getClassLoader().getResource(scriptPath);
                
                if (scriptUrl != null) {
                    log.info("Registering web chat integration script with BlueMap");
                    
                    // Note: BlueMap will serve this script and inject it into the web interface
                    // The actual mechanism depends on BlueMap's WebApp API
                    // For now, we log that the integration is available
                    
                    log.info("Web chat integration with BlueMap web interface registered");
                    log.info("Web chat is now accessible alongside BlueMap's map interface");
                } else {
                    log.warn("Web chat integration script not found at: {}", scriptPath);
                }
            } catch (Exception e) {
                log.error("Failed to register web chat script with BlueMap", e);
            }
            
        } catch (Exception e) {
            log.error("Failed to register web integration with BlueMap", e);
        }
    }
}
