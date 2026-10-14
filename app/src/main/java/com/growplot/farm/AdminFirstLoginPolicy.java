package com.growplot.farm;

/**
 * First-login policy for the reserved GrowPlot administrator.
 *
 * A public APK must NOT contain a shared known administrator password.
 * The authentication service must provision the owner account securely,
 * mark it as requiring password change, and verify every session server-side.
 */
public final class AdminFirstLoginPolicy {
    private AdminFirstLoginPolicy() {}

    public static final int MIN_PASSWORD_LENGTH = 8;

    /** Enforced by trusted auth service before granting any admin permissions. */
    public static boolean requiresPasswordChange(boolean passwordChangeRequired) {
        return passwordChangeRequired;
    }

    public static String validateNewPassword(char[] next, char[] confirmation,
                                             char[] previous) {
        if (next == null || next.length < MIN_PASSWORD_LENGTH || next.length > 128)
            return "Password must be between 8 and 128 characters";
        if (confirmation == null || !java.util.Arrays.equals(next, confirmation))
            return "Passwords do not match";
        if (previous != null && java.util.Arrays.equals(next, previous))
            return "Choose a different password";
        return null;
    }
}
