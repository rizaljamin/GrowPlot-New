package com.growplot.farm;

import java.util.Locale;

/**
 * Authorization policy for Issue #8.
 *
 * IMPORTANT: These checks must only receive a role from a verified session
 * issued by a trusted backend. Never trust a client-provided role or username.
 * Admin bootstrap must be restricted to the verified app owner.
 */
public final class AdminPolicy {
    public static final String BOOTSTRAP_USERNAME = "growplot_admin";

    private AdminPolicy() {}

    public static boolean isReservedUsername(String username) {
        return username != null &&
            BOOTSTRAP_USERNAME.equals(username.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * For POWER_USER, only the trusted server-verified growplot_admin identity
     * can manage accounts. Ordinary power users are excluded.
     */
    public static boolean canManageAccounts(UserIdentity.Role trustedRole,
                                            boolean active, String verifiedUsername) {
        return active && (trustedRole == UserIdentity.Role.CREATOR ||
            (trustedRole == UserIdentity.Role.POWER_USER &&
             isReservedUsername(verifiedUsername)));
    }

    public static UserIdentity.Role roleForSelfRegistration() {
        return UserIdentity.Role.STANDARD;
    }
}
