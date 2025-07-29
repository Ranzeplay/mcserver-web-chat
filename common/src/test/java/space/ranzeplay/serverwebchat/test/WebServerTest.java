package space.ranzeplay.serverwebchat.test;

public class WebServerTest {
    public static void main(String[] args) {
        System.out.println("Starting web server test...");
        
        TestWebServer server = new TestWebServer();
        server.start();
        
        System.out.println("Web server started on port 8080");
        System.out.println("Visit http://localhost:8080 to test");
        System.out.println("Press Enter to stop...");
        
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        server.stop();
        System.out.println("Web server stopped");
    }
}