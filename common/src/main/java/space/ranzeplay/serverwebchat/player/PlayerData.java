package space.ranzeplay.serverwebchat.player;

import space.ranzeplay.serverwebchat.Constants;
import space.ranzeplay.serverwebchat.util.SimpleJson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PlayerData {
    private String uuid;
    private String playerName;
    private String passwordHash;
    private String salt;
    private long otpTimestamp;
    private String otp;
    private long createdAt;
    private long lastLogin;
    
    private static final String PLAYERS_DIR = "config/players";
    
    public PlayerData() {
        this.createdAt = System.currentTimeMillis();
    }
    
    public PlayerData(String uuid, String playerName) {
        this();
        this.uuid = uuid;
        this.playerName = playerName;
    }
    
    // Getters and setters
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
    
    public long getOtpTimestamp() { return otpTimestamp; }
    public void setOtpTimestamp(long otpTimestamp) { this.otpTimestamp = otpTimestamp; }
    
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getLastLogin() { return lastLogin; }
    public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }
    
    // Utility methods
    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.isEmpty();
    }
    
    public boolean isOtpValid() {
        return otp != null && !otp.isEmpty() && 
               (System.currentTimeMillis() - otpTimestamp) < 120000; // 120 seconds
    }
    
    public void clearOtp() {
        this.otp = null;
        this.otpTimestamp = 0;
    }
    
    // File operations
    public void save() {
        try {
            Path playersDir = Paths.get(PLAYERS_DIR);
            if (!Files.exists(playersDir)) {
                Files.createDirectories(playersDir);
            }
            
            File file = new File(playersDir.toFile(), uuid + ".json");
            Map<String, Object> data = new HashMap<>();
            data.put("uuid", uuid != null ? uuid : "");
            data.put("playerName", playerName != null ? playerName : "");
            data.put("passwordHash", passwordHash != null ? passwordHash : "");
            data.put("salt", salt != null ? salt : "");
            data.put("otpTimestamp", otpTimestamp);
            data.put("otp", otp != null ? otp : "");
            data.put("createdAt", createdAt);
            data.put("lastLogin", lastLogin);
            
            String json = SimpleJson.createObject(data);
            
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
            }
            Constants.LOG.debug("Saved player data for {}", playerName);
        } catch (IOException e) {
            Constants.LOG.error("Failed to save player data for {}", playerName, e);
        }
    }
    
    public static PlayerData load(String uuid) {
        try {
            File file = new File(PLAYERS_DIR, uuid + ".json");
            if (!file.exists()) {
                return null;
            }
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            }
            
            Map<String, String> data = SimpleJson.parseObject(content.toString());
            PlayerData playerData = new PlayerData();
            playerData.setUuid(data.get("uuid"));
            playerData.setPlayerName(data.get("playerName"));
            playerData.setPasswordHash(data.get("passwordHash"));
            playerData.setSalt(data.get("salt"));
            playerData.setOtpTimestamp(parseLong(data.get("otpTimestamp")));
            playerData.setOtp(data.get("otp"));
            playerData.setCreatedAt(parseLong(data.get("createdAt")));
            playerData.setLastLogin(parseLong(data.get("lastLogin")));
            
            Constants.LOG.debug("Loaded player data for {}", playerData.getPlayerName());
            return playerData;
        } catch (Exception e) {
            Constants.LOG.error("Failed to load player data for UUID {}", uuid, e);
            return null;
        }
    }
    
    public static PlayerData findByPlayerName(String playerName) {
        try {
            Path playersDir = Paths.get(PLAYERS_DIR);
            if (!Files.exists(playersDir)) {
                return null;
            }
            
            File[] files = playersDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
            if (files == null) {
                return null;
            }
            
            for (File file : files) {
                try {
                    StringBuilder content = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line);
                        }
                    }
                    
                    Map<String, String> data = SimpleJson.parseObject(content.toString());
                    if (playerName.equals(data.get("playerName"))) {
                        String uuid = file.getName().replace(".json", "");
                        return load(uuid);
                    }
                } catch (IOException e) {
                    Constants.LOG.warn("Failed to read player file {}", file.getName(), e);
                }
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to search for player {}", playerName, e);
        }
        return null;
    }
    
    private static long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}