package com.growplot.farm;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Policy for the sole bootstrap administrator (GitHub issue #8).
 *
 * This is a policy helper, NOT an authentication service. A trusted server must
 * atomically create the first admin, verify ownership, and enforce every admin
 * action on the server. A username alone is never proof of ownership.
 */
public final class AdminPolicy {
    public static final String BOOTSTRAP_USERNAME = "growplot_admin";

    private AdminPolicy() {}

    /** Prevent self-service signup from claiming the reserved admin identity. */
    public static boolean isReservedUsername(String username) {
        return username != null &&
            BOOTSTRAP_USERNAME.equals(username.trim().toLowerCase(Locale.ROOT));
    }

    /** Only an already authenticated, active creator can administer users. */
    public static boolean canManageAccounts(UserIdentity.Role trustedRole, boolean active) {
        return active && trustedRole == UserIdentity.Role.CREATOR;
    }

    /** Creator role cannot be assigned by public registration or a client request. */
    public static UserIdentity.Role roleForSelfRegistration() {
        return UserIdentity.Role.STANDARD;
    }
}
