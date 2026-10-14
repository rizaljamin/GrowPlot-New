package com.growplot.farm;

import android.net.Uri;

/**
 * Parses an HTTPS admin activation app link.
 * The token is untrusted until a trusted server validates expiry, ownership,
 * one-time use and authorization. Never activate admin from URI parsing alone.
 */
public final class AdminActivationLink {
    public static final String HOST = "auth.growplot.app";
    public static final String PATH = "/activate/admin";

    private AdminActivationLink() {}

    public static String extractUnverifiedToken(Uri uri) {
        if (uri == null || !"https".equalsIgnoreCase(uri.getScheme()) ||
            !HOST.equalsIgnoreCase(uri.getHost()) || !PATH.equals(uri.getPath()))
            return null;
        String token = uri.getQueryParameter("token");
        if (token == null || token.length() < 32 || token.length() > 512 ||
            !token.matches("[A-Za-z0-9_-]+")) return null;
        return token;
    }
}
