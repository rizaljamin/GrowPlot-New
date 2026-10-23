package com.growplot.farm;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Authenticated bridge to Malaysia's public monthly retail-price aggregation. */
public final class MarketPriceBridge {
    private static final String REGION = "us-central1";
    private final Activity activity;
    private final WebView webView;

    public MarketPriceBridge(Activity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
    }

    @JavascriptInterface
    public void loadMonthlyPrices(String requestId, String month, String aliasesJson) {
        if (requestId == null || !requestId.matches("[A-Za-z0-9_-]{1,120}")) return;
        if (month == null || !month.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            deliver(requestId, error("invalid-argument", "Choose a valid month."));
            return;
        }
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            deliver(requestId, error("unauthenticated", "Sign in to GrowPlot to load public market prices."));
            return;
        }

        List<String> aliases = new ArrayList<>();
        try {
            JSONArray source = new JSONArray(aliasesJson == null ? "[]" : aliasesJson);
            for (int i = 0; i < source.length() && aliases.size() < 8; i++) {
                String alias = source.optString(i, "").trim();
                if (alias.length() >= 3 && alias.length() <= 60) aliases.add(alias);
            }
        } catch (JSONException ignored) { }
        if (aliases.isEmpty()) {
            deliver(requestId, error("invalid-argument", "No searchable crop name is available."));
            return;
        }

        Map<String, Object> request = new HashMap<>();
        request.put("month", month);
        request.put("aliases", aliases);
        FirebaseFunctions.getInstance(REGION)
                .getHttpsCallable("getMonthlyRetailCropPrices")
                .call(request)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        HttpsCallableResult result = task.getResult();
                        Object data = result == null ? null : result.getData();
                        if (data instanceof Map) deliver(requestId, new JSONObject((Map<?, ?>) data));
                        else deliver(requestId, error("internal", "The public price source returned an empty response."));
                        return;
                    }
                    Exception failure = task.getException();
                    String code = "unavailable";
                    if (failure instanceof FirebaseFunctionsException) {
                        code = ((FirebaseFunctionsException) failure).getCode().name().toLowerCase();
                    }
                    String message = failure == null || failure.getMessage() == null
                            ? "Could not load monthly market prices. Check your connection and try again."
                            : failure.getMessage();
                    deliver(requestId, error(code, message));
                });
    }

    private static JSONObject error(String code, String message) {
        JSONObject result = new JSONObject();
        try {
            result.put("status", "error");
            result.put("errorCode", code);
            result.put("message", message);
        } catch (JSONException ignored) { }
        return result;
    }

    private void deliver(String requestId, JSONObject response) {
        String responseText = response.toString();
        activity.runOnUiThread(() -> {
            if (webView == null) return;
            String script = "window.GrowPlotMarketPricesResponse&&window.GrowPlotMarketPricesResponse("
                    + JSONObject.quote(requestId) + "," + JSONObject.quote(responseText) + ")";
            webView.evaluateJavascript(script, null);
        });
    }
}
