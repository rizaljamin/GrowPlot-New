package com.growplot.farm;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * OAuth 2.0 authorization-code + PKCE launcher for Google/Facebook.
 * Requires a registered redirect URI and a trusted backend to exchange the code,
 * validate provider identity, establish session and assign permissions.
 * NEVER treat an authorization code or redirect alone as proof of login.
 */
public final class SocialAuth {
    private static final SecureRandom RNG = new SecureRandom();
    private SocialAuth() {}

    public static final class Request {
        public final String state;
        public final String verifier;
        public final String provider;
        private Request(String state, String verifier, String provider) {
            this.state = state; this.verifier = verifier; this.provider = provider;
        }
    }

    public static Request begin(Activity activity, String provider, String clientId,
                                String redirectUri) throws Exception {
        if (!"google".equals(provider) && !"facebook".equals(provider))
            throw new IllegalArgumentException("Unsupported provider");
        if (clientId == null || clientId.trim().isEmpty())
            throw new IllegalArgumentException("Provider client ID is not configured");
        if (redirectUri == null || !redirectUri.startsWith("https://"))
            throw new IllegalArgumentException("Verified HTTPS redirect URI required");
        String state = random(32);
        String verifier = random(32);
        String challenge = Base64.encodeToString(
                MessageDigest.getInstance("SHA-256").digest(
                        verifier.getBytes(java.nio.charset.StandardCharsets.US_ASCII)),
                Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        Uri.Builder url;
        if ("google".equals(provider)) {
            url = Uri.parse("https://accounts.google.com/o/oauth2/v2/auth").buildUpon()
                .appendQueryParameter("scope", "openid email profile")
                .appendQueryParameter("response_type", "code");
        } else {
            url = Uri.parse("https://www.facebook.com/v21.0/dialog/oauth").buildUpon()
                .appendQueryParameter("scope", "public_profile,email")
                .appendQueryParameter("response_type", "code");
        }
        Uri authorization = url.appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256").build();
        Intent intent = new Intent(Intent.ACTION_VIEW, authorization);
        activity.startActivity(intent);
        return new Request(state, verifier, provider);
    }

    private static String random(int length) {
        byte[] bytes = new byte[length];
        RNG.nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }
}
