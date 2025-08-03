package space.ranzeplay.MCServerWebChat.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import space.ranzeplay.MCServerWebChat.models.UserState;
import space.ranzeplay.MCServerWebChat.services.AuthService;
import space.ranzeplay.MCServerWebChat.services.ChatService;
import space.ranzeplay.MCServerWebChat.services.MessageHistoryService;

import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final ConcurrentHashMap<ChannelHandlerContext, String> authenticatedConnections = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ChannelHandlerContext, UserState> connectionStates = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ChannelHandlerContext, String> pendingUsernames = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            String message = ((TextWebSocketFrame) frame).text();
            handleTextMessage(ctx, message);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Initialize connection state
        connectionStates.put(ctx, UserState.UNAUTHENTICATED);
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
        UserState currentState = connectionStates.get(ctx);
        
        String username = json.get("username").getAsString();
        String password = json.has("password") ? json.get("password").getAsString() : null;

        AuthService authService = AuthService.getInstance();
        
        if (password != null) {
            // Login attempt with password
            if (authService.userExists(username)) {
                // Existing user - authenticate
                String token = authService.authenticate(username, password);
                if (token != null) {
                    setConnectionState(ctx, UserState.AUTHENTICATED, username);
                    sendAuthSuccess(ctx, token, username);
                    MessageHistoryService.getInstance().sendHistoryToClient(ctx);
                } else {
                    sendAuthFailure(ctx, "Invalid credentials");
                }
            } else {
                // New user - start OTP process
                String otp = authService.generateOTP(username, password);
                ChatService.getInstance().sendOTPToPlayer(username, otp);
                setConnectionState(ctx, UserState.OTP_REQUIRED, username);
                sendOTPRequired(ctx, "OTP sent to in-game chat");
            }
        } else {
            sendAuthFailure(ctx, "Password is required");
        }
    }

    private void handleOtpVerify(ChannelHandlerContext ctx, JsonObject json) {
        UserState currentState = connectionStates.get(ctx);
        
        if (currentState != UserState.OTP_REQUIRED) {
            sendError(ctx, "OTP verification not required in current state");
            return;
        }
        
        String username = pendingUsernames.get(ctx);
        if (username == null) {
            sendError(ctx, "No pending OTP verification");
            return;
        }
        
        String otp = json.get("otp").getAsString();
        // Password is no longer required - it was stored during auth stage

        AuthService authService = AuthService.getInstance();
        
        if (authService.verifyOTP(username, otp)) {
            // Create new user account using stored password
            String token = authService.createUserFromOTP(username);
            if (token != null) {
                setConnectionState(ctx, UserState.AUTHENTICATED, username);
                sendAuthSuccess(ctx, token, username);
                MessageHistoryService.getInstance().sendHistoryToClient(ctx);
            } else {
                sendAuthFailure(ctx, "Failed to create user account");
            }
        } else {
            sendAuthFailure(ctx, "Invalid OTP");
        }
    }

    private void handleChatMessage(ChannelHandlerContext ctx, JsonObject json) {
        UserState currentState = connectionStates.get(ctx);
        
        if (currentState != UserState.AUTHENTICATED) {
            sendError(ctx, "Not authenticated");
            return;
        }
        
        String username = authenticatedConnections.get(ctx);
        if (username == null) {
            sendError(ctx, "Authentication error");
            return;
        }

        String message = json.get("message").getAsString();
        ChatService.getInstance().broadcastWebMessage(username, message);
        MessageHistoryService.getInstance().addMessage(username, message, "web");
    }

    /**
     * Set connection state and manage associated data
     */
    private void setConnectionState(ChannelHandlerContext ctx, UserState state, String username) {
        connectionStates.put(ctx, state);
        
        switch (state) {
            case AUTHENTICATED:
                authenticatedConnections.put(ctx, username);
                pendingUsernames.remove(ctx);
                break;
            case OTP_REQUIRED:
                pendingUsernames.put(ctx, username);
                break;
            case UNAUTHENTICATED:
                authenticatedConnections.remove(ctx);
                pendingUsernames.remove(ctx);
                break;
        }
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
        connectionStates.remove(ctx);
        pendingUsernames.remove(ctx);
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