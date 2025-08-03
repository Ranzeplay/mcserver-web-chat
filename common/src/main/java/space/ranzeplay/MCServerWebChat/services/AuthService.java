package space.ranzeplay.MCServerWebChat.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private static AuthService instance;
    private final Map<String, String> users = new ConcurrentHashMap<>(); // username -> hashed password
    private final Map<String, String> pendingOTPs = new ConcurrentHashMap<>(); // username -> OTP
    private final Map<String, Long> otpTimestamps = new ConcurrentHashMap<>(); // username -> timestamp
    private final SecretKey jwtKey = Keys.hmacShaKeyFor("my-very-secure-jwt-secret-key-for-mcserver-web-chat".getBytes(StandardCharsets.UTF_8));
    private final Random random = new Random();

    private AuthService() {}

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public String generateOTP(String username) {
        String otp = String.format("%06d", random.nextInt(1000000));
        pendingOTPs.put(username, otp);
        otpTimestamps.put(username, System.currentTimeMillis());
        return otp;
    }

    public boolean verifyOTP(String username, String otp) {
        String expectedOTP = pendingOTPs.get(username);
        Long timestamp = otpTimestamps.get(username);
        
        if (expectedOTP == null || timestamp == null) {
            return false;
        }
        
        // OTP expires after 5 minutes
        if (System.currentTimeMillis() - timestamp > 5 * 60 * 1000) {
            pendingOTPs.remove(username);
            otpTimestamps.remove(username);
            return false;
        }
        
        boolean valid = expectedOTP.equals(otp);
        if (valid) {
            pendingOTPs.remove(username);
            otpTimestamps.remove(username);
        }
        
        return valid;
    }

    public String createUser(String username, String password) {
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        users.put(username, hashedPassword);
        return generateJWT(username);
    }

    public String authenticate(String username, String password) {
        String hashedPassword = users.get(username);
        if (hashedPassword == null) {
            return null;
        }
        
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
        if (result.verified) {
            return generateJWT(username);
        }
        
        return null;
    }

    private String generateJWT(String username) {
        Instant now = Instant.now();
        Date expiration = Date.from(now.plus(24, ChronoUnit.HOURS));
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(expiration)
                .signWith(jwtKey)
                .compact();
    }

    public boolean validateJWT(String token) {
        try {
            Jwts.parser()
                .verifyWith(jwtKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromJWT(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(jwtKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}