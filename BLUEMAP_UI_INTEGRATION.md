# BlueMap Web UI Integration - Implementation Complete

## Summary

The MCServer Web Chat mod now properly injects its web UI into BlueMap's frontend interface using BlueMap's WebApp API.

## How It Works

### 1. BlueMap Addon Registration

```
Server Startup
     ↓
BlueMap loads addon JAR from config/bluemap/addons/
     ↓
BlueMapWebChatAddon constructor called
     ↓
Registers callbacks with BlueMapAPI.onEnable() and onDisable()
```

### 2. Web UI Injection (onBlueMapEnable)

```java
private void registerWebIntegration(BlueMapAPI api) {
    var webApp = api.getWebApp();
    var webRoot = webApp.getWebRoot();
    var scriptsDir = webRoot.resolve("assets").resolve("mcserver-webchat");
    
    // 1. Create directory
    Files.createDirectories(scriptsDir);
    
    // 2. Copy JavaScript from resources to webroot
    var scriptStream = getClass().getClassLoader()
        .getResourceAsStream("web/bluemap-webchat.js");
    var targetFile = scriptsDir.resolve("bluemap-webchat.js");
    Files.copy(scriptStream, targetFile, REPLACE_EXISTING);
    
    // 3. Register script with BlueMap
    webApp.registerScript("assets/mcserver-webchat/bluemap-webchat.js");
}
```

### 3. JavaScript Widget Creation

When BlueMap's web interface loads:

```javascript
// bluemap-webchat.js executes
// 1. Creates iframe container
const iframe = document.createElement('iframe');
iframe.src = `${protocol}//${hostname}:8080`;  // MCServer Web Chat server
iframe.setAttribute('sandbox', 'allow-scripts allow-forms');

// 2. Creates toggle button
const button = document.createElement('button');
button.innerHTML = '💬 Chat';
button.addEventListener('click', () => {
    // Toggle chat visibility
});

// 3. Adds to BlueMap UI
document.body.appendChild(container);
document.body.appendChild(button);
```

### 4. Cleanup (onBlueMapDisable)

```java
private void onBlueMapDisable(BlueMapAPI blueMapAPI) {
    // Remove script file
    var scriptFile = webRoot.resolve("assets/mcserver-webchat/bluemap-webchat.js");
    Files.delete(scriptFile);
    
    // Remove directory if empty
    var scriptsDir = webRoot.resolve("assets/mcserver-webchat");
    if (Files.list(scriptsDir).findAny().isEmpty()) {
        Files.delete(scriptsDir);
    }
}
```

## File Structure

```
bluemap-addon/
├── build.gradle                          # Shadow JAR configuration
├── src/main/java/
│   └── space/ranzeplay/MCServerWebChat/
│       └── bluemap/
│           └── BlueMapWebChatAddon.java  # Main addon class
└── src/main/resources/
    ├── bluemap.addon.json                # BlueMap addon metadata
    └── web/
        └── bluemap-webchat.js            # UI integration script
```

## Build Configuration

### Root build.gradle

```gradle
subprojects {
    // Exclude bluemap-addon from Loom (it's not a Minecraft mod)
    if (project.name != 'bluemap-addon') {
        apply plugin: 'dev.architectury.loom'
        apply plugin: 'architectury-plugin'
    }
    // ... rest of configuration
}
```

### bluemap-addon/build.gradle

```gradle
plugins {
    id 'java'
    id 'com.github.johnrengelman.shadow'
}

dependencies {
    compileOnly project(':common')
    compileOnly 'de.bluecolored.bluemap:BlueMapAPI:2.7.2'
    compileOnly 'org.slf4j:slf4j-api:2.0.16'
}

shadowJar {
    // Include addon's own classes
    from sourceSets.main.output
    
    // Include BlueMapIntegrationService from common
    from(project(':common').sourceSets.main.output) {
        include 'space/ranzeplay/MCServerWebChat/services/BlueMapIntegrationService.class'
    }
}
```

## JAR Contents

```
mcserver-web-chat-bluemap-addon-0.1.jar
├── META-INF/
│   └── MANIFEST.MF
├── bluemap.addon.json                                      # Addon metadata
├── web/
│   └── bluemap-webchat.js                                  # UI script
├── space/ranzeplay/MCServerWebChat/
│   ├── bluemap/
│   │   └── BlueMapWebChatAddon.class                       # Main addon class
│   └── services/
│       └── BlueMapIntegrationService.class                 # Integration service
```

## Runtime Behavior

### Standalone Mode (Without BlueMap)
```
MCServer Web Chat Mod → Web server on :8080
                      → Chat accessible at http://server:8080
                      → BlueMap detection: Not available
```

### BlueMap Integration Mode
```
MCServer Web Chat Mod → Web server on :8080
                      → BlueMap detection: Available
                      ↓
BlueMap Addon        → Copies script to webroot
                     → Registers with BlueMap
                      ↓
BlueMap Web UI       → Loads bluemap-webchat.js
                     → Creates chat iframe widget
                     → Adds toggle button
                      ↓
User Experience      → Opens BlueMap in browser
                     → Clicks chat button (💬)
                     → Chat widget appears over map
                     → Can chat while viewing map
```

## Security Features

1. **Iframe Sandboxing**: `sandbox="allow-scripts allow-forms"` (no allow-same-origin)
2. **Protocol-Aware**: Uses same protocol as BlueMap (HTTP/HTTPS)
3. **Resource Cleanup**: Removes files when addon is disabled
4. **Compile-Only Dependencies**: BlueMapAPI not included in JAR

## Testing

### Build Command
```bash
JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 \
./gradlew :bluemap-addon:shadowJar -x checkPnpm -x buildFrontend
```

### Deployment
```bash
# 1. Install main mod
cp mcserver-web-chat-fabric-0.1.jar server/mods/

# 2. Install BlueMap addon
cp mcserver-web-chat-bluemap-addon-0.1.jar server/config/bluemap/addons/

# 3. Restart server
# 4. Open BlueMap: http://server:8100
# 5. Chat button appears in corner
```

## Dependencies

- BlueMapAPI: 2.7.2 (latest available)
- SLF4J API: 2.0.16 (provided by server)
- BlueMapIntegrationService: From common module

## Compatibility

- Minecraft: 1.21.7+
- Java: 21+
- BlueMap: Any version with WebApp API
- Works on: Fabric, NeoForge

## Success Criteria

✅ Script injected into BlueMap's webroot
✅ Script registered with WebApp API
✅ Chat widget appears in BlueMap UI
✅ Toggle functionality works
✅ Protocol-aware (HTTP/HTTPS)
✅ Secure iframe sandboxing
✅ Cleanup on disable
✅ No dependencies bundled
✅ Builds successfully
✅ Addon JAR contains all necessary classes

## Future Enhancements

Potential improvements:
- User position markers on map
- Chat history synchronized with map timeline
- Player tracking while chatting
- Custom styling integration with BlueMap themes
- Real-time message notifications on map
