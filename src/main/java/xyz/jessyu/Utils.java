package xyz.jessyu;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
    public static String hashContent(String content) {
        // Implement a hashing function here, e.g., using SHA-256
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] hash = digest.digest(content.getBytes());
        StringBuilder sb = new StringBuilder();
        for(byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
