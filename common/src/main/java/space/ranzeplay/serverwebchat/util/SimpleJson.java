package space.ranzeplay.serverwebchat.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple JSON utility class to avoid external dependencies
 */
public class SimpleJson {
    
    public static Map<String, String> parseObject(String json) {
        Map<String, String> result = new HashMap<>();
        if (json == null || json.trim().isEmpty()) {
            return result;
        }
        
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            return result;
        }
        
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) {
            return result;
        }
        
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");
                result.put(key, value);
            }
        }
        
        return result;
    }
    
    public static String createObject(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return "{}";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Boolean) {
                sb.append(value.toString().toLowerCase());
            } else if (value instanceof Number) {
                sb.append(value.toString());
            } else {
                sb.append("\"").append(value.toString()).append("\"");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    public static String createSimpleJson(String key, String value) {
        return "{\"" + key + "\":\"" + value + "\"}";
    }
    
    public static String createErrorJson(String error) {
        return "{\"error\":\"" + error + "\"}";
    }
}