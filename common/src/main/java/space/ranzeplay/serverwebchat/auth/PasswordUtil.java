package space.ranzeplay.serverwebchat.auth;

import space.ranzeplay.serverwebchat.Constants;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    private static final SecureRandom secureRandom = new SecureRandom();
    
    public static String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            Constants.LOG.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Unable to hash password", e);
        }
    }
    
    public static boolean verifyPassword(String password, String hash, String salt) {
        String hashedInput = hashPassword(password, salt);
        return hashedInput.equals(hash);
    }
}