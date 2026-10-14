package com.growplot.farm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** DEVICE-LOCAL FLOW TEST ONLY: no real email ownership verification or admin authorization. */
public final class AdminOtpDemo {
    private static final SecureRandom RNG = new SecureRandom();
    private static final String PREF = "growplot_admin_otp_demo";
    private AdminOtpDemo() {}

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return android.util.Base64.encodeToString(digest, android.util.Base64.NO_WRAP);
        } catch (Exception ex) { throw new IllegalStateException(ex); }
    }

    public static void login(Activity activity, String password) {
        SharedPreferences prefs = activity.getSharedPreferences(PREF, Activity.MODE_PRIVATE);
        if (!prefs.getBoolean("activated", false)) {
            if (!"growplot_admin".equals(password)) {
                Toast.makeText(activity, "Incorrect initial administrator password.", Toast.LENGTH_LONG).show();
                return;
            }
            setupPassword(activity);
            return;
        }
        try {
            byte[] salt = android.util.Base64.decode(prefs.getString("password_salt", ""), android.util.Base64.DEFAULT);
            byte[] expected = android.util.Base64.decode(prefs.getString("password_hash", ""), android.util.Base64.DEFAULT);
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                    (password == null ? "" : password).toCharArray(), salt, 310000, 256);
            byte[] actual = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            boolean valid = MessageDigest.isEqual(expected, actual);
            new AlertDialog.Builder(activity).setTitle(valid ? "Administrator login" : "Incorrect password")
                    .setMessage(valid ? "Password verified for this device-local test. Full account permissions are not yet connected." :
                            "Please enter your new administrator password.")
                    .setPositiveButton("OK", null).show();
        } catch (Exception ex) {
            Toast.makeText(activity, "Unable to verify administrator password.", Toast.LENGTH_LONG).show();
        }
    }

    private static void showOtp(Activity activity) {
        EditText code = new EditText(activity);
        code.setHint("Enter 6-digit OTP");
        code.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Verify GrowPlot administrator")
                .setMessage("Send the prepared email to growplot2026@gmail.com, then enter its OTP here. Alternatively, use the activation link flow.")
                .setView(code).setNegativeButton("Cancel", null)
                .setPositiveButton("Verify OTP", null).create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            SharedPreferences prefs = activity.getSharedPreferences(PREF, Activity.MODE_PRIVATE);
            int attempts = prefs.getInt("attempts", 0);
            String input = code.getText().toString().trim();
            boolean valid = attempts < 5 && input.matches("[0-9]{6}")
                    && System.currentTimeMillis() < prefs.getLong("expires", 0)
                    && MessageDigest.isEqual(hash(input).getBytes(StandardCharsets.UTF_8),
                        prefs.getString("otp", "").getBytes(StandardCharsets.UTF_8));
            if (!valid) {
                prefs.edit().putInt("attempts", attempts + 1).apply();
                code.setError("Invalid or expired OTP (maximum five attempts)");
                return;
            }
            dialog.dismiss();
            setupPassword(activity);
        }));
        dialog.show();
    }

    private static void setupPassword(Activity activity) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(activity);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        EditText password = new EditText(activity);
        password.setHint("New password (8–128 characters)");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText confirm = new EditText(activity);
        confirm.setHint("Confirm new password");
        confirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.setPadding(32, 16, 32, 8);
        layout.addView(password);
        layout.addView(confirm);
        AlertDialog dialog = new AlertDialog.Builder(activity).setTitle("Set administrator password")
                .setMessage("Development-only local setup; does not grant real admin permissions.")
                .setView(layout).setNegativeButton("Cancel", null)
                .setPositiveButton("Save password", null).create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String a = password.getText().toString(), b = confirm.getText().toString();
            if (a.length() < 8 || a.length() > 128 || !a.equals(b)) {
                confirm.setError("Passwords must match (8–128 characters)");
                return;
            }
            try {
                byte[] salt = new byte[16]; RNG.nextBytes(salt);
                javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(a.toCharArray(), salt, 310000, 256);
                byte[] verifier = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
                activity.getSharedPreferences(PREF, Activity.MODE_PRIVATE).edit()
                    .putString("password_salt", android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
                    .putString("password_hash", android.util.Base64.encodeToString(verifier, android.util.Base64.NO_WRAP))
                    .putBoolean("activated", true).remove("otp").remove("expires").apply();
                dialog.dismiss();
                Toast.makeText(activity, "Test admin password created on this device.", Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(activity, "Password setup failed.", Toast.LENGTH_LONG).show();
            }
        }));
        dialog.show();
    }
}
