package com.growplot.farm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import android.util.Base64;

/**
 * DEVICE-LOCAL DEMO ONLY. An emailed link proves neither email ownership nor
 * server-side identity. This MUST NOT authorize remote account management.
 */
public final class AdminActivationDemo {
    private static final String PREF = "growplot_admin_activation_demo";
    private static final long TEN_MINUTES = 600_000L;
    private static final SecureRandom RANDOM = new SecureRandom();
    private AdminActivationDemo() {}

    private static String digest(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception ex) { throw new IllegalStateException(ex); }
    }

    public static void request(Activity activity) {
        SharedPreferences p = activity.getSharedPreferences(PREF, Activity.MODE_PRIVATE);
        if (p.getBoolean("activated", false)) {
            Toast.makeText(activity, "Demo admin already activated on this device.", Toast.LENGTH_LONG).show();
            return;
        }
        long now = System.currentTimeMillis();
        if (now - p.getLong("last_request", 0) < 60_000L) {
            Toast.makeText(activity, "Please wait one minute before requesting again.", Toast.LENGTH_LONG).show();
            return;
        }
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        p.edit().putString("token_hash", digest(token)).putLong("expires", now + TEN_MINUTES)
                .putLong("last_request", now).putInt("attempts", 0).apply();
        String link = "growplot://activate-admin?token=" + Uri.encode(token);
        Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:growplot2026@gmail.com"));
        email.putExtra(Intent.EXTRA_SUBJECT, "GrowPlot demo administrator activation");
        email.putExtra(Intent.EXTRA_TEXT, "Tap this link on the same Android device within 10 minutes:\n\n" +
                link + "\n\nDevelopment test only. This does not verify ownership of an email account.");
        try {
            activity.startActivity(Intent.createChooser(email, "Send GrowPlot activation email"));
            Toast.makeText(activity, "Send the email, then open its link on this device.", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(activity, "No email application available.", Toast.LENGTH_LONG).show();
        }
    }

    public static void handleLink(Activity activity, Uri uri) {
        if (uri == null || !"growplot".equals(uri.getScheme()) ||
                !"activate-admin".equals(uri.getHost())) return;
        String token = uri.getQueryParameter("token");
        SharedPreferences p = activity.getSharedPreferences(PREF, Activity.MODE_PRIVATE);
        int attempts = p.getInt("attempts", 0);
        boolean valid = token != null && attempts < 5 && !p.getBoolean("activated", false)
                && System.currentTimeMillis() < p.getLong("expires", 0)
                && digest(token).equals(p.getString("token_hash", ""));
        if (!valid) {
            p.edit().putInt("attempts", attempts + 1).apply();
            new AlertDialog.Builder(activity).setTitle("Activation link invalid")
                    .setMessage("This demo link is expired, used, or belongs to another device. Request a new link.")
                    .setPositiveButton("OK", null).show();
            return;
        }
        LinearLayout form = new LinearLayout(activity);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(20 * activity.getResources().getDisplayMetrics().density);
        form.setPadding(pad, pad, pad, 0);
        EditText password = new EditText(activity);
        password.setHint("New password (8–128 characters)");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText confirm = new EditText(activity);
        confirm.setHint("Confirm password");
        confirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        form.addView(password);
        form.addView(confirm);
        AlertDialog dialog = new AlertDialog.Builder(activity).setTitle("GrowPlot demo admin password")
                .setMessage("Device-local testing only. This does not grant server administrator privileges.")
                .setView(form).setNegativeButton("Cancel", null)
                .setPositiveButton("Set password", null).create();
        dialog.setOnShowListener(unused -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String a = password.getText().toString(), b = confirm.getText().toString();
            if (a.length() < 8 || a.length() > 128 || !a.equals(b)) {
                confirm.setError("Passwords must match and contain 8–128 characters.");
                return;
            }
            // PBKDF2 salted verifier, stored only in this device's private preferences.
            try {
                byte[] salt = new byte[16]; RANDOM.nextBytes(salt);
                javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                        a.toCharArray(), salt, 310000, 256);
                byte[] key = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(spec).getEncoded();
                p.edit().putString("password_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                        .putString("password_hash", Base64.encodeToString(key, Base64.NO_WRAP))
                        .putBoolean("activated", true).remove("token_hash").remove("expires").apply();
                dialog.dismiss();
                Toast.makeText(activity, "Demo admin password saved on this device.", Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(activity, "Could not save demo password.", Toast.LENGTH_LONG).show();
            }
        }));
        dialog.show();
    }
}
