package space.ranzeplay.MCServerWebChat.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Service to manage BlueMap integration.
 * This service detects if BlueMap is available and manages integration with it.
 */
@Slf4j
public class BlueMapIntegrationService {
    private static BlueMapIntegrationService instance;
    
    @Getter
    private final boolean blueMapAvailable;
    
    private Object blueMapAPI; // Will be BlueMapAPI when available
    private final List<Consumer<Object>> onBlueMapEnableCallbacks = new ArrayList<>();
    
    private BlueMapIntegrationService() {
        blueMapAvailable = checkBlueMapAvailability();
    }
    
    public static synchronized BlueMapIntegrationService getInstance() {
        if (instance == null) {
            instance = new BlueMapIntegrationService();
        }
        return instance;
    }
    
    /**
     * Checks if BlueMap API is available at runtime.
     * This check is performed once during initialization.
     */
    private boolean checkBlueMapAvailability() {
        try {
            // Try to load BlueMap API class
            Class.forName("de.bluecolored.bluemap.api.BlueMapAPI");
            log.info("BlueMap API detected - integration features will be enabled");
            return true;
        } catch (ClassNotFoundException e) {
            log.debug("BlueMap API not found - running in standalone mode");
            return false;
        }
    }
    
    /**
     * Register a callback to be called when BlueMap API becomes available.
     * If BlueMap is already available, the callback is called immediately.
     * 
     * @param callback The callback to register
     */
    public void onBlueMapEnable(Consumer<Object> callback) {
        if (blueMapAvailable && blueMapAPI != null) {
            callback.accept(blueMapAPI);
        }
        onBlueMapEnableCallbacks.add(callback);
    }
    
    /**
     * Called by BlueMap addon when BlueMap API becomes available.
     * This method uses Object to avoid compile-time dependency on BlueMap API.
     * 
     * @param api The BlueMap API instance
     */
    public void setBlueMapAPI(Object api) {
        this.blueMapAPI = api;
        log.info("BlueMap API initialized - enabling integration features");
        
        // Call all registered callbacks
        for (Consumer<Object> callback : onBlueMapEnableCallbacks) {
            try {
                callback.accept(api);
            } catch (Exception e) {
                log.error("Error calling BlueMap enable callback", e);
            }
        }
    }
    
    /**
     * Called when BlueMap API is disabled.
     */
    public void onBlueMapDisable() {
        log.info("BlueMap API disabled");
        this.blueMapAPI = null;
    }
}
