package com.growplot.farm;

import java.util.Locale;
import java.util.regex.Pattern;

/** Validation for self-service native GrowPlot registration (Issue #7).
 * Server must repeat validation and enforce unique usernames and emails.
 */
public final class RegistrationRules {
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{5,30}$");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private RegistrationRules() {}

    public static String validate(String fullName, String username, String email,
                                  char[] password, char[] confirmation) {
        if (fullName == null || fullName.trim().isEmpty()) return "Full name is required";
        if (username == null || !USERNAME.matcher(username).matches())
            return "Username must be 5–30 letters, numbers or underscores";
        if (email == null || email.length() > 254 || !EMAIL.matcher(email.trim()).matches())
            return "Enter a valid email address";
        if (password == null || password.length < 8 || password.length > 128)
            return "Use a password between 8 and 128 characters";
        if (confirmation == null || !java.util.Arrays.equals(password, confirmation))
            return "Passwords do not match";
        String lowered = new String(password).toLowerCase(Locale.ROOT);
        if (lowered.contains("password") || lowered.contains("123456") ||
            lowered.equals(username.toLowerCase(Locale.ROOT)))
            return "Choose a less common password";
        return null;
    }
}
