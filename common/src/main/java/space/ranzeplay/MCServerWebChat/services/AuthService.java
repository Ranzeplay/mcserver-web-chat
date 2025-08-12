package space.ranzeplay.MCServerWebChat.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AuthService {
    private static AuthService instance;
    private final Map<String, String> users = new ConcurrentHashMap<>(); // username -> hashed password (cached)
    private final Map<String, String> pendingOTPs = new ConcurrentHashMap<>(); // username -> OTP
    private final Map<String, Long> otpTimestamps = new ConcurrentHashMap<>(); // username -> timestamp
    private final Map<String, String> pendingPasswords = new ConcurrentHashMap<>(); // username -> password (for OTP flow)
    private final SecretKey jwtKey = Keys.hmacShaKeyFor("my-very-secure-jwt-secret-key-for-mcserver-web-chat".getBytes(StandardCharsets.UTF_8));
    private final Random random = new Random();
    private final DatabaseService databaseService;

    private AuthService() {
        this.databaseService = DatabaseService.getInstance();
        loadUsers();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public boolean userExists(String username) {
        // Check database directly for most up-to-date info
        return databaseService.userExists(username);
    }

    public String generateOTP(String username, String password) {
        String otp = String.format("%06d", random.nextInt(1000000));
        pendingOTPs.put(username, otp);
        otpTimestamps.put(username, System.currentTimeMillis());
        // Store password for later use in OTP verification
        pendingPasswords.put(username, password);
        log.debug("Generated OTP for user: {}", username);
        return otp;
    }

    public boolean verifyOTP(String username, String otp) {
        String expectedOTP = pendingOTPs.get(username);
        Long timestamp = otpTimestamps.get(username);
        
        if (expectedOTP == null || timestamp == null) {
            log.debug("OTP verification failed for {}: no pending OTP found", username);
            return false;
        }
        
        // OTP expires after 5 minutes
        if (System.currentTimeMillis() - timestamp > 5 * 60 * 1000) {
            pendingOTPs.remove(username);
            otpTimestamps.remove(username);
            pendingPasswords.remove(username);
            log.debug("OTP verification failed for {}: OTP expired", username);
            return false;
        }
        
        boolean valid = expectedOTP.equals(otp);
        if (valid) {
            pendingOTPs.remove(username);
            otpTimestamps.remove(username);
            // Don't remove pending password yet - we need it for user creation
            log.debug("OTP verification successful for {}", username);
        } else {
            log.debug("OTP verification failed for {}: invalid OTP", username);
        }
        
        return valid;
    }

    public String createUser(String username, String password) {
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        
        // Save to database
        databaseService.saveUser(username, hashedPassword);
        
        // Update cache
        users.put(username, hashedPassword);
        
        log.info("Created new user: {}", username);
        return generateJWT(username);
    }

    public String createUserFromOTP(String username) {
        String password = pendingPasswords.remove(username);
        if (password == null) {
            log.warn("No pending password found for user: {}", username);
            return null; // No pending password found
        }
        return createUser(username, password);
    }

    public String authenticate(String username, String password) {
        // Get from database (most up-to-date)
        String hashedPassword = databaseService.getUserHashedPassword(username);
        if (hashedPassword == null) {
            log.debug("Authentication failed for {}: user not found", username);
            return null;
        }
        
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
        if (result.verified) {
            log.debug("Authentication successful for {}", username);
            return generateJWT(username);
        }
        
        log.debug("Authentication failed for {}: invalid password", username);
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

    public String validateToken(String token) {
        try {
            String username = Jwts.parser()
                    .verifyWith(jwtKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            
            // Also verify the user still exists
            if (userExists(username)) {
                log.debug("Token validation successful for user: {}", username);
                return username;
            } else {
                log.debug("Token validation failed: user {} no longer exists", username);
                return null;
            }
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateJWT(String token) {
        try {
            Jwts.parser()
                .verifyWith(jwtKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
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
            log.debug("Failed to extract username from JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Load users from persistent storage into cache
     */
    private void loadUsers() {
        Map<String, String> loadedUsers = databaseService.loadAllUsers();
        users.putAll(loadedUsers);
        log.info("Loaded {} users from database", loadedUsers.size());
    }
}