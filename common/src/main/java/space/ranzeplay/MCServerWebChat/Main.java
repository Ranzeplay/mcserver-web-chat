package space.ranzeplay.MCServerWebChat;

public final class Main {
    public static final String MOD_ID = "mcserver-web-chat";
    private static WebServer webServer;

    public static void init() {
        // Write common init code here.
        System.out.println("Initializing MC Web Chat mod...");
        
        // Start the web server
        webServer = new WebServer();
        
        // Start the server in a separate thread to avoid blocking mod initialization
        Thread serverThread = new Thread(() -> {
            webServer.start();
        });
        serverThread.setDaemon(true);
        serverThread.setName("MC-Web-Chat-Server");
        serverThread.start();
        
        System.out.println("MC Web Chat mod initialized successfully!");
    }
    
    public static void shutdown() {
        if (webServer != null && webServer.isRunning()) {
            webServer.stop();
        }
    }
    
    public static WebServer getWebServer() {
        return webServer;
    }
}
