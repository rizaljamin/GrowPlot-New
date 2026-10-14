package com.growplot.farm;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import java.security.SecureRandom;
import java.util.Locale;

/**
 * Development-only email handoff for Issue #8.
 *
 * Generates a fresh code on first activation attempt and opens the device's
 * email composer addressed to the owner. The user must explicitly send it.
 * This is NOT unattended email delivery or secure remote identity verification.
 * Never use this local code as proof of administrator ownership in production.
 */
public final class AdminActivationEmail {
    private static final SecureRandom RANDOM = new SecureRandom();
    private AdminActivationEmail() {}

    public static String generateTestCode() {
        return String.format(Locale.US, "%06d", RANDOM.nextInt(1_000_000));
    }

    public static void openEmailComposer(Activity activity, String code) {
        if (!AdminActivationPolicy.isValidCodeFormat(code))
            throw new IllegalArgumentException("Invalid activation code");
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + AdminActivationPolicy.OWNER_EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, "GrowPlot administrator test activation");
        intent.putExtra(Intent.EXTRA_TEXT,
            "GrowPlot administrator activation test code: " + code +
            "\n\nDevelopment-only email flow test. Not production account verification.");
        activity.startActivity(Intent.createChooser(intent, "Send GrowPlot test activation"));
    }
}
