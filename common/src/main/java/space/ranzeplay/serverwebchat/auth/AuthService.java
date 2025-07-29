package space.ranzeplay.serverwebchat.auth;

import space.ranzeplay.serverwebchat.Constants;
import space.ranzeplay.serverwebchat.player.PlayerData;
import space.ranzeplay.serverwebchat.util.SimpleJson;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthService {
    
    public String handleLogin(String requestBody) {
        try {
            Map<String, String> request = SimpleJson.parseObject(requestBody);
            String playerName = request.get("playerName");
            String password = request.get("password");
            String otp = request.get("otp");
            
            if (playerName == null || playerName.trim().isEmpty()) {
                return SimpleJson.createErrorJson("Player name is required");
            }
            
            PlayerData playerData = PlayerData.findByPlayerName(playerName);
            
            // New player registration
            if (playerData == null) {
                if (password == null || password.trim().isEmpty()) {
                    return SimpleJson.createErrorJson("Password is required for new players");
                }
                
                String uuid = UUID.randomUUID().toString();
                playerData = new PlayerData(uuid, playerName);
                
                // Hash password
                String salt = PasswordUtil.generateSalt();
                String passwordHash = PasswordUtil.hashPassword(password, salt);
                playerData.setSalt(salt);
                playerData.setPasswordHash(passwordHash);
                
                // Generate and send OTP
                String generatedOtp = OtpUtil.generateOtp();
                playerData.setOtp(generatedOtp);
                playerData.setOtpTimestamp(System.currentTimeMillis());
                playerData.save();
                
                OtpUtil.sendOtpToPlayer(playerName, generatedOtp);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "New player registered. Please provide the OTP sent to you.");
                response.put("requiresOtp", true);
                return SimpleJson.createObject(response);
            }
            
            // Existing player login
            if (!playerData.hasPassword()) {
                return SimpleJson.createErrorJson("Player data corrupted");
            }
            
            if (password == null || password.trim().isEmpty()) {
                return SimpleJson.createErrorJson("Password is required");
            }
            
            // Verify password
            if (!PasswordUtil.verifyPassword(password, playerData.getPasswordHash(), playerData.getSalt())) {
                return SimpleJson.createErrorJson("Invalid password");
            }
            
            // Check if OTP is required
            if (otp == null || otp.trim().isEmpty()) {
                // Generate and send new OTP
                String generatedOtp = OtpUtil.generateOtp();
                playerData.setOtp(generatedOtp);
                playerData.setOtpTimestamp(System.currentTimeMillis());
                playerData.save();
                
                OtpUtil.sendOtpToPlayer(playerName, generatedOtp);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "OTP sent. Please provide the OTP to complete login.");
                response.put("requiresOtp", true);
                return SimpleJson.createObject(response);
            }
            
            // Verify OTP
            if (!playerData.isOtpValid()) {
                return SimpleJson.createErrorJson("OTP expired. Please request a new one.");
            }
            
            if (!otp.equals(playerData.getOtp())) {
                return SimpleJson.createErrorJson("Invalid OTP");
            }
            
            // Login successful - generate JWT token
            String token = JwtUtil.generateToken(playerData.getUuid(), playerData.getPlayerName());
            if (token == null) {
                return SimpleJson.createErrorJson("Failed to generate authentication token");
            }
            
            // Update last login and clear OTP
            playerData.setLastLogin(System.currentTimeMillis());
            playerData.clearOtp();
            playerData.save();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("uuid", playerData.getUuid());
            response.put("playerName", playerData.getPlayerName());
            
            return SimpleJson.createObject(response);
            
        } catch (Exception e) {
            Constants.LOG.error("Error processing login request", e);
            return SimpleJson.createErrorJson("Invalid request format");
        }
    }
}