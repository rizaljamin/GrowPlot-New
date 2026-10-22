package com.growplot.farm;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Authenticated Android bridge for the Firebase callable that proxies Vertex AI. */
public final class VertexDiagnosisBridge {
    private static final String REGION = "us-central1";
    private static final int MAX_BASE64_CHARS = 7_000_000;

    private final Activity activity;
    private final WebView webView;

    public VertexDiagnosisBridge(Activity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
    }

    @JavascriptInterface
    public void analyze(String requestId, String imageBase64, String mimeType, String contextJson) {
        if (requestId == null || !requestId.matches("[A-Za-z0-9_-]{1,120}")) return;
        if (imageBase64 == null || imageBase64.isEmpty() || imageBase64.length() > MAX_BASE64_CHARS) {
            deliver(requestId, error("invalid-argument", "The selected photo is empty or too large to send."));
            return;
        }
        if (!"image/jpeg".equals(mimeType) && !"image/png".equals(mimeType) && !"image/webp".equals(mimeType)) {
            deliver(requestId, error("invalid-argument", "The selected photo must be JPEG, PNG, or WebP."));
            return;
        }
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            deliver(requestId, error("unauthenticated", "Sign in to GrowPlot before running Vertex analysis."));
            return;
        }

        Map<String, Object> image = new HashMap<>();
        image.put("mimeType", mimeType);
        image.put("data", imageBase64);
        List<Map<String, Object>> images = new ArrayList<>();
        images.add(image);

        Map<String, Object> context = new HashMap<>();
        try {
            JSONObject source = new JSONObject(contextJson == null ? "{}" : contextJson);
            putIfString(context, "species", source);
            putIfString(context, "symptoms", source);
        } catch (JSONException ignored) { }

        Map<String, Object> request = new HashMap<>();
        request.put("images", images);
        request.put("context", context);

        FirebaseFunctions.getInstance(REGION)
                .getHttpsCallable("vertexImageDiagnosis")
                .call(request)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        HttpsCallableResult result = task.getResult();
                        Object data = result == null ? null : result.getData();
                        if (data instanceof Map) {
                            deliver(requestId, new JSONObject((Map<?, ?>) data));
                        } else {
                            deliver(requestId, error("internal", "Vertex returned an empty response."));
                        }
                        return;
                    }

                    Exception failure = task.getException();
                    String code = "unavailable";
                    if (failure instanceof FirebaseFunctionsException) {
                        code = ((FirebaseFunctionsException) failure).getCode().name().toLowerCase();
                    }
                    String message = failure == null || failure.getMessage() == null
                            ? "The Vertex request failed. Check the connection and try again."
                            : failure.getMessage();
                    deliver(requestId, error(code, message));
                });
    }

    private static void putIfString(Map<String, Object> target, String key, JSONObject source) {
        Object value = source.opt(key);
        if (value instanceof String) target.put(key, value);
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
            String script = "window.GrowPlotVertexDiagnosisResponse&&window.GrowPlotVertexDiagnosisResponse("
                    + JSONObject.quote(requestId) + "," + JSONObject.quote(responseText) + ")";
            webView.evaluateJavascript(script, null);
        });
    }
}
