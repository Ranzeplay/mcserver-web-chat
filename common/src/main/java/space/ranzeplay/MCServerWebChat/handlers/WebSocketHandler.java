package space.ranzeplay.MCServerWebChat.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import space.ranzeplay.MCServerWebChat.services.AuthService;
import space.ranzeplay.MCServerWebChat.services.ChatService;
import space.ranzeplay.MCServerWebChat.services.MessageHistoryService;

import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final ConcurrentHashMap<ChannelHandlerContext, String> authenticatedConnections = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            String message = ((TextWebSocketFrame) frame).text();
            handleTextMessage(ctx, message);
        }
    }

    private void handleTextMessage(ChannelHandlerContext ctx, String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.get("type").getAsString();

            switch (type) {
                case "auth":
                    handleAuth(ctx, json);
                    break;
                case "otp_verify":
                    handleOtpVerify(ctx, json);
                    break;
                case "chat":
                    handleChatMessage(ctx, json);
                    break;
                default:
                    sendError(ctx, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            sendError(ctx, "Invalid message format: " + e.getMessage());
        }
    }

    private void handleAuth(ChannelHandlerContext ctx, JsonObject json) {
        String username = json.get("username").getAsString();
        String password = json.has("password") ? json.get("password").getAsString() : null;

        AuthService authService = AuthService.getInstance();
        
        if (password != null) {
            // Login attempt
            String token = authService.authenticate(username, password);
            if (token != null) {
                authenticatedConnections.put(ctx, username);
                sendAuthSuccess(ctx, token, username);
                // Send message history
                MessageHistoryService.getInstance().sendHistoryToClient(ctx);
            } else {
                sendAuthFailure(ctx, "Invalid credentials");
            }
        } else {
            // Check if user exists, if not, trigger OTP process
            if (!authService.userExists(username)) {
                String otp = authService.generateOTP(username);
                ChatService.getInstance().sendOTPToPlayer(username, otp);
                sendOTPRequired(ctx, "OTP sent to in-game chat");
            } else {
                sendAuthFailure(ctx, "Password required for existing user");
            }
        }
    }

    private void handleOtpVerify(ChannelHandlerContext ctx, JsonObject json) {
        String username = json.get("username").getAsString();
        String otp = json.get("otp").getAsString();
        String password = json.get("password").getAsString();

        AuthService authService = AuthService.getInstance();
        
        if (authService.verifyOTP(username, otp)) {
            // Create new user account
            String token = authService.createUser(username, password);
            authenticatedConnections.put(ctx, username);
            sendAuthSuccess(ctx, token, username);
            MessageHistoryService.getInstance().sendHistoryToClient(ctx);
        } else {
            sendAuthFailure(ctx, "Invalid OTP");
        }
    }

    private void handleChatMessage(ChannelHandlerContext ctx, JsonObject json) {
        String username = authenticatedConnections.get(ctx);
        if (username == null) {
            sendError(ctx, "Not authenticated");
            return;
        }

        String message = json.get("message").getAsString();
        ChatService.getInstance().broadcastWebMessage(username, message);
        MessageHistoryService.getInstance().addMessage(username, message, "web");
    }

    private void sendAuthSuccess(ChannelHandlerContext ctx, String token, String username) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "auth_success");
        response.addProperty("token", token);
        response.addProperty("username", username);
        ctx.writeAndFlush(new TextWebSocketFrame(gson.toJson(response)));
    }

    private void sendAuthFailure(ChannelHandlerContext ctx, String reason) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "auth_failure");
        response.addProperty("reason", reason);
        ctx.writeAndFlush(new TextWebSocketFrame(gson.toJson(response)));
    }

    private void sendOTPRequired(ChannelHandlerContext ctx, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "otp_required");
        response.addProperty("message", message);
        ctx.writeAndFlush(new TextWebSocketFrame(gson.toJson(response)));
    }

    private void sendError(ChannelHandlerContext ctx, String error) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "error");
        response.addProperty("message", error);
        ctx.writeAndFlush(new TextWebSocketFrame(gson.toJson(response)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        authenticatedConnections.remove(ctx);
    }

    public static void broadcastToWebClients(String message) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "chat");
        json.addProperty("message", message);
        String jsonString = gson.toJson(json);

        for (ChannelHandlerContext ctx : authenticatedConnections.keySet()) {
            if (ctx.channel().isActive()) {
                ctx.writeAndFlush(new TextWebSocketFrame(jsonString));
            }
        }
    }
}