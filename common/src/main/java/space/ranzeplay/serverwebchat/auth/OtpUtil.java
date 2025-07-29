package space.ranzeplay.serverwebchat.auth;

import space.ranzeplay.serverwebchat.Constants;

import java.security.SecureRandom;

public class OtpUtil {
    private static final SecureRandom secureRandom = new SecureRandom();
    
    public static String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }
    
    public static boolean isOtpExpired(long timestamp) {
        return (System.currentTimeMillis() - timestamp) > 120000; // 120 seconds
    }
    
    public static void sendOtpToPlayer(String playerName, String otp) {
        // In a real implementation, this would send the OTP to the player
        // For now, we'll just log it (in production, this should be sent via chat or other means)
        Constants.LOG.info("OTP for player {}: {}", playerName, otp);
        // TODO: Implement actual OTP delivery mechanism (e.g., in-game chat message)
    }
}