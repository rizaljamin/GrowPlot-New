package com.growplot.farm;

import org.json.JSONException;
import org.json.JSONObject;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * User identity record primitives for Issues #7/#8.
 *
 * SECURITY BOUNDARY: This class does not authenticate users or grant permissions.
 * A trusted backend must verify identity, enforce roles and own the credential store.
 * Never use a client-side role field to authorize access to other users.
 */
public final class UserIdentity {
    public enum Source { OWN, GOOGLE, FACEBOOK }
    public enum Role { STANDARD, POWER_USER, CREATOR }

    private static final int ITERATIONS = 310000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private UserIdentity() {}

    public static JSONObject profile(String id, String displayName, String email,
                                     Source source, String providerSubject,
                                     Role role, boolean active) throws JSONException {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Missing user id");
        if (source != Source.OWN && (providerSubject == null || providerSubject.isEmpty()))
            throw new IllegalArgumentException("Verified provider subject required");
        JSONObject record = new JSONObject();
        record.put("schemaVersion", 1);
        record.put("userId", id);
        record.put("displayName", displayName == null ? "" : displayName.trim());
        record.put("email", email == null ? "" : email.trim());
        record.put("idSource", source.name().toLowerCase(java.util.Locale.ROOT));
        record.put("providerSubject", source == Source.OWN ? JSONObject.NULL : providerSubject);
        record.put("role", role.name().toLowerCase(java.util.Locale.ROOT));
        record.put("active", active);
        return record;
    }

    /** Password verifier only. Do not store passwords or password-recovery encryption keys. */
    public static JSONObject hashOwnPassword(char[] password) throws Exception {
        if (password == null || password.length < 12)
            throw new IllegalArgumentException("Password must contain at least 12 characters");
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(password, salt, ITERATIONS);
        JSONObject verifier = new JSONObject();
        verifier.put("algorithm", "PBKDF2WithHmacSHA256");
        verifier.put("iterations", ITERATIONS);
        verifier.put("salt", Base64.getEncoder().encodeToString(salt));
        verifier.put("hash", Base64.getEncoder().encodeToString(hash));
        return verifier;
    }

    public static boolean verifyOwnPassword(char[] password, JSONObject verifier) throws Exception {
        if (password == null || verifier == null ||
            !"PBKDF2WithHmacSHA256".equals(verifier.optString("algorithm"))) return false;
        int iterations = verifier.getInt("iterations");
        if (iterations < 100000 || iterations > 1000000) return false;
        byte[] salt = Base64.getDecoder().decode(verifier.getString("salt"));
        byte[] expected = Base64.getDecoder().decode(verifier.getString("hash"));
        if (salt.length != SALT_BYTES || expected.length != HASH_BITS / 8) return false;
        return MessageDigest.isEqual(expected, derive(password, salt, iterations));
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    /** Statistics are computed by trusted backend after creator/power-user authorization. */
    public static JSONObject summary(int own, int google, int facebook) throws JSONException {
        if (own < 0 || google < 0 || facebook < 0)
            throw new IllegalArgumentException("Negative count");
        JSONObject out = new JSONObject();
        out.put("total", own + google + facebook);
        out.put("own", own);
        out.put("google", google);
        out.put("facebook", facebook);
        return out;
    }
}
