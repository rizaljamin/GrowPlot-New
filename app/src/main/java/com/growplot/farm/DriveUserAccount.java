package com.growplot.farm;

import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Shared Drive-backed account document schema and password verifier.
 * No credential or password is ever written to device preferences.
 * Account documents must be retrieved from a trusted authenticated service;
 * a Drive folder name alone is not an authorization mechanism.
 */
public final class DriveUserAccount {
    private static final SecureRandom RANDOM = new SecureRandom();
    private DriveUserAccount() {}

    public static JSONObject create(String userId, String username, String role, char[] password)
            throws Exception {
        if (userId == null || !userId.matches("[a-zA-Z0-9_-]{1,80}"))
            throw new IllegalArgumentException("Invalid user ID");
        if (username == null || username.trim().isEmpty())
            throw new IllegalArgumentException("Username required");
        if (password == null || password.length < 8)
            throw new IllegalArgumentException("Password must be at least 8 characters");
        byte[] salt = new byte[16]; RANDOM.nextBytes(salt);
        byte[] hash = derive(password, salt);
        JSONObject account = new JSONObject();
        account.put("schemaVersion", 1);
        account.put("userId", userId);
        account.put("username", username.trim());
        account.put("role", "ADMIN".equals(role) ? "ADMIN" : "USER");
        account.put("passwordAlgorithm", "PBKDF2WithHmacSHA256");
        account.put("passwordIterations", 310000);
        account.put("passwordSalt", android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP));
        account.put("passwordHash", android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP));
        account.put("plots", new org.json.JSONArray());
        return account;
    }

    public static boolean verify(JSONObject account, char[] password) {
        if (account == null || password == null) return false;
        try {
            if (account.optInt("schemaVersion") != 1 ||
                    !"PBKDF2WithHmacSHA256".equals(account.optString("passwordAlgorithm")) ||
                    account.optInt("passwordIterations") != 310000) return false;
            byte[] salt = android.util.Base64.decode(account.getString("passwordSalt"), android.util.Base64.DEFAULT);
            byte[] expected = android.util.Base64.decode(account.getString("passwordHash"), android.util.Base64.DEFAULT);
            if (salt.length != 16 || expected.length != 32) return false;
            return MessageDigest.isEqual(expected, derive(password, salt));
        } catch (Exception e) { return false; }
    }

    public static boolean isAdministrator(JSONObject account) {
        return account != null && "ADMIN".equals(account.optString("role"))
                && "growplot_admin".equals(account.optString("username"));
    }

    private static byte[] derive(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 310000, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        finally { spec.clearPassword(); }
    }
}
