package com.growplot.farm;

/**
 * Contract for one-time creator activation (Issue #8).
 * The trusted server generates, sends and verifies activation codes.
 * The client must never generate authoritative codes or grant itself roles.
 */
public final class AdminActivationPolicy {
    public static final String ADMIN_USERNAME = "growplot_admin";
    public static final String OWNER_EMAIL = "growplot2026@gmail.com";
    public static final int CODE_LENGTH = 6;
    public static final int CODE_EXPIRY_SECONDS = 600;
    public static final int MAX_ATTEMPTS = 5;
    public static final int MIN_PASSWORD_LENGTH = 8;

    private AdminActivationPolicy() {}

    public static boolean isValidCodeFormat(String code) {
        if (code == null || code.length() != CODE_LENGTH) return false;
        for (int i = 0; i < code.length(); i++)
            if (code.charAt(i) < '0' || code.charAt(i) > '9') return false;
        return true;
    }

    public static String validatePassword(char[] password, char[] confirmation) {
        if (password == null || password.length < MIN_PASSWORD_LENGTH || password.length > 128)
            return "Password must be between 8 and 128 characters";
        if (confirmation == null || !java.util.Arrays.equals(password, confirmation))
            return "Passwords do not match";
        return null;
    }
}
