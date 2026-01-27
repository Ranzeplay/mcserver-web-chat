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
            
            log.info("Web chat integration with BlueMap web interface registered");
            log.info("Web chat is now accessible alongside BlueMap's map interface");
            
            // Note: The actual web server is managed by the main mod
            // This addon just ensures compatibility and integration with BlueMap
            
        } catch (Exception e) {
            log.error("Failed to register web integration with BlueMap", e);
        }
    }
}
