package space.ranzeplay.serverwebchat.auth;

import space.ranzeplay.serverwebchat.Constants;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class JwtUtil {
    private static final String SECRET_KEY = "serverwebchat-secret-key-change-in-production";
    private static final String ALGORITHM = "HmacSHA256";
    
    public static String generateToken(String playerUuid, String playerName) {
        try {
            // JWT Header
            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(header.getBytes(StandardCharsets.UTF_8));
            
            // JWT Payload
            long now = System.currentTimeMillis() / 1000;
            long exp = now + 3600; // 1 hour expiration
            String payload = String.format(
                "{\"sub\":\"%s\",\"name\":\"%s\",\"iat\":%d,\"exp\":%d}",
                playerUuid, playerName, now, exp
            );
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
            
            // JWT Signature
            String data = encodedHeader + "." + encodedPayload;
            String signature = sign(data);
            
            return data + "." + signature;
        } catch (Exception e) {
            Constants.LOG.error("Failed to generate JWT token", e);
            return null;
        }
    }
    
    public static boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            
            String data = parts[0] + "." + parts[1];
            String expectedSignature = sign(data);
            
            if (!expectedSignature.equals(parts[2])) {
                return false;
            }
            
            // Check expiration
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            // Simple extraction of exp field (not a full JSON parser)
            int expIndex = payload.indexOf("\"exp\":");
            if (expIndex != -1) {
                String expStr = payload.substring(expIndex + 6);
                int endIndex = expStr.indexOf(",");
                if (endIndex == -1) endIndex = expStr.indexOf("}");
                if (endIndex != -1) {
                    long exp = Long.parseLong(expStr.substring(0, endIndex).trim());
                    return System.currentTimeMillis() / 1000 < exp;
                }
            }
            
            return true;
        } catch (Exception e) {
            Constants.LOG.error("Failed to validate JWT token", e);
            return false;
        }
    }
    
    private static String sign(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        mac.init(secretKeySpec);
        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }
}