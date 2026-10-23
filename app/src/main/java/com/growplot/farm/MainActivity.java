package com.growplot.farm;

import android.app.Activity;
import android.app.Dialog;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Intent;
import android.content.ClipData;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Environment;
import android.view.WindowInsets;
import android.view.Window;
import android.view.View;
import android.view.Gravity;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.MediaController;
import android.widget.VideoView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.provider.OpenableColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Base64;
import android.text.Html;
import android.text.InputType;
import android.accounts.Account;
import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.Scope;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

public class MainActivity extends Activity {
    private static final int REQUEST_IMAGE_FOLDER = 410;
    private static final int REQUEST_DEVICE_LOCATION = 411;
    private static final int REQUEST_DIAGNOSIS_PHOTO = 412;
    private static final int REQUEST_DIAGNOSIS_VIDEO = 413;
    private static final int REQUEST_DIAGNOSIS_PICKER = 414;
    private static final int REQUEST_DIAGNOSIS_FOLDER = 415;
    private static final int REQUEST_FEED_CAMERA = 416;
    private static final int REQUEST_FEED_PICKER = 417;
    private static final int REQUEST_DRIVE_AUTHORIZATION = 418;
    private static final int REQUEST_VERTEX_AUTHORIZATION = 419;
    private static final String VERTEX_API_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
    private static final String DRIVE_API_SCOPE = "https://www.googleapis.com/auth/drive";
    // All GrowPlot users store feed images in Rizal Jamin's shared central Drive.
    private static final String DRIVE_OWNER_EMAIL = "rizaljamin@gmail.com";
    private static final String DRIVE_API_FOLDER_KEY_PREFIX = "data_feed_drive_api_folder_";
    private static final String DRIVE_API_EMAIL_KEY_PREFIX = "data_feed_drive_api_email_";
    private static final String DRIVE_API_FOLDER_NAME_KEY_PREFIX = "data_feed_drive_api_folder_name_";
    private static final String DRIVE_API_ACCOUNT_EMAIL_PREFIX = "data_feed_drive_api_account_email_";
    private static final String PREFS = "growplot_snapshot_settings";
    private static final String PREF_SELECTED_URIS = "selected_image_uris";
    private static final String PREF_IMAGE_FEED_TREE_URI = "image_feed_tree_uri";
    private static final String PREF_INTERVAL = "refresh_interval_minutes";
    private static final String PREF_CCTV_SNAPSHOT_INTERVAL_MILLIS = "cctv_snapshot_interval_millis";
    private static final String PREF_CCTV_IP_PREFIX = "cctv_ip_plot-1_";
    private static final String PREF_VIGI_CCTV03_CREDENTIALS = "vigi_cctv03_credentials_encrypted";
    private static final String VIGI_CCTV03_KEY_ALIAS = "GrowPlotVigiCctv03";
    private static final String PREF_TIMELINE_INTERVAL = "photo_timeline_interval";
    private static final String PREF_ACTIVE_PLOT_ID = "active_plot_id";
    private static final String PREF_WEB_STORAGE_MIGRATED = "web_storage_migrated_to_appassets";
    private static final String PREF_DIAGNOSIS_TREE_URI = "diagnosis_tree_uri";
    private static final String PREF_DIAGNOSIS_ROOT_DOCUMENT_ID = "diagnosis_root_document_id";
    private static final String PREF_DIAGNOSIS_FOLDER_LABEL = "diagnosis_folder_label";
    private static final String PREF_GROWPLOT_TREE_URI = "growplot_root_tree_uri";
    private static final String DEFAULT_CCTV01_IP = "192.168.1.241";
    private static final String DEFAULT_CCTV02_IP = "192.168.1.227";
    private static final String DEFAULT_CCTV03_IP = "192.168.1.228";
    private static final String PREF_FARM_DATA_USER_ID = "farm_data_user_id";
    private static final String PREF_PENDING_DIAGNOSIS_UPLOADS = "pending_diagnosis_uploads";
    private static final String GROWPLOT_STORAGE_CONFIG_FILE = "storage-paths.json";
    private static final String DEFAULT_PLOT_ID = "plot-1";
    private volatile String pickerPlotId = DEFAULT_PLOT_ID;
    private volatile boolean pickerCreatesDefaultFeed;
    private volatile String pickerDefaultFolderName = "Demo_plot";
    private volatile String googleDriveAccessToken;
    private final ThreadLocal<StringBuilder> driveApiTrace = ThreadLocal.withInitial(StringBuilder::new);
    private volatile long googleDriveTokenIssuedAt;
    private volatile String vertexAccessToken;
    private volatile long vertexTokenIssuedAt;
    private volatile boolean vertexAuthorizationInProgress;
    private volatile boolean vertexAuthorizationFailedOnce;
    private volatile int pendingVertexCamera;
    private volatile String pendingVertexPlotId = DEFAULT_PLOT_ID;
    private volatile String pendingVertexSpecies = "";
    private volatile String pendingVertexSymptom = "";
    private volatile String pendingVertexPixelMetrics = "";
    private volatile SnapshotBridge pendingVertexBridge;
    private volatile boolean driveAuthorizationInProgress;
    private volatile boolean pendingDriveFolderBrowse;
    private volatile boolean pendingDriveReconnectOnly;
    private volatile String pendingDriveAuthPlotId = DEFAULT_PLOT_ID;
    private volatile String pendingDriveAuthFolderName = "Demo_plot";
    private volatile String locationPermissionPlotId = DEFAULT_PLOT_ID;
    private volatile String locationLookupPlotId = DEFAULT_PLOT_ID;
    private volatile boolean locationRequestInFlight;
    private volatile boolean locationPermissionRequestInFlight;
    private LocationListener locationListener;
    private static final int MAX_FEED_FILES = 1500;
    private static final String APP_ASSET_PREFIX = "/assets/images/";
    private static final String APP_BASELINE_PREFIX = "/assets/baseline/";
    private static final String APP_HISTORY_PREFIX = "/assets/history/";
    private static final String APP_DIAGNOSIS_PREFIX = "/assets/diagnosis/";
    private static final Pattern CAMERA_FILE = Pattern.compile("(?i)^cctv[_ -]?0?([1-6])(?:[_ -].*)?\\.(jpe?g|png)$");
    private static final Pattern CAMERA_LABEL = Pattern.compile("(?i)(?:^|[^A-Z0-9])(?:CCTV|CAMERA|CAM|QUADRANT|QUAD|Q)[ _-]*0?([1-6])(?:[^0-9]|$)");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService scanWorker = Executors.newSingleThreadExecutor();
    private final ExecutorService geocodeWorker = Executors.newSingleThreadExecutor();
    private final ExecutorService diagnosisWorker = Executors.newSingleThreadExecutor();
    private final ExecutorService diagnosisCvWorker = Executors.newSingleThreadExecutor();
    private final ExecutorService kindwiseWorker = Executors.newSingleThreadExecutor();
    private final ExecutorService farmDataWorker = Executors.newSingleThreadExecutor();
    private final ExecutorService cctvSnapshotWorker = Executors.newSingleThreadExecutor();
    private Uri pendingDiagnosisOutput;
    private volatile Uri pendingFeedPhotoOutput;
    private volatile int pendingFeedCamera = 1;
    private volatile String pendingFeedPlotId = DEFAULT_PLOT_ID;
    private String pendingDiagnosisKind = "";
    private String pendingDiagnosisPlotId = DEFAULT_PLOT_ID;
    private String pendingDiagnosisPlotName = "Plot_1";
    private int pendingDiagnosisCamera;
    private WebView web;
    private AccountApiBridge accountApiBridge;
    private FrameLayout appRoot;
    private FrameLayout ipWebcamVlcFrame;
    private VLCVideoLayout ipWebcamVlcLayout;
    private TextView ipWebcamRefreshButton;
    private boolean cctv01OverlayShown;
    private FrameLayout ipWebcam2VlcFrame;
    private VLCVideoLayout ipWebcam2VlcLayout;
    private TextView ipWebcam2RefreshButton;
    private boolean cctv02OverlayShown;
    private FrameLayout ipWebcam3VlcFrame;
    private VLCVideoLayout ipWebcam3VlcLayout;
    private TextView ipWebcam3RefreshButton;
    private boolean cctv03OverlayShown;
    private boolean cctvVlcFeedsAllowedOnActiveScreen;
    private boolean cctv03CredentialsPromptShowing;
    private boolean cctv03CredentialCheckInProgress;
    private boolean cctv03CredentialsRejected;
    private boolean activityResumed;
    private volatile long cctvSnapshotIntervalMillis = 900_000L;
    private final Runnable cctv01SnapshotTicker = new Runnable() {
        @Override public void run() {
            if (!activityResumed || ipWebcamVlcPlayer == null || ipWebcamVlcLayout == null) return;
            checkIpWebcamBeforeSnapshot(1);
            mainHandler.postDelayed(this, cctvSnapshotIntervalMillis);
        }
    };
    private final Runnable cctv02SnapshotTicker = new Runnable() {
        @Override public void run() {
            if (!activityResumed || ipWebcam2VlcPlayer == null || ipWebcam2VlcLayout == null) return;
            checkIpWebcamBeforeSnapshot(2);
            mainHandler.postDelayed(this, nextSnapshotCheckDelay(2));
        }
    };
    private LibVLC ipWebcamVlc;
    private org.videolan.libvlc.MediaPlayer ipWebcamVlcPlayer;
    private LibVLC ipWebcam2Vlc;
    private org.videolan.libvlc.MediaPlayer ipWebcam2VlcPlayer;
    private LibVLC ipWebcam3Vlc;
    private org.videolan.libvlc.MediaPlayer ipWebcam3VlcPlayer;
    private final Runnable cctv03SnapshotTicker = new Runnable() {
        @Override public void run() {
            if (!activityResumed || ipWebcam3VlcPlayer == null || ipWebcam3VlcLayout == null) return;
            checkIpWebcamBeforeSnapshot(3);
            mainHandler.postDelayed(this, cctvSnapshotIntervalMillis);
        }
    };
    private boolean pageReady;
    private volatile boolean scanInProgress;
    private volatile boolean drivePathRestorePending;
    private volatile boolean rescanRequested;
    private String pendingPageUpdate;
    private volatile int refreshIntervalMinutes = 5;
    private volatile String timelineInterval = "daily";

    private final Runnable scheduledScan = new Runnable() {
        @Override public void run() {
            scanSnapshotFolder();
            mainHandler.postDelayed(this, refreshIntervalMinutes * 60_000L);
        }
    };

    private static final class Photo {
        String name;
        String sourceKey;
        Uri uri;
        int camera;
        long capturedAt;
        long modifiedAt;
        String displayTime;
        String historyFile;
        String plotId;
    }

    private static final class FrameFeatures {
        double greenPercent;
        double brightness;
    }

    private static final class DriveFeedFolderOption {
        final String id; final String path;
        DriveFeedFolderOption(String id, String path) { this.id = id; this.path = path; }
    }

    private static final class FeedDocument {
        Uri uri;
        String name;
        int cameraHint;
        FeedDocument(Uri uri, String name, int cameraHint) { this.uri = uri; this.name = name; this.cameraHint = cameraHint; }
    }

    private WebResourceResponse localImageResponse(String url) {
        Uri uri = Uri.parse(url);
        String path = uri.getPath();
        if (path == null) return null;
        if (path.startsWith(APP_BASELINE_PREFIX)) {
            String name = path.substring(APP_BASELINE_PREFIX.length());
            if (!name.matches("cctv_0[1-6]\\.jpg")) return null;
            try {
                return new WebResourceResponse("image/jpeg", null, getAssets().open("images/" + name));
            } catch (Exception ignored) { return null; }
        }
        if (path.startsWith(APP_HISTORY_PREFIX)) {
            String relative = path.substring(APP_HISTORY_PREFIX.length());
            if (!relative.matches("[A-Za-z0-9_-]{1,64}/cctv_0[1-6]_\\d+_[0-9a-f]+\\.jpg")) return null;
            String[] pieces = relative.split("/", 2);
            try {
                File archived = new File(new File(new File(getFilesDir(), "photo_history"), pieces[0]), pieces[1]);
                if (!archived.isFile()) return null;
                return new WebResourceResponse("image/jpeg", null, new FileInputStream(archived));
            } catch (Exception ignored) { return null; }
        }
        if (!path.startsWith(APP_ASSET_PREFIX)) return null;
        String relative = path.substring(APP_ASSET_PREFIX.length());
        if (!relative.matches("[A-Za-z0-9_-]{1,64}/cctv_0[1-6]\\.jpg")) return null;
        String[] pieces = relative.split("/", 2);
        try {
            File latest = new File(new File(new File(getFilesDir(), "latest_images"), pieces[0]), pieces[1]);
            if (!latest.isFile() && !DEFAULT_PLOT_ID.equals(pieces[0])) return null;
            InputStream input = latest.isFile() ? new FileInputStream(latest) : getAssets().open("images/" + pieces[1]);
            return new WebResourceResponse("image/jpeg", null, input);
        } catch (Exception ignored) { return null; }
    }

    private WebResourceResponse localDiagnosisMediaResponse(String url, Map<String, String> headers) {
        String path = Uri.parse(url).getPath();
        if (path == null || !path.startsWith(APP_DIAGNOSIS_PREFIX)) return null;
        String relative = path.substring(APP_DIAGNOSIS_PREFIX.length());
        if (!relative.matches("[A-Za-z0-9_-]{1,64}/cctv_0[1-6]/[A-Za-z0-9_-]{1,64}\\.(jpg|mp4)")) return null;
        String[] parts = relative.split("/");
        File media = new File(new File(new File(getFilesDir(), "diagnosis_media"), parts[0]), parts[1] + "/" + parts[2]);
        if (!media.isFile()) {
            media = new File(new File(new File(getCacheDir(), "diagnosis_drive_cache"), parts[0]), parts[1] + "/" + parts[2]);
        }
        if (!media.isFile()) return null;
        try {
            boolean video = parts[2].endsWith(".mp4");
            long start = 0;
            long end = Math.max(0, media.length() - 1);
            String range = null;
            if (headers != null) for (Map.Entry<String, String> entry : headers.entrySet()) {
                if ("range".equalsIgnoreCase(entry.getKey())) { range = entry.getValue(); break; }
            }
            if (video && range != null && range.toLowerCase(Locale.ROOT).startsWith("bytes=")) {
                String[] bounds = range.substring(6).split("-", 2);
                if (bounds.length != 2 || media.length() == 0) throw new IllegalArgumentException("Invalid video byte range");
                if (bounds[0].isEmpty()) {
                    long suffixLength = Long.parseLong(bounds[1]);
                    start = Math.max(0, media.length() - suffixLength);
                } else {
                    start = Long.parseLong(bounds[0]);
                    if (!bounds[1].isEmpty()) end = Long.parseLong(bounds[1]);
                }
                end = Math.min(end, media.length() - 1);
                if (start < 0 || start >= media.length() || end < start) {
                    Map<String, String> invalidRangeHeaders = new HashMap<>();
                    invalidRangeHeaders.put("Content-Range", "bytes */" + media.length());
                    invalidRangeHeaders.put("Content-Length", "0");
                    return new WebResourceResponse("video/mp4", null, 416, "Range Not Satisfiable",
                            invalidRangeHeaders, new ByteArrayInputStream(new byte[0]));
                }
            }
            FileInputStream stream = new FileInputStream(media);
            long skipped = stream.skip(start);
            while (skipped < start) {
                long more = stream.skip(start - skipped);
                if (more <= 0) break;
                skipped += more;
            }
            Map<String, String> responseHeaders = new HashMap<>();
            responseHeaders.put("Accept-Ranges", "bytes");
            responseHeaders.put("Content-Length", String.valueOf(Math.max(0, end - start + 1)));
            responseHeaders.put("Cache-Control", "no-store");
            if (video && range != null) responseHeaders.put("Content-Range", "bytes " + start + "-" + end + "/" + media.length());
            return new WebResourceResponse(video ? "video/mp4" : "image/jpeg", null,
                    video && range != null ? 206 : 200, video && range != null ? "Partial Content" : "OK",
                    responseHeaders, stream);
        } catch (Exception ignored) { return null; }
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null) AdminActivationDemo.handleLink(this, intent.getData());
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (getIntent() != null && getIntent().getData() != null) {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                    () -> AdminActivationDemo.handleLink(this, getIntent().getData()), 900);
        }
        if (state != null) {
            pendingDiagnosisKind = state.getString("diagnosis_kind", "");
            pendingDiagnosisPlotId = safePlotId(state.getString("diagnosis_plot", DEFAULT_PLOT_ID));
            pendingDiagnosisPlotName = safeFolderName(state.getString("diagnosis_plot_name", "Plot_1"));
            pendingDiagnosisCamera = state.getInt("diagnosis_camera", 0);
            pendingDiagnosisOutput = state.getParcelable("diagnosis_output");
            pickerPlotId = safePlotId(state.getString("feed_picker_plot", DEFAULT_PLOT_ID));
            pickerDefaultFolderName = safeFolderName(state.getString("feed_picker_folder", "Demo_plot"));
            pickerCreatesDefaultFeed = state.getBoolean("feed_picker_creates_default", false);
        }
        refreshIntervalMinutes = getPrefs().getInt(PREF_INTERVAL, 5);
        cctvSnapshotIntervalMillis = getPrefs().getLong(PREF_CCTV_SNAPSHOT_INTERVAL_MILLIS, 900_000L);
        if (cctvSnapshotIntervalMillis < 30_000L || cctvSnapshotIntervalMillis > 86_400_000L) {
            cctvSnapshotIntervalMillis = 900_000L;
        }
        timelineInterval = getPrefs().getString(PREF_TIMELINE_INTERVAL, "daily");
        if (!"daily".equals(timelineInterval) && !"weekly".equals(timelineInterval) && !"monthly".equals(timelineInterval)
                && !"yearly".equals(timelineInterval)) timelineInterval = "daily";
        web = new WebView(this);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setLoadsImagesAutomatically(true);
        // CCTV01 embeds the local IP Webcam HTTP MJPEG stream in the app's HTTPS asset page.
        // Allow that mixed resource; network_security_config still permits cleartext only to
        // 192.168.1.241 and 192.168.1.227, so this does not enable HTTP to arbitrary hosts.
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setBlockNetworkLoads(false);
        settings.setBlockNetworkImage(false);
        settings.setUserAgentString(settings.getUserAgentString() + " GrowPlot/" + BuildConfig.VERSION_NAME.replace(' ', '-'));
        web.addJavascriptInterface(new SnapshotBridge(), "GrowPlotBridge");
        web.addJavascriptInterface(new OpenCvHsvBridge(), "GrowPlotOpenCV");
        web.addJavascriptInterface(new VertexDiagnosisBridge(this, web), "GrowPlotVertex");
        accountApiBridge = new AccountApiBridge(this, web);
        web.addJavascriptInterface(accountApiBridge, "GrowPlotAccount");
        web.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
      String requestedUrl = request.getUrl().toString();
      if ("https://appassets.androidplatform.net/leaf-cv.js".equals(requestedUrl)) {
          try {
              return new WebResourceResponse("application/javascript", "UTF-8", getAssets().open("leaf-cv.js"));
          } catch (Exception error) {
              android.util.Log.e("GrowPlotCV", "Failed to serve bundled leaf-cv.js", error);
          }
      }

                WebResourceResponse diagnosis = localDiagnosisMediaResponse(request.getUrl().toString(), request.getRequestHeaders());
                if (diagnosis != null) return diagnosis;
                WebResourceResponse image = localImageResponse(request.getUrl().toString());
                return image != null ? image : super.shouldInterceptRequest(view, request);
            }

            @Override public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                WebResourceResponse diagnosis = localDiagnosisMediaResponse(url, null);
                if (diagnosis != null) return diagnosis;
                WebResourceResponse image = localImageResponse(url);
                return image != null ? image : super.shouldInterceptRequest(view, url);
            }

            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url != null && url.startsWith("file:///android_asset/migrate.html")) return;
                pageReady = true;
                try (InputStream onboardingInput = getAssets().open("new-user-onboarding.js");
                     ByteArrayOutputStream onboardingBytes = new ByteArrayOutputStream()) {
                    byte[] onboardingBuffer = new byte[8192];
                    int onboardingRead;
                    while ((onboardingRead = onboardingInput.read(onboardingBuffer)) != -1)
                        onboardingBytes.write(onboardingBuffer, 0, onboardingRead);
                    view.evaluateJavascript(onboardingBytes.toString(StandardCharsets.UTF_8.name()), null);
                } catch (Exception onboardingError) {
                    android.util.Log.w("GrowPlot", "New-user onboarding preview could not load", onboardingError);
                }
                try (InputStream accountInput = getAssets().open("account-login.js"); ByteArrayOutputStream accountBytes = new ByteArrayOutputStream()) {
                    byte[] accountBuffer = new byte[8192]; int accountRead;
                    while ((accountRead = accountInput.read(accountBuffer)) != -1) accountBytes.write(accountBuffer, 0, accountRead);
                    view.evaluateJavascript(accountBytes.toString(StandardCharsets.UTF_8.name()), null);
                } catch (Exception accountError) { android.util.Log.e("GrowPlot", "Account login UI could not load", accountError); }
                try (InputStream dataFeedUiInput = getAssets().open("data-feed-ui.js");
                     ByteArrayOutputStream dataFeedUiBytes = new ByteArrayOutputStream()) {
                    byte[] dataFeedUiBuffer = new byte[4096];
                    int dataFeedUiRead;
                    while ((dataFeedUiRead = dataFeedUiInput.read(dataFeedUiBuffer)) != -1)
                        dataFeedUiBytes.write(dataFeedUiBuffer, 0, dataFeedUiRead);
                    view.evaluateJavascript(dataFeedUiBytes.toString(StandardCharsets.UTF_8.name()), null);
                } catch (Exception dataFeedUiError) {
                    android.util.Log.e("GrowPlot", "Data feed UI could not load", dataFeedUiError);
                }
                if (pendingPageUpdate != null) {
                    String update = pendingPageUpdate;
                    pendingPageUpdate = null;
                    sendToPage(update);
                } else {
                    JSONObject initial = makeStatus("Checking this plot’s Google Drive data feed connection.");
                    try { initial.put("plotId", safePlotId(getPrefs().getString(PREF_ACTIVE_PLOT_ID, DEFAULT_PLOT_ID))); } catch (Exception ignored) { }
                    publishStatus(initial);
                }
                if (url != null && url.startsWith("https://appassets.androidplatform.net/")) {
                    view.post(MainActivity.this::installCctv01VlcPositionListener);
                }
            }
        });
        web.setBackgroundColor(0xFFF4F7F1);
        appRoot = new FrameLayout(this);
        appRoot.setBackgroundColor(0xFFF4F7F1);
        appRoot.addView(web, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            appRoot.setOnApplyWindowInsetsListener((view, insets) -> {
                int topInset = insets.getInsets(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.displayCutout()).top;
                view.setPadding(0, topInset, 0, 0);
                return insets.inset(0, topInset, 0, 0);
            });
        }
        getWindow().setStatusBarColor(0xFFF4F7F1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(decor.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(appRoot);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) appRoot.requestApplyInsets();
        if (getPrefs().getBoolean(PREF_WEB_STORAGE_MIGRATED, false)) loadMainPage();
        else web.loadUrl("file:///android_asset/migrate.html");
        drivePathRestorePending = true;
        diagnosisWorker.execute(() -> {
            try {
                restoreDrivePathsFromConfig();
                if (!isDiagnosisDriveReady()) adoptExistingGrowPlotDriveGrant();
            } finally {
                runOnUiThread(() -> {
                    drivePathRestorePending = false;
                    if (isFinishing()) return;
                    scanSnapshotFolder();
                    refreshDiagnosisMediaForActivePlot();
                });
            }
        });
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        outState.putString("diagnosis_kind", pendingDiagnosisKind);
        outState.putString("diagnosis_plot", pendingDiagnosisPlotId);
        outState.putString("diagnosis_plot_name", pendingDiagnosisPlotName);
        outState.putInt("diagnosis_camera", pendingDiagnosisCamera);
        if (pendingDiagnosisOutput != null) outState.putParcelable("diagnosis_output", pendingDiagnosisOutput);
        outState.putString("feed_picker_plot", pickerPlotId);
        outState.putString("feed_picker_folder", pickerDefaultFolderName);
        outState.putBoolean("feed_picker_creates_default", pickerCreatesDefaultFeed);
        super.onSaveInstanceState(outState);
    }

    private SharedPreferences getPrefs() {
        return getSharedPreferences(PREFS, MODE_PRIVATE);
    }

    private String defaultCctvIp(int camera) {
        if (camera == 1) return DEFAULT_CCTV01_IP;
        if (camera == 2) return DEFAULT_CCTV02_IP;
        if (camera == 3) return DEFAULT_CCTV03_IP;
        return "";
    }

    private String cctvIp(int camera) {
        return getPrefs().getString(PREF_CCTV_IP_PREFIX + String.format(Locale.US, "%02d", camera), defaultCctvIp(camera));
    }

    // Owner-scoped camera routes. Demo plot deliberately keeps legacy local IP settings.
    private final String[] assignedStreamUrls = new String[4];
    private volatile boolean userStreamMode = false;
    private int assignedStreamGeneration = 0;
    private String activeStreamScope = "";
    private boolean assignedStreamsLoading = false;

    private void refreshAssignedPlayers() {
        if (ipWebcamVlcLayout != null) startIpWebcamVlc(ipWebcamVlcLayout);
        if (ipWebcam2VlcLayout != null) startCctv02Vlc(ipWebcam2VlcLayout);
        if (ipWebcam3VlcLayout != null) startVigiCctv03Vlc(ipWebcam3VlcLayout);
    }

    private void refreshAssignedPlayer(int slot) {
        // An assignment may be removed or disabled: clear the old frame instead of retaining it.
        if (userStreamMode && (slot < 1 || slot > 3 || assignedStreamUrls[slot] == null || assignedStreamUrls[slot].isEmpty())) {
            resetCctvSurfaces();
            for (int camera = 1; camera <= 3; camera++) {
                if (assignedStreamUrls[camera] != null && !assignedStreamUrls[camera].isEmpty()) {
                    if (camera == 1 && ipWebcamVlcLayout != null) startIpWebcamVlc(ipWebcamVlcLayout);
                    if (camera == 2 && ipWebcam2VlcLayout != null) startCctv02Vlc(ipWebcam2VlcLayout);
                    if (camera == 3 && ipWebcam3VlcLayout != null) startVigiCctv03Vlc(ipWebcam3VlcLayout);
                }
            }
            return;
        }
        if (slot == 1 && ipWebcamVlcLayout != null) startIpWebcamVlc(ipWebcamVlcLayout);
        else if (slot == 2 && ipWebcam2VlcLayout != null) startCctv02Vlc(ipWebcam2VlcLayout);
        else if (slot == 3 && ipWebcam3VlcLayout != null) startVigiCctv03Vlc(ipWebcam3VlcLayout);
    }

    // Reset every video surface whenever the selected camera scope changes.
    // Merely stopping VLC leaves its final decoded frame visible in TextureView.
    private void resetCctvSurfaces() {
        stopIpWebcamVlc();
        stopCctv02Vlc();
        stopCctv03Vlc();
        // VLC detach/stop may leave a previously decoded frame in TextureView.
        // Destroy each old video surface, then install a fresh black surface BELOW status controls.
        if (ipWebcamVlcFrame != null && ipWebcamVlcLayout != null) {
            ipWebcamVlcFrame.removeView(ipWebcamVlcLayout);
            ipWebcamVlcLayout = new VLCVideoLayout(this);
            ipWebcamVlcLayout.setBackgroundColor(Color.BLACK);
            ipWebcamVlcFrame.addView(ipWebcamVlcLayout, 0, new FrameLayout.LayoutParams(-1, -1));
        }
        if (ipWebcam2VlcFrame != null && ipWebcam2VlcLayout != null) {
            ipWebcam2VlcFrame.removeView(ipWebcam2VlcLayout);
            ipWebcam2VlcLayout = new VLCVideoLayout(this);
            ipWebcam2VlcLayout.setBackgroundColor(Color.BLACK);
            ipWebcam2VlcFrame.addView(ipWebcam2VlcLayout, 0, new FrameLayout.LayoutParams(-1, -1));
        }
        if (ipWebcam3VlcFrame != null && ipWebcam3VlcLayout != null) {
            ipWebcam3VlcFrame.removeView(ipWebcam3VlcLayout);
            ipWebcam3VlcLayout = new VLCVideoLayout(this);
            ipWebcam3VlcLayout.setBackgroundColor(Color.BLACK);
            ipWebcam3VlcFrame.addView(ipWebcam3VlcLayout, 0, new FrameLayout.LayoutParams(-1, -1));
        }
        if (ipWebcamVlcFrame != null) ipWebcamVlcFrame.setBackgroundColor(Color.BLACK);
        if (ipWebcam2VlcFrame != null) ipWebcam2VlcFrame.setBackgroundColor(Color.BLACK);
        if (ipWebcam3VlcFrame != null) ipWebcam3VlcFrame.setBackgroundColor(Color.BLACK);
    }

    private void returnToDemoStreams() {
        ++assignedStreamGeneration;
        activeStreamScope = "";
        java.util.Arrays.fill(assignedStreamUrls, null);
        // Demo is independent of Firebase and uses the three Settings camera URLs.
        userStreamMode = false;
        assignedStreamsLoading = false;
        resetCctvSurfaces();
        refreshAssignedPlayers();
    }

    private void loadAssignedStreams(String plotId, String greenhouseId, boolean force) {
        final String uidNow = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null ? "" : com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        String scope = uidNow+"|"+plotId+"|"+greenhouseId;
        if (!force && scope.equals(activeStreamScope)) return;
        activeStreamScope = scope;
        final int generation = ++assignedStreamGeneration;
        userStreamMode = !"plot-1".equals(plotId);
        java.util.Arrays.fill(assignedStreamUrls, null);
        assignedStreamsLoading = userStreamMode;
        resetCctvSurfaces(); // Always reset all old frames before starting the newly selected scope.
        if (!userStreamMode) refreshAssignedPlayers(); // Demo uses saved Settings IPs.
        if (!userStreamMode) { return; }
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || !plotId.matches("[A-Za-z0-9_-]{1,100}") || !greenhouseId.matches("greenhouse-[1-9][0-9]*")) {
            return;
        }
        final String uid = user.getUid();
        com.google.firebase.firestore.FirebaseFirestore store = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        // Administrator scope is granted only by a verified Firebase custom claim.
        user.getIdToken(false).addOnSuccessListener(token -> {
            boolean admin = Boolean.TRUE.equals(token.getClaims().get("admin"))
                    || Boolean.TRUE.equals(token.getClaims().get("powerUser"));
            com.google.firebase.firestore.Query assignments = admin
                    ? store.collection("feed_assignments")
                    : store.collection("feed_assignments").whereEqualTo("ownerUserId",uid);
            assignments.get().addOnSuccessListener(rows -> {
                if (generation != assignedStreamGeneration) return;
                assignedStreamsLoading = false;
                for (com.google.firebase.firestore.QueryDocumentSnapshot assignment : rows) {
                    if (!plotId.equals(assignment.getString("plotId")) || !greenhouseId.equals(assignment.getString("greenhouseId"))) continue;
                    String section = assignment.getString("sectionId"), deviceId = assignment.getString("deviceId");
                    if (section == null || deviceId == null || !section.matches("section-[1-3]") || !deviceId.matches("[A-Za-z0-9_-]{1,100}")) continue;
                    int slot = Integer.parseInt(section.substring(8));
                    String ownerUid=assignment.getString("ownerUserId");
                    if(ownerUid==null||(!admin&&!uid.equals(ownerUid)))continue;
                    store.collection("data_feeds").document(ownerUid+"_"+deviceId).get().addOnSuccessListener(device -> {
                        if (generation != assignedStreamGeneration || !device.exists() || !ownerUid.equals(device.getString("ownerUserId")) || !("AV".equalsIgnoreCase(device.getString("deviceType")) || "Camera".equalsIgnoreCase(device.getString("deviceType")))) return;
                        String host = device.getString("ipAddress"),protocol=device.getString("protocol"),path=device.getString("streamPath");
                        Number port = device.getDouble("port");
                        if (host == null || !host.matches("[A-Za-z0-9.-]{1,253}")) return;
                        String scheme = "RTSP".equalsIgnoreCase(protocol) ? "rtsp" : "HTTPS".equalsIgnoreCase(protocol) ? "https" : "http";
                        int actualPort = port == null ? ("rtsp".equals(scheme)?554:8080) : port.intValue();
                        if (actualPort < 1 || actualPort > 65535) return;
                        if (path == null || path.isEmpty()) path = "rtsp".equals(scheme) ? "/stream1" : "/videofeed";
                        if (!path.startsWith("/") || path.contains("..") || path.contains("@") || path.contains("?")) return;
                        assignedStreamUrls[slot] = scheme+"://"+host+":"+actualPort+path;
                        refreshAssignedPlayer(slot);
                    });
                }
            }).addOnFailureListener(e -> { assignedStreamsLoading = false; android.util.Log.w("GrowPlotStreams", "Assignment retrieval failed", e); });
        }).addOnFailureListener(e -> { assignedStreamsLoading=false; android.util.Log.w("GrowPlotStreams","Privilege verification failed",e); });
    }

    private String cctvStreamUrl(int camera) {
        if (userStreamMode) return assignedStreamUrls[camera] == null ? "" : assignedStreamUrls[camera];
        String ip = cctvIp(camera);
        return ip.isEmpty() ? "" : "http://" + ip + ":8080/videofeed";
    }

    private String[] vigiCctv03Credentials() {
        try {
            String encoded = getPrefs().getString(PREF_VIGI_CCTV03_CREDENTIALS, "");
            if (encoded.isEmpty()) return new String[]{"", ""};
            byte[] combined = Base64.decode(encoded, Base64.NO_WRAP);
            byte[] iv = java.util.Arrays.copyOfRange(combined, 0, 12);
            byte[] ciphertext = java.util.Arrays.copyOfRange(combined, 12, combined.length);
            KeyStore store = KeyStore.getInstance("AndroidKeyStore");
            store.load(null);
            SecretKey key = (SecretKey) store.getKey(VIGI_CCTV03_KEY_ALIAS, null);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            JSONObject credentials = new JSONObject(new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8));
            return new String[]{credentials.optString("username", ""), credentials.optString("password", "")};
        } catch (Exception ignored) { return new String[]{"", ""}; }
    }

    boolean saveVigiCctv03Credentials(String username, String password) {
        try {
            if ((username == null || username.isEmpty()) && (password == null || password.isEmpty())) {
                return true; // Empty Settings fields must not erase a saved camera login.
            }
            if (password == null || password.isEmpty()) {
                String[] previous = vigiCctv03Credentials();
                if (!previous[1].isEmpty()) password = previous[1];
            }
            KeyStore store = KeyStore.getInstance("AndroidKeyStore");
            store.load(null);
            if (!store.containsAlias(VIGI_CCTV03_KEY_ALIAS)) {
                KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                generator.init(new KeyGenParameterSpec.Builder(VIGI_CCTV03_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(true).build());
                generator.generateKey();
                store.load(null);
            }
            SecretKey key = (SecretKey) store.getKey(VIGI_CCTV03_KEY_ALIAS, null);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            JSONObject credentials = new JSONObject();
            credentials.put("username", username == null ? "" : username);
            credentials.put("password", password == null ? "" : password);
            byte[] ciphertext = cipher.doFinal(credentials.toString().getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[cipher.getIV().length + ciphertext.length];
            System.arraycopy(cipher.getIV(), 0, combined, 0, cipher.getIV().length);
            System.arraycopy(ciphertext, 0, combined, cipher.getIV().length, ciphertext.length);
            if (!getPrefs().edit().putString(PREF_VIGI_CCTV03_CREDENTIALS, Base64.encodeToString(combined, Base64.NO_WRAP)).commit()) return false;
            cctv03CredentialsRejected = false;
            return true;
        } catch (Exception ignored) { return false; }
    }

    private String pluginApiKey(String provider) {
        try {
            String encoded = getPrefs().getString("plugin_api_keys_encrypted", "");
            if (encoded.isEmpty()) return "";
            byte[] bytes = Base64.decode(encoded, Base64.NO_WRAP);
            KeyStore store = KeyStore.getInstance("AndroidKeyStore"); store.load(null);
            SecretKey key = (SecretKey) store.getKey(VIGI_CCTV03_KEY_ALIAS, null);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, java.util.Arrays.copyOfRange(bytes, 0, 12)));
            JSONObject keys = new JSONObject(new String(cipher.doFinal(java.util.Arrays.copyOfRange(bytes, 12, bytes.length)), StandardCharsets.UTF_8));
            return keys.optString(provider, "");
        } catch (Exception unavailable) { return ""; }
    }

    private boolean savePluginApiKeys(String payload) {
        try {
            JSONObject updates = new JSONObject(payload), keys = new JSONObject();
            for (String provider : new String[]{"plantnet", "plantid", "kindwise"}) {
                String entered = updates.optString(provider, "").trim();
                keys.put(provider, entered.isEmpty() ? pluginApiKey(provider) : entered);
            }
            KeyStore store = KeyStore.getInstance("AndroidKeyStore"); store.load(null);
            if (!store.containsAlias(VIGI_CCTV03_KEY_ALIAS)) {
                KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                generator.init(new KeyGenParameterSpec.Builder(VIGI_CCTV03_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(true).build()); generator.generateKey(); store.load(null);
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, (SecretKey) store.getKey(VIGI_CCTV03_KEY_ALIAS, null));
            byte[] encrypted = cipher.doFinal(keys.toString().getBytes(StandardCharsets.UTF_8));
            byte[] bytes = new byte[cipher.getIV().length + encrypted.length];
            System.arraycopy(cipher.getIV(), 0, bytes, 0, cipher.getIV().length);
            System.arraycopy(encrypted, 0, bytes, cipher.getIV().length, encrypted.length);
            return getPrefs().edit().putString("plugin_api_keys_encrypted", Base64.encodeToString(bytes, Base64.NO_WRAP)).commit();
        } catch (Exception error) { return false; }
    }

    private boolean isPrivateIpv4OrBlank(String address) {
        if (address == null || address.trim().isEmpty()) return true;
        String[] octets = address.trim().split("\\.", -1);
        if (octets.length != 4) return false;
        int[] values = new int[4];
        for (int i = 0; i < 4; i++) {
            if (!octets[i].matches("\\d{1,3}")) return false;
            try { values[i] = Integer.parseInt(octets[i]); }
            catch (NumberFormatException ignored) { return false; }
            if (values[i] < 0 || values[i] > 255) return false;
        }
        // Tailscale assigns IPv4 node addresses from 100.64.0.0/10.
        // Permit these VPN addresses without allowing arbitrary public endpoints.
        boolean privateAddress = values[0] == 10
                || (values[0] == 172 && values[1] >= 16 && values[1] <= 31)
                || (values[0] == 192 && values[1] == 168);
        boolean tailscaleAddress = values[0] == 100 && values[1] >= 64 && values[1] <= 127;
        return (privateAddress || tailscaleAddress) && values[3] > 0 && values[3] < 255;
    }

    private boolean hasPersistedReadWriteGrant(Uri treeUri) {
        if (treeUri == null) return false;
        for (android.content.UriPermission grant : getContentResolver().getPersistedUriPermissions()) {
            if (grant.getUri().equals(treeUri) && grant.isReadPermission() && grant.isWritePermission()) return true;
        }
        return false;
    }

    private Uri findGrowPlotScopedTreeUri() {
        SharedPreferences preferences = getPrefs();
        Set<String> candidates = new java.util.LinkedHashSet<>();
        for (String key : new String[]{PREF_GROWPLOT_TREE_URI, PREF_DIAGNOSIS_TREE_URI,
                PREF_IMAGE_FEED_TREE_URI, feedTreeKey(DEFAULT_PLOT_ID)}) {
            String value = preferences.getString(key, null);
            if (value != null) candidates.add(value);
        }
        for (String key : preferences.getAll().keySet()) {
            if (key.startsWith("data_feed_tree_uri_")) {
                String value = preferences.getString(key, null);
                if (value != null) candidates.add(value);
            }
        }
        for (android.content.UriPermission grant : getContentResolver().getPersistedUriPermissions()) {
            Uri uri = grant.getUri();
            if (uri.getAuthority() != null && uri.getAuthority().toLowerCase(Locale.ROOT)
                    .contains("google.android.apps.docs")) candidates.add(uri.toString());
        }
        for (String value : candidates) {
            try {
                Uri candidate = Uri.parse(value);
                String authority = candidate.getAuthority();
                if (authority == null || !authority.toLowerCase(Locale.ROOT).contains("google.android.apps.docs")
                        || !hasPersistedReadWriteGrant(candidate)) continue;
                String name = getDocumentName(candidate);
                if ("GrowPlot".equalsIgnoreCase(name) || "Projects".equalsIgnoreCase(name)
                        || "My Drive".equalsIgnoreCase(name) || "Drive".equalsIgnoreCase(name)) return candidate;
            } catch (Exception ignored) { }
        }
        return null;
    }

    private String resolveGrowPlotDocumentId(Uri treeUri) throws Exception {
        String id = DocumentsContract.getTreeDocumentId(treeUri);
        String selectedName = getDocumentName(treeUri);
        if ("growplot".equalsIgnoreCase(selectedName)) return id;
        if ("Projects".equalsIgnoreCase(selectedName)) return findOrCreateChildFolder(treeUri, id, "growplot");
        if ("My Drive".equalsIgnoreCase(selectedName) || "Drive".equalsIgnoreCase(selectedName)) {
            String projectsId = findOrCreateChildFolder(treeUri, id, "Projects");
            return findOrCreateChildFolder(treeUri, projectsId, "growplot");
        }
        return null;
    }

    private void rememberGrowPlotTreeIfScoped(Uri treeUri) {
        try {
            if (resolveGrowPlotDocumentId(treeUri) != null) {
                getPrefs().edit().putString(PREF_GROWPLOT_TREE_URI, treeUri.toString()).apply();
            }
        } catch (Exception ignored) { }
    }

    private String relativePathFromGrowPlot(String displayPath) {
        if (displayPath == null) return null;
        for (String marker : new String[]{"/Projects/growplot/", "/My Drive/GrowPlot/", "/GrowPlot/"}) {
            int index = displayPath.indexOf(marker);
            if (index >= 0) {
                String path = displayPath.substring(index + marker.length()).replace('\\', '/');
                if (path.matches("[A-Za-z0-9 _./-]{1,240}") && !path.contains("../") && !path.startsWith("../")) return path;
            }
        }
        return null;
    }

    private void installCctv01VlcPositionListener() {
        if (web == null || isFinishing()) return;
        web.evaluateJavascript("(()=>{const host=document.querySelector('#cameras');if(!host)return false;let css=document.getElementById('growplot-vlc-cctv-style');if(!css){css=document.createElement('style');css.id='growplot-vlc-cctv-style';css.textContent='#cameras .camera:nth-child(-n+3) .feedimg,#cameras .camera:nth-child(-n+3) .mocktag,#cameras .camera:nth-child(-n+3) .stamp{display:none!important}';document.head.appendChild(css)}let scheduled=false,last=['','',''];const send=()=>{if(scheduled)return true;scheduled=true;requestAnimationFrame(()=>{scheduled=false;const dashboard=document.getElementById('mainDashboard'),health=document.getElementById('healthPanel'),auth=document.getElementById('authScreen'),blockingOverlayActive=[...document.querySelectorAll('.modal.open,dialog[open],[role=\"dialog\"][aria-modal=\"true\"],#growplotUserManagementPage')].some(el=>{if(el.hidden)return false;const style=getComputedStyle(el);return style.display!=='none'&&style.visibility!=='hidden'&&style.visibility!=='collapse'&&el.getClientRects().length>0}),homeActive=!!dashboard&&!dashboard.hidden&&(!health||health.hidden)&&!blockingOverlayActive&&(!auth||auth.hidden);if(window.GrowPlotBridge)GrowPlotBridge.setCctvVlcFeedsAllowedOnActiveScreen(homeActive);for(let i=0;i<3;i++){const e=document.querySelector('#cameras .camera:nth-child('+(i+1)+') .feed');if(!e)continue;const im=e.querySelector('.feedimg');if(im&&im.hasAttribute('src'))im.removeAttribute('src');const r=e.getBoundingClientRect(),offscreen=r.bottom<=0||r.top>=window.innerHeight,coords=[r.left,r.top,r.width,r.height,offscreen?1:0,homeActive?1:0].join(',');if(coords!==last[i]){last[i]=coords;if(i===0)GrowPlotBridge.updateCctv01VlcPosition(coords);else if(i===1)GrowPlotBridge.updateCctv02VlcPosition(coords);else GrowPlotBridge.updateCctv03VlcPosition(coords)}}});return true};if(!window.__growplotVlcPositionInstalled){window.addEventListener('scroll',send,{passive:true});window.addEventListener('resize',send,{passive:true});new MutationObserver(send).observe(host,{childList:true,subtree:true});new MutationObserver(send).observe(document.body,{childList:true,subtree:true,attributes:true,attributeFilter:['hidden','class','open','style','role','aria-modal']});window.__growplotVlcPositionInstalled=true}send();setTimeout(send,400);setTimeout(send,1200);return true})()", null);
    }

    /** Keep native VLC children out of the way unless the dashboard is active. */
    private void setCctvVlcFeedsAllowedOnActiveScreen(boolean allowed) {
        cctvVlcFeedsAllowedOnActiveScreen = allowed;
        if (allowed) return; // Position callbacks reveal feeds after valid dashboard bounds arrive.
        hideVlcFrame(ipWebcamVlcFrame);
        hideVlcFrame(ipWebcam2VlcFrame);
        hideVlcFrame(ipWebcam3VlcFrame);
        cctv01OverlayShown = false;
        cctv02OverlayShown = false;
        cctv03OverlayShown = false;
    }

    private void hideVlcFrame(FrameLayout frame) {
        if (frame == null) return;
        frame.animate().cancel();
        frame.setAlpha(0f);
        frame.setVisibility(View.INVISIBLE);
    }

    private void positionCctv01Vlc(String coordinates) {
        if (appRoot == null || coordinates == null || isFinishing()) return;
        try {
            String[] values = coordinates.split(",");
            if (values.length < 6) return;
            float density = getResources().getDisplayMetrics().density;
            int left = Math.round(Float.parseFloat(values[0]) * density);
            int top = Math.round(Float.parseFloat(values[1]) * density);
            int width = Math.round(Float.parseFloat(values[2]) * density);
            int height = Math.round(Float.parseFloat(values[3]) * density);
            boolean offscreen = "1".equals(values[4]);
            boolean homeActive = "1".equals(values[5]);
            boolean shouldShow = !offscreen && homeActive && cctvVlcFeedsAllowedOnActiveScreen;
            if (width <= 0 || height <= 0) return;

            if (ipWebcamVlcFrame == null) {
                ipWebcamVlcFrame = new FrameLayout(this);
                ipWebcamVlcFrame.setBackgroundColor(Color.BLACK);
                ipWebcamVlcFrame.setAlpha(0f);
                ipWebcamVlcFrame.setVisibility(View.INVISIBLE);
                ipWebcamVlcFrame.setClipChildren(true);
                ipWebcamVlcFrame.setClipToPadding(true);
                ipWebcamVlcFrame.setElevation(dp(8));
                ipWebcamVlcLayout = new VLCVideoLayout(this);
                ipWebcamVlcLayout.setBackgroundColor(Color.BLACK);
                ipWebcamVlcFrame.addView(ipWebcamVlcLayout, new FrameLayout.LayoutParams(-1, -1));
                installCctvStatusDot(ipWebcamVlcFrame, 1);
                ipWebcamRefreshButton = new TextView(this);
                ipWebcamRefreshButton.setText("↻ Refresh");
                ipWebcamRefreshButton.setTextColor(Color.WHITE);
                ipWebcamRefreshButton.setTextSize(12);
                ipWebcamRefreshButton.setTypeface(null, android.graphics.Typeface.BOLD);
                ipWebcamRefreshButton.setGravity(Gravity.CENTER);
                ipWebcamRefreshButton.setPadding(dp(10), 0, dp(10), 0);
                ipWebcamRefreshButton.setContentDescription("Reconnect to IP Webcam");
                GradientDrawable buttonBackground = new GradientDrawable();
                buttonBackground.setColor(0xE6174A35);
                buttonBackground.setCornerRadius(dp(8));
                buttonBackground.setStroke(dp(1), 0xFFEAF4EC);
                ipWebcamRefreshButton.setBackground(buttonBackground);
                ipWebcamRefreshButton.setElevation(dp(6));
                ipWebcamRefreshButton.setOnClickListener(view -> reconnectIpWebcamVlc());
                FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(-2, dp(36), Gravity.TOP | Gravity.RIGHT);
                buttonParams.setMargins(0, dp(8), dp(8), 0);
                ipWebcamVlcFrame.addView(ipWebcamRefreshButton, buttonParams);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height, Gravity.TOP | Gravity.LEFT);
                params.leftMargin = left;
                params.topMargin = top;
                appRoot.addView(ipWebcamVlcFrame, params);
                if (activityResumed) startIpWebcamVlc(ipWebcamVlcLayout);
            } else {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) ipWebcamVlcFrame.getLayoutParams();
                if (params.width != width || params.height != height || params.leftMargin != left || params.topMargin != top) {
                    params.width = width;
                    params.height = height;
                    params.leftMargin = left;
                    params.topMargin = top;
                    ipWebcamVlcFrame.setLayoutParams(params);
                }
            }
            if (shouldShow != cctv01OverlayShown) {
                cctv01OverlayShown = shouldShow;
                ipWebcamVlcFrame.animate().cancel();
                if (shouldShow) {
                    ipWebcamVlcFrame.setAlpha(0f);
                    ipWebcamVlcFrame.setVisibility(View.VISIBLE);
                    ipWebcamVlcFrame.animate().alpha(1f).setDuration(180L).start();
                } else if (!homeActive) {
                    ipWebcamVlcFrame.animate().alpha(0f).setDuration(180L)
                            .withEndAction(() -> {
                                if (!cctv01OverlayShown && ipWebcamVlcFrame != null) {
                                    ipWebcamVlcFrame.setVisibility(View.INVISIBLE);
                                }
                            }).start();
                } else {
                    ipWebcamVlcFrame.animate().cancel();
                    ipWebcamVlcFrame.setAlpha(0f);
                    ipWebcamVlcFrame.setVisibility(View.INVISIBLE);
                }
            }
        } catch (Exception ignored) { }
    }

    // Blink red when inactive and green only while the VLC player reports playback.
    private final TextView[] cctvStatusDots = new TextView[3];
    private final Runnable cctvStatusRefresh = new Runnable() {
        @Override public void run() {
            org.videolan.libvlc.MediaPlayer[] players = {ipWebcamVlcPlayer, ipWebcam2VlcPlayer, ipWebcam3VlcPlayer};
            for (int i = 0; i < players.length; i++) {
                TextView dot = cctvStatusDots[i];
                if (dot == null) continue;
                boolean playing = false;
                try { playing = players[i] != null && players[i].isPlaying(); }
                catch (Exception ignored) { }
                dot.setTextColor(playing ? Color.GREEN : ((System.currentTimeMillis() / 500L) % 2 == 0 ? Color.RED : Color.TRANSPARENT));
                dot.setContentDescription("CCTV " + (i + 1) + (playing ? " active" : " inactive"));
            }
            mainHandler.postDelayed(this, 1000);
        }
    };

    private void installCctvStatusDot(FrameLayout frame, int camera) {
        TextView dot = new TextView(this);
        dot.setText("●");
        dot.setTextSize(19);
        dot.setTextColor(Color.RED);
        dot.setContentDescription("CCTV " + camera + " inactive");
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(30), dp(30), Gravity.TOP | Gravity.LEFT);
        params.leftMargin = dp(7);
        params.topMargin = dp(5);
        frame.addView(dot, params);
        dot.bringToFront();
        android.view.animation.AlphaAnimation blink = new android.view.animation.AlphaAnimation(1f, 0.25f);
        blink.setDuration(650);
        blink.setRepeatMode(android.view.animation.Animation.REVERSE);
        blink.setRepeatCount(android.view.animation.Animation.INFINITE);
        dot.startAnimation(blink);
        cctvStatusDots[camera - 1] = dot;
        mainHandler.removeCallbacks(cctvStatusRefresh);
        mainHandler.post(cctvStatusRefresh);
    }

    private void startIpWebcamVlc(VLCVideoLayout videoLayout) {
        stopIpWebcamVlc();
        ipWebcamVlcLayout = videoLayout;
        try {
            ArrayList<String> options = new ArrayList<>();
            options.add("--no-audio");
            options.add("--network-caching=250");
            options.add("--live-caching=250");
            ipWebcamVlc = new LibVLC(this, options);
            ipWebcamVlcPlayer = new org.videolan.libvlc.MediaPlayer(ipWebcamVlc);
            // Use TextureView composition so Android modal dialogs can cover and hide
            // the VLC surface instead of a SurfaceView layer staying on top.
            ipWebcamVlcPlayer.attachViews(videoLayout, null, true, false);
            // IP Webcam MJPEG: reconnect on VLC playback failure rather than leaving a black frame.
            final org.videolan.libvlc.MediaPlayer sessionPlayer = ipWebcamVlcPlayer;
            sessionPlayer.setEventListener(event -> {
                if (event.type == org.videolan.libvlc.MediaPlayer.Event.EncounteredError) {
                    mainHandler.postDelayed(() -> {
                        if (!isFinishing() && activityResumed && ipWebcamVlcPlayer == sessionPlayer
                                && ipWebcamVlcLayout == videoLayout) {
                            startIpWebcamVlc(videoLayout);
                        }
                    }, 5000L);
                }
            });
            if (cctvStreamUrl(1).isEmpty()) { stopIpWebcamVlc(); return; }
            Media media = new Media(ipWebcamVlc, Uri.parse(cctvStreamUrl(1)));
            media.addOption(":network-caching=250");
            media.addOption(":live-caching=250");
            ipWebcamVlcPlayer.setMedia(media);
            media.release();
            ipWebcamVlcPlayer.play();
            mainHandler.removeCallbacks(cctv01SnapshotTicker);
            mainHandler.postDelayed(cctv01SnapshotTicker, cctvSnapshotIntervalMillis);
        } catch (Exception error) {
            stopIpWebcamVlc();
            TextView message = new TextView(this);
            message.setText("VLC could not start this stream");
            message.setTextColor(Color.WHITE);
            message.setGravity(Gravity.CENTER);
            if (videoLayout.getChildCount() == 0) videoLayout.addView(message,
                    new FrameLayout.LayoutParams(-1, -1));
        }
    }

    private void positionCctv02Vlc(String coordinates) {
        if (appRoot == null || coordinates == null || isFinishing()) return;
        try {
            String[] values = coordinates.split(",");
            if (values.length < 6) return;
            float density = getResources().getDisplayMetrics().density;
            int left = Math.round(Float.parseFloat(values[0]) * density);
            int top = Math.round(Float.parseFloat(values[1]) * density);
            int width = Math.round(Float.parseFloat(values[2]) * density);
            int height = Math.round(Float.parseFloat(values[3]) * density);
            boolean offscreen = "1".equals(values[4]);
            boolean homeActive = "1".equals(values[5]);
            boolean shouldShow = !offscreen && homeActive && cctvVlcFeedsAllowedOnActiveScreen;
            if (width <= 0 || height <= 0) return;

            if (ipWebcam2VlcFrame == null) {
                ipWebcam2VlcFrame = new FrameLayout(this);
                ipWebcam2VlcFrame.setBackgroundColor(Color.BLACK);
                ipWebcam2VlcFrame.setAlpha(0f);
                ipWebcam2VlcFrame.setVisibility(View.INVISIBLE);
                ipWebcam2VlcFrame.setClipChildren(true);
                ipWebcam2VlcFrame.setClipToPadding(true);
                ipWebcam2VlcFrame.setElevation(dp(8));
                ipWebcam2VlcLayout = new VLCVideoLayout(this);
                ipWebcam2VlcLayout.setBackgroundColor(Color.BLACK);
                ipWebcam2VlcFrame.addView(ipWebcam2VlcLayout, new FrameLayout.LayoutParams(-1, -1));
                installCctvStatusDot(ipWebcam2VlcFrame, 2);
                ipWebcam2RefreshButton = new TextView(this);
                ipWebcam2RefreshButton.setText("↻ Refresh");
                ipWebcam2RefreshButton.setTextColor(Color.WHITE);
                ipWebcam2RefreshButton.setTextSize(12);
                ipWebcam2RefreshButton.setTypeface(null, android.graphics.Typeface.BOLD);
                ipWebcam2RefreshButton.setGravity(Gravity.CENTER);
                ipWebcam2RefreshButton.setPadding(dp(10), 0, dp(10), 0);
                ipWebcam2RefreshButton.setContentDescription("Reconnect to CCTV02 IP Webcam");
                GradientDrawable buttonBackground = new GradientDrawable();
                buttonBackground.setColor(0xE6174A35);
                buttonBackground.setCornerRadius(dp(8));
                buttonBackground.setStroke(dp(1), 0xFFEAF4EC);
                ipWebcam2RefreshButton.setBackground(buttonBackground);
                ipWebcam2RefreshButton.setElevation(dp(6));
                ipWebcam2RefreshButton.setOnClickListener(view -> reconnectCctv02Vlc());
                FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(-2, dp(36), Gravity.TOP | Gravity.RIGHT);
                buttonParams.setMargins(0, dp(8), dp(8), 0);
                ipWebcam2VlcFrame.addView(ipWebcam2RefreshButton, buttonParams);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height, Gravity.TOP | Gravity.LEFT);
                params.leftMargin = left;
                params.topMargin = top;
                appRoot.addView(ipWebcam2VlcFrame, params);
                if (activityResumed) startCctv02Vlc(ipWebcam2VlcLayout);
            } else {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) ipWebcam2VlcFrame.getLayoutParams();
                if (params.width != width || params.height != height || params.leftMargin != left || params.topMargin != top) {
                    params.width = width;
                    params.height = height;
                    params.leftMargin = left;
                    params.topMargin = top;
                    ipWebcam2VlcFrame.setLayoutParams(params);
                }
            }
            if (shouldShow != cctv02OverlayShown) {
                cctv02OverlayShown = shouldShow;
                ipWebcam2VlcFrame.animate().cancel();
                if (shouldShow) {
                    ipWebcam2VlcFrame.setAlpha(0f);
                    ipWebcam2VlcFrame.setVisibility(View.VISIBLE);
                    ipWebcam2VlcFrame.animate().alpha(1f).setDuration(180L).start();
                } else if (!homeActive) {
                    ipWebcam2VlcFrame.animate().alpha(0f).setDuration(180L)
                            .withEndAction(() -> {
                                if (!cctv02OverlayShown && ipWebcam2VlcFrame != null) {
                                    ipWebcam2VlcFrame.setVisibility(View.INVISIBLE);
                                }
                            }).start();
                } else {
                    ipWebcam2VlcFrame.animate().cancel();
                    ipWebcam2VlcFrame.setAlpha(0f);
                    ipWebcam2VlcFrame.setVisibility(View.INVISIBLE);
                }
            }
        } catch (Exception ignored) { }
    }

    private void positionCctv03Vlc(String coordinates) {
        if (appRoot == null || coordinates == null || isFinishing()) return;
        try {
            String[] values = coordinates.split(",");
            if (values.length < 6) return;
            float density = getResources().getDisplayMetrics().density;
            int left = Math.round(Float.parseFloat(values[0]) * density);
            int top = Math.round(Float.parseFloat(values[1]) * density);
            int width = Math.round(Float.parseFloat(values[2]) * density);
            int height = Math.round(Float.parseFloat(values[3]) * density);
            boolean offscreen = "1".equals(values[4]);
            boolean homeActive = "1".equals(values[5]);
            boolean shouldShow = !offscreen && homeActive && cctvVlcFeedsAllowedOnActiveScreen;
            if (width <= 0 || height <= 0) return;

            if (ipWebcam3VlcFrame == null) {
                ipWebcam3VlcFrame = new FrameLayout(this);
                ipWebcam3VlcFrame.setBackgroundColor(Color.BLACK);
                ipWebcam3VlcFrame.setAlpha(0f);
                ipWebcam3VlcFrame.setVisibility(View.INVISIBLE);
                ipWebcam3VlcFrame.setClipChildren(true);
                ipWebcam3VlcFrame.setClipToPadding(true);
                ipWebcam3VlcFrame.setElevation(dp(8));
                ipWebcam3VlcLayout = new VLCVideoLayout(this);
                ipWebcam3VlcLayout.setBackgroundColor(Color.BLACK);
                ipWebcam3VlcFrame.addView(ipWebcam3VlcLayout, new FrameLayout.LayoutParams(-1, -1));
                installCctvStatusDot(ipWebcam3VlcFrame, 3);
                ipWebcam3RefreshButton = new TextView(this);
                ipWebcam3RefreshButton.setText("↻ Refresh");
                ipWebcam3RefreshButton.setTextColor(Color.WHITE);
                ipWebcam3RefreshButton.setTextSize(12);
                ipWebcam3RefreshButton.setTypeface(null, android.graphics.Typeface.BOLD);
                ipWebcam3RefreshButton.setGravity(Gravity.CENTER);
                ipWebcam3RefreshButton.setPadding(dp(10), 0, dp(10), 0);
                ipWebcam3RefreshButton.setContentDescription("Reconnect to VIGI CCTV03");
                GradientDrawable buttonBackground = new GradientDrawable();
                buttonBackground.setColor(0xE6174A35);
                buttonBackground.setCornerRadius(dp(8));
                buttonBackground.setStroke(dp(1), 0xFFEAF4EC);
                ipWebcam3RefreshButton.setBackground(buttonBackground);
                ipWebcam3RefreshButton.setElevation(dp(6));
                ipWebcam3RefreshButton.setOnClickListener(view -> reconnectCctv03Vlc());
                FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(-2, dp(36), Gravity.TOP | Gravity.RIGHT);
                buttonParams.setMargins(0, dp(8), dp(8), 0);
                ipWebcam3VlcFrame.addView(ipWebcam3RefreshButton, buttonParams);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height, Gravity.TOP | Gravity.LEFT);
                params.leftMargin = left;
                params.topMargin = top;
                appRoot.addView(ipWebcam3VlcFrame, params);
                if (activityResumed) startVigiCctv03Vlc(ipWebcam3VlcLayout);
            } else {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) ipWebcam3VlcFrame.getLayoutParams();
                if (params.width != width || params.height != height || params.leftMargin != left || params.topMargin != top) {
                    params.width = width;
                    params.height = height;
                    params.leftMargin = left;
                    params.topMargin = top;
                    ipWebcam3VlcFrame.setLayoutParams(params);
                }
            }
            if (shouldShow != cctv03OverlayShown) {
                cctv03OverlayShown = shouldShow;
                ipWebcam3VlcFrame.animate().cancel();
                if (shouldShow) {
                    ipWebcam3VlcFrame.setAlpha(0f);
                    ipWebcam3VlcFrame.setVisibility(View.VISIBLE);
                    ipWebcam3VlcFrame.animate().alpha(1f).setDuration(180L).start();
                } else if (!homeActive) {
                    ipWebcam3VlcFrame.animate().alpha(0f).setDuration(180L)
                            .withEndAction(() -> {
                                if (!cctv03OverlayShown && ipWebcam3VlcFrame != null) {
                                    ipWebcam3VlcFrame.setVisibility(View.INVISIBLE);
                                }
                            }).start();
                } else {
                    ipWebcam3VlcFrame.animate().cancel();
                    ipWebcam3VlcFrame.setAlpha(0f);
                    ipWebcam3VlcFrame.setVisibility(View.INVISIBLE);
                }
            }
        } catch (Exception ignored) { }
    }

    private void startCctv02Vlc(VLCVideoLayout videoLayout) {
        stopCctv02Vlc();
        ipWebcam2VlcLayout = videoLayout;
        try {
            ArrayList<String> options = new ArrayList<>();
            options.add("--no-audio");
            options.add("--network-caching=250");
            options.add("--live-caching=250");
            ipWebcam2Vlc = new LibVLC(this, options);
            ipWebcam2VlcPlayer = new org.videolan.libvlc.MediaPlayer(ipWebcam2Vlc);
            // Match CCTV01 so this feed respects the same app/window layering.
            ipWebcam2VlcPlayer.attachViews(videoLayout, null, true, false);
            // IP Webcam MJPEG: reconnect on VLC playback failure rather than leaving a black frame.
            final org.videolan.libvlc.MediaPlayer sessionPlayer = ipWebcam2VlcPlayer;
            sessionPlayer.setEventListener(event -> {
                if (event.type == org.videolan.libvlc.MediaPlayer.Event.EncounteredError) {
                    mainHandler.postDelayed(() -> {
                        if (!isFinishing() && activityResumed && ipWebcam2VlcPlayer == sessionPlayer
                                && ipWebcam2VlcLayout == videoLayout) {
                            startCctv02Vlc(videoLayout);
                        }
                    }, 5000L);
                }
            });
            String route = cctvStreamUrl(2);
            if (route.isEmpty()) { stopCctv02Vlc(); return; }
            if (userStreamMode && (route.startsWith("rtsp://") || route.startsWith("rtsps://"))) {
                String[] credentials = vigiCctv03Credentials();
                if (credentials[0].isEmpty() || credentials[1].isEmpty()) {
                    stopCctv02Vlc();
                    promptForAssignedVigiCctv02Credentials();
                    return;
                }
                int schemeEnd = route.indexOf("://") + 3;
                if (!route.substring(schemeEnd).split("/",2)[0].contains("@")) {
                    route = route.substring(0,schemeEnd)+Uri.encode(credentials[0])+":"+Uri.encode(credentials[1])+"@"+route.substring(schemeEnd);
                }
            }
            Media media = new Media(ipWebcam2Vlc, Uri.parse(route));
            media.addOption(":network-caching=250");
            media.addOption(":live-caching=250");
            ipWebcam2VlcPlayer.setMedia(media);
            media.release();
            ipWebcam2VlcPlayer.play();
            mainHandler.removeCallbacks(cctv02SnapshotTicker);
            mainHandler.postDelayed(cctv02SnapshotTicker, nextSnapshotCheckDelay(2));
        } catch (Exception error) {
            stopCctv02Vlc();
            TextView message = new TextView(this);
            message.setText("VLC could not start the CCTV02 stream");
            message.setTextColor(Color.WHITE);
            message.setGravity(Gravity.CENTER);
            if (videoLayout.getChildCount() == 0) videoLayout.addView(message,
                    new FrameLayout.LayoutParams(-1, -1));
        }
    }

    private void reconnectCctv02Vlc() {
        if (isFinishing() || !activityResumed) return;
        VLCVideoLayout videoLayout = ipWebcam2VlcLayout;
        if (videoLayout == null) {
            installCctv01VlcPositionListener();
            return;
        }
        stopCctv02Vlc();
        if (ipWebcam2RefreshButton != null) {
            ipWebcam2RefreshButton.setEnabled(false);
            ipWebcam2RefreshButton.setAlpha(0.65f);
        }
        Toast.makeText(this, "Reconnecting to CCTV02 IP Webcam…", Toast.LENGTH_SHORT).show();
        mainHandler.postDelayed(() -> {
            if (isFinishing() || !activityResumed || ipWebcam2VlcLayout != videoLayout) return;
            startCctv02Vlc(videoLayout);
            if (ipWebcam2RefreshButton != null) {
                ipWebcam2RefreshButton.setEnabled(true);
                ipWebcam2RefreshButton.setAlpha(1f);
            }
        }, 500);
    }

    private void stopCctv02Vlc() {
        mainHandler.removeCallbacks(cctv02SnapshotTicker);
        if (ipWebcam2VlcPlayer != null) {
            try { ipWebcam2VlcPlayer.stop(); } catch (Exception ignored) { }
            try { ipWebcam2VlcPlayer.detachViews(); } catch (Exception ignored) { }
            ipWebcam2VlcPlayer.release();
            ipWebcam2VlcPlayer = null;
        }
        if (ipWebcam2Vlc != null) {
            ipWebcam2Vlc.release();
            ipWebcam2Vlc = null;
        }
    }

    private void startVigiCctv03Vlc(VLCVideoLayout videoLayout) {
        stopCctv03Vlc();
        ipWebcam3VlcLayout = videoLayout;
        if (userStreamMode) {
            String route = cctvStreamUrl(3);
            if (route == null || route.isEmpty()) { stopCctv03Vlc(); return; }
            // Assigned RTSP feeds must request camera credentials at connection time.
            // Never persist the camera password in Firebase or embed it in a log.
            if (route.startsWith("rtsp://") || route.startsWith("rtsps://")) {
                String[] credentials = vigiCctv03Credentials();
                if (credentials[0].isEmpty() || credentials[1].isEmpty() || cctv03CredentialsRejected) {
                    showCctv03VigiMessage(videoLayout, "Enter VIGI credentials to connect");
                    promptForCctv03VigiCredentials();
                    return;
                }
                if (!route.substring(route.indexOf("://") + 3).split("/", 2)[0].contains("@")) {
                    int schemeEnd = route.indexOf("://") + 3;
                    route = route.substring(0, schemeEnd)
                            + Uri.encode(credentials[0]) + ":" + Uri.encode(credentials[1]) + "@"
                            + route.substring(schemeEnd);
                }
            }
            try {
                ArrayList<String> options = new ArrayList<>();
                options.add("--no-audio"); options.add("--network-caching=350");
                ipWebcam3Vlc = new LibVLC(this, options);
                ipWebcam3VlcPlayer = new org.videolan.libvlc.MediaPlayer(ipWebcam3Vlc);
                ipWebcam3VlcPlayer.attachViews(videoLayout, null, true, false);
                Media media = new Media(ipWebcam3Vlc, Uri.parse(route));
                media.addOption(":network-caching=350");
                ipWebcam3VlcPlayer.setMedia(media); media.release(); ipWebcam3VlcPlayer.play();
            } catch (Exception error) { stopCctv03Vlc(); }
            return;
        }
        String ip = cctvIp(3);
        String[] credentials = vigiCctv03Credentials();
        for (int i = videoLayout.getChildCount() - 1; i >= 0; i--) {
            View child = videoLayout.getChildAt(i);
            if (child instanceof TextView) videoLayout.removeViewAt(i);
        }
        if (ip.isEmpty()) {
            showCctv03VigiMessage(videoLayout, "Set the CCTV03 VIGI IP address in Settings");
            return;
        }
        if (credentials[0].isEmpty() || credentials[1].isEmpty() || cctv03CredentialsRejected) {
            showCctv03VigiMessage(videoLayout, "Enter the VIGI camera account to connect");
            promptForCctv03VigiCredentials();
            return;
        }
        try {
            ArrayList<String> options = new ArrayList<>();
            options.add("--no-audio");
            options.add("--network-caching=500");
            options.add("--live-caching=500");
            ipWebcam3Vlc = new LibVLC(this, options);
            ipWebcam3VlcPlayer = new org.videolan.libvlc.MediaPlayer(ipWebcam3Vlc);
            ipWebcam3VlcPlayer.attachViews(videoLayout, null, true, false);
            final org.videolan.libvlc.MediaPlayer sessionPlayer = ipWebcam3VlcPlayer;
            sessionPlayer.setEventListener(event -> {
                if (event.type == org.videolan.libvlc.MediaPlayer.Event.EncounteredError) {
                    mainHandler.post(() -> checkCctv03RejectedCredentials(sessionPlayer, ip, credentials));
                }
            });
            String rtspUrl = "rtsp://" + Uri.encode(credentials[0]) + ":" + Uri.encode(credentials[1])
                    + "@" + ip + ":554/stream1";
            Media media = new Media(ipWebcam3Vlc, Uri.parse(rtspUrl));
            media.addOption(":rtsp-user=" + credentials[0]);
            media.addOption(":rtsp-pwd=" + credentials[1]);
            media.addOption(":network-caching=500");
            media.addOption(":live-caching=500");
            ipWebcam3VlcPlayer.setMedia(media);
            media.release();
            ipWebcam3VlcPlayer.play();
            mainHandler.removeCallbacks(cctv03SnapshotTicker);
            mainHandler.postDelayed(cctv03SnapshotTicker, snapshotDue(3) ? 5000L : cctvSnapshotIntervalMillis);
        } catch (Exception error) {
            stopCctv03Vlc();
            TextView message = new TextView(this);
            message.setText("VLC could not start the VIGI stream");
            message.setTextColor(Color.WHITE);
            message.setGravity(Gravity.CENTER);
            if (videoLayout.getChildCount() == 0) videoLayout.addView(message, new FrameLayout.LayoutParams(-1, -1));
        }
    }

    private void showCctv03VigiMessage(VLCVideoLayout layout, String messageText) {
        for (int i = layout.getChildCount() - 1; i >= 0; i--) {
            View child = layout.getChildAt(i);
            if (child instanceof TextView) layout.removeViewAt(i);
        }
        TextView message = new TextView(this);
        message.setText(messageText);
        message.setTextColor(Color.WHITE);
        message.setGravity(Gravity.CENTER);
        message.setPadding(dp(12), dp(8), dp(12), dp(8));
        layout.addView(message, new FrameLayout.LayoutParams(-1, -1));
    }

    private void checkCctv03RejectedCredentials(org.videolan.libvlc.MediaPlayer player, String ip, String[] credentials) {
        if (player != ipWebcam3VlcPlayer || cctv03CredentialCheckInProgress) return;
        cctv03CredentialCheckInProgress = true;
        cctvSnapshotWorker.execute(() -> {
            RtspCredentialVerifier.Result result = RtspCredentialVerifier.verify(ip, 554, credentials[0], credentials[1]);
            mainHandler.post(() -> {
                cctv03CredentialCheckInProgress = false;
                if (player != ipWebcam3VlcPlayer || !ip.equals(cctvIp(3))) return;
                if (result == RtspCredentialVerifier.Result.REJECTED) {
                    cctv03CredentialsRejected = true;
                    if (ipWebcam3VlcLayout != null) showCctv03VigiMessage(ipWebcam3VlcLayout, "VIGI rejected the saved login. Update the camera username or password.");
                    if (cctvVlcFeedsAllowedOnActiveScreen) promptForCctv03VigiCredentials();
                } else if (ipWebcam3VlcLayout != null) {
                    showCctv03VigiMessage(ipWebcam3VlcLayout, "VIGI RTSP stream unavailable. Retrying connection…");
                    final VLCVideoLayout retryLayout = ipWebcam3VlcLayout;
                    mainHandler.postDelayed(() -> {
                        if (!isFinishing() && activityResumed && cctvVlcFeedsAllowedOnActiveScreen
                                && !userStreamMode && !cctv03CredentialsRejected
                                && player == ipWebcam3VlcPlayer && ipWebcam3VlcLayout == retryLayout) {
                            startVigiCctv03Vlc(retryLayout);
                        }
                    }, 5000L);
                }
            });
        });
    }

    private void promptForCctv03VigiCredentials() {
        if (!activityResumed || isFinishing() || cctv03CredentialsPromptShowing) return;
        cctv03CredentialsPromptShowing = true;
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(24), dp(8), dp(24), dp(4));
        TextView note = new TextView(this);
        note.setText("Enter the camera account created in the VIGI app. GrowPlot will save it encrypted on this device.");
        form.addView(note, new LinearLayout.LayoutParams(-1, -2));
        EditText username = new EditText(this);
        username.setSingleLine(true);
        username.setHint("VIGI username");
        String[] savedCredentials = vigiCctv03Credentials();
        username.setText(savedCredentials[0]);
        username.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        form.addView(username, new LinearLayout.LayoutParams(-1, -2));
        EditText password = new EditText(this);
        password.setSingleLine(true);
        password.setHint("VIGI password");
        if (!cctv03CredentialsRejected) password.setText(savedCredentials[1]);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        form.addView(password, new LinearLayout.LayoutParams(-1, -2));

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("Connect CCTV03 · VIGI")
                .setView(form)
                .setNegativeButton("Cancel", (d, which) -> { })
                .setPositiveButton("Connect", null)
                .create();
        dialog.setOnDismissListener(d -> cctv03CredentialsPromptShowing = false);
        dialog.setOnShowListener(ignored -> dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String user = username.getText().toString().trim();
            String secret = password.getText().toString();
            if (user.isEmpty()) {
                username.setError("Enter the VIGI username");
                return;
            }
            if (secret.isEmpty()) {
                password.setError("Enter the VIGI password");
                return;
            }
            if (!saveVigiCctv03Credentials(user, secret)) {
                password.setError("Could not protect these credentials on this device");
                return;
            }
            username.setText("");
            password.setText("");
            dialog.dismiss();
            reconnectCctv03Vlc();
        }));
        dialog.show();
    }

    // CCTV02 can host an assigned VIGI RTSP feed (e.g. PP5 Section 2).
    // Keep the camera login local to Android Keystore, not Firestore.
    private void promptForAssignedVigiCctv02Credentials() {
        if (!activityResumed || isFinishing() || cctv03CredentialsPromptShowing) return;
        cctv03CredentialsPromptShowing = true;
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(24),dp(8),dp(24),dp(4));
        EditText username = new EditText(this);
        username.setSingleLine(true);
        username.setHint("VIGI username");
        username.setText(vigiCctv03Credentials()[0]);
        form.addView(username);
        EditText password = new EditText(this);
        password.setSingleLine(true);
        password.setHint("VIGI password");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        form.addView(password);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
            .setTitle("Connect assigned CCTV02 · VIGI")
            .setView(form).setNegativeButton("Cancel",(d,w)->{})
            .setPositiveButton("Connect",null).create();
        dialog.setOnDismissListener(d->cctv03CredentialsPromptShowing=false);
        dialog.setOnShowListener(d->dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String user=username.getText().toString().trim(),secret=password.getText().toString();
            if(user.isEmpty()){username.setError("Enter VIGI username");return;}
            if(secret.isEmpty()){password.setError("Enter VIGI password");return;}
            if(!saveVigiCctv03Credentials(user,secret)){password.setError("Could not encrypt camera login on this device");return;}
            password.setText("");dialog.dismiss();reconnectCctv02Vlc();
        }));
        dialog.show();
    }

    private void reconnectCctv03Vlc() {
        if (isFinishing() || !activityResumed) return;
        VLCVideoLayout videoLayout = ipWebcam3VlcLayout;
        if (videoLayout == null) {
            installCctv01VlcPositionListener();
            return;
        }
        stopCctv03Vlc();
        mainHandler.postDelayed(() -> {
            if (!isFinishing() && activityResumed && ipWebcam3VlcLayout == videoLayout) startVigiCctv03Vlc(videoLayout);
        }, 500);
    }

    private void stopCctv03Vlc() {
        mainHandler.removeCallbacks(cctv03SnapshotTicker);
        if (ipWebcam3VlcPlayer != null) {
            try { ipWebcam3VlcPlayer.stop(); } catch (Exception ignored) { }
            try { ipWebcam3VlcPlayer.detachViews(); } catch (Exception ignored) { }
            ipWebcam3VlcPlayer.release();
            ipWebcam3VlcPlayer = null;
        }
        if (ipWebcam3Vlc != null) {
            ipWebcam3Vlc.release();
            ipWebcam3Vlc = null;
        }
    }

    private void reconnectIpWebcamVlc() {
        if (isFinishing() || !activityResumed) return;
        VLCVideoLayout videoLayout = ipWebcamVlcLayout;
        if (videoLayout == null) {
            installCctv01VlcPositionListener();
            return;
        }
        stopIpWebcamVlc();
        if (ipWebcamRefreshButton != null) {
            ipWebcamRefreshButton.setEnabled(false);
            ipWebcamRefreshButton.setAlpha(0.65f);
        }
        Toast.makeText(this, "Reconnecting to IP Webcam…", Toast.LENGTH_SHORT).show();
        mainHandler.postDelayed(() -> {
            if (isFinishing() || !activityResumed || ipWebcamVlcLayout != videoLayout) return;
            startIpWebcamVlc(videoLayout);
            if (ipWebcamRefreshButton != null) {
                ipWebcamRefreshButton.setEnabled(true);
                ipWebcamRefreshButton.setAlpha(1f);
            }
        }, 500);
    }

    private void stopIpWebcamVlc() {
        mainHandler.removeCallbacks(cctv01SnapshotTicker);
        if (ipWebcamVlcPlayer != null) {
            try { ipWebcamVlcPlayer.stop(); } catch (Exception ignored) { }
            try { ipWebcamVlcPlayer.detachViews(); } catch (Exception ignored) { }
            ipWebcamVlcPlayer.release();
            ipWebcamVlcPlayer = null;
        }
        if (ipWebcamVlc != null) {
            ipWebcamVlc.release();
            ipWebcamVlc = null;
        }
    }

    private final java.util.Set<String> snapshotInFlight = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    private String snapshotFlightKey(int camera,String plotId) {
        return safePlotId(plotId)+"|"+camera;
    }

    private String snapshotLastSuccessKey(int camera,String plotId) {
        return "last_successful_snapshot_"+plotId+"_"+camera;
    }

    private boolean snapshotDue(int camera) {
        String plotId=safePlotId(getPrefs().getString(PREF_ACTIVE_PLOT_ID,DEFAULT_PLOT_ID));
        long last=getPrefs().getLong(snapshotLastSuccessKey(camera,plotId),0L);
        return !snapshotInFlight.contains(snapshotFlightKey(camera,plotId))
                && (last<=0 || System.currentTimeMillis()-last>=cctvSnapshotIntervalMillis);
    }
    private long nextSnapshotCheckDelay(int camera) {
        String plotId=safePlotId(getPrefs().getString(PREF_ACTIVE_PLOT_ID,DEFAULT_PLOT_ID));
        // A missing plot folder is a configuration problem, not a frame-timing problem.
        // Back off to the configured interval rather than retrying PixelCopy every 5 seconds.
        if (!isFeedFolderReady(plotId)) return cctvSnapshotIntervalMillis;
        long last=getPrefs().getLong(snapshotLastSuccessKey(camera,plotId),0L);
        if (snapshotInFlight.contains(snapshotFlightKey(camera,plotId))) return 5000L;
        if (last<=0) return 5000L;
        long remaining=cctvSnapshotIntervalMillis-(System.currentTimeMillis()-last);
        return remaining<=0 ? 5000L : remaining;
    }

    private void checkIpWebcamBeforeSnapshot(int camera) {
        boolean ready = camera == 1
                ? ipWebcamVlcPlayer != null && ipWebcamVlcLayout != null && ipWebcamVlcFrame != null && ipWebcamVlcFrame.getVisibility() == View.VISIBLE
                : camera == 2 ? ipWebcam2VlcPlayer != null && ipWebcam2VlcLayout != null && ipWebcam2VlcFrame != null && ipWebcam2VlcFrame.getVisibility() == View.VISIBLE
                : ipWebcam3VlcPlayer != null && ipWebcam3VlcLayout != null && ipWebcam3VlcFrame != null && ipWebcam3VlcFrame.getVisibility() == View.VISIBLE;
        org.videolan.libvlc.MediaPlayer player = camera == 1 ? ipWebcamVlcPlayer
                : camera == 2 ? ipWebcam2VlcPlayer : ipWebcam3VlcPlayer;
        if (!activityResumed || !ready || !cctvVlcFeedsAllowedOnActiveScreen || !player.isPlaying() || !snapshotDue(camera)) return;
        String activePlot = safePlotId(getPrefs().getString(PREF_ACTIVE_PLOT_ID, DEFAULT_PLOT_ID));
        if (!isFeedFolderReady(activePlot)) {
            String warningKey = "feed_folder_not_ready_" + activePlot + "_" + camera;
            if (!getPrefs().getBoolean(warningKey, false)) {
                getPrefs().edit().putBoolean(warningKey, true).apply();
                notifyFeedCaptureStatus("CCTV" + String.format(Locale.US, "%02d", camera)
                        + " is live, but this plot has no connected image-feed folder. Connect the plot folder in Settings to save snapshots.");
            }
            return;
        }
        // RTSP is already decoded by VLC. Do not probe it as an HTTP MJPEG feed.
        String streamUrl = cctvStreamUrl(camera);
        boolean rtsp = streamUrl.regionMatches(true, 0, "rtsp://", 0, 7)
                || streamUrl.regionMatches(true, 0, "rtsps://", 0, 8);
        if (rtsp || camera == 3) {
            captureCctvVlcFrame(camera);
            return;
        }
        try {
            cctvSnapshotWorker.execute(() -> {
                boolean available = isIpWebcamFrameAvailable(camera);
                runOnUiThread(() -> {
                    if (!activityResumed || isFinishing()) return;
                    if (available) {
                        captureCctvVlcFrame(camera);
                    } else {
                        notifyFeedCaptureStatus("IP Webcam for CCTV" + String.format(Locale.US, "%02d", camera) + " is unavailable; snapshot skipped.");
                    }
                });
            });
        } catch (Exception ignored) { }
    }

    private boolean isIpWebcamFrameAvailable(int camera) {
        HttpURLConnection connection = null;
        try {
            String streamUrl = cctvStreamUrl(camera);
            if (streamUrl.isEmpty()) return false;
            connection = (HttpURLConnection) new URL(streamUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "multipart/x-mixed-replace, image/jpeg");
            connection.setConnectTimeout(2500);
            connection.setReadTimeout(2500);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(false);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return false;

            // A live MJPEG response can return HTTP 200 while the camera has stopped
            // producing frames. Require the start of a JPEG frame before capturing VLC.
            try (InputStream stream = connection.getInputStream()) {
                int previous = -1;
                int scanned = 0;
                byte[] buffer = new byte[2048];
                while (scanned < 16 * 1024) {
                    int count = stream.read(buffer, 0, Math.min(buffer.length, 16 * 1024 - scanned));
                    if (count <= 0) return false;
                    for (int i = 0; i < count; i++) {
                        int current = buffer[i] & 0xFF;
                        if (previous == 0xFF && current == 0xD8) return true;
                        previous = current;
                    }
                    scanned += count;
                }
            }
        } catch (Exception ignored) {
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
        return false;
    }


    private void captureCctvVlcFrame(int camera) {
        boolean ready = camera == 1
                ? ipWebcamVlcPlayer != null && ipWebcamVlcLayout != null && ipWebcamVlcFrame != null && ipWebcamVlcFrame.getVisibility() == View.VISIBLE
                : camera == 2 ? ipWebcam2VlcPlayer != null && ipWebcam2VlcLayout != null && ipWebcam2VlcFrame != null && ipWebcam2VlcFrame.getVisibility() == View.VISIBLE
                : ipWebcam3VlcPlayer != null && ipWebcam3VlcLayout != null && ipWebcam3VlcFrame != null && ipWebcam3VlcFrame.getVisibility() == View.VISIBLE;
        org.videolan.libvlc.MediaPlayer player = camera == 1 ? ipWebcamVlcPlayer
                : camera == 2 ? ipWebcam2VlcPlayer : ipWebcam3VlcPlayer;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !activityResumed || !ready
                || !cctvVlcFeedsAllowedOnActiveScreen || !player.isPlaying()) return;
        final String capturePlotId = safePlotId(getPrefs().getString(PREF_ACTIVE_PLOT_ID, DEFAULT_PLOT_ID));
        VLCVideoLayout layout = camera == 1 ? ipWebcamVlcLayout : camera == 2 ? ipWebcam2VlcLayout : ipWebcam3VlcLayout;
        SurfaceView surface = findVlcSurfaceView(layout);
        if (surface != null && surface.getWidth() > 0 && surface.getHeight() > 0
                && surface.getHolder().getSurface().isValid()) {
            Bitmap frame = Bitmap.createBitmap(surface.getWidth(), surface.getHeight(), Bitmap.Config.ARGB_8888);
            PixelCopy.request(surface, frame, result -> {
                if (result != PixelCopy.SUCCESS) {
                    if (!frame.isRecycled()) frame.recycle();
                    notifyFeedCaptureStatus("CCTV"+String.format(Locale.US,"%02d",camera)+" snapshot: video frame unavailable (PixelCopy " + result + "); will retry.");
                    return;
                }
                saveCctvVlcFrameToDrive(camera, frame, capturePlotId);
            }, mainHandler);
            return;
        }
        android.view.TextureView texture = findVlcTextureView(layout);
        if (texture != null && texture.getWidth() > 0 && texture.getHeight() > 0 && texture.isAvailable()) {
            Bitmap frame = texture.getBitmap();
            if (frame != null) { saveCctvVlcFrameToDrive(camera, frame, capturePlotId); return; }
        }
        notifyFeedCaptureStatus("CCTV"+String.format(Locale.US,"%02d",camera)+" snapshot: video surface not ready; will retry.");
    }

    private void saveCctvVlcFrameToDrive(int camera, Bitmap frame, String plotId) {
        String key=snapshotFlightKey(camera,plotId);
        if (!snapshotInFlight.add(key)) {
            if (!frame.isRecycled()) frame.recycle();
            return;
        }
        cctvSnapshotWorker.execute(() -> {
            try {
                saveFeedPhotoToDrive(null, frame, plotId, camera, false);
                getPrefs().edit().putLong(snapshotLastSuccessKey(camera,plotId),System.currentTimeMillis()).apply();
                mainHandler.post(() -> {
                    notifyFeedCaptureStatus("CCTV"+String.format(Locale.US,"%02d",camera)+" snapshot saved successfully to the configured plot folder.");
                    scanSnapshotFolder(plotId);
                });
            } catch (Exception error) {
                runOnUiThread(() -> notifyFeedCaptureStatus("CCTV" + String.format(Locale.US, "%02d", camera) + " frame could not be saved to the selected plot Google Drive image feed: "
                        + (error.getMessage() == null ? "check the saved folder permission" : error.getMessage())));
            } finally {
                snapshotInFlight.remove(key);
                if (!frame.isRecycled()) frame.recycle();
            }
        });
    }

    private SurfaceView findVlcSurfaceView(View view) {
        if (view instanceof SurfaceView) return (SurfaceView) view;
        if (!(view instanceof android.view.ViewGroup)) return null;
        android.view.ViewGroup group = (android.view.ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            SurfaceView surface = findVlcSurfaceView(group.getChildAt(i));
            if (surface != null) return surface;
        }
        return null;
    }

    private android.view.TextureView findVlcTextureView(View view) {
        if (view instanceof android.view.TextureView) return (android.view.TextureView) view;
        if (!(view instanceof android.view.ViewGroup)) return null;
        android.view.ViewGroup group = (android.view.ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            android.view.TextureView texture = findVlcTextureView(group.getChildAt(i));
            if (texture != null) return texture;
        }
        return null;
    }

    private String childFileDocumentId(Uri treeUri, String parentDocumentId, String fileName) throws Exception {
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId);
        String[] columns = {DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME};
        try (Cursor cursor = getContentResolver().query(childrenUri, columns, null, null, null)) {
            if (cursor == null) throw new IllegalStateException("Google Drive could not list the GrowPlot config folder");
            int idColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            int nameColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            while (cursor.moveToNext()) {
                if (idColumn >= 0 && nameColumn >= 0 && fileName.equalsIgnoreCase(cursor.getString(nameColumn))) return cursor.getString(idColumn);
            }
        }
        return null;
    }

    private String farmDataUserId(String requested) {
        String configured = requested == null ? "" : requested.trim();
        if (!configured.matches("[A-Za-z0-9_-]{1,48}")) configured = "default-user";
        getPrefs().edit().putString(PREF_FARM_DATA_USER_ID, configured).apply();
        return configured;
    }

    private Uri farmDataTreeUri() throws Exception {
        Uri treeUri = null;
        String saved = getPrefs().getString(PREF_GROWPLOT_TREE_URI, null);
        if (saved != null) {
            Uri candidate = Uri.parse(saved);
            if (hasPersistedReadWriteGrant(candidate)) treeUri = candidate;
        }
        if (treeUri == null) treeUri = findGrowPlotScopedTreeUri();
        if (treeUri == null) throw new IllegalStateException("Saved Google Drive access to the GrowPlot folder is unavailable");
        String rootId = resolveGrowPlotDocumentId(treeUri);
        if (rootId == null) throw new IllegalStateException("The saved Drive permission does not include the GrowPlot folder");
        getPrefs().edit().putString(PREF_GROWPLOT_TREE_URI, treeUri.toString()).apply();
        return treeUri;
    }

    private String farmScopeKey(String plotId, String greenhouse, int section) {
        return safePlotId(plotId) + "|" + safeFolderName(greenhouse) + "|" + section;
    }

    private JSONObject readFarmDriveJson(Uri treeUri, String parentId, String fileName) throws Exception {
        String fileId = childFileDocumentId(treeUri, parentId, fileName);
        if (fileId == null) return null;
        Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, fileId);
        try (InputStream input = getContentResolver().openInputStream(fileUri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) return null;
            byte[] buffer = new byte[8192];
            int read, total = 0;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > 4 * 1024 * 1024) throw new IllegalStateException("Farm data file exceeds 4 MB");
                output.write(buffer, 0, read);
            }
            return new JSONObject(output.toString(StandardCharsets.UTF_8.name()));
        }
    }

    private void writeFarmDriveJson(Uri treeUri, String parentId, String fileName, JSONObject data) throws Exception {
        String fileId = childFileDocumentId(treeUri, parentId, fileName);
        Uri fileUri;
        if (fileId == null) {
            fileUri = DocumentsContract.createDocument(getContentResolver(),
                    DocumentsContract.buildDocumentUriUsingTree(treeUri, parentId), "application/json", fileName);
            if (fileUri == null) throw new IllegalStateException("Google Drive refused to create " + fileName);
        } else fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, fileId);
        try (OutputStream output = getContentResolver().openOutputStream(fileUri, "wt")) {
            if (output == null) throw new IllegalStateException("Google Drive could not open " + fileName + " for writing");
            output.write((data.toString(2) + "\n").getBytes(StandardCharsets.UTF_8));
            output.flush();
        }
    }

    private String farmUserFolder(Uri treeUri, String rootId, String userId, boolean create) throws Exception {
        String dbId = create ? findOrCreateChildFolder(treeUri, rootId, "db") : findExistingChildFolder(treeUri, rootId, "db");
        if (dbId == null) return null;
        String userdataId = create ? findOrCreateChildFolder(treeUri, dbId, "userdata") : findExistingChildFolder(treeUri, dbId, "userdata");
        if (userdataId == null) return null;
        return create ? findOrCreateChildFolder(treeUri, userdataId, userId) : findExistingChildFolder(treeUri, userdataId, userId);
    }

    private JSONObject loadFarmPlantingsFromDrive(String requestedUserId) throws Exception {
        String userId = farmDataUserId(requestedUserId);
        Uri treeUri = farmDataTreeUri();
        String rootId = resolveGrowPlotDocumentId(treeUri);
        String userFolderId = farmUserFolder(treeUri, rootId, userId, true);
        JSONObject response = new JSONObject();
        response.put("userId", userId);
        JSONArray records = new JSONArray(), deletedIds = new JSONArray();
        if (userFolderId != null) {
            JSONObject index = readFarmDriveJson(treeUri, userFolderId, "crop-plantings-index.json");
            if (index != null) {
                JSONArray scopes = index.optJSONArray("scopes");
                if (scopes != null) {
                    Set<String> visited = new HashSet<>();
                    for (int i = 0; i < scopes.length(); i++) {
                        JSONObject scope = scopes.optJSONObject(i);
                        if (scope == null) continue;
                        String plotId = safePlotId(scope.optString("plotId", DEFAULT_PLOT_ID));
                        String greenhouse = safeFolderName(scope.optString("greenhouse", "1"));
                        int section = scope.optInt("section", 0);
                        if (section < 1 || section > 6 || !visited.add(farmScopeKey(plotId, greenhouse, section))) continue;
                        String plotFolder = findExistingChildFolder(treeUri, userFolderId, plotId);
                        if (plotFolder == null) continue;
                        String greenhouseFolder = findExistingChildFolder(treeUri, plotFolder, "greenhouse_" + greenhouse);
                        if (greenhouseFolder == null) continue;
                        String sectionFolder = findExistingChildFolder(treeUri, greenhouseFolder,
                                String.format(Locale.US, "section_%02d", section));
                        if (sectionFolder == null) continue;
                        JSONObject sectionData = readFarmDriveJson(treeUri, sectionFolder, "crop-plantings.json");
                        JSONArray sectionRecords = sectionData == null ? null : sectionData.optJSONArray("records");
                        if (sectionRecords != null) for (int j = 0; j < sectionRecords.length(); j++) {
                            JSONObject record = sectionRecords.optJSONObject(j);
                            if (record != null) records.put(record);
                        }
                    }
                }
                JSONArray removed = index.optJSONArray("deletedIds");
                if (removed != null) for (int i = 0; i < removed.length(); i++) {
                    String id = removed.optString(i, "");
                    if (!id.isEmpty()) deletedIds.put(id);
                }
            }
        }
        JSONObject management = userFolderId == null ? null : readFarmDriveJson(treeUri, userFolderId, "farm-management.json");
        response.put("records", records);
        response.put("deletedIds", deletedIds);
        response.put("irrigationSchedules", management == null ? new JSONArray() : management.optJSONArray("irrigationSchedules") == null ? new JSONArray() : management.optJSONArray("irrigationSchedules"));
        response.put("maintenanceTasks", management == null ? new JSONArray() : management.optJSONArray("maintenanceTasks") == null ? new JSONArray() : management.optJSONArray("maintenanceTasks"));
        response.put("managementDeletedIds", management == null ? new JSONArray() : management.optJSONArray("deletedIds") == null ? new JSONArray() : management.optJSONArray("deletedIds"));
        response.put("ok", true);
        response.put("path", "/db/userdata/" + userId);
        return response;
    }

    private JSONObject saveFarmPlantingsToDrive(String payload) throws Exception {
        JSONObject request = new JSONObject(payload == null ? "{}" : payload);
        String userId = farmDataUserId(request.optString("userId", "default-user"));
        JSONArray records = request.optJSONArray("records");
        if (records == null) records = new JSONArray();
        JSONArray requestedDeleted = request.optJSONArray("deletedIds");
        Uri treeUri = farmDataTreeUri();
        String rootId = resolveGrowPlotDocumentId(treeUri);
        String userFolderId = farmUserFolder(treeUri, rootId, userId, true);
        if (userFolderId == null) throw new IllegalStateException("Could not create the GrowPlot/db/userdata folder");

        JSONObject oldIndex = readFarmDriveJson(treeUri, userFolderId, "crop-plantings-index.json");
        Map<String, JSONObject> scopes = new java.util.LinkedHashMap<>();
        JSONArray oldScopes = oldIndex == null ? null : oldIndex.optJSONArray("scopes");
        if (oldScopes != null) for (int i = 0; i < oldScopes.length(); i++) {
            JSONObject scope = oldScopes.optJSONObject(i);
            if (scope == null) continue;
            String plotId = safePlotId(scope.optString("plotId", DEFAULT_PLOT_ID));
            String greenhouse = safeFolderName(scope.optString("greenhouse", "1"));
            int section = scope.optInt("section", 0);
            if (section < 1 || section > 6) continue;
            JSONObject safeScope = new JSONObject();
            safeScope.put("plotId", plotId); safeScope.put("greenhouse", greenhouse); safeScope.put("section", section);
            scopes.put(farmScopeKey(plotId, greenhouse, section), safeScope);
        }
        Map<String, JSONArray> grouped = new java.util.LinkedHashMap<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject record = records.optJSONObject(i);
            if (record == null) continue;
            String plotId = safePlotId(record.optString("plotId", DEFAULT_PLOT_ID));
            String greenhouse = safeFolderName(record.optString("greenhouse", "1"));
            String sectionValue = record.optString("section", "1");
            boolean allSections = "all".equalsIgnoreCase(sectionValue);
            int section = allSections ? 0 : record.optInt("section", 1);
            if (!allSections && (section < 1 || section > 6)) {
                throw new IllegalArgumentException("Section must be between 1 and 6, or all");
            }
            int firstSection = allSections ? 1 : section;
            int lastSection = allSections ? 6 : section;
            for (int targetSection = firstSection; targetSection <= lastSection; targetSection++) {
                String scopeKey = farmScopeKey(plotId, greenhouse, targetSection);
                JSONObject scope = new JSONObject();
                scope.put("plotId", plotId); scope.put("greenhouse", greenhouse); scope.put("section", targetSection);
                scopes.put(scopeKey, scope);
                JSONArray group = grouped.get(scopeKey);
                if (group == null) { group = new JSONArray(); grouped.put(scopeKey, group); }
                JSONObject copy = new JSONObject(record.toString());
                copy.put("plotId", plotId); copy.put("greenhouse", greenhouse);
                copy.put("section", allSections ? "all" : targetSection);
                copy.put("userId", userId);
                group.put(copy);
            }
        }

        Set<String> deleted = new java.util.LinkedHashSet<>();
        JSONArray oldDeleted = oldIndex == null ? null : oldIndex.optJSONArray("deletedIds");
        if (oldDeleted != null) for (int i = 0; i < oldDeleted.length(); i++) {
            String id = oldDeleted.optString(i, ""); if (!id.isEmpty()) deleted.add(id);
        }
        if (requestedDeleted != null) for (int i = 0; i < requestedDeleted.length(); i++) {
            String id = requestedDeleted.optString(i, ""); if (id.matches("[A-Za-z0-9_-]{1,100}")) deleted.add(id);
        }
        for (String scopeKey : scopes.keySet()) {
            JSONObject scope = scopes.get(scopeKey);
            String plotId = scope.getString("plotId"), greenhouse = scope.getString("greenhouse");
            int section = scope.getInt("section");
            String plotFolder = findOrCreateChildFolder(treeUri, userFolderId, plotId);
            String greenhouseFolder = findOrCreateChildFolder(treeUri, plotFolder, "greenhouse_" + greenhouse);
            String sectionFolder = findOrCreateChildFolder(treeUri, greenhouseFolder,
                    String.format(Locale.US, "section_%02d", section));
            JSONObject sectionData = new JSONObject();
            sectionData.put("schemaVersion", 1); sectionData.put("userId", userId);
            sectionData.put("plotId", plotId); sectionData.put("greenhouse", greenhouse); sectionData.put("section", section);
            sectionData.put("updatedAt", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(new Date()));
            sectionData.put("records", grouped.containsKey(scopeKey) ? grouped.get(scopeKey) : new JSONArray());
            writeFarmDriveJson(treeUri, sectionFolder, "crop-plantings.json", sectionData);
        }
        JSONArray scopeArray = new JSONArray(); for (JSONObject scope : scopes.values()) scopeArray.put(scope);
        JSONArray deletedArray = new JSONArray(); for (String id : deleted) deletedArray.put(id);
        JSONObject index = new JSONObject();
        index.put("schemaVersion", 1); index.put("userId", userId);
        index.put("updatedAt", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(new Date()));
        index.put("scopes", scopeArray); index.put("deletedIds", deletedArray);
        writeFarmDriveJson(treeUri, userFolderId, "crop-plantings-index.json", index);
        JSONArray irrigationSchedules = request.optJSONArray("irrigationSchedules");
        if (irrigationSchedules == null) irrigationSchedules = new JSONArray();
        JSONArray maintenanceTasks = request.optJSONArray("maintenanceTasks");
        if (maintenanceTasks == null) maintenanceTasks = new JSONArray();
        JSONArray managementDeletedIds = request.optJSONArray("managementDeletedIds");
        if (managementDeletedIds == null) managementDeletedIds = new JSONArray();
        JSONObject management = new JSONObject();
        management.put("schemaVersion", 1); management.put("userId", userId);
        management.put("updatedAt", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(new Date()));
        management.put("irrigationSchedules", irrigationSchedules);
        management.put("maintenanceTasks", maintenanceTasks);
        management.put("deletedIds", managementDeletedIds);
        writeFarmDriveJson(treeUri, userFolderId, "farm-management.json", management);
        JSONObject response = new JSONObject(); response.put("ok", true); response.put("userId", userId);
        response.put("path", "/db/userdata/" + userId); response.put("scopeCount", scopes.size());
        return response;
    }

    private void notifyFarmPlantingsLoaded(JSONObject result) {
        if (web == null) return;
        String argument = JSONObject.quote(result == null ? "{}" : result.toString());
        runOnUiThread(() -> { if (web != null) web.evaluateJavascript("window.onFarmPlantingsLoaded&&window.onFarmPlantingsLoaded(" + argument + ")", null); });
    }

    private void notifyFarmPlantingsSaved(JSONObject result) {
        if (web == null) return;
        String argument = JSONObject.quote(result == null ? "{}" : result.toString());
        runOnUiThread(() -> { if (web != null) web.evaluateJavascript("window.onFarmPlantingsSaved&&window.onFarmPlantingsSaved(" + argument + ")", null); });
    }

    private JSONObject readDriveStorageConfig(Uri treeUri, String growPlotDocumentId) throws Exception {
        String configId = findExistingChildFolder(treeUri, growPlotDocumentId, "config");
        if (configId == null) return new JSONObject();
        String fileId = childFileDocumentId(treeUri, configId, GROWPLOT_STORAGE_CONFIG_FILE);
        if (fileId == null) return new JSONObject();
        Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, fileId);
        try (InputStream input = getContentResolver().openInputStream(fileUri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) return new JSONObject();
            byte[] buffer = new byte[4096];
            int read, total = 0;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > 128 * 1024) throw new IllegalStateException("GrowPlot storage config is too large");
                output.write(buffer, 0, read);
            }
            return new JSONObject(output.toString(StandardCharsets.UTF_8.name()));
        }
    }

    private JSONObject readBundledStorageConfig() throws Exception {
        try (InputStream input = getAssets().open("config/storage-paths.json");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read, total = 0;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > 128 * 1024) throw new IllegalStateException("Bundled GrowPlot storage config is too large");
                output.write(buffer, 0, read);
            }
            return new JSONObject(output.toString(StandardCharsets.UTF_8.name()));
        }
    }

    private void restoreDrivePathsFromConfig() {
        Uri treeUri = findGrowPlotScopedTreeUri();
        if (treeUri == null) return;
        try {
            String growPlotId = resolveGrowPlotDocumentId(treeUri);
            if (growPlotId == null) return;
            getPrefs().edit().putString(PREF_GROWPLOT_TREE_URI, treeUri.toString()).apply();
            JSONObject config = readDriveStorageConfig(treeUri, growPlotId);
            boolean createDriveConfig = config.length() == 0;
            if (createDriveConfig) config = readBundledStorageConfig();
            String diagnosisId = findOrCreateChildFolder(treeUri, growPlotId, "Diagnosis");
            getPrefs().edit().putString(PREF_DIAGNOSIS_TREE_URI, treeUri.toString())
                    .putString(PREF_DIAGNOSIS_ROOT_DOCUMENT_ID, diagnosisId)
                    .putString(PREF_DIAGNOSIS_FOLDER_LABEL, getDocumentPathLabel(treeUri, diagnosisId)).apply();
            JSONObject feeds = config.optJSONObject("imageFeeds");
            if (feeds != null) {
                java.util.Iterator<String> plotIds = feeds.keys();
                while (plotIds.hasNext()) {
                    String plotId = plotIds.next();
                    if (!plotId.matches("[A-Za-z0-9_-]{1,64}")) continue;
                    JSONObject entry = feeds.optJSONObject(plotId);
                    String path = entry == null ? null : entry.optString("relativePath", null);
                    if (path == null || !path.regionMatches(true, 0, "Image_Feed/", 0, "Image_Feed/".length())) continue;
                    String documentId = growPlotId;
                    boolean found = true;
                    for (String segment : path.split("/")) {
                        if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) { found = false; break; }
                        documentId = findOrCreateChildFolder(treeUri, documentId, segment);
                    }
                    if (!found) continue;
                    String safeId = safePlotId(plotId);
                    // Drive config is authoritative for the default plot paths. Refresh local
                    // document IDs from it on launch so a stale but still-valid folder selection
                    // cannot keep pointing at yesterday's image_feed location.
                    getPrefs().edit().putString(feedTreeKey(safeId), treeUri.toString())
                            .putString(feedFolderDocumentKey(safeId), documentId)
                            .putString(feedLocationKey(safeId), getDocumentPathLabel(treeUri, documentId)).apply();
                }
            }
            // Create the shared config on the first launch that can see the saved GrowPlot grant.
            // This writes already-saved image-feed labels plus the canonical Diagnosis path without
            // asking the user to reconnect or rewriting an existing config on every launch.
            if (createDriveConfig) saveDriveStorageConfig(treeUri);
        } catch (Exception ignored) { }
    }

    private boolean saveDriveStorageConfig(Uri preferredTreeUri) {
        try {
            Uri treeUri = preferredTreeUri;
            String growPlotId = treeUri == null ? null : resolveGrowPlotDocumentId(treeUri);
            if (growPlotId == null) {
                String saved = getPrefs().getString(PREF_GROWPLOT_TREE_URI, null);
                treeUri = saved == null ? findGrowPlotScopedTreeUri() : Uri.parse(saved);
                if (treeUri == null) return false;
                growPlotId = resolveGrowPlotDocumentId(treeUri);
            }
            if (growPlotId == null) return false;
            getPrefs().edit().putString(PREF_GROWPLOT_TREE_URI, treeUri.toString()).apply();
            String configId = findOrCreateChildFolder(treeUri, growPlotId, "config");
            JSONObject config = readDriveStorageConfig(treeUri, growPlotId);
            if (config.length() == 0) config = readBundledStorageConfig();
            JSONObject feeds = config.optJSONObject("imageFeeds");
            if (feeds == null) feeds = new JSONObject();
            String labelPrefix = "data_feed_location_label_";
            for (Map.Entry<String, ?> pref : getPrefs().getAll().entrySet()) {
                String key = pref.getKey();
                if (!key.startsWith(labelPrefix) || !(pref.getValue() instanceof String)) continue;
                String plotId = safePlotId(key.substring(labelPrefix.length()));
                String displayPath = (String) pref.getValue();
                String relativePath = relativePathFromGrowPlot(displayPath);
                JSONObject entry = feeds.optJSONObject(plotId);
                if (entry == null) entry = new JSONObject();
                entry.put("displayPath", displayPath);
                if (relativePath != null) entry.put("relativePath", relativePath);
                else entry.remove("relativePath");
                feeds.put(plotId, entry);
            }
            config.put("schemaVersion", 1);
            config.put("updatedAt", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(new Date()));
            config.put("rootPath", "GrowPlot");
            config.put("imageFeedPattern", "Image_Feed/<plot-folder>");
            config.put("imageFeeds", feeds);
            JSONObject diagnosis = config.optJSONObject("diagnosis");
            if (diagnosis == null) diagnosis = new JSONObject();
            diagnosis.put("rootPath", "Diagnosis");
            diagnosis.put("mediaPattern", "Diagnosis/Plot_<n>/Quadrant_<01-06>");
            diagnosis.put("displayPath", getDocumentPathLabel(treeUri, findOrCreateChildFolder(treeUri, growPlotId, "Diagnosis")));
            config.put("diagnosis", diagnosis);
            String fileId = childFileDocumentId(treeUri, configId, GROWPLOT_STORAGE_CONFIG_FILE);
            Uri fileUri;
            if (fileId == null) {
                fileUri = DocumentsContract.createDocument(getContentResolver(),
                        DocumentsContract.buildDocumentUriUsingTree(treeUri, configId), "application/json", GROWPLOT_STORAGE_CONFIG_FILE);
                if (fileUri == null) throw new IllegalStateException("Google Drive refused to create the storage config file");
            } else fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, fileId);
            try (OutputStream output = getContentResolver().openOutputStream(fileUri, "wt")) {
                if (output == null) throw new IllegalStateException("Google Drive could not open the storage config for writing");
                output.write((config.toString(2) + "\n").getBytes(StandardCharsets.UTF_8));
                output.flush();
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (userStreamMode && !activeStreamScope.isEmpty()) {
            String[] parts = activeStreamScope.split("\\|", -1);
            if (parts.length == 3) mainHandler.postDelayed(() -> loadAssignedStreams(parts[1], parts[2], true), 500);
        }
        activityResumed = true;
        mainHandler.removeCallbacks(scheduledScan);
        scanSnapshotFolder();
        mainHandler.postDelayed(scheduledScan, refreshIntervalMinutes * 60_000L);
        if (!"[]".equals(getPrefs().getString(PREF_PENDING_DIAGNOSIS_UPLOADS, "[]"))) flushPendingDiagnosisUploads();
        if (ipWebcamVlcLayout != null && ipWebcamVlcPlayer == null) {
            mainHandler.postDelayed(this::reconnectIpWebcamVlc, 800);
        }
        if (ipWebcam2VlcLayout != null && ipWebcam2VlcPlayer == null) {
            mainHandler.postDelayed(this::reconnectCctv02Vlc, 800);
        }
        if (ipWebcam3VlcLayout != null && ipWebcam3VlcPlayer == null) {
            mainHandler.postDelayed(this::reconnectCctv03Vlc, 800);
        }
    }

    @Override protected void onPause() {
        // Release the live stream while GrowPlot is backgrounded. Reconnect on return.
        activityResumed = false;
        stopIpWebcamVlc();
        stopCctv02Vlc();
        stopCctv03Vlc();
        super.onPause();
    }

    @Override protected void onDestroy() {
        mainHandler.removeCallbacks(scheduledScan);
        if (locationListener != null) {
            try { ((LocationManager) getSystemService(LOCATION_SERVICE)).removeUpdates(locationListener); }
            catch (Exception ignored) { }
            locationListener = null;
            locationRequestInFlight = false;
        }
        locationPermissionRequestInFlight = false;
        stopIpWebcamVlc();
        stopCctv02Vlc();
        stopCctv03Vlc();
        if (ipWebcamVlcFrame != null && ipWebcamVlcFrame.getParent() instanceof FrameLayout) {
            ((FrameLayout) ipWebcamVlcFrame.getParent()).removeView(ipWebcamVlcFrame);
            ipWebcamVlcFrame = null;
        }
        ipWebcamVlcLayout = null;
        ipWebcamRefreshButton = null;
        if (ipWebcam2VlcFrame != null && ipWebcam2VlcFrame.getParent() instanceof FrameLayout) {
            ((FrameLayout) ipWebcam2VlcFrame.getParent()).removeView(ipWebcam2VlcFrame);
            ipWebcam2VlcFrame = null;
        }
        ipWebcam2VlcLayout = null;
        ipWebcam2RefreshButton = null;
        if (ipWebcam3VlcFrame != null && ipWebcam3VlcFrame.getParent() instanceof FrameLayout) {
            ((FrameLayout) ipWebcam3VlcFrame.getParent()).removeView(ipWebcam3VlcFrame);
            ipWebcam3VlcFrame = null;
        }
        ipWebcam3VlcLayout = null;
        ipWebcam3RefreshButton = null;
        scanWorker.shutdownNow();
        geocodeWorker.shutdownNow();
        diagnosisWorker.shutdownNow();
        kindwiseWorker.shutdownNow();
        diagnosisCvWorker.shutdownNow();
        if (web != null) {
            web.removeJavascriptInterface("GrowPlotBridge");
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VERTEX_AUTHORIZATION) {
            if (resultCode != RESULT_OK || data == null) {
                finishVertexAuthorization(null, "Google Cloud access was cancelled. Tap Run diagnosis and grant access to Vertex AI.");
                return;
            }
            try {
                AuthorizationResult authorization = Identity.getAuthorizationClient(this).getAuthorizationResultFromIntent(data);
                finishVertexAuthorization(authorization, null);
            } catch (Exception error) {
                finishVertexAuthorization(null, "Google Cloud authorization failed: " + safeErrorMessage(error));
            }
            return;
        }
        if (requestCode == REQUEST_DRIVE_AUTHORIZATION) {
            if (resultCode != RESULT_OK || data == null) {
                driveAuthorizationInProgress = false;
                pendingDriveFolderBrowse = false;
                pendingDriveReconnectOnly = false;
                notifyLoginDriveStatus(pendingDriveAuthPlotId, false, true, "Google Drive authorization was cancelled.");
                publishStatus(makeStatus("Google Drive authorization was cancelled."));
                return;
            }
            try {
                AuthorizationResult authorization = Identity.getAuthorizationClient(this).getAuthorizationResultFromIntent(data);
                completeDriveAuthorization(authorization);
            } catch (Exception error) {
                driveAuthorizationInProgress = false;
                notifyLoginDriveStatus(pendingDriveAuthPlotId, false, true,
                        "Google Drive authorization failed: " + safeErrorMessage(error));
            }
            return;
        }
        if (requestCode == REQUEST_FEED_CAMERA || requestCode == REQUEST_FEED_PICKER) {
            handleFeedPhotoResult(requestCode, resultCode, data);
            return;
        }
        if (requestCode == REQUEST_DIAGNOSIS_PHOTO || requestCode == REQUEST_DIAGNOSIS_VIDEO
                || requestCode == REQUEST_DIAGNOSIS_PICKER) {
            handleDiagnosisActivityResult(requestCode, resultCode, data);
            return;
        }
        if (requestCode == REQUEST_DIAGNOSIS_FOLDER) {
            handleDiagnosisFolderResult(resultCode, data);
            return;
        }
        if (requestCode != REQUEST_IMAGE_FOLDER) return;
        if (resultCode != RESULT_OK || data == null) {
            if (pickerCreatesDefaultFeed) {
                pickerCreatesDefaultFeed = false;
                publishStatus(makeStatus("Google Drive selection was cancelled. Tap Connect to folder to try again."));
            }
            return;
        }
        Uri folderUri = data.getData();
        if (folderUri == null) return;
        String plotId = safePlotId(pickerPlotId);
        String selectedFolderName = getDocumentName(folderUri);
        String selectedDocumentId = DocumentsContract.getTreeDocumentId(folderUri);
        boolean selectedRoot = "Projects".equalsIgnoreCase(selectedFolderName)
                || "GrowPlot".equalsIgnoreCase(selectedFolderName)
                || "Image_Feed".equalsIgnoreCase(selectedFolderName)
                || "My Drive".equalsIgnoreCase(selectedFolderName)
                || "Drive".equalsIgnoreCase(selectedFolderName);
        boolean selectedPlotFolder = selectedFolderName.equalsIgnoreCase(safeFolderName(pickerDefaultFolderName));
        if (pickerCreatesDefaultFeed && !selectedRoot && !selectedPlotFolder) {
            publishStatus(makeStatus("In Google Drive, choose this plot's existing folder under Projects/growplot/Image_Feed, or choose Projects, growplot, or Image_Feed to create it."));
            return;
        }
        int grantFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if ((grantFlags & Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0
                || (grantFlags & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) == 0) {
            publishStatus(makeStatus("Google Drive needs lasting read and write access. Choose the GrowPlot folder again and keep access enabled."));
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(folderUri, grantFlags);
        } catch (SecurityException error) {
            publishStatus(makeStatus("Android did not grant lasting access to that folder. Choose the data feed folder again."));
            return;
        }
        String locationLabel = null;
        boolean storageConfigSaved = false;
        try {
            String targetDocumentId = DocumentsContract.getTreeDocumentId(folderUri);
            String defaultRelativePath = null;
            if (pickerCreatesDefaultFeed) {
                if (selectedPlotFolder && !selectedRoot) {
                    // Keep the exact Drive folder the user selected; do not infer an
                    // email subfolder from the Firebase account.
                    targetDocumentId = selectedDocumentId;
                    locationLabel = getDocumentPathLabel(folderUri, targetDocumentId);
                } else {
                    String imageFeedId;
                    if ("Image_Feed".equalsIgnoreCase(selectedFolderName)) {
                        imageFeedId = selectedDocumentId;
                    } else {
                        targetDocumentId = resolveGrowPlotDocumentId(folderUri);
                        if (targetDocumentId == null) throw new IllegalStateException("Select Projects, growplot, Image_Feed, or this plot's existing Image_Feed folder in Google Drive");
                        imageFeedId = findOrCreateChildFolder(folderUri, targetDocumentId, "Image_Feed");
                    }
                    String emailFolder = signedInEmailFolderName();
                    String userFolderId = findOrCreateChildFolder(folderUri, imageFeedId, emailFolder);
                    String plotFolder = safeFolderName(pickerDefaultFolderName);
                    targetDocumentId = findOrCreateChildFolder(folderUri, userFolderId, plotFolder);
                    defaultRelativePath = "Image_Feed/" + emailFolder + "/" + plotFolder;
                }
            }
            if (locationLabel == null) {
                locationLabel = defaultRelativePath == null
                        ? getDocumentPathLabel(folderUri, targetDocumentId)
                        : "Google Drive: /Projects/growplot/" + defaultRelativePath;
            }
            JSONObject config = new JSONObject(getPrefs().getString("data_feed_config_" + plotId, "{}"));
            config.put("plotId", plotId);
            config.put("enabled", true);
            config.put("folderUri", folderUri.toString());
            getPrefs().edit()
                    .putString(feedTreeKey(plotId), folderUri.toString())
                    .putString(feedFolderDocumentKey(plotId), targetDocumentId)
                    .putString(feedLocationKey(plotId), locationLabel)
                    .putString("data_feed_config_" + plotId, config.toString())
                    .putString(PREF_ACTIVE_PLOT_ID, plotId)
                    .remove(driveApiFolderKey(plotId))
                    .remove(DRIVE_API_EMAIL_KEY_PREFIX + plotId)
                    .remove(DRIVE_API_FOLDER_NAME_KEY_PREFIX + plotId)
                    .remove(DRIVE_API_ACCOUNT_EMAIL_PREFIX + plotId)
                    .apply();
            rememberGrowPlotTreeIfScoped(folderUri);
            storageConfigSaved = saveDriveStorageConfig(folderUri);
            if (accountApiBridge != null) {
                accountApiBridge.saveImageFeedPath(plotId, locationLabel, relativePathFromGrowPlot(locationLabel));
            }
        } catch (Exception error) {
            String reason = error.getMessage() == null ? "Drive could not create or open the folder." : error.getMessage();
            publishStatus(makeStatus(pickerCreatesDefaultFeed
                    ? "Could not create the selected plot's Google Drive folder: " + reason
                    : "Could not save this plot's data feed: " + reason));
            return;
        }
        pickerCreatesDefaultFeed = false;
        notifyFeedSettingsSaved(plotId, locationLabel);
        if (!storageConfigSaved) {
            JSONObject status = makeStatus("Image feed location is saved on this device. To sync its path into Drive config, choose GrowPlot as the shared folder scope.");
            try { status.put("plotId", plotId); } catch (Exception ignored) { }
            publishStatus(status);
        }
        scanSnapshotFolder();
    }

    private void handleFeedPhotoResult(int requestCode, int resultCode, Intent data) {
        Uri output = pendingFeedPhotoOutput;
        pendingFeedPhotoOutput = null;
        if (resultCode != RESULT_OK) {
            deletePendingFeedPhoto(output);
            notifyFeedCaptureStatus("Photo cancelled.");
            return;
        }
        Uri source = requestCode == REQUEST_FEED_PICKER
                ? (data == null ? null : data.getData())
                : (data != null && data.getData() != null ? data.getData() : output);
        Bitmap thumbnail = data != null && data.getExtras() != null
                && data.getExtras().get("data") instanceof Bitmap
                ? (Bitmap) data.getExtras().get("data") : null;
        final int camera = pendingFeedCamera;
        final String plotId = safePlotId(pendingFeedPlotId);
        if (source == null && thumbnail == null) {
            deletePendingFeedPhoto(output);
            notifyFeedCaptureStatus("No photo was returned. Try the camera again or choose an image file.");
            return;
        }
        notifyFeedCaptureStatus("Saving photo to this plot’s Google Drive image folder…");
        scanWorker.execute(() -> {
            try {
                saveFeedPhotoToDrive(source, thumbnail, plotId, camera,
                        requestCode == REQUEST_FEED_CAMERA);
                runOnUiThread(() -> {
                    deletePendingFeedPhoto(output);
                    notifyFeedCaptureStatus("Photo saved to " + feedLocationLabel(plotId)
                            + " as CCTV" + String.format(Locale.US, "%02d", camera) + ".");
                    scanSnapshotFolder(plotId);
                });
            } catch (Exception error) {
                deletePendingFeedPhoto(output);
                String reason = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
                runOnUiThread(() -> notifyFeedCaptureStatus("Could not save photo to this plot’s image folder: " + reason));
            } finally {
                if (thumbnail != null && !thumbnail.isRecycled()) thumbnail.recycle();
            }
        });
    }

    private void launchFeedPhotoCapture(int camera, String plotId) {
        if (!isFeedFolderReady(plotId)) {
            notifyFeedCaptureStatus("Connect this plot’s image folder first using Connect to folder in this panel.");
            return;
        }
        pendingFeedCamera = camera;
        pendingFeedPlotId = safePlotId(plotId);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "GrowPlot_capture_" + System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GrowPlot");
                // Do not mark the output pending: several camera apps stall when an
                // ACTION_IMAGE_CAPTURE output points at a pending MediaStore row.
                pendingFeedPhotoOutput = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (pendingFeedPhotoOutput != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, pendingFeedPhotoOutput);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.setClipData(ClipData.newUri(getContentResolver(), "GrowPlot camera output", pendingFeedPhotoOutput));
                }
            } catch (Exception ignored) { pendingFeedPhotoOutput = null; }
        }
        if (intent.resolveActivity(getPackageManager()) == null) {
            deletePendingFeedPhoto(pendingFeedPhotoOutput);
            pendingFeedPhotoOutput = null;
            notifyFeedCaptureStatus("No compatible camera app was found. Choose an image file instead.");
            return;
        }
        notifyFeedCaptureStatus("Taking a photo for CCTV " + String.format(Locale.US, "%02d", camera) + "…");
        try { startActivityForResult(Intent.createChooser(intent, "Take photo for CCTV " + camera), REQUEST_FEED_CAMERA); }
        catch (Exception error) {
            deletePendingFeedPhoto(pendingFeedPhotoOutput);
            pendingFeedPhotoOutput = null;
            notifyFeedCaptureStatus("Could not open the camera. Choose an image file instead.");
        }
    }

    private void launchFeedPhotoPicker(int camera, String plotId) {
        if (!isFeedFolderReady(plotId)) {
            notifyFeedCaptureStatus("Connect this plot’s image folder first using Connect to folder in this panel.");
            return;
        }
        pendingFeedCamera = camera;
        pendingFeedPlotId = safePlotId(plotId);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        notifyFeedCaptureStatus("Choose a photo for CCTV " + String.format(Locale.US, "%02d", camera) + ". It will be copied into this plot’s image folder.");
        try { startActivityForResult(intent, REQUEST_FEED_PICKER); }
        catch (Exception error) { notifyFeedCaptureStatus("No image file picker is available on this device."); }
    }

    private boolean isFeedFolderReady(String plotId) {
        String id = safePlotId(plotId);
        String apiFolder = driveApiFolderForSignedInUser(id);
        if (apiFolder != null && !apiFolder.isEmpty()) return true;
        String tree = getPrefs().getString(feedTreeKey(id), null);
        String folder = getPrefs().getString(feedFolderDocumentKey(id), null);
        return tree != null && folder != null && hasPersistedReadWriteGrant(Uri.parse(tree));
    }

    private void saveFeedPhotoToDrive(Uri source, Bitmap thumbnail, String plotId, int camera,
                                      boolean portraitCapture) throws Exception {
        String id = safePlotId(plotId);
        String apiFolderId = driveApiFolderForSignedInUser(id);
        String treeText = getPrefs().getString(feedTreeKey(id), null);
        String folderId = getPrefs().getString(feedFolderDocumentKey(id), null);
        if ((apiFolderId == null || apiFolderId.isEmpty()) && (treeText == null || folderId == null))
            throw new IllegalStateException("The image-feed folder is not configured for this plot");
        Uri treeUri = treeText == null ? null : Uri.parse(treeText);
        if ((apiFolderId == null || apiFolderId.isEmpty())
                && (treeUri == null || !hasPersistedReadWriteGrant(treeUri)))
            throw new IllegalStateException("Saved Google Drive folder access is unavailable; reconnect the feed in Settings");
        Bitmap bitmap = thumbnail;
        Bitmap decodedBitmap = null;
        Bitmap orientedBitmap = null;
        int exifOrientation = ExifInterface.ORIENTATION_NORMAL;
        if (source != null) {
            try (InputStream input = getContentResolver().openInputStream(source)) {
                if (input != null) exifOrientation = new ExifInterface(input).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            } catch (Exception ignored) { exifOrientation = ExifInterface.ORIENTATION_NORMAL; }
            try (InputStream input = getContentResolver().openInputStream(source)) {
                if (input != null) decodedBitmap = BitmapFactory.decodeStream(input);
            }
            if (decodedBitmap != null) bitmap = decodedBitmap;
        }
        if (bitmap == null && thumbnail != null) bitmap = thumbnail;
        if (bitmap == null) throw new IllegalStateException("The selected image could not be decoded");
        float rotation = exifOrientation == ExifInterface.ORIENTATION_ROTATE_90
                || exifOrientation == ExifInterface.ORIENTATION_TRANSPOSE ? 90f
                : exifOrientation == ExifInterface.ORIENTATION_ROTATE_180 ? 180f
                : exifOrientation == ExifInterface.ORIENTATION_ROTATE_270
                || exifOrientation == ExifInterface.ORIENTATION_TRANSVERSE ? 270f : 0f;
        if (rotation != 0f) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            orientedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap = orientedBitmap;
        }
        if (portraitCapture && bitmap.getWidth() > bitmap.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90f);
            Bitmap portraitBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (orientedBitmap != null && orientedBitmap != decodedBitmap && orientedBitmap != thumbnail) {
                orientedBitmap.recycle();
            }
            orientedBitmap = portraitBitmap;
            bitmap = portraitBitmap;
        }
        SimpleDateFormat snapshotClock = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US);
        snapshotClock.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        String fileName = "CCTV" + String.format(Locale.US, "%02d", camera) + "_"
                + snapshotClock.format(new Date()) + ".jpg";
        if (apiFolderId != null && !apiFolderId.isEmpty()) {
            try {
                uploadDriveApiPhoto(apiFolderId, fileName, bitmap);
            } finally {
                if (orientedBitmap != null && orientedBitmap != decodedBitmap
                        && orientedBitmap != thumbnail && !orientedBitmap.isRecycled()) orientedBitmap.recycle();
                if (decodedBitmap != null && decodedBitmap != thumbnail && !decodedBitmap.isRecycled()) decodedBitmap.recycle();
            }
            return;
        }
        Uri folderUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, folderId);
        Uri destination = DocumentsContract.createDocument(getContentResolver(), folderUri, "image/jpeg", fileName);
        if (destination == null) throw new IllegalStateException("Google Drive could not create the photo in the image-feed folder");
        try (OutputStream output = getContentResolver().openOutputStream(destination, "w")) {
            if (output == null) throw new IllegalStateException("Google Drive could not open the new photo");
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)) throw new IllegalStateException("Could not convert the selected image to JPEG");
            output.flush();
        } catch (Exception error) {
            try { DocumentsContract.deleteDocument(getContentResolver(), destination); } catch (Exception ignored) { }
            throw error;
        } finally {
            if (orientedBitmap != null && orientedBitmap != decodedBitmap
                    && orientedBitmap != thumbnail && !orientedBitmap.isRecycled()) orientedBitmap.recycle();
            if (decodedBitmap != null && decodedBitmap != thumbnail && !decodedBitmap.isRecycled()) decodedBitmap.recycle();
        }
    }

    private void deletePendingFeedPhoto(Uri uri) {
        if (uri == null) return;
        try { getContentResolver().delete(uri, null, null); } catch (Exception ignored) { }
    }

    private void notifyFeedCaptureStatus(String message) {
        if (web != null) web.evaluateJavascript("window.onFeedCaptureStatus&&window.onFeedCaptureStatus(" + JSONObject.quote(message) + ")", null);
    }

    private void handleDiagnosisActivityResult(int requestCode, int resultCode, Intent data) {
        final Uri output = pendingDiagnosisOutput;
        final int camera = pendingDiagnosisCamera;
        final String plotId = safePlotId(pendingDiagnosisPlotId);
        final String plotName = safeFolderName(pendingDiagnosisPlotName);
        final String kind = requestCode == REQUEST_DIAGNOSIS_PICKER
                ? mediaKind(data == null ? null : data.getData()) : pendingDiagnosisKind;
        pendingDiagnosisOutput = null;
        if (resultCode != RESULT_OK) {
            deletePendingDiagnosisOutput(output);
            notifyDiagnosisStatus("Capture or media selection cancelled.");
            return;
        }
        Uri returned = data == null ? null : data.getData();
        Uri source = requestCode == REQUEST_DIAGNOSIS_PICKER ? returned : (returned != null ? returned : output);
        if (camera < 1 || camera > 6) {
            deletePendingDiagnosisOutput(output);
            notifyDiagnosisStatus("The camera session expired. Reopen Diagnose and try again.");
            return;
        }
        if ("photo".equals(kind) && diagnosisPhotoCount(camera, plotId) >= 5) {
            deletePendingDiagnosisOutput(output);
            notifyDiagnosisStatus("This quadrant already has 5 photos. Remove a photo before adding another.");
            return;
        }
        if (source == null && requestCode == REQUEST_DIAGNOSIS_PHOTO && data != null
                && data.getExtras() != null && data.getExtras().get("data") instanceof Bitmap) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            notifyDiagnosisStatus("Saving the captured photo and thumbnail…");
            diagnosisWorker.execute(() -> {
                try { persistAndPostDiagnosisMedia(camera, plotId, plotName, "photo", saveDiagnosisThumbnail(thumbnail, camera, plotId)); }
                catch (Exception error) { runOnUiThread(() -> notifyDiagnosisStatus("Could not save the captured photo.")); }
                finally { deletePendingDiagnosisOutput(output); }
            });
        } else if (source != null) {
            Uri mediaUri = source;
            notifyDiagnosisStatus("Saving the captured media and preparing its thumbnail…");
            diagnosisWorker.execute(() -> {
                try {
                    File saved = copyDiagnosisMedia(mediaUri, kind, camera, plotId);
                    deletePendingDiagnosisOutput(output);
                    persistAndPostDiagnosisMedia(camera, plotId, plotName, kind, saved);
                } catch (Exception error) {
                    deletePendingDiagnosisOutput(output);
                    runOnUiThread(() -> notifyDiagnosisStatus("Could not save that media to this diagnosis."));
                }
            });
        } else {
            deletePendingDiagnosisOutput(output);
            if (requestCode == REQUEST_DIAGNOSIS_VIDEO) {
                notifyDiagnosisStatus("The camera did not return the video file. Select the new recording from Google Photos or Files to save it.");
                launchDiagnosisVideoPicker(camera, plotId, plotName);
            } else {
                notifyDiagnosisStatus("The camera returned no media. Try another camera app or choose an existing file.");
            }
        }
    }

    private void handleDiagnosisFolderResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            notifyDiagnosisStorageStatus("Google Drive folder selection cancelled. Media will stay on this device.");
            return;
        }
        Uri treeUri = data.getData();
        int grantFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if ((grantFlags & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) == 0
                || (grantFlags & Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0) {
            notifyDiagnosisStorageStatus("Google Drive needs read and write access. Choose a writable GrowPlot folder.");
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(treeUri, grantFlags);
            String oldTree = getPrefs().getString(PREF_DIAGNOSIS_TREE_URI, null);
            if (oldTree == null || !oldTree.equals(treeUri.toString())) clearDiagnosisFolderIdCache();
            String rootDocumentId = resolveDiagnosisRoot(treeUri);
            String label = getDocumentPathLabel(treeUri, rootDocumentId);
            getPrefs().edit().putString(PREF_DIAGNOSIS_TREE_URI, treeUri.toString())
                    .putString(PREF_DIAGNOSIS_ROOT_DOCUMENT_ID, rootDocumentId)
                    .putString(PREF_DIAGNOSIS_FOLDER_LABEL, label)
                    .apply();
            rememberGrowPlotTreeIfScoped(treeUri);
            boolean configSaved = saveDriveStorageConfig(treeUri);
            notifyDiagnosisStorageStatus("Google Drive is ready as the default storage: " + label
                    + (configSaved ? ". The path is saved in GrowPlot/config/storage-paths.json." : ". The device has saved access, but choose GrowPlot as the shared folder scope to save both paths in Drive config."));
            flushPendingDiagnosisUploads();
            refreshDiagnosisMediaForActivePlot();
        } catch (Exception error) {
            String reason = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
            notifyDiagnosisStorageStatus("Could not connect this Google Drive folder: " + reason
                    + ". Select GrowPlot; GrowPlot will use GrowPlot/Diagnosis automatically.");
        }
    }

    private void openDiagnosisFolderPicker() {
        if (adoptExistingGrowPlotDriveGrant()) {
            notifyDiagnosisStorageStatus("Using the default Google Drive folder: GrowPlot/Diagnosis");
            refreshDiagnosisMediaForActivePlot();
            return;
        }
        requestDiagnosisFolderAuthorization();
    }

    private void requestDiagnosisFolderAuthorization() {
        if (isFinishing()) return;
        // Android requires an explicit, persisted Storage Access Framework grant before an app
        // can read or write a user's private Google Drive files. Ask once for the parent scope;
        // resolveDiagnosisRoot creates/uses the canonical GrowPlot/Diagnosis path automatically.
        notifyDiagnosisStorageStatus("One-time Android permission needed: choose GrowPlot in Google Drive. GrowPlot will use GrowPlot/Diagnosis automatically.");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(Intent.EXTRA_TITLE, "Allow GrowPlot to access GrowPlot in Google Drive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQUEST_DIAGNOSIS_FOLDER);
        } catch (Exception error) {
            notifyDiagnosisStorageStatus("Android could not open the Drive permission screen. Open Settings → Apps → GrowPlot → Permissions, then retry. "
                    + (error.getMessage() == null ? "" : error.getMessage()));
        }
    }

    private boolean adoptExistingGrowPlotDriveGrant() {
        String activePlot = safePlotId(getPrefs().getString(PREF_ACTIVE_PLOT_ID, DEFAULT_PLOT_ID));
        Set<String> candidates = new java.util.LinkedHashSet<>();
        for (String saved : new String[]{getPrefs().getString(PREF_GROWPLOT_TREE_URI, null),
                getPrefs().getString(PREF_DIAGNOSIS_TREE_URI, null),
                getPrefs().getString(PREF_IMAGE_FEED_TREE_URI, null),
                getPrefs().getString(feedTreeKey(activePlot), null)}) {
            if (saved != null) candidates.add(saved);
        }
        List<android.content.UriPermission> grants = getContentResolver().getPersistedUriPermissions();
        // If app preferences were cleared while Android still retained the SAF grant, recover the
        // saved tree from the provider instead of reopening the folder picker on every launch.
        for (android.content.UriPermission grant : grants) {
            Uri uri = grant.getUri();
            String authority = uri.getAuthority();
            if (authority != null && authority.toLowerCase(Locale.ROOT).contains("google.android.apps.docs")
                    && grant.isReadPermission() && grant.isWritePermission()) candidates.add(uri.toString());
        }
        for (String candidate : candidates) {
            Uri treeUri = Uri.parse(candidate);
            String authority = treeUri.getAuthority();
            if (authority == null || !authority.toLowerCase(Locale.ROOT).contains("google.android.apps.docs")) continue;
            boolean read = false, write = false;
            for (android.content.UriPermission grant : grants) {
                if (grant.getUri().equals(treeUri)) {
                    read = grant.isReadPermission();
                    write = grant.isWritePermission();
                    break;
                }
            }
            if (!read || !write) continue;
            try {
                String diagnosisId = resolveDiagnosisRoot(treeUri);
                String label = getDocumentPathLabel(treeUri, diagnosisId);
                getPrefs().edit().putString(PREF_DIAGNOSIS_TREE_URI, treeUri.toString())
                        .putString(PREF_DIAGNOSIS_ROOT_DOCUMENT_ID, diagnosisId)
                        .putString(PREF_DIAGNOSIS_FOLDER_LABEL, label)
                        .apply();
                rememberGrowPlotTreeIfScoped(treeUri);
                return true;
            } catch (Exception ignored) { }
        }
        return false;
    }

    /** Resolve one canonical path from the granted parent: GrowPlot/Diagnosis. */
    private String resolveDiagnosisRoot(Uri treeUri) throws Exception {
        String id = DocumentsContract.getTreeDocumentId(treeUri);
        String selectedName = getDocumentName(treeUri);
        if ("Diagnosis".equalsIgnoreCase(selectedName)) return id;
        if ("GrowPlot".equalsIgnoreCase(selectedName)) {
            return findOrCreateChildFolder(treeUri, id, "Diagnosis");
        }
        if ("Projects".equalsIgnoreCase(selectedName)) {
            String growPlotId = findOrCreateChildFolder(treeUri, id, "GrowPlot");
            return findOrCreateChildFolder(treeUri, growPlotId, "Diagnosis");
        }
        if ("My Drive".equalsIgnoreCase(selectedName) || "Drive".equalsIgnoreCase(selectedName)) {
            String projectsId = findOrCreateChildFolder(treeUri, id, "Projects");
            String growPlotId = findOrCreateChildFolder(treeUri, projectsId, "GrowPlot");
            return findOrCreateChildFolder(treeUri, growPlotId, "Diagnosis");
        }
        throw new IllegalArgumentException("Choose the Projects, GrowPlot, or Diagnosis folder; refusing to create Diagnosis inside " + selectedName);
    }

    private void launchDiagnosisVideoPicker(int camera, String plotId, String plotName) {
        pendingDiagnosisCamera = camera;
        pendingDiagnosisPlotId = safePlotId(plotId);
        pendingDiagnosisPlotName = safeFolderName(plotName);
        pendingDiagnosisOutput = null;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try { startActivityForResult(intent, REQUEST_DIAGNOSIS_PICKER); }
        catch (Exception error) { notifyDiagnosisStatus("No video picker is available. The video was not imported."); }
    }

    private void deletePendingDiagnosisOutput(Uri output) {
        if (output != null) try { getContentResolver().delete(output, null, null); } catch (Exception ignored) { }
    }

    private int diagnosisPhotoCount(int camera, String plotId) {
        File directory = new File(new File(new File(getFilesDir(), "diagnosis_media"), safePlotId(plotId)),
                String.format(Locale.US, "cctv_%02d", camera));
        File[] photos = directory.listFiles((dir, name) -> name.toLowerCase(Locale.US).endsWith(".jpg")
                && !name.toLowerCase(Locale.US).endsWith("_thumb.jpg"));
        return photos == null ? 0 : photos.length;
    }

    private File copyDiagnosisMedia(Uri source, String kind, int camera, String plotId) throws Exception {
        String extension = "video".equals(kind) ? ".mp4" : ".jpg";
        File directory = new File(new File(new File(getFilesDir(), "diagnosis_media"), safePlotId(plotId)),
                String.format(Locale.US, "cctv_%02d", camera));
        if (!directory.isDirectory() && !directory.mkdirs()) throw new IllegalStateException("Cannot create diagnosis folder");
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date());
        File target = new File(directory, stamp + extension);
        try (InputStream input = getContentResolver().openInputStream(source);
             FileOutputStream output = new FileOutputStream(target)) {
            if (input == null) throw new IllegalStateException("Media could not be opened");
            byte[] buffer = new byte[16384];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        }
        if (target.length() == 0) { target.delete(); throw new IllegalStateException("Empty media file"); }
        return target;
    }

    private File saveDiagnosisThumbnail(Bitmap bitmap, int camera, String plotId) throws Exception {
        File directory = new File(new File(new File(getFilesDir(), "diagnosis_media"), safePlotId(plotId)),
                String.format(Locale.US, "cctv_%02d", camera));
        if (!directory.isDirectory() && !directory.mkdirs()) throw new IllegalStateException("Cannot create diagnosis folder");
        File target = new File(directory, new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date()) + ".jpg");
        try (FileOutputStream output = new FileOutputStream(target)) {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)) throw new IllegalStateException("Image encode failed");
        }
        return target;
    }

    private String mediaKind(Uri uri) {
        if (uri == null) return "photo";
        String mime = getContentResolver().getType(uri);
        return mime != null && mime.startsWith("video/") ? "video" : "photo";
    }

    private void persistAndPostDiagnosisMedia(int camera, String plotId, String plotName, String kind, File saved) {
        String drivePath = null;
        String driveError = null;
        boolean driveReady = isDiagnosisDriveReady() || adoptExistingGrowPlotDriveGrant();
        if (driveReady) {
            try { drivePath = saveDiagnosisMediaToDrive(saved, kind, camera, plotId); }
            catch (Exception error) {
                driveError = error.getMessage() == null ? "Google Drive could not save this file." : error.getMessage();
                queuePendingDiagnosisUpload(camera, plotId, kind, saved);
            }
        } else {
            driveError = "One-time Google Drive permission is needed to upload this media.";
            queuePendingDiagnosisUpload(camera, plotId, kind, saved);
        }
        postDiagnosisMedia(camera, plotId, kind, saved, drivePath, driveError);
        if (!driveReady) runOnUiThread(this::openDiagnosisFolderPicker);
    }

    private synchronized void queuePendingDiagnosisUpload(int camera, String plotId, String kind, File media) {
        JSONArray queue;
        try { queue = new JSONArray(getPrefs().getString(PREF_PENDING_DIAGNOSIS_UPLOADS, "[]")); }
        catch (Exception ignored) { queue = new JSONArray(); }
        JSONObject item = new JSONObject();
        try {
            item.put("camera", camera);
            item.put("plotId", safePlotId(plotId));
            item.put("kind", kind);
            item.put("path", media.getAbsolutePath());
            item.put("fileName", media.getName());
            for (int i = queue.length() - 1; i >= 0; i--) {
                JSONObject old = queue.optJSONObject(i);
                if (old != null && media.getAbsolutePath().equals(old.optString("path"))) queue.remove(i);
            }
            queue.put(item);
            getPrefs().edit().putString(PREF_PENDING_DIAGNOSIS_UPLOADS, queue.toString()).apply();
        } catch (Exception ignored) { }
    }

    private void flushPendingDiagnosisUploads() {
        diagnosisWorker.execute(() -> {
            if (!isDiagnosisDriveReady() && !adoptExistingGrowPlotDriveGrant()) return;
            JSONArray queue;
            try { queue = new JSONArray(getPrefs().getString(PREF_PENDING_DIAGNOSIS_UPLOADS, "[]")); }
            catch (Exception ignored) { queue = new JSONArray(); }
            JSONArray remaining = new JSONArray();
            for (int i = 0; i < queue.length(); i++) {
                JSONObject pending = queue.optJSONObject(i);
                if (pending == null) continue;
                File file = new File(pending.optString("path", ""));
                int camera = pending.optInt("camera", 0);
                String plotId = safePlotId(pending.optString("plotId", DEFAULT_PLOT_ID));
                String kind = pending.optString("kind", "photo");
                try {
                    if (!file.isFile() || camera < 1 || camera > 6) throw new IllegalStateException("Local media is no longer available");
                    String drivePath = saveDiagnosisMediaToDrive(file, kind, camera, plotId);
                    markDiagnosisMediaDriveSaved(camera, plotId, file.getName(), drivePath);
                } catch (Exception error) {
                    remaining.put(pending);
                }
            }
            getPrefs().edit().putString(PREF_PENDING_DIAGNOSIS_UPLOADS, remaining.toString()).apply();
        });
    }

    private void markDiagnosisMediaDriveSaved(int camera, String plotId, String fileName, String drivePath) {
        String key = diagnosisMediaIndexKey(camera, plotId);
        JSONArray old;
        try { old = new JSONArray(getPrefs().getString(key, "[]")); }
        catch (Exception ignored) { old = new JSONArray(); }
        JSONArray updated = new JSONArray();
        for (int i = 0; i < old.length(); i++) {
            JSONObject item = old.optJSONObject(i);
            if (item == null) continue;
            if (fileName.equals(item.optString("fileName"))) {
                try { item.put("driveSaved", true); item.put("drivePath", drivePath); item.remove("driveError"); }
                catch (Exception ignored) { }
            }
            updated.put(item);
        }
        getPrefs().edit().putString(key, updated.toString()).apply();
        runOnUiThread(() -> {
            if (web != null) {
                String script = "window.onDiagnosisMediaDriveSaved&&window.onDiagnosisMediaDriveSaved("
                        + camera + "," + JSONObject.quote(safePlotId(plotId)) + ","
                        + JSONObject.quote(fileName) + "," + JSONObject.quote(drivePath) + ")";
                web.evaluateJavascript(script, null);
            }
            notifyDiagnosisStatus("Media uploaded to Google Drive: " + drivePath);
        });
    }

    private void clearDiagnosisFolderIdCache() {
        SharedPreferences preferences = getPrefs();
        SharedPreferences.Editor editor = preferences.edit();
        for (String key : preferences.getAll().keySet()) {
            if (key.startsWith("diagnosis_plot_document_") || key.startsWith("diagnosis_quadrant_document_")) editor.remove(key);
        }
        editor.remove(PREF_DIAGNOSIS_ROOT_DOCUMENT_ID).apply();
    }

    private String saveDiagnosisMediaToDrive(File localFile, String kind, int camera, String plotId) throws Exception {
        String treeText = getPrefs().getString(PREF_DIAGNOSIS_TREE_URI, null);
        if (treeText == null) throw new IllegalStateException("Google Drive destination is not configured");
        Uri treeUri = Uri.parse(treeText);
        String rootId = getPrefs().getString(PREF_DIAGNOSIS_ROOT_DOCUMENT_ID, null);
        if (rootId == null) {
            rootId = resolveDiagnosisRoot(treeUri);
            getPrefs().edit().putString(PREF_DIAGNOSIS_ROOT_DOCUMENT_ID, rootId)
                    .putString(PREF_DIAGNOSIS_FOLDER_LABEL, getDocumentPathLabel(treeUri, rootId)).apply();
        }
        String plotFolder = diagnosisPlotFolder(plotId);
        String plotKey = "diagnosis_plot_document_" + safePlotId(plotId);
        String plotDocumentId = getPrefs().getString(plotKey, null);
        if (plotDocumentId == null) {
            plotDocumentId = findOrCreateChildFolder(treeUri, rootId, plotFolder);
            getPrefs().edit().putString(plotKey, plotDocumentId).apply();
        }
        String quadrantName = "Quadrant_" + String.format(Locale.US, "%02d", camera);
        String quadrantKey = "diagnosis_quadrant_document_" + safePlotId(plotId) + "_" + String.format(Locale.US, "%02d", camera);
        String quadrantId = getPrefs().getString(quadrantKey, null);
        if (quadrantId == null) {
            quadrantId = findOrCreateChildFolder(treeUri, plotDocumentId, quadrantName);
            getPrefs().edit().putString(quadrantKey, quadrantId).apply();
        }
        Uri quadrantUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, quadrantId);
        String mime = "video".equals(kind) ? "video/mp4" : "image/jpeg";
        Uri destination = DocumentsContract.createDocument(getContentResolver(), quadrantUri, mime, localFile.getName());
        if (destination == null) throw new IllegalStateException("Drive did not create the media file");
        try (InputStream input = new FileInputStream(localFile);
             OutputStream output = getContentResolver().openOutputStream(destination, "w")) {
            if (output == null) throw new IllegalStateException("Drive did not open the new file for writing");
            byte[] buffer = new byte[65536];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            output.flush();
        } catch (Exception error) {
            try { DocumentsContract.deleteDocument(getContentResolver(), destination); } catch (Exception ignored) { }
            throw error;
        }
        return "Diagnosis/" + plotFolder + "/" + quadrantName + "/" + localFile.getName();
    }

    private void postDiagnosisMedia(int camera, String plotId, String kind, File saved, String drivePath, String driveError) {
        JSONObject item = new JSONObject();
        try {
            item.put("camera", camera);
            item.put("plotId", safePlotId(plotId));
            item.put("kind", kind);
            item.put("url", "https://appassets.androidplatform.net/assets/diagnosis/" + safePlotId(plotId)
                    + "/" + String.format(Locale.US, "cctv_%02d", camera) + "/" + saved.getName());
            item.put("fileName", saved.getName());
            if (drivePath != null) item.put("drivePath", drivePath);
            item.put("driveSaved", drivePath != null);
            if (driveError != null) item.put("driveError", driveError);
            if ("video".equals(kind)) {
                File poster = createDiagnosisVideoPoster(saved, camera, plotId);
                if (poster != null) item.put("thumbnailUrl", "https://appassets.androidplatform.net/assets/diagnosis/"
                        + safePlotId(plotId) + "/" + String.format(Locale.US, "cctv_%02d", camera) + "/" + poster.getName());
            }
            item.put("capturedAt", new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US)
                    .format(new Date(saved.lastModified())) + " MYT");
        } catch (Exception ignored) { }
        saveDiagnosisMediaIndex(camera, plotId, item);
        runOnUiThread(() -> {
            if (web != null) web.evaluateJavascript("window.onDiagnosisMediaAdded&&window.onDiagnosisMediaAdded(" + item + ")", null);
            if (drivePath != null) notifyDiagnosisStatus("Saved on this device and to Google Drive: " + drivePath);
            else if (driveError != null) notifyDiagnosisStatus("Saved on this device, but Google Drive upload failed: " + driveError);
            else notifyDiagnosisStatus("Saved on this device. Choose a Google Drive Diagnosis folder to enable uploads.");
        });
    }

    private String diagnosisStorageLabel() {
        String label = getPrefs().getString(PREF_DIAGNOSIS_FOLDER_LABEL, null);
        return label == null ? "Not connected · default: Google Drive / GrowPlot / Diagnosis" : label;
    }

    private boolean isDiagnosisDriveReady() {
        // The user granted a persistent Android Storage Access Framework permission.
        // Do not poll UriPermissions on every capture/read; trust the saved connection
        // and let the actual Drive operation report revocation or provider failures.
        return getPrefs().getString(PREF_DIAGNOSIS_TREE_URI, null) != null
                && getPrefs().getString(PREF_DIAGNOSIS_ROOT_DOCUMENT_ID, null) != null;
    }

    private String diagnosisPlotFolder(String plotId) {
        Matcher matcher = Pattern.compile("(?i)^plot[-_ ]*(\\d+)").matcher(safePlotId(plotId));
        return matcher.find() ? "Plot_" + matcher.group(1) : safeFolderName(plotId);
    }

    private void notifyDiagnosisStorageStatus(String message) {
        if (web != null) web.evaluateJavascript("window.onDiagnosisStorageStatus&&window.onDiagnosisStorageStatus(" + JSONObject.quote(message) + ")", null);
    }

    private String diagnosisMediaIndexKey(int camera, String plotId) {
        return "diagnosis_media_index_" + safePlotId(plotId) + "_cctv_" + String.format(Locale.US, "%02d", camera);
    }

    private void saveDiagnosisMediaIndex(int camera, String plotId, JSONObject item) {
        JSONArray list = new JSONArray();
        try {
            JSONArray old = new JSONArray(getPrefs().getString(diagnosisMediaIndexKey(camera, plotId), "[]"));
            for (int i = 0; i < old.length(); i++) {
                JSONObject existing = old.optJSONObject(i);
                if (existing != null && !item.optString("fileName").equals(existing.optString("fileName"))) list.put(existing);
            }
        } catch (Exception ignored) { }
        list.put(item);
        getPrefs().edit().putString(diagnosisMediaIndexKey(camera, plotId), list.toString()).apply();
    }

    private String readDiagnosisMediaIndex(int camera, String plotId) {
        return getPrefs().getString(diagnosisMediaIndexKey(camera, plotId), "[]");
    }

    private void refreshDiagnosisMediaForActivePlot() {
        String plotId = safePlotId(getPrefs().getString(PREF_ACTIVE_PLOT_ID, DEFAULT_PLOT_ID));
        for (int camera = 1; camera <= 6; camera++) refreshDiagnosisMediaFromDrive(camera, plotId);
    }

    private void refreshDiagnosisMediaFromDrive(int camera, String plotId) {
        if (camera < 1 || camera > 6 || !isDiagnosisDriveReady()) return;
        final String id = safePlotId(plotId);
        diagnosisWorker.execute(() -> {
            JSONArray result = new JSONArray();
            String errorMessage = null;
            try {
                if (!isDiagnosisDriveReady()) throw new IllegalStateException("Google Drive Diagnosis folder is not connected");
                result = listAndCacheDriveDiagnosisMedia(camera, id);
            }
            catch (Exception error) { errorMessage = error.getMessage() == null ? "Drive folder could not be read." : error.getMessage(); }
            final String error = errorMessage;
            final String json = result.toString();
            runOnUiThread(() -> {
                if (web == null) return;
                String script = "window.onDiagnosisMediaSync&&window.onDiagnosisMediaSync(" + camera + ","
                        + JSONObject.quote(id) + "," + JSONObject.quote(json) + "," + JSONObject.quote(error) + ")";
                web.evaluateJavascript(script, null);
            });
        });
    }

    private static final class DriveDiagnosisMedia {
        String documentId;
        String displayName;
        String mimeType;
        long modified;
        long size;
    }

    private static final class PlantNetImageCandidate {
        byte[] jpeg;
        String source;
        double quality;
        PlantNetImageCandidate(byte[] jpeg, String source, double quality) { this.jpeg = jpeg; this.source = source; this.quality = quality; }
    }

    private JSONArray listAndCacheDriveDiagnosisMedia(int camera, String plotId) throws Exception {
        JSONArray result = new JSONArray();
        String treeText = getPrefs().getString(PREF_DIAGNOSIS_TREE_URI, null);
        String rootId = getPrefs().getString(PREF_DIAGNOSIS_ROOT_DOCUMENT_ID, null);
        if (treeText == null || rootId == null) return result;
        Uri treeUri = Uri.parse(treeText);
        String plotFolder = diagnosisPlotFolder(plotId);
        String plotDocumentId = findExistingChildFolder(treeUri, rootId, plotFolder);
        if (plotDocumentId == null) return result;
        String quadrantName = "Quadrant_" + String.format(Locale.US, "%02d", camera);
        String quadrantId = findExistingChildFolder(treeUri, plotDocumentId, quadrantName);
        if (quadrantId == null) return result;
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, quadrantId);
        String[] columns = {DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                DocumentsContract.Document.COLUMN_SIZE};
        List<DriveDiagnosisMedia> media = new ArrayList<>();
        try (Cursor cursor = getContentResolver().query(childrenUri, columns, null, null, null)) {
            if (cursor == null) throw new IllegalStateException("Google Drive could not list " + plotFolder + "/" + quadrantName);
            int idColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            int nameColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            int mimeColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);
            int modifiedColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
            int sizeColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE);
            while (cursor.moveToNext()) {
                if (idColumn < 0 || nameColumn < 0 || mimeColumn < 0) continue;
                String mime = cursor.getString(mimeColumn);
                String name = cursor.getString(nameColumn);
                if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime) || name == null) continue;
                boolean video = mime != null && mime.startsWith("video/");
                boolean photo = mime != null && mime.startsWith("image/");
                if (!video && !photo) continue;
                DriveDiagnosisMedia item = new DriveDiagnosisMedia();
                item.documentId = cursor.getString(idColumn);
                item.displayName = name;
                item.mimeType = mime;
                item.modified = modifiedColumn >= 0 && !cursor.isNull(modifiedColumn) ? cursor.getLong(modifiedColumn) : 0;
                item.size = sizeColumn >= 0 && !cursor.isNull(sizeColumn) ? cursor.getLong(sizeColumn) : -1;
                media.add(item);
            }
        }
        Collections.sort(media, (left, right) -> Long.compare(right.modified, left.modified));
        int limit = Math.min(media.size(), 10);
        File directory = new File(new File(new File(getCacheDir(), "diagnosis_drive_cache"), plotId),
                String.format(Locale.US, "cctv_%02d", camera));
        if (!directory.isDirectory() && !directory.mkdirs()) throw new IllegalStateException("Cannot create Drive playback cache");
        Set<String> keepNames = new HashSet<>();
        for (int index = 0; index < limit; index++) {
            DriveDiagnosisMedia item = media.get(index);
            boolean video = item.mimeType.startsWith("video/");
            String extension = video ? ".mp4" : ".jpg";
            long stamp = Math.max(0, item.modified);
            String cacheName = "d" + Long.toHexString(stamp) + "_" + Integer.toHexString(item.documentId.hashCode()) + extension;
            keepNames.add(cacheName);
            File cached = new File(directory, cacheName);
            if (!cached.isFile() || (item.size >= 0 && cached.length() != item.size)) {
                Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, item.documentId);
                File temp = new File(directory, cacheName + ".part");
                try (InputStream input = getContentResolver().openInputStream(documentUri);
                     FileOutputStream output = new FileOutputStream(temp)) {
                    if (input == null) throw new IllegalStateException("Drive could not open " + item.displayName);
                    byte[] buffer = new byte[65536];
                    int read;
                    while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
                    output.flush();
                } catch (Exception error) {
                    temp.delete();
                    throw error;
                }
                if (item.size >= 0 && temp.length() != item.size) {
                    temp.delete();
                    throw new IllegalStateException("Drive download was incomplete for " + item.displayName);
                }
                if (cached.exists()) cached.delete();
                if (!temp.renameTo(cached)) throw new IllegalStateException("Could not finalize Drive playback cache");
            }
            String relativeUrl = "https://appassets.androidplatform.net/assets/diagnosis/" + plotId + "/"
                    + String.format(Locale.US, "cctv_%02d", camera) + "/" + cacheName;
            JSONObject row = new JSONObject();
            row.put("camera", camera);
            row.put("plotId", plotId);
            row.put("kind", video ? "video" : "photo");
            row.put("url", relativeUrl);
            row.put("fileName", item.displayName);
            row.put("driveSaved", true);
            row.put("drivePath", "Diagnosis/" + plotFolder + "/" + quadrantName + "/" + item.displayName);
            row.put("driveDocumentId", item.documentId);
            long date = item.modified > 0 ? item.modified : cached.lastModified();
            row.put("capturedAt", new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US).format(new Date(date)) + " MYT");
            if (video) {
                File poster = createDiagnosisVideoPoster(cached, camera, plotId);
                if (poster != null) {
                    keepNames.add(poster.getName());
                    row.put("thumbnailUrl", relativeUrl.replace(cacheName, poster.getName()));
                }
            }
            result.put(row);
        }
        File[] cachedFiles = directory.listFiles();
        if (cachedFiles != null) for (File cachedFile : cachedFiles) {
            if (!keepNames.contains(cachedFile.getName())) cachedFile.delete();
        }
        return result;
    }

    private String findExistingChildFolder(Uri treeUri, String parentDocumentId, String folderName) throws Exception {
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId);
        String[] columns = {DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE};
        try (Cursor cursor = getContentResolver().query(childrenUri, columns, null, null, null)) {
            if (cursor == null) throw new IllegalStateException("Google Drive could not list folders");
            int idColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            int nameColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            int mimeColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);
            while (cursor.moveToNext()) {
                if (idColumn >= 0 && nameColumn >= 0 && mimeColumn >= 0
                        && folderName.equals(cursor.getString(nameColumn))
                        && DocumentsContract.Document.MIME_TYPE_DIR.equals(cursor.getString(mimeColumn))) {
                    return cursor.getString(idColumn);
                }
            }
        }
        return null;
    }

    private File createDiagnosisVideoPoster(File video, int camera, String plotId) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Bitmap frame = null;
        try {
            retriever.setDataSource(video.getAbsolutePath());
            frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (frame == null) return null;
            File poster = new File(video.getParentFile(), video.getName().replaceFirst("\\.mp4$", "_thumb.jpg"));
            try (FileOutputStream output = new FileOutputStream(poster)) {
                if (!frame.compress(Bitmap.CompressFormat.JPEG, 86, output)) return null;
            }
            return poster;
        } catch (Exception ignored) { return null; }
        finally {
            try { retriever.release(); } catch (Exception ignored) { }
            if (frame != null && !frame.isRecycled()) frame.recycle();
        }
    }

    private void notifyDiagnosisStatus(String message) {
        if (web != null) web.evaluateJavascript("window.onDiagnosisStatus&&window.onDiagnosisStatus(" + JSONObject.quote(message) + ")", null);
    }

    private void launchDiagnosisCapture(String kind, int camera, String plotId, String plotName) {
        if (camera < 1 || camera > 6) return;
        if ("photo".equals(kind) && diagnosisPhotoCount(camera, plotId) >= 5) {
            notifyDiagnosisStatus("This quadrant already has 5 photos. Remove a photo before adding another.");
            return;
        }
        pendingDiagnosisKind = kind;
        pendingDiagnosisCamera = camera;
        pendingDiagnosisPlotId = safePlotId(plotId);
        pendingDiagnosisPlotName = safeFolderName(plotName);
        int requestCode = "video".equals(kind) ? REQUEST_DIAGNOSIS_VIDEO : REQUEST_DIAGNOSIS_PHOTO;
        try {
            Intent intent = new Intent("video".equals(kind) ? MediaStore.ACTION_VIDEO_CAPTURE : MediaStore.ACTION_IMAGE_CAPTURE);
            if ("video".equals(kind)) {
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            }
            // Let the camera app manage its own output. Supplying a pending MediaStore URI
            // caused some camera apps to stall while writing/finalizing the capture.
            pendingDiagnosisOutput = null;
            if (intent.resolveActivity(getPackageManager()) == null) {
                if ("video".equals(kind)) {
                    notifyDiagnosisStatus("No video camera app was found. Choose a recorded video from the picker instead.");
                    launchDiagnosisVideoPicker(camera, plotId, plotName);
                } else notifyDiagnosisStatus("No compatible camera app was found. Choose an existing photo instead.");
                return;
            }
            Intent chooser = Intent.createChooser(intent, "Record plant " + kind);
            startActivityForResult(chooser, requestCode);
        } catch (Exception error) {
            if (pendingDiagnosisOutput != null) try { getContentResolver().delete(pendingDiagnosisOutput, null, null); } catch (Exception ignored) { }
            pendingDiagnosisOutput = null;
            if ("video".equals(kind)) launchDiagnosisVideoPicker(camera, plotId, plotName);
            else notifyDiagnosisStatus("No compatible camera app was found. Choose an existing photo instead.");
        }
    }

    private void launchDiagnosisPicker(int camera, String plotId, String plotName) {
        pendingDiagnosisCamera = camera;
        pendingDiagnosisPlotId = safePlotId(plotId);
        pendingDiagnosisPlotName = safeFolderName(plotName);
        pendingDiagnosisOutput = null;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        notifyDiagnosisStatus("Choose a photo or video. GrowPlot saves it on this device first, then uploads it to Google Drive.");
        try { startActivityForResult(intent, REQUEST_DIAGNOSIS_PICKER); }
        catch (Exception error) { notifyDiagnosisStatus("No media picker is available on this device. " + (error.getMessage() == null ? "" : error.getMessage())); }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_DEVICE_LOCATION) return;
        String plotId = safePlotId(locationPermissionPlotId);
        locationPermissionRequestInFlight = false;
        if (hasLocationPermission()) requestDeviceLocationForPlot(plotId);
        else sendDeviceLocation(plotId, null, "Location permission was not granted.");
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (web != null) web.requestLayout();
    }

    private final class SnapshotBridge {
        @JavascriptInterface public void loginAdminOtpDemo(String password) {
            runOnUiThread(() -> AdminOtpDemo.login(MainActivity.this, password, () -> web.evaluateJavascript("window.GrowPlotAdminVerified&&window.GrowPlotAdminVerified()", null)));
        }
        @JavascriptInterface public void requestAdminActivationDemo() {
            runOnUiThread(() -> AdminActivationDemo.request(MainActivity.this));
        }
        @JavascriptInterface public String getCctvCameraIpAddresses() {
            JSONArray addresses = new JSONArray();
            for (int camera = 1; camera <= 6; camera++) addresses.put(cctvIp(camera));
            return addresses.toString();
        }

        @JavascriptInterface public boolean saveCctvCameraIpAddresses(String json) {
            try {
                JSONArray addresses = new JSONArray(json);
                if (addresses.length() != 6) return false;
                String[] values = new String[6];
                for (int i = 0; i < 6; i++) {
                    values[i] = addresses.optString(i, "").trim();
                    if (!isPrivateIpv4OrBlank(values[i])) return false;
                }
                if (values[0].isEmpty() || values[1].isEmpty()) return false;
                SharedPreferences.Editor editor = getPrefs().edit();
                for (int i = 0; i < 6; i++) {
                    editor.putString(PREF_CCTV_IP_PREFIX + String.format(Locale.US, "%02d", i + 1), values[i]);
                }
                editor.apply();
                runOnUiThread(() -> {
                    reconnectIpWebcamVlc();
                    reconnectCctv02Vlc();
                    reconnectCctv03Vlc();
                });
                return true;
            } catch (Exception ignored) { return false; }
        }

        @JavascriptInterface public String getPluginApiKeyStatus() {
            try { return new JSONObject().put("plantnet", !pluginApiKey("plantnet").isEmpty() || !BuildConfig.PLANTNET_API_KEY.isEmpty())
                    .put("plantid", !pluginApiKey("plantid").isEmpty())
                    .put("kindwise", !pluginApiKey("kindwise").isEmpty()).toString(); }
            catch (Exception error) { return "{}"; }
        }
        @JavascriptInterface public boolean savePluginApiKeys(String payload) {
            return MainActivity.this.savePluginApiKeys(payload);
        }
        @JavascriptInterface public void openPluginKeyPortal(String provider) {
            final String url = "plantnet".equals(provider) ? "https://my.plantnet.org/settings/api-key"
                    : "plantid".equals(provider) ? "https://admin.kindwise.com/login"
                    : "kindwise".equals(provider) ? "https://admin.kindwise.com/login" : "";
            if (!url.isEmpty()) runOnUiThread(() -> { try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception ignored) { } });
        }

        @JavascriptInterface public String getVigiCctv03Credentials() {
            try {
                String[] credentials = vigiCctv03Credentials();
                return new JSONObject().put("username", credentials[0]).put("passwordConfigured", !credentials[1].isEmpty()).toString();
            } catch (Exception ignored) { return "{}"; }
        }

        @JavascriptInterface public boolean saveVigiCctv03Credentials(String username, String password) {
            if (!MainActivity.this.saveVigiCctv03Credentials(username == null ? "" : username.trim(), password == null ? "" : password)) return false;
            runOnUiThread(MainActivity.this::reconnectCctv03Vlc);
            return true;
        }

        @JavascriptInterface public void migrateLegacyWebStorage(String payload) {
            if (getPrefs().getBoolean(PREF_WEB_STORAGE_MIGRATED, false)) return;
            getPrefs().edit().putString("legacy_web_storage_payload", payload == null ? "{}" : payload)
                    .putBoolean(PREF_WEB_STORAGE_MIGRATED, true).apply();
            runOnUiThread(MainActivity.this::loadMainPage);
        }

        @JavascriptInterface public String getLegacyWebStorage() {
            return getPrefs().getString("legacy_web_storage_payload", "{}");
        }

        @JavascriptInterface public String getFarmDataUserId() {
            return farmDataUserId("default-user");
        }

        @JavascriptInterface public void loadFarmPlantings() {
            farmDataWorker.execute(() -> {
                try { notifyFarmPlantingsLoaded(loadFarmPlantingsFromDrive(farmDataUserId("default-user"))); }
                catch (Exception error) {
                    JSONObject response = new JSONObject();
                    try { response.put("ok", false); response.put("userId", farmDataUserId("default-user"));
                        response.put("records", new JSONArray()); response.put("deletedIds", new JSONArray());
                        response.put("irrigationSchedules", new JSONArray()); response.put("maintenanceTasks", new JSONArray());
                        response.put("managementDeletedIds", new JSONArray());
                        response.put("message", error.getMessage() == null ? "Could not read the farm database from Google Drive" : error.getMessage()); }
                    catch (Exception ignored) { }
                    notifyFarmPlantingsLoaded(response);
                }
            });
        }

        @JavascriptInterface public void saveFarmPlantings(String payload) {
            farmDataWorker.execute(() -> {
                try { notifyFarmPlantingsSaved(saveFarmPlantingsToDrive(payload)); }
                catch (Exception error) {
                    JSONObject response = new JSONObject();
                    try { response.put("ok", false); response.put("message", error.getMessage() == null
                            ? "Google Drive could not save these farm records" : error.getMessage()); }
                    catch (Exception ignored) { }
                    notifyFarmPlantingsSaved(response);
                }
            });
        }

        @JavascriptInterface public void chooseImageFolder(String plotId) {
            pickerPlotId = safePlotId(plotId);
            pickerCreatesDefaultFeed = false;
            getPrefs().edit().putString(PREF_ACTIVE_PLOT_ID, pickerPlotId).apply();
            runOnUiThread(MainActivity.this::openImageFeedFolderPicker);
        }

        @JavascriptInterface public void browseDefaultFeedFolder(String plotId, String folderName) {
            pickerPlotId = safePlotId(plotId);
            pickerDefaultFolderName = safeFolderName(folderName);
            pickerCreatesDefaultFeed = true;
            getPrefs().edit().putString(PREF_ACTIVE_PLOT_ID, pickerPlotId).apply();
            runOnUiThread(() -> {
                publishStatus(makeStatus("Choose the Google Drive folder for this plot."));
                openImageFeedFolderPicker(true);
            });
        }

        @JavascriptInterface public void createDefaultDataFeed(String plotId, String folderName) {
            pickerPlotId = safePlotId(plotId);
            pickerDefaultFolderName = safeFolderName(folderName);
            pickerCreatesDefaultFeed = true;
            getPrefs().edit().putString(PREF_ACTIVE_PLOT_ID, pickerPlotId).apply();
            runOnUiThread(() -> authorizeAndConnectDriveFeed(pickerPlotId, pickerDefaultFolderName));
        }

        @JavascriptInterface public String getFeedLocation(String plotId) {
            String id = safePlotId(plotId);
            String savedDriveFolder = getPrefs().getString(driveApiFolderKey(id), null);
            if (savedDriveFolder != null && driveApiFolderForSignedInUser(id) == null)
                return expectedFeedPathLabel(id);
            return feedLocationLabel(id);
        }

        @JavascriptInterface public boolean hasDriveApiFeedFolder(String plotId) {
            String folderId = driveApiFolderForSignedInUser(safePlotId(plotId));
            return folderId != null && !folderId.trim().isEmpty();
        }

        @JavascriptInterface public String getExpectedFeedPath(String plotId, String folderName) {
            return expectedFeedPathLabel(plotId, folderName);
        }

        @JavascriptInterface public void openIpWebcam() {
            runOnUiThread(MainActivity.this::installCctv01VlcPositionListener);
        }

        @JavascriptInterface public void setCctvVlcFeedsAllowedOnActiveScreen(boolean allowed) {
            runOnUiThread(() -> MainActivity.this.setCctvVlcFeedsAllowedOnActiveScreen(allowed));
        }

        @JavascriptInterface public void updateCctv01VlcPosition(String coordinates) {
            runOnUiThread(() -> MainActivity.this.positionCctv01Vlc(coordinates));
        }
        @JavascriptInterface public void updateCctv02VlcPosition(String coordinates) {
            runOnUiThread(() -> MainActivity.this.positionCctv02Vlc(coordinates));
        }

        @JavascriptInterface public void updateCctv03VlcPosition(String coordinates) {
            runOnUiThread(() -> MainActivity.this.positionCctv03Vlc(coordinates));
        }

        @JavascriptInterface public void openGoogleMaps(String latitude, String longitude) {
            try {
                double lat = Double.parseDouble(latitude);
                double lon = Double.parseDouble(longitude);
                if (lat < -90 || lat > 90 || lon < -180 || lon > 180) return;
                Uri maps = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "%2C" + lon);
                openGoogleMapsUri(maps);
            } catch (Exception ignored) { }
        }

        @JavascriptInterface public void openGoogleMapsAddress(String addressText) {
            String query = addressText == null || addressText.trim().isEmpty() ? "Malaysia" : addressText.trim();
            Uri maps = new Uri.Builder().scheme("https").authority("www.google.com")
                    .path("/maps/search/").appendQueryParameter("api", "1")
                    .appendQueryParameter("query", query).build();
            openGoogleMapsUri(maps);
        }

        private void openGoogleMapsUri(Uri maps) {
            runOnUiThread(() -> {
                Intent googleMaps = new Intent(Intent.ACTION_VIEW, maps);
                googleMaps.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(googleMaps);
                } catch (Exception mapsUnavailable) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, maps)); } catch (Exception ignored) { }
                }
            });
        }

        @JavascriptInterface public void requestDeviceLocation(String plotId) {
            requestDeviceLocationPermission(safePlotId(plotId));
        }

        @JavascriptInterface public void geocodePlotAddress(String addressText, String plotId) {
            final String id = safePlotId(plotId);
            String query = addressText == null ? "" : addressText.trim();
            if (query.isEmpty()) return;
            if (!query.toLowerCase(Locale.ROOT).contains("malaysia")) query += ", Malaysia";
            final String searchText = query;
            geocodeWorker.execute(() -> {
                try {
                    if (!Geocoder.isPresent()) throw new IllegalStateException("Address lookup is unavailable.");
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    if (Build.VERSION.SDK_INT >= 33) {
                        geocoder.getFromLocationName(searchText, 1, addresses -> {
                            Address found = addresses == null || addresses.isEmpty() ? null : addresses.get(0);
                            sendAddressGeocode(id, searchText, found);
                        });
                    } else {
                        List<Address> addresses = geocoder.getFromLocationName(searchText, 1);
                        sendAddressGeocode(id, searchText, addresses == null || addresses.isEmpty() ? null : addresses.get(0));
                    }
                } catch (Exception error) {
                    sendAddressGeocode(id, searchText, null);
                }
            });
        }

        @JavascriptInterface public void ensurePlotFeed(String plotId) {
            String id = safePlotId(plotId);
            getPrefs().edit().putString("data_feed_config_" + id,
                    getPrefs().getString("data_feed_config_" + id, "{\"plotId\":\"" + id + "\",\"enabled\":false}")).apply();
        }

        @JavascriptInterface public void returnToDemoStreams() {
            runOnUiThread(() -> MainActivity.this.returnToDemoStreams());
        }
        @JavascriptInterface public void setActiveStreamingContext(String plotId, String greenhouseId) {
            runOnUiThread(() -> MainActivity.this.loadAssignedStreams(safePlotId(plotId), greenhouseId, false));
        }
        @JavascriptInterface public void setActivePlot(String plotId) {
            String id = safePlotId(plotId);
            getPrefs().edit().putString(PREF_ACTIVE_PLOT_ID, id).apply();
            // Home selector supplies the matching greenhouse; do not overwrite it with greenhouse-1.
            scanSnapshotFolder();
        }

        @JavascriptInterface public void checkNow(String plotId) {
            scanSnapshotFolder(safePlotId(plotId));
        }

        @JavascriptInterface public void captureFeedPhoto(int camera, String plotId) {
            if (camera < 1 || camera > 6) return;
            runOnUiThread(() -> launchFeedPhotoCapture(camera, safePlotId(plotId)));
        }

        @JavascriptInterface public void chooseFeedPhoto(int camera, String plotId) {
            if (camera < 1 || camera > 6) return;
            runOnUiThread(() -> launchFeedPhotoPicker(camera, safePlotId(plotId)));
        }

        @JavascriptInterface public int getRefreshInterval() {
            return refreshIntervalMinutes;
        }

        @JavascriptInterface public void setRefreshInterval(int minutes) {
            if (minutes != 1 && minutes != 5 && minutes != 15 && minutes != 30) minutes = 5;
            final int interval = minutes;
            runOnUiThread(() -> {
                refreshIntervalMinutes = interval;
                getPrefs().edit().putInt(PREF_INTERVAL, interval).apply();
                mainHandler.removeCallbacks(scheduledScan);
                scanSnapshotFolder();
                mainHandler.postDelayed(scheduledScan, refreshIntervalMinutes * 60_000L);
            });
        }

        @JavascriptInterface public double getCctvSnapshotIntervalMinutes() {
            return cctvSnapshotIntervalMillis / 60_000.0;
        }

        @JavascriptInterface public void setCctvSnapshotIntervalMinutes(double minutes) {
            if (Double.isNaN(minutes) || Double.isInfinite(minutes)) return;
            final long intervalMillis = Math.round(Math.max(0.5, Math.min(1440.0, minutes)) * 60_000.0);
            runOnUiThread(() -> {
                cctvSnapshotIntervalMillis = intervalMillis;
                getPrefs().edit().putLong(PREF_CCTV_SNAPSHOT_INTERVAL_MILLIS, intervalMillis).apply();
                mainHandler.removeCallbacks(cctv01SnapshotTicker);
                mainHandler.removeCallbacks(cctv02SnapshotTicker);
                mainHandler.removeCallbacks(cctv03SnapshotTicker);
                if (activityResumed && ipWebcamVlcPlayer != null && ipWebcamVlcLayout != null) {
                    mainHandler.postDelayed(cctv01SnapshotTicker, cctvSnapshotIntervalMillis);
                }
                if (activityResumed && ipWebcam2VlcPlayer != null && ipWebcam2VlcLayout != null) {
                    mainHandler.postDelayed(cctv02SnapshotTicker, nextSnapshotCheckDelay(2));
                }
                if (activityResumed && ipWebcam3VlcPlayer != null && ipWebcam3VlcLayout != null) {
                    mainHandler.postDelayed(cctv03SnapshotTicker, cctvSnapshotIntervalMillis);
                }
            });
        }

        @JavascriptInterface public String getTimelineInterval() {
            return timelineInterval;
        }

        @JavascriptInterface public void setTimelineInterval(String interval) {
            if (!"daily".equals(interval) && !"weekly".equals(interval) && !"monthly".equals(interval)
                    && !"yearly".equals(interval)) return;
            runOnUiThread(() -> {
                timelineInterval = interval;
                getPrefs().edit().putString(PREF_TIMELINE_INTERVAL, interval).apply();
                scanSnapshotFolder();
            });
        }

        @JavascriptInterface public void captureDiagnosisPhoto(int camera, String plotId, String plotName) {
            runOnUiThread(() -> launchDiagnosisCapture("photo", camera, plotId, plotName));
        }

        @JavascriptInterface public void recordDiagnosisVideo(int camera, String plotId, String plotName) {
            runOnUiThread(() -> launchDiagnosisCapture("video", camera, plotId, plotName));
        }

        @JavascriptInterface public void chooseDiagnosisMedia(int camera, String plotId, String plotName) {
            runOnUiThread(() -> launchDiagnosisPicker(camera, plotId, plotName));
        }

        @JavascriptInterface public void diagnosePhotosWithPlantNet(int camera, String plotId, String species, String symptom, String pixelMetrics) {
            if (!hasVertexAccessToken() && !vertexAuthorizationFailedOnce) {
                pendingVertexCamera = camera;
                pendingVertexPlotId = safePlotId(plotId);
                pendingVertexSpecies = species == null ? "" : species;
                pendingVertexSymptom = symptom == null ? "" : symptom;
                pendingVertexPixelMetrics = pixelMetrics == null ? "" : pixelMetrics;
                pendingVertexBridge = this;
                if (!vertexAuthorizationInProgress) {
                    vertexAuthorizationInProgress = true;
                    runOnUiThread(MainActivity.this::authorizeVertexForPendingDiagnosis);
                }
                return;
            }
            vertexAuthorizationFailedOnce = false;
            final String id = safePlotId(plotId);
            diagnosisWorker.execute(() -> {
                JSONObject response;
                try {
                    File feedImage = new File(new File(new File(getFilesDir(), "latest_images"), id), cameraFileName(camera));
                    List<PlantNetImageCandidate> candidates = new ArrayList<>();
                    try { candidates.addAll(loadDiagnosisPhotosFromDrive(camera, id)); }
                    catch (Exception driveError) { if (!feedImage.isFile()) throw driveError; }
                    boolean feedImageIncluded = feedImage.isFile() && feedImage.length() > 0;
                    if (feedImageIncluded) {
                        try {
                            byte[] jpeg = readPlantNetImage(Uri.fromFile(feedImage));
                            if (jpeg != null && jpeg.length > 0) candidates.add(new PlantNetImageCandidate(jpeg,
                                    "Current Home Image_Feed snapshot: CCTV" + String.format(Locale.US, "%02d", camera),
                                    scorePlantNetImage(jpeg)));
                        } catch (Exception feedError) { if (candidates.isEmpty()) throw feedError; }
                    }
                    if (candidates.isEmpty()) throw new IllegalStateException("No readable photos are available for this quadrant");
                    List<PlantNetImageCandidate> selected = selectBestPlantNetImages(candidates);
                    List<byte[]> selectedImages = candidateBytes(selected);
                    diagnosisCvWorker.execute(() -> sendVertexResult(camera, id,
                            requestVertexImageAnalysis(selectedImages, species, symptom, pixelMetrics)));
                    requestKindwiseDiagnosis(camera, id, selectedImages, candidates.size(), selected,
                            species, symptom, pixelMetrics, "quadrant_photos",
                            "Diagnosis/" + diagnosisPlotFolder(id) + "/Quadrant_" + String.format(Locale.US, "%02d", camera),
                            "Selected the clearest available quadrant photos using approximate sharpness, exposure, and visible-plant-area checks.");
                    // Request species identification first so a slower disease check cannot delay the
                    // crop identity the user needs before interpreting disease candidates.
                    JSONObject speciesIdentification = identifyPlantSpeciesWithPlantNet(selectedImages);
                    response = identifyWithPlantNet(selectedImages);
                    // Keep species and disease outcomes separate so either provider can fail independently.
                    response.put("speciesIdentification", speciesIdentification);
                    response.put("speciesContext", species == null ? "" : species);
                    response.put("symptomContext", symptom == null ? "" : symptom);
                    response.put("pixelContext", pixelMetrics == null ? "" : pixelMetrics);
                    response.put("mediaType", "quadrant_photos");
                    response.put("sourceFolder", feedImageIncluded
                            ? "Current Image_Feed snapshot plus saved Diagnosis photos when available"
                            : "Diagnosis/" + diagnosisPlotFolder(id) + "/Quadrant_" + String.format(Locale.US, "%02d", camera));
                    response.put("candidateImageCount", candidates.size());
                    response.put("feedImageIncluded", feedImageIncluded);
                    response.put("selectionNote", "Selected the clearest available quadrant photos using approximate sharpness, exposure, and visible-plant-area checks; the current Home Image_Feed snapshot is included when available.");
                    response.put("selectedSources", candidateSources(selected));
                } catch (Exception error) {
                    response = plantNetError(error);
                    sendVertexResult(camera, id, vertexStatus("error", error.getMessage() == null ? "No readable photos were available for Vertex image analysis." : error.getMessage()));
                }
                sendPlantNetResult(camera, id, response);
            });
        }

        @JavascriptInterface public void identifyPlantSpeciesFromPhotos(int camera, String plotId) {
            final String id = safePlotId(plotId);
            diagnosisWorker.execute(() -> {
                JSONObject response;
                try {
                    List<PlantNetImageCandidate> candidates;
                    try { candidates = loadDiagnosisPhotosFromDrive(camera, id); }
                    catch (Exception driveError) {
                        candidates = loadDiagnosisPhotosFromDeviceIndex(camera, id);
                        if (candidates.isEmpty()) throw driveError;
                    }
                    List<PlantNetImageCandidate> selected = selectBestPlantNetImages(candidates);
                    List<byte[]> images = candidateBytes(selected);
                    JSONArray providerResults = new JSONArray();
                    providerResults.put(identifyPlantSpeciesWithPlantNet(images));
                    providerResults.put(identifyPlantSpeciesWithPlantId(images));
                    response = new JSONObject();
                    boolean anyOk = false, allNotConfigured = providerResults.length() > 0;
                    for (int i = 0; i < providerResults.length(); i++) {
                        String status = providerResults.getJSONObject(i).optString("status");
                        anyOk |= "ok".equals(status);
                        allNotConfigured &= "not_configured".equals(status);
                    }
                    response.put("status", anyOk ? "ok" : "error");
                    if (!anyOk && allNotConfigured) response.put("status", "not_configured");
                    response.put("providers", providerResults);
                    response.put("candidateImageCount", candidates.size());
                    response.put("imageCountSubmitted", images.size());
                    response.put("selectedSources", candidateSources(selected));
                    response.put("selectionNote", "Both services received the same clearest saved photos. Their scores are separate model rankings, not proof of identity.");
                } catch (Exception error) {
                    response = plantNetError(error);
                    try { response.put("provider", "Pl@ntNet species identification"); }
                    catch (Exception ignored) { }
                }
                sendPlantSpeciesResult(camera, id, response);
            });
        }

        @JavascriptInterface public void chooseDiagnosisDriveFolder() {
            runOnUiThread(() -> requestDiagnosisFolderAuthorization());
        }

        @JavascriptInterface public String getDiagnosisStorageLabel() {
            return diagnosisStorageLabel();
        }


        @JavascriptInterface public String getDiagnosisApiConfig() {
            JSONObject config = new JSONObject();
            try {
                config.put("url", BuildConfig.DIAGNOSIS_API_URL == null ? "" : BuildConfig.DIAGNOSIS_API_URL);
                config.put("proxyToken", BuildConfig.DIAGNOSIS_PROXY_TOKEN == null ? "" : BuildConfig.DIAGNOSIS_PROXY_TOKEN);
                config.put("vertexConfigured", true);
                config.put("vertexDirect", true);
                config.put("project", BuildConfig.VERTEX_PROJECT_ID);
                config.put("location", BuildConfig.VERTEX_LOCATION);
                config.put("model", BuildConfig.VERTEX_MODEL_ID);
            } catch (Exception ignored) { }
            return config.toString();
        }

        @JavascriptInterface public String getDiagnosisMedia(int camera, String plotId) {
            if (camera < 1 || camera > 6) return "[]";
            return readDiagnosisMediaIndex(camera, plotId);
        }

        @JavascriptInterface public void refreshDiagnosisMedia(int camera, String plotId) {
            refreshDiagnosisMediaFromDrive(camera, plotId);
        }

        @JavascriptInterface public void playDiagnosisVideo(int camera, String plotId, String mediaUrl) {
            runOnUiThread(() -> playDiagnosisVideoInApp(camera, plotId, mediaUrl));
        }

        @JavascriptInterface public void removeDiagnosisMedia(int camera, String plotId, String documentId,
                                                               String fileName, String mediaUrl) {
            final String id = safePlotId(plotId);
            if (camera < 1 || camera > 6) return;
            diagnosisWorker.execute(() -> removeDiagnosisMediaFromStorage(camera, id,
                    documentId == null ? "" : documentId, fileName == null ? "" : fileName,
                    mediaUrl == null ? "" : mediaUrl));
        }

        @JavascriptInterface public void openDiagnosisDriveMedia(String documentId, String mimeType) {
            runOnUiThread(() -> openDiagnosisDriveMediaInPlayer(documentId, mimeType));
        }
    }


    private void playDiagnosisVideoInApp(int camera, String plotId, String mediaUrl) {
        String id = safePlotId(plotId);
        if (camera < 1 || camera > 6 || mediaUrl == null) {
            notifyDiagnosisStatus("The video reference is invalid. Refresh the diagnosis list.");
            return;
        }
        String quadrant = String.format(Locale.US, "cctv_%02d", camera);
        String expectedPrefix = "https://appassets.androidplatform.net/assets/diagnosis/" + id + "/" + quadrant + "/";
        if (!mediaUrl.startsWith(expectedPrefix)) {
            notifyDiagnosisStatus("The video reference does not match this plot and quadrant.");
            return;
        }
        String fileName = Uri.parse(mediaUrl).getLastPathSegment();
        if (fileName == null || !fileName.matches("[A-Za-z0-9_-]{1,64}\\.mp4")) {
            notifyDiagnosisStatus("The video file name is invalid.");
            return;
        }
        try {
            File local = new File(new File(new File(getFilesDir(), "diagnosis_media"), id), quadrant + "/" + fileName).getCanonicalFile();
            File cached = new File(new File(new File(getCacheDir(), "diagnosis_drive_cache"), id), quadrant + "/" + fileName).getCanonicalFile();
            File media = local.isFile() ? local : (cached.isFile() ? cached : null);
            if (media == null) {
                notifyDiagnosisStatus("The video is not in the device playback cache yet. Reopen this diagnosis and wait for Drive sync.");
                return;
            }
            Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            FrameLayout frame = new FrameLayout(this);
            frame.setBackgroundColor(Color.BLACK);
            VideoView player = new VideoView(this);
            frame.addView(player, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            TextView pixelOverlay = new TextView(this);
            pixelOverlay.setTextColor(Color.WHITE);
            pixelOverlay.setTextSize(10);
            pixelOverlay.setPadding(dp(9), dp(7), dp(48), dp(7));
            pixelOverlay.setBackgroundColor(0xCC172D20);
            pixelOverlay.setText(Html.fromHtml("<font color='#3EAA5D'>● Green —%</font>　<font color='#F1D451'>● Yellow —%</font><br><font color='#A46A45'>● Brown —%</font>　<font color='#C7CFCA'>● Other —%</font>",Html.FROM_HTML_MODE_LEGACY));
            FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
            frame.addView(pixelOverlay, overlayParams);
            TextView closeVideo = new TextView(this);
            closeVideo.setText("×");
            closeVideo.setTextColor(Color.WHITE);
            closeVideo.setTextSize(28);
            closeVideo.setGravity(Gravity.CENTER);
            closeVideo.setBackgroundColor(0xCC172D20);
            closeVideo.setContentDescription("Close video player");
            closeVideo.setOnClickListener(view -> dialog.dismiss());
            FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(dp(42), dp(42), Gravity.TOP | Gravity.RIGHT);
            frame.addView(closeVideo, closeParams);
            dialog.setContentView(frame);
            MediaController controls = new MediaController(this);
            controls.setAnchorView(player);
            player.setMediaController(controls);
            player.setOnPreparedListener(mp -> {
                if (mp.getVideoWidth() <= 0 || mp.getVideoHeight() <= 0) {
                    notifyDiagnosisStatus("Android opened the MP4 but found no decodable video frames. The file may be audio-only or use an unsupported video codec.");
                } else {
                    notifyDiagnosisStatus("Video is playing. Color screen will appear in the diagnosis results when complete; tap × to close at any time.");
                }
                if (mp.getVideoWidth() > 0 && mp.getVideoHeight() > 0) {
                    resizeDiagnosisVideoDialog(dialog, mp.getVideoWidth(), mp.getVideoHeight());
                    startDiagnosisVideoAnalysis(camera, id, media, pixelOverlay, dialog);
                }
                player.start();
            });
            player.setOnInfoListener((mp, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) notifyDiagnosisStatus("Video frames are now playing in GrowPlot.");
                return false;
            });
            player.setOnErrorListener((mp, what, extra) -> {
                dialog.dismiss();
                notifyDiagnosisStatus("Android could not decode this video (player error " + what + ", detail " + extra + ").");
                return true;
            });
            dialog.setOnDismissListener(ignored -> player.stopPlayback());
            dialog.show();
            android.util.DisplayMetrics display = getResources().getDisplayMetrics();
            int initialWidth = (int)(display.widthPixels * .48f);
            int initialHeight = (int)(display.heightPixels * .38f);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setDimAmount(.22f);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.getWindow().setLayout(initialWidth, initialHeight);
            }
            player.setVideoPath(media.getAbsolutePath());
        } catch (Exception error) {
            notifyDiagnosisStatus("Could not open the cached video: " + (error.getMessage() == null ? "unknown player error" : error.getMessage()));
        }
    }


    private void resizeDiagnosisVideoDialog(Dialog dialog, int videoWidth, int videoHeight) {
        if (dialog == null || videoWidth <= 0 || videoHeight <= 0 || dialog.getWindow() == null) return;
        android.util.DisplayMetrics display = getResources().getDisplayMetrics();
        // Match the diagnosis card's media thumbnail bounds: half the screen width
        // and roughly the 180dp preview height, scaled for the current display.
        int maxWidth = (int)(display.widthPixels * .48f);
        int maxHeight = Math.min(dp(180), (int)(display.heightPixels * .38f));
        float aspect = videoWidth / (float) videoHeight;
        int width = Math.min(maxWidth, (int)(maxHeight * aspect));
        int height = Math.min(maxHeight, (int)(width / aspect));
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setLayout(width, height);
    }

    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + .5f); }

    private byte[] readPlantNetImage(Uri imageUri) throws Exception {
        BitmapFactory.Options bounds = new BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
        try (InputStream input = getContentResolver().openInputStream(imageUri)) {
            if (input == null) throw new IllegalStateException("Google Drive could not open a quadrant photo");
            BitmapFactory.decodeStream(input, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw new IllegalStateException("A quadrant photo could not be decoded");
        int sample = 1, longest = Math.max(bounds.outWidth, bounds.outHeight);
        while (longest / sample > 1600) sample *= 2;
        Bitmap decoded;
        BitmapFactory.Options options = new BitmapFactory.Options(); options.inSampleSize = sample;
        try (InputStream input = getContentResolver().openInputStream(imageUri)) {
            if (input == null) throw new IllegalStateException("Google Drive could not reopen a quadrant photo");
            decoded = BitmapFactory.decodeStream(input, null, options);
        }
        if (decoded == null) throw new IllegalStateException("A quadrant photo could not be decoded");
        try { return plantNetJpeg(decoded); }
        finally { if (!decoded.isRecycled()) decoded.recycle(); }
    }

    private List<PlantNetImageCandidate> loadDiagnosisPhotosFromDrive(int camera, String plotId) throws Exception {
        if (camera < 1 || camera > 6) throw new IllegalArgumentException("Invalid quadrant number");
        if (!isDiagnosisDriveReady()) throw new IllegalStateException("Google Drive Diagnosis folder is not connected");
        String treeText = getPrefs().getString(PREF_DIAGNOSIS_TREE_URI, null);
        String rootId = getPrefs().getString(PREF_DIAGNOSIS_ROOT_DOCUMENT_ID, null);
        if (treeText == null || rootId == null) throw new IllegalStateException("Google Drive Diagnosis folder is not connected");
        Uri treeUri = Uri.parse(treeText);
        String plotFolder = diagnosisPlotFolder(plotId);
        String plotIdOnDrive = findExistingChildFolder(treeUri, rootId, plotFolder);
        if (plotIdOnDrive == null) throw new IllegalStateException("No Google Drive folder found for " + plotFolder);
        String quadrantName = "Quadrant_" + String.format(Locale.US, "%02d", camera);
        String quadrantId = findExistingChildFolder(treeUri, plotIdOnDrive, quadrantName);
        if (quadrantId == null) throw new IllegalStateException("No Google Drive folder found for " + plotFolder + "/" + quadrantName);
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, quadrantId);
        String[] columns = {DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_LAST_MODIFIED};
        List<DriveDiagnosisMedia> photos = new ArrayList<>();
        try (Cursor cursor = getContentResolver().query(childrenUri, columns, null, null, null)) {
            if (cursor == null) throw new IllegalStateException("Google Drive could not list " + plotFolder + "/" + quadrantName);
            int idColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            int nameColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            int mimeColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);
            int modifiedColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
            while (cursor.moveToNext()) {
                if (idColumn < 0 || mimeColumn < 0) continue;
                String mime = cursor.getString(mimeColumn);
                if (mime == null || !mime.startsWith("image/")) continue;
                DriveDiagnosisMedia photo = new DriveDiagnosisMedia();
                photo.documentId = cursor.getString(idColumn);
                photo.displayName = nameColumn >= 0 ? cursor.getString(nameColumn) : "Quadrant photo";
                photo.modified = modifiedColumn >= 0 && !cursor.isNull(modifiedColumn) ? cursor.getLong(modifiedColumn) : 0;
                photos.add(photo);
            }
        }
        Collections.sort(photos, (left, right) -> Long.compare(right.modified, left.modified));
        if (photos.isEmpty()) throw new IllegalStateException("No photos found in Google Drive " + plotFolder + "/" + quadrantName + ". Add a photo to that quadrant first.");
        List<PlantNetImageCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < Math.min(20, photos.size()); i++) {
            DriveDiagnosisMedia photo = photos.get(i);
            Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, photo.documentId);
            byte[] jpeg = readPlantNetImage(documentUri);
            if (jpeg != null && jpeg.length > 0) candidates.add(new PlantNetImageCandidate(jpeg, "Drive photo: " + photo.displayName, scorePlantNetImage(jpeg)));
        }
        if (candidates.isEmpty()) throw new IllegalStateException("Photos exist in the quadrant folder, but none could be read by GrowPlot");
        return candidates;
    }

    private List<PlantNetImageCandidate> loadDiagnosisPhotosFromDeviceIndex(int camera, String plotId) throws Exception {
        if (camera < 1 || camera > 6) throw new IllegalArgumentException("Invalid quadrant number");
        String id = safePlotId(plotId);
        String quadrant = String.format(Locale.US, "cctv_%02d", camera);
        JSONArray index = new JSONArray(readDiagnosisMediaIndex(camera, id));
        List<PlantNetImageCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < index.length(); i++) {
            JSONObject item = index.optJSONObject(i);
            if (item == null) continue;
            String name = item.optString("fileName", "");
            if (!name.matches("[A-Za-z0-9_-]{1,100}\\.(jpg|jpeg|png)")) continue;
            File localDir = new File(new File(new File(getFilesDir(), "diagnosis_media"), id), quadrant).getCanonicalFile();
            File cacheDir = new File(new File(new File(getCacheDir(), "diagnosis_drive_cache"), id), quadrant).getCanonicalFile();
            File local = new File(localDir, name).getCanonicalFile();
            File cached = new File(cacheDir, name).getCanonicalFile();
            File photo = local.getParentFile().equals(localDir) && local.isFile() ? local
                    : (cached.getParentFile().equals(cacheDir) && cached.isFile() ? cached : null);
            if (photo == null) continue;
            byte[] jpeg = readPlantNetImage(Uri.fromFile(photo));
            if (jpeg != null && jpeg.length > 0) candidates.add(new PlantNetImageCandidate(jpeg,
                    "Saved device photo: " + name, scorePlantNetImage(jpeg)));
        }
        return candidates;
    }

    private byte[] plantNetJpeg(Bitmap source) {
        Bitmap scaled = source;
        try {
            int max = Math.max(source.getWidth(), source.getHeight());
            if (max > 800) {
                float ratio = 800f / max;
                scaled = Bitmap.createScaledBitmap(source, Math.max(1, Math.round(source.getWidth() * ratio)), Math.max(1, Math.round(source.getHeight() * ratio)), true);
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 82, output);
            return output.toByteArray();
        } catch (Exception ignored) { return null; }
        finally { if (scaled != source && !scaled.isRecycled()) scaled.recycle(); }
    }

    private double scorePlantNetImage(byte[] jpeg) {
        BitmapFactory.Options options = new BitmapFactory.Options(); options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length, options);
        if (bitmap == null) return 0;
        Bitmap small = bitmap;
        try {
            int edge = 96; float scale = Math.min(1f, edge / (float)Math.max(bitmap.getWidth(), bitmap.getHeight()));
            if (scale < 1f) small = Bitmap.createScaledBitmap(bitmap, Math.max(1, Math.round(bitmap.getWidth() * scale)), Math.max(1, Math.round(bitmap.getHeight() * scale)), true);
            int w = small.getWidth(), h = small.getHeight(), count = w * h; int[] px = new int[count]; small.getPixels(px, 0, w, 0, 0, w, h);
            double brightness = 0, clipped = 0, plantPixels = 0, lapMean = 0, lapSq = 0; int edgeCount = 0;
            for (int i = 0; i < count; i++) {
                int r = Color.red(px[i]), g = Color.green(px[i]), b = Color.blue(px[i]);
                int lum = (r * 30 + g * 59 + b * 11) / 100; brightness += lum;
                if (lum < 12 || lum > 245) clipped++;
                if ((g > r * 1.08 && g > b * 1.04 && g > 38) || (r > 75 && g > 65 && r > b * 1.15 && g > b * 1.12)) plantPixels++;
            }
            for (int y = 1; y < h - 1; y++) for (int x = 1; x < w - 1; x++) {
                int i = y * w + x;
                int c = luminance(px[i]), lap = luminance(px[i-1]) + luminance(px[i+1]) + luminance(px[i-w]) + luminance(px[i+w]) - 4 * c;
                lapMean += lap; lapSq += (double)lap * lap; edgeCount++;
            }
            double meanLum = brightness / Math.max(1, count), clippedFrac = clipped / Math.max(1, count);
            double lapVariance = edgeCount == 0 ? 0 : lapSq / edgeCount - Math.pow(lapMean / edgeCount, 2);
            double sharp = Math.min(1d, Math.sqrt(Math.max(0, lapVariance)) / 28d);
            double plant = Math.min(1d, (plantPixels / Math.max(1, count)) / .45d);
            double exposure = Math.max(0d, 1d - Math.abs(meanLum - 128d) / 128d) * (1d - Math.min(.8d, clippedFrac * 2d));
            return sharp * .45d + plant * .30d + exposure * .25d;
        } finally { if (small != bitmap && !small.isRecycled()) small.recycle(); if (!bitmap.isRecycled()) bitmap.recycle(); }
    }

    private int luminance(int color) { return (Color.red(color) * 30 + Color.green(color) * 59 + Color.blue(color) * 11) / 100; }

    private List<PlantNetImageCandidate> selectBestPlantNetImages(List<PlantNetImageCandidate> candidates) {
        List<PlantNetImageCandidate> sorted = new ArrayList<>(candidates);
        Collections.sort(sorted, (a, b) -> Double.compare(b.quality, a.quality));
        return new ArrayList<>(sorted.subList(0, Math.min(5, sorted.size())));
    }

    private List<byte[]> candidateBytes(List<PlantNetImageCandidate> candidates) {
        List<byte[]> images = new ArrayList<>(); for (PlantNetImageCandidate candidate : candidates) images.add(candidate.jpeg); return images;
    }

    private JSONArray candidateSources(List<PlantNetImageCandidate> candidates) {
        JSONArray sources = new JSONArray(); for (PlantNetImageCandidate candidate : candidates) {
            JSONObject item = new JSONObject();
            try { item.put("source", candidate.source); item.put("qualityScore", Math.round(candidate.quality * 100)); sources.put(item); }
            catch (Exception ignored) { }
        }
        return sources;
    }

    private JSONObject identifyWithPlantNet(List<byte[]> images) {
        String key = pluginApiKey("plantnet");
        if (key.isEmpty()) key = BuildConfig.PLANTNET_API_KEY;
        if (key == null || key.trim().isEmpty()) return plantNetStatus("not_configured", "Add the Pl@ntNet API key in Settings → Plugins · API Keys.");
        if (images == null || images.isEmpty()) return plantNetStatus("error", "No readable photos or video frames were available for Pl@ntNet.");
        HttpURLConnection connection = null;
        try {
            String boundary = "GrowPlot" + System.currentTimeMillis();
            URL url = new URL("https://my-api.plantnet.org/v2/diseases/identify?api-key=" + URLEncoder.encode(key, "UTF-8") + "&lang=en&nb-results=5");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST"); connection.setConnectTimeout(15000); connection.setReadTimeout(30000);
            connection.setDoOutput(true); connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try (OutputStream out = connection.getOutputStream()) {
                for (int i = 0; i < Math.min(5, images.size()); i++) {
                    out.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"images\"; filename=\"growplot-" + (i + 1) + ".jpg\"\r\nContent-Type: image/jpeg\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                    out.write(images.get(i)); out.write("\r\n".getBytes(StandardCharsets.UTF_8));
                }
                out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
            StringBuilder body = new StringBuilder();
            if (stream != null) try (InputStream input = stream) { byte[] buffer = new byte[8192]; int count; while ((count = input.read(buffer)) != -1) body.append(new String(buffer, 0, count, StandardCharsets.UTF_8)); }
            if (status < 200 || status >= 300) {
                JSONObject error = plantNetStatus("error", "Pl@ntNet returned HTTP " + status
                        + (body.length() > 0 ? ": " + body.substring(0, Math.min(body.length(), 300)) : ""));
                int remaining = remainingPlantNetQuota(connection.getHeaderField("RateLimit"));
                if (status == 429 && remaining < 0) remaining = 0;
                if (remaining >= 0) error.put("remainingIdentificationRequests", remaining);
                String retryAfter = connection.getHeaderField("Retry-After");
                if (retryAfter != null && !retryAfter.isEmpty()) error.put("quotaResetsAt", retryAfter);
                return error;
            }
            JSONObject result = new JSONObject(body.toString());
            if (!result.has("remainingIdentificationRequests")) {
                int remaining = remainingPlantNetQuota(connection.getHeaderField("RateLimit"));
                if (remaining >= 0) result.put("remainingIdentificationRequests", remaining);
            }
            result.put("provider", "Pl@ntNet"); result.put("status", "ok"); result.put("imageCountSubmitted", Math.min(5, images.size()));
            return result;
        } catch (Exception error) { return plantNetStatus("error", error.getMessage() == null ? "Network request failed." : error.getMessage()); }
        finally { if (connection != null) connection.disconnect(); }
    }

    private JSONObject identifyPlantSpeciesWithPlantNet(List<byte[]> images) {
        String key = pluginApiKey("plantnet");
        if (key.isEmpty()) key = BuildConfig.PLANTNET_API_KEY;
        if (key == null || key.trim().isEmpty()) return plantNetStatus("not_configured", "Add the Pl@ntNet API key in Settings → Plugins · API Keys.");
        if (images == null || images.isEmpty()) return plantNetStatus("error", "No readable photos were available for species identification.");
        HttpURLConnection connection = null;
        try {
            String boundary = "GrowPlotSpecies" + System.currentTimeMillis();
            URL url = new URL("https://my-api.plantnet.org/v2/identify/all?api-key="
                    + URLEncoder.encode(key, "UTF-8") + "&lang=en&nb-results=5&type=kt");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST"); connection.setConnectTimeout(15000); connection.setReadTimeout(30000);
            connection.setDoOutput(true); connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try (OutputStream out = connection.getOutputStream()) {
                for (int i = 0; i < Math.min(5, images.size()); i++) {
                    out.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"images\"; filename=\"growplot-species-" + (i + 1) + ".jpg\"\r\nContent-Type: image/jpeg\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                    out.write(images.get(i)); out.write("\r\n".getBytes(StandardCharsets.UTF_8));
                    out.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"organs\"\r\n\r\nauto\r\n").getBytes(StandardCharsets.UTF_8));
                }
                out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
            StringBuilder body = new StringBuilder();
            if (stream != null) try (InputStream input = stream) { byte[] buffer = new byte[8192]; int count; while ((count = input.read(buffer)) != -1) body.append(new String(buffer, 0, count, StandardCharsets.UTF_8)); }
            if (status < 200 || status >= 300) {
                JSONObject error = plantNetStatus("error", "Pl@ntNet species identification returned HTTP " + status
                        + (body.length() > 0 ? ": " + body.substring(0, Math.min(body.length(), 300)) : ""));
                int remaining = remainingPlantNetQuota(connection.getHeaderField("RateLimit"));
                if (status == 429 && remaining < 0) remaining = 0;
                if (remaining >= 0) error.put("remainingIdentificationRequests", remaining);
                return error;
            }
            JSONObject result = new JSONObject(body.toString());
            if (!result.has("remainingIdentificationRequests")) {
                int remaining = remainingPlantNetQuota(connection.getHeaderField("RateLimit"));
                if (remaining >= 0) result.put("remainingIdentificationRequests", remaining);
            }
            result.put("provider", "Pl@ntNet species identification"); result.put("status", "ok");
            result.put("imageCountSubmitted", Math.min(5, images.size()));
            return result;
        } catch (Exception error) {
            return plantNetStatus("error", error.getMessage() == null ? "Species identification request failed." : error.getMessage());
        } finally { if (connection != null) connection.disconnect(); }
    }

    private JSONObject identifyPlantSpeciesWithPlantId(List<byte[]> images) {
        String key = pluginApiKey("plantid");
        if (key == null || key.trim().isEmpty())
            return plantIdentityStatus("Kindwise plant.id", "not_configured", "Add a separate Kindwise plant.id API key in Settings → Plugins · API Keys.");
        if (images == null || images.isEmpty())
            return plantIdentityStatus("Kindwise plant.id", "error", "No readable photos were available for plant.id identification.");
        HttpURLConnection connection = null;
        try {
            JSONArray encodedImages = new JSONArray();
            for (int i = 0; i < Math.min(5, images.size()); i++)
                encodedImages.put(Base64.encodeToString(images.get(i), Base64.NO_WRAP));
            JSONObject request = new JSONObject().put("images", encodedImages);
            URL url = new URL("https://api.plant.id/v3/identification?details=common_names,url");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST"); connection.setConnectTimeout(15000); connection.setReadTimeout(45000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Api-Key", key);
            connection.setRequestProperty("Content-Type", "application/json");
            try (OutputStream out = connection.getOutputStream()) {
                out.write(request.toString().getBytes(StandardCharsets.UTF_8));
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
            StringBuilder body = new StringBuilder();
            if (stream != null) try (InputStream input = stream) {
                byte[] buffer = new byte[8192]; int count;
                while ((count = input.read(buffer)) != -1) body.append(new String(buffer, 0, count, StandardCharsets.UTF_8));
            }
            if (status < 200 || status >= 300)
                return plantIdentityStatus("Kindwise plant.id", "error", "plant.id returned HTTP " + status
                        + (body.length() > 0 ? ": " + body.substring(0, Math.min(body.length(), 300)) : ""));
            JSONObject result = new JSONObject(body.toString());
            result.put("provider", "Kindwise plant.id");
            result.put("status", "ok");
            result.put("imageCountSubmitted", Math.min(5, images.size()));
            return result;
        } catch (Exception error) {
            return plantIdentityStatus("Kindwise plant.id", "error", error.getMessage() == null ? "plant.id request failed." : error.getMessage());
        } finally { if (connection != null) connection.disconnect(); }
    }

    private JSONObject plantIdentityStatus(String provider, String status, String message) {
        JSONObject result = new JSONObject();
        try {
            result.put("provider", provider);
            result.put("status", status);
            result.put("message", message);
            result.put("results", new JSONArray());
        } catch (Exception ignored) { }
        return result;
    }

    private void requestKindwiseDiagnosis(int camera, String plotId, List<byte[]> images, int candidateCount,
                                          List<PlantNetImageCandidate> selected, String species, String symptom,
                                          String pixelMetrics, String mediaType, String sourceFolder, String selectionNote) {
        final List<byte[]> submitted = new ArrayList<>(images);
        final JSONArray selectedSources = candidateSources(selected);
        kindwiseWorker.execute(() -> {
            JSONObject result = identifyWithKindwise(submitted);
            try {
                result.put("speciesContext", species == null ? "" : species);
                result.put("symptomContext", symptom == null ? "" : symptom);
                result.put("pixelContext", pixelMetrics == null ? "" : pixelMetrics);
                result.put("mediaType", mediaType);
                result.put("sourceFolder", sourceFolder);
                result.put("candidateImageCount", candidateCount);
                result.put("imageCountSubmitted", Math.min(5, submitted.size()));
                result.put("selectionNote", selectionNote);
                result.put("selectedSources", selectedSources);
            } catch (Exception ignored) { }
            sendKindwiseResult(camera, plotId, result);
        });
    }

    private JSONObject identifyWithKindwise(List<byte[]> images) {
        String key = pluginApiKey("kindwise");
        if (key == null || key.trim().isEmpty()) {
            return kindwiseStatus("not_configured", "Add the Kindwise crop.health API key in Settings → Plugins · API Keys.");
        }
        if (images == null || images.isEmpty()) {
            return kindwiseStatus("error", "No readable photos or video frames were available for Kindwise.");
        }
        HttpURLConnection connection = null;
        try {
            JSONArray encodedImages = new JSONArray();
            for (int i = 0; i < Math.min(5, images.size()); i++) {
                encodedImages.put(Base64.encodeToString(images.get(i), Base64.NO_WRAP));
            }
            JSONObject payload = new JSONObject().put("images", encodedImages);
            URL url = new URL("https://crop.kindwise.com/api/v1/identification");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(45000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Api-Key", key);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            try (OutputStream out = connection.getOutputStream()) {
                out.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
            StringBuilder body = new StringBuilder();
            if (stream != null) try (InputStream input = stream) {
                byte[] buffer = new byte[8192]; int count;
                while ((count = input.read(buffer)) != -1) body.append(new String(buffer, 0, count, StandardCharsets.UTF_8));
            }
            if (status < 200 || status >= 300) {
                String message = status == 401 || status == 403 ? "Kindwise rejected the saved API key. Check it in Settings."
                        : status == 429 ? "Kindwise request quota is exhausted or rate-limited."
                        : "Kindwise returned HTTP " + status + ".";
                return kindwiseStatus("error", message);
            }
            JSONObject response = new JSONObject(body.toString());
            JSONObject modelResult = response.optJSONObject("result");
            if (modelResult == null) return kindwiseStatus("error", "Kindwise returned an unreadable diagnosis result.");
            JSONArray candidates = new JSONArray();
            appendKindwiseCandidates(candidates, modelResult.optJSONObject("crop"), "crop");
            appendKindwiseCandidates(candidates, modelResult.optJSONObject("disease"), "health issue");
            JSONObject result = kindwiseStatus("ok", "");
            result.put("model", response.optString("model_version", ""));
            result.put("candidates", candidates);
            result.put("imageCountSubmitted", Math.min(5, images.size()));
            return result;
        } catch (Exception error) {
            return kindwiseStatus("error", error.getMessage() == null ? "Kindwise network request failed." : error.getMessage());
        } finally { if (connection != null) connection.disconnect(); }
    }

    private void appendKindwiseCandidates(JSONArray target, JSONObject group, String type) throws Exception {
        if (group == null) return;
        JSONArray suggestions = group.optJSONArray("suggestions");
        if (suggestions == null) return;
        for (int i = 0; i < suggestions.length() && i < 5; i++) {
            JSONObject suggestion = suggestions.optJSONObject(i);
            if (suggestion == null) continue;
            JSONObject item = new JSONObject();
            item.put("type", type);
            item.put("name", suggestion.optString("name", "Unlabelled result"));
            item.put("confidence", suggestion.optDouble("probability", 0));
            JSONObject details = suggestion.optJSONObject("details");
            if (details != null) item.put("details", details);
            target.put(item);
        }
    }

    private JSONObject kindwiseStatus(String status, String message) {
        JSONObject result = new JSONObject();
        try { result.put("provider", "Kindwise crop.health"); result.put("status", status); result.put("message", message); result.put("candidates", new JSONArray()); }
        catch (Exception ignored) { }
        return result;
    }

    private int remainingPlantNetQuota(String rateLimitHeader) {
        if (rateLimitHeader == null) return -1;
        Matcher matcher = Pattern.compile("(?:^|[;,])\\s*r=(\\d+)").matcher(rateLimitHeader);
        if (!matcher.find()) return -1;
        try { return Integer.parseInt(matcher.group(1)); }
        catch (NumberFormatException ignored) { return -1; }
    }

    private JSONObject plantNetStatus(String status, String message) {
        JSONObject result = new JSONObject();
        try { result.put("provider", "Pl@ntNet"); result.put("status", status); result.put("message", message); result.put("results", new JSONArray()); }
        catch (Exception ignored) { }
        return result;
    }

    private JSONObject plantNetError(Exception error) {
        return plantNetStatus("error", error.getMessage() == null ? "Pl@ntNet request failed." : error.getMessage());
    }

    private JSONObject identifyVideoAndDriveEvidence(int camera, String plotId, List<PlantNetImageCandidate> videoFrames, String species, String symptom, String pixelMetrics) {
        JSONObject response;
        try {
            List<PlantNetImageCandidate> candidates = new ArrayList<>();
            String photoNotice = "";
            try { candidates.addAll(loadDiagnosisPhotosFromDrive(camera, plotId)); }
            catch (Exception photoError) { photoNotice = "Quadrant photos were unavailable: " + (photoError.getMessage() == null ? "Drive could not read them" : photoError.getMessage()); }
            if (videoFrames != null) candidates.addAll(videoFrames);
            if (candidates.isEmpty()) throw new IllegalStateException(photoNotice.isEmpty() ? "No quadrant photos or readable video frames were available" : photoNotice);
            List<PlantNetImageCandidate> selected = selectBestPlantNetImages(candidates);
            List<byte[]> selectedImages = candidateBytes(selected);
            requestKindwiseDiagnosis(camera, plotId, selectedImages, candidates.size(), selected,
                    species, symptom, pixelMetrics, "quadrant_photos_and_video_frames",
                    "Diagnosis/" + diagnosisPlotFolder(plotId) + "/Quadrant_" + String.format(Locale.US, "%02d", camera),
                    "Compared saved quadrant photos and sampled video frames using approximate sharpness, exposure, and visible-plant-area checks; submitted the clearest five.");
            response = identifyWithPlantNet(selectedImages);
            response.put("speciesContext", species == null ? "" : species);
            response.put("symptomContext", symptom == null ? "" : symptom);
            response.put("pixelContext", pixelMetrics == null ? "" : pixelMetrics);
            response.put("mediaType", "quadrant_photos_and_video_frames");
            response.put("sourceFolder", "Diagnosis/" + diagnosisPlotFolder(plotId) + "/Quadrant_" + String.format(Locale.US, "%02d", camera));
            response.put("candidateImageCount", candidates.size());
            response.put("selectionNote", "Compared saved quadrant photos and sampled video frames using approximate sharpness, exposure, and visible-plant-area checks; submitted the clearest five.");
            if (!photoNotice.isEmpty()) response.put("sourceNotice", photoNotice + ". The available video frames were still checked.");
            response.put("selectedSources", candidateSources(selected));
        } catch (Exception error) { response = plantNetError(error); }
        return response;
    }

    private JSONObject requestVertexImageAnalysis(List<byte[]> images, String species, String symptom, String pixelMetrics) {
        if (images == null || images.isEmpty()) return vertexStatus("error", "No readable photos were available for Vertex image analysis.");
        final String token = hasVertexAccessToken() ? vertexAccessToken : "";
        if (token.isEmpty()) return vertexStatus("not_authorized",
                "Google Cloud access was not granted. Tap Run diagnosis and approve the Google Cloud permission prompt.");
        final String project = BuildConfig.VERTEX_PROJECT_ID;
        final String location = BuildConfig.VERTEX_LOCATION;
        final String model = BuildConfig.VERTEX_MODEL_ID;
        final String host = "global".equals(location) ? "https://aiplatform.googleapis.com"
                : "https://" + location + "-aiplatform.googleapis.com";
        final String endpoint = host + "/v1/projects/" + Uri.encode(project)
                + "/locations/" + Uri.encode(location) + "/publishers/google/models/"
                + Uri.encode(model) + ":generateContent";
        HttpURLConnection connection = null;
        try {
            JSONArray parts = new JSONArray();
            for (int i = 0; i < Math.min(5, images.size()); i++) {
                byte[] image = images.get(i);
                if (image == null || image.length == 0) continue;
                JSONObject inlineData = new JSONObject();
                inlineData.put("mimeType", "image/jpeg");
                inlineData.put("data", Base64.encodeToString(image, Base64.NO_WRAP));
                parts.put(new JSONObject().put("inlineData", inlineData));
            }
            String prompt = "Assess the visible plant in these farm-monitoring photos. Return JSON only with keys: "
                    + "summary (string), speciesCandidates (array of objects with commonName, scientificName, confidence 0 to 1), "
                    + "visibleFindings (string array), possibleCauses (string array), nextChecks (string array), limitations (string array). "
                    + "Describe only visible evidence. Do not confirm species, disease, or treatment. "
                    + "GrowPlot's separate pixel screen reports these sampled colour metrics: "
                    + (pixelMetrics == null ? "not available" : pixelMetrics)
                    + ". Treat those values as supplied measurements; do not claim to calculate them yourself. "
                    + "User-supplied species context: " + (species == null || species.trim().isEmpty() ? "none" : species)
                    + ". User-reported symptom: " + (symptom == null || symptom.trim().isEmpty() ? "none" : symptom);
            parts.put(new JSONObject().put("text", prompt));
            JSONObject request = new JSONObject()
                    .put("contents", new JSONArray().put(new JSONObject()
                            .put("role", "user").put("parts", parts)))
                    .put("generationConfig", new JSONObject()
                            .put("responseMimeType", "application/json")
                            .put("temperature", 0.2));
            connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(90000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            try (OutputStream out = connection.getOutputStream()) {
                out.write(request.toString().getBytes(StandardCharsets.UTF_8));
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
            StringBuilder body = new StringBuilder();
            if (stream != null) try (InputStream input = stream) {
                byte[] buffer = new byte[8192]; int count;
                while ((count = input.read(buffer)) != -1) body.append(new String(buffer, 0, count, StandardCharsets.UTF_8));
            }
            if (status < 200 || status >= 300) {
                String detail = "Vertex AI returned HTTP " + status + ".";
                try {
                    JSONObject error = new JSONObject(body.toString()).optJSONObject("error");
                    if (error != null && !error.optString("message", "").isEmpty())
                        detail += " " + error.optString("message");
                } catch (Exception ignored) { }
                return vertexStatus("error", detail);
            }
            JSONObject payload = new JSONObject(body.toString());
            JSONArray candidates = payload.optJSONArray("candidates");
            String responseText = "";
            if (candidates != null && candidates.length() > 0) {
                JSONObject first = candidates.optJSONObject(0);
                JSONObject content = first == null ? null : first.optJSONObject("content");
                JSONArray responseParts = content == null ? null : content.optJSONArray("parts");
                if (responseParts != null) {
                    StringBuilder combined = new StringBuilder();
                    for (int i = 0; i < responseParts.length(); i++) {
                        JSONObject part = responseParts.optJSONObject(i);
                        if (part != null) combined.append(part.optString("text", ""));
                    }
                    responseText = combined.toString().trim();
                }
            }
            if (responseText.isEmpty()) return vertexStatus("error", "Vertex AI returned no readable image analysis.");
            JSONObject analysis;
            try { analysis = new JSONObject(responseText); }
            catch (Exception ignored) { analysis = new JSONObject().put("summary", responseText); }
            JSONObject result = vertexStatus("ok", "");
            result.put("model", model);
            result.put("analysis", analysis);
            result.put("imageCountSubmitted", Math.min(5, images.size()));
            result.put("project", project);
            result.put("location", location);
            return result;
        } catch (Exception error) {
            String detail = error.getMessage() == null ? "connection failed" : error.getMessage();
            return vertexStatus("error", "Direct Vertex AI request failed: " + detail);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void writeVertexUtf8(OutputStream out, String value) throws java.io.IOException {
        out.write(value.getBytes(StandardCharsets.UTF_8));
    }

    private JSONObject vertexStatus(String status, String message) {
        JSONObject result = new JSONObject();
        try { result.put("provider", "Gemini on Vertex AI"); result.put("status", status); if (message != null && !message.isEmpty()) result.put("message", message); }
        catch (Exception ignored) { }
        return result;
    }

    private void sendVertexResult(int camera, String plotId, JSONObject result) {
        runOnUiThread(() -> {
            if (web == null) return;
            web.evaluateJavascript("window.onVertexDiagnosis&&window.onVertexDiagnosis(" + camera + "," + JSONObject.quote(plotId) + "," + JSONObject.quote(result.toString()) + ")", null);
        });
    }

    private void sendPlantNetResult(int camera, String plotId, JSONObject result) {
        runOnUiThread(() -> {
            if (web == null) return;
            web.evaluateJavascript("window.onPlantNetDiagnosis&&window.onPlantNetDiagnosis(" + camera + "," + JSONObject.quote(plotId) + "," + JSONObject.quote(result.toString()) + ")", null);
        });
    }

    private void sendPlantSpeciesResult(int camera, String plotId, JSONObject result) {
        runOnUiThread(() -> {
            if (web == null) return;
            web.evaluateJavascript("window.onPlantSpeciesIdentification&&window.onPlantSpeciesIdentification(" + camera + "," + JSONObject.quote(plotId) + "," + JSONObject.quote(result.toString()) + ")", null);
        });
    }

    private void sendKindwiseResult(int camera, String plotId, JSONObject result) {
        runOnUiThread(() -> {
            if (web == null) return;
            web.evaluateJavascript("window.onKindwiseDiagnosis&&window.onKindwiseDiagnosis(" + camera + "," + JSONObject.quote(plotId) + "," + JSONObject.quote(result.toString()) + ")", null);
        });
    }

    private void startDiagnosisVideoAnalysis(int camera, String plotId, File video, TextView overlay, Dialog dialog) {
        diagnosisCvWorker.execute(() -> {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(video.getAbsolutePath());
                String durationValue = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long durationMs = durationValue == null ? 0 : Long.parseLong(durationValue);
                if (durationMs <= 0) throw new IllegalStateException("Video duration is unavailable");
                int samples = Math.min(10, Math.max(5, (int)(durationMs / 1200)));
                double green=0,yellow=0,brown=0,other=0; int valid=0;
                List<PlantNetImageCandidate> videoCandidates = new ArrayList<>();
                for (int i=0;i<samples;i++) {
                    if (i>0) { try { Thread.sleep(Math.max(300L,durationMs/samples)); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); return; } }
                    Bitmap frame = retriever.getFrameAtTime((long)((durationMs*1000d)*i/samples), MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    if (frame == null) continue;
                    byte[] modelImage = plantNetJpeg(frame);
                    if (modelImage != null && modelImage.length > 0) videoCandidates.add(new PlantNetImageCandidate(modelImage, "Video frame " + (i + 1), scorePlantNetImage(modelImage)));
                    Bitmap small=Bitmap.createScaledBitmap(frame,96,54,true); if(small!=frame)frame.recycle();
                    double[] colors=measureDiagnosisPixels(small); small.recycle();
                    green+=colors[0];yellow+=colors[1];brown+=colors[2];other+=colors[3];valid++;
                    final String chips = pixelIntensityMarkup(colors[0],colors[1],colors[2],colors[3]);
                    final int frameNo=i+1,total=samples;
                    runOnUiThread(() -> overlay.setText(Html.fromHtml("<small>Frame "+frameNo+" of "+total+" · sampled pixel colors</small><br>"+chips,Html.FROM_HTML_MODE_LEGACY)));
                }
                if(valid==0)throw new IllegalStateException("No video frames could be decoded");
                double g=green/valid,y=yellow/valid,b=brown/valid,o=other/valid;
                String followup;
                if(y>=.08&&y>=b)followup="Yellow-like pixels are elevated. Confirm they are on leaves, compare young and old growth, and check root-zone moisture, drainage, and soil pH/EC.";
                else if(b>=.08)followup="Brown-like pixels are elevated. Check whether margins are fully dead or there are distinct spreading spots; inspect for heat, pests, and water stress.";
                else if(g<.035)followup="Little green-like plant area was detected. Retake a closer, brighter video with the affected leaf filling the frame.";
                else followup="No strong yellow or brown cue appeared in the sampled frames. This does not rule out disease, pests, or root problems; inspect leaf undersides, new growth, and roots.";
                String report="Video color screen complete. The saved result and care suggestions are shown below.";
                JSONObject result = new JSONObject();
                result.put("summary", "Video screening completed for CCTV " + String.format(Locale.US, "%02d", camera) + ".");
                result.put("metrics", "Across " + valid + " sampled video frames: green-like " + pct(g) + "%, yellow-like " + pct(y) + "%, brown-like " + pct(b) + "%, and other pixels " + pct(o) + "%.");
                JSONArray findings = new JSONArray();
                findings.put(followup);
                findings.put("Remove only leaves that are fully dead or badly damaged, using clean shears. Keep leaves that still have healthy green tissue.");
                findings.put("Check soil moisture about 5–8 cm below the surface and observe drainage after watering. Aim for evenly moist soil, not waterlogged or bone dry; check whether the root zone dries too quickly or stays saturated.");
                findings.put("Do not add NPK from pixel colors alone. Check soil pH/EC or get a soil test and compare symptoms on young versus old leaves first. If a deficiency is supported, use a crop-appropriate NPK at the label rate and avoid over-fertilising.");
                result.put("findings", findings);
                result.put("limitations", "This is a basic sampled pixel-color screen, not a trained disease model. Color can be affected by lighting, background, age, and camera settings; it cannot confirm a disease or identify a pathogen.");
                result.put("capturedAt", new SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()).format(new Date()));
                result.put("imageCount", valid); result.put("mediaType", "video");
                String resultJson = result.toString();
                final List<PlantNetImageCandidate> analyzedVideoFrames = videoCandidates;
                runOnUiThread(()->{
                    notifyDiagnosisStatus(report);
                    String script = "window.onDiagnosisVideoAnalysis&&window.onDiagnosisVideoAnalysis(" + camera + "," + JSONObject.quote(plotId) + "," + JSONObject.quote(resultJson) + ")";
                    if (web != null) web.evaluateJavascript(script, ignored -> { if (dialog.isShowing()) dialog.dismiss(); });
                    else if (dialog.isShowing()) dialog.dismiss();
                });
                runOnUiThread(() -> notifyDiagnosisStatus("Video pixel analysis is complete. Comparing saved photos from this Drive quadrant with sampled video frames and sending the clearest images to Pl@ntNet…"));
                diagnosisWorker.execute(() -> {
                    String species = "", symptom = "";
                    JSONObject plantNet = identifyVideoAndDriveEvidence(camera, plotId, analyzedVideoFrames, species, symptom, result.optString("metrics", ""));
                    sendPlantNetResult(camera, plotId, plantNet);
                });
            } catch(Exception error) {
                final String message="Video color analysis unavailable: "+(error.getMessage()==null?"could not inspect frames":error.getMessage())+". Playback may continue. A.I.-generated screening may require human expert confirmation.";
                runOnUiThread(()->{
                    overlay.setText(message);
                    notifyDiagnosisStatus(message);
                    JSONObject result = new JSONObject();
                    try { result.put("summary", "Video color screen could not complete."); result.put("metrics", "Playback may continue, but no readable frames were available for analysis."); result.put("findings", new JSONArray().put(message)); result.put("limitations", "A.I.-generated screening may require confirmation by a qualified human plant health expert."); result.put("capturedAt", new SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()).format(new Date())); result.put("imageCount", 0); result.put("mediaType", "video"); } catch (Exception ignored) { }
                    if (web != null) web.evaluateJavascript("window.onDiagnosisVideoAnalysis&&window.onDiagnosisVideoAnalysis(" + camera + "," + JSONObject.quote(plotId) + "," + JSONObject.quote(result.toString()) + ")", null);
                });
            } finally { try { retriever.release(); } catch(Exception ignored) { } }
        });
    }

    private String pixelIntensityMarkup(double green,double yellow,double brown,double other) {
        return "<font color='#3EAA5D'>● Green "+pct(green)+"%</font>　<font color='#F1D451'>● Yellow "+pct(yellow)+"%</font><br>"+
                "<font color='#A46A45'>● Brown "+pct(brown)+"%</font>　<font color='#C7CFCA'>● Other "+pct(other)+"%</font>";
    }

    private static String pct(double value) { return String.format(Locale.US,"%.1f",value*100); }

    private double[] measureDiagnosisPixels(Bitmap bitmap) {
        int width=bitmap.getWidth(),height=bitmap.getHeight(),total=width*height;int[] pixels=new int[total];bitmap.getPixels(pixels,0,width,0,0,width,height);
        int green=0,yellow=0,brown=0;
        for(int color:pixels){int r=Color.red(color),g=Color.green(color),b=Color.blue(color);if(g>r*1.10&&g>b*1.04&&g>45)green++;else if(r>100&&g>90&&r>g*.92&&g>b*1.18)yellow++;else if(r>65&&r>g*1.22&&r>b*1.12)brown++;}
        int other=Math.max(0,total-green-yellow-brown);return new double[]{green/(double)total,yellow/(double)total,brown/(double)total,other/(double)total};
    }

    private void removeDiagnosisMediaFromStorage(int camera, String plotId, String documentId, String fileName, String mediaUrl) {
        String error = null;
        try {
            if (!fileName.isEmpty() && (fileName.contains("/") || fileName.contains("\\") || fileName.equals(".") || fileName.equals("..") || !(fileName.toLowerCase(Locale.ROOT).endsWith(".jpg") || fileName.toLowerCase(Locale.ROOT).endsWith(".jpeg") || fileName.toLowerCase(Locale.ROOT).endsWith(".png") || fileName.toLowerCase(Locale.ROOT).endsWith(".mp4")))) throw new IllegalArgumentException("Invalid media filename");
            String treeText=getPrefs().getString(PREF_DIAGNOSIS_TREE_URI,null), rootId=getPrefs().getString(PREF_DIAGNOSIS_ROOT_DOCUMENT_ID,null);
            if (!documentId.isEmpty()) {
                if(treeText==null||rootId==null||!isDiagnosisDriveReady()) throw new IllegalStateException("Google Drive Diagnosis folder is not connected");
                Uri tree=Uri.parse(treeText); String plotDoc=findExistingChildFolder(tree,rootId,diagnosisPlotFolder(plotId));
                String quadrant="Quadrant_"+String.format(Locale.US,"%02d",camera);
                String quadrantDoc=plotDoc==null?null:findExistingChildFolder(tree,plotDoc,quadrant);
                if(quadrantDoc==null) throw new IllegalStateException("The saved diagnosis folder was not found");
                Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(tree,quadrantDoc); boolean exactChild=false;
                try(Cursor c=getContentResolver().query(children,new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID},null,null,null)){
                    if(c!=null){int col=c.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);while(c.moveToNext())if(col>=0&&documentId.equals(c.getString(col))){exactChild=true;break;}}
                }
                if(!exactChild) throw new IllegalStateException("That file is no longer in this plot and quadrant folder");
                Uri target=DocumentsContract.buildDocumentUriUsingTree(tree,documentId);
                if(!DocumentsContract.deleteDocument(getContentResolver(),target)) throw new IllegalStateException("Google Drive did not confirm deletion");
            }
            JSONArray index=new JSONArray(readDiagnosisMediaIndex(camera,plotId));
            JSONArray kept=new JSONArray(); String foundUrl=mediaUrl, foundThumb="";
            for(int i=0;i<index.length();i++){JSONObject row=index.optJSONObject(i);if(row==null)continue;
                boolean match=!documentId.isEmpty()?documentId.equals(row.optString("driveDocumentId")):(!fileName.isEmpty()?fileName.equals(row.optString("fileName")):mediaUrl.equals(row.optString("url")));
                if(match){if(foundUrl.isEmpty())foundUrl=row.optString("url");foundThumb=row.optString("thumbnailUrl");}else kept.put(row);
            }
            getPrefs().edit().putString(diagnosisMediaIndexKey(camera,plotId),kept.toString()).apply();
            deleteDiagnosisLocalAsset(plotId,camera,fileName);
            deleteDiagnosisCacheAsset(plotId,camera,foundUrl);
            deleteDiagnosisCacheAsset(plotId,camera,foundThumb);
            final String doc=documentId, name=fileName;
            runOnUiThread(()->{if(web!=null)web.evaluateJavascript("window.onDiagnosisMediaRemoved&&window.onDiagnosisMediaRemoved("+camera+","+JSONObject.quote(plotId)+","+JSONObject.quote(doc)+","+JSONObject.quote(name)+",null)",null);refreshDiagnosisMediaFromDrive(camera,plotId);});
        }catch(Exception ex){error=ex.getMessage()==null?"Media could not be deleted":ex.getMessage();}
        if(error!=null){final String message=error;runOnUiThread(()->{if(web!=null)web.evaluateJavascript("window.onDiagnosisMediaRemoved&&window.onDiagnosisMediaRemoved("+camera+","+JSONObject.quote(plotId)+","+JSONObject.quote(documentId)+","+JSONObject.quote(fileName)+","+JSONObject.quote(message)+")",null);});}
    }

    private void deleteDiagnosisLocalAsset(String plotId,int camera,String name){if(name==null||name.isEmpty())return;try{File dir=new File(new File(new File(getFilesDir(),"diagnosis_media"),plotId),String.format(Locale.US,"cctv_%02d",camera)).getCanonicalFile();File f=new File(dir,name).getCanonicalFile();if(f.getParentFile().equals(dir))f.delete();}catch(Exception ignored){}}
    private void deleteDiagnosisCacheAsset(String plotId,int camera,String assetUrl){try{String expected="https://appassets.androidplatform.net/assets/diagnosis/"+plotId+"/cctv_"+String.format(Locale.US,"%02d",camera)+"/";if(assetUrl==null||!assetUrl.startsWith(expected))return;String name=Uri.parse(assetUrl).getLastPathSegment();if(name==null||!name.matches("[A-Za-z0-9_-]{1,100}\\.(jpg|jpeg|png|mp4)"))return;File dir=new File(new File(new File(getCacheDir(),"diagnosis_drive_cache"),plotId),String.format(Locale.US,"cctv_%02d",camera)).getCanonicalFile();File f=new File(dir,name).getCanonicalFile();if(f.getParentFile().equals(dir))f.delete();}catch(Exception ignored){}}

    private void openDiagnosisDriveMediaInPlayer(String documentId, String mimeType) {
        String treeText = getPrefs().getString(PREF_DIAGNOSIS_TREE_URI, null);
        if (treeText == null || documentId == null || documentId.isEmpty()) {
            notifyDiagnosisStatus("The Google Drive video reference is unavailable. Refresh the diagnosis list.");
            return;
        }
        try {
            Uri treeUri = Uri.parse(treeText);
            Uri mediaUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId);
            Intent view = new Intent(Intent.ACTION_VIEW);
            view.setDataAndType(mediaUri, mimeType == null || mimeType.isEmpty() ? "video/mp4" : mimeType);
            view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(view, "Play diagnosis video"));
        } catch (Exception error) {
            notifyDiagnosisStatus("No video player could open this Google Drive file. Open it from the Drive app.");
        }
    }

    private String signedInFirebaseEmail() {
        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String email = user == null ? null : user.getEmail();
        if (email == null || email.trim().isEmpty())
            throw new IllegalStateException("Sign in to connect this plot's Google Drive folder.");
        return email.trim();
    }

    private String signedInEmailFolderName() {
        String safe = signedInFirebaseEmail().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9@._+-]+", "_");
        if (safe.length() > 100) safe = safe.substring(0, 100);
        return safe;
    }

    private String driveApiFolderKey(String plotId) {
        return DRIVE_API_FOLDER_KEY_PREFIX + safePlotId(plotId);
    }

    private String driveApiFolderForSignedInUser(String plotId) {
        String id = safePlotId(plotId);
        String folderId = getPrefs().getString(driveApiFolderKey(id), null);
        String savedEmailFolder = getPrefs().getString(DRIVE_API_EMAIL_KEY_PREFIX + id, null);
        if (folderId == null || savedEmailFolder == null) return null;
        try { return savedEmailFolder.equals(signedInEmailFolderName()) ? folderId : null; }
        catch (Exception ignored) { return null; }
    }

    private void authorizeAndConnectDriveFeed(String plotId, String folderName) {
        pendingDriveAuthPlotId = safePlotId(plotId);
        pendingDriveAuthFolderName = safeFolderName(folderName);
        if (driveAuthorizationInProgress) return;
        driveAuthorizationInProgress = true;
        notifyLoginDriveStatus(pendingDriveAuthPlotId, false, false,
                "Requesting Google Drive access for the assigned plot folder…");
        try {
            AuthorizationRequest.Builder requestBuilder = new AuthorizationRequest.Builder()
                    .setRequestedScopes(Collections.singletonList(new Scope(DRIVE_API_SCOPE)));
            // Every GrowPlot account uses the central owner Drive. The Firebase email is used only
            // to select that user's subfolder beneath Projects/growplot/Image_Feed.
            requestBuilder.setAccount(new Account(DRIVE_OWNER_EMAIL, "com.google"));
            Identity.getAuthorizationClient(this).authorize(requestBuilder.build())
                    .addOnSuccessListener(this, this::completeDriveAuthorization)
                    .addOnFailureListener(this, error -> {
                        driveAuthorizationInProgress = false;
                        pendingDriveFolderBrowse = false;
                        pendingDriveReconnectOnly = false;
                        notifyLoginDriveStatus(pendingDriveAuthPlotId, false, true,
                                "Google Drive authorization failed: " + safeErrorMessage(error));
                    });
        } catch (Exception error) {
            driveAuthorizationInProgress = false;
            pendingDriveFolderBrowse = false;
            pendingDriveReconnectOnly = false;
            notifyLoginDriveStatus(pendingDriveAuthPlotId, false, true,
                    "Google Drive authorization could not start: " + safeErrorMessage(error));
        }
    }

    private void completeDriveAuthorization(AuthorizationResult authorization) {
        if (authorization == null) {
            driveAuthorizationInProgress = false;
            notifyLoginDriveStatus(pendingDriveAuthPlotId, false, true, "Google Drive returned no authorization result.");
            return;
        }
        if (authorization.hasResolution()) {
            try {
                startIntentSenderForResult(authorization.getPendingIntent().getIntentSender(),
                        REQUEST_DRIVE_AUTHORIZATION, null, 0, 0, 0);
            } catch (Exception error) {
                driveAuthorizationInProgress = false;
                notifyLoginDriveStatus(pendingDriveAuthPlotId, false, true,
                        "Google Drive consent could not be opened: " + safeErrorMessage(error));
            }
            return;
        }
        String token = authorization.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            driveAuthorizationInProgress = false;
            notifyLoginDriveStatus(pendingDriveAuthPlotId, false, true,
                    "Google Drive authorization returned no access token. Check the Drive scope in Google Auth Platform.");
            return;
        }
        googleDriveAccessToken = token;
        googleDriveTokenIssuedAt = System.currentTimeMillis();
        driveAuthorizationInProgress = false;
        final String plotId = safePlotId(pendingDriveAuthPlotId);
        final String folderName = safeFolderName(pendingDriveAuthFolderName);
        farmDataWorker.execute(() -> {
            driveApiTrace.set(new StringBuilder());
            try {
                String driveAccountEmail = getDriveAccountEmail();
                if (!DRIVE_OWNER_EMAIL.equalsIgnoreCase(driveAccountEmail)) {
                    throw new IllegalStateException("Google Drive authorized " + driveAccountEmail
                            + " but GrowPlot requires the shared owner Drive " + DRIVE_OWNER_EMAIL
                            + ". Select that Google account and connect again.");
                }
                getPrefs().edit()
                        .putString(DRIVE_API_ACCOUNT_EMAIL_PREFIX + plotId, driveAccountEmail).apply();
                if (pendingDriveFolderBrowse) {
                    loadDriveFeedFolderOptions(plotId);
                } else if (pendingDriveReconnectOnly) {
                    pendingDriveReconnectOnly = false;
                    String folderId = getPrefs().getString(driveApiFolderKey(plotId), null);
                    if (folderId == null || folderId.isEmpty()) throw new IllegalStateException("The saved Google Drive folder is missing. Choose it again in Settings.");
                    listDriveFiles("'" + folderId.replace("'", "\\'") + "' in parents and trashed=false");
                    notifyLoginDriveStatus(plotId, true, true, feedLocationLabel(plotId));
                    scanSnapshotFolder(plotId);
                } else {
                    connectDefaultFeedWithDriveApi(plotId, folderName);
                }
            } catch (Exception error) {
                String message = "Google Drive folder connection failed: " + driveApiDiagnosticMessage(error);
                notifyLoginDriveStatus(plotId, false, true, message);
                if (pendingDriveFolderBrowse) {
                    pendingDriveFolderBrowse = false;
                    publishStatus(makeStatus(message));
                }
            }
        });
    }

    private String getDriveAccountEmail() throws Exception {
        JSONObject about = driveApiJsonRequest("GET", "/drive/v3/about?fields=user(emailAddress)", null);
        JSONObject user = about.optJSONObject("user");
        return user == null ? "" : user.optString("emailAddress", "").trim();
    }

    private void loadDriveFeedFolderOptions(String plotId) {
        farmDataWorker.execute(() -> {
            driveApiTrace.set(new StringBuilder());
            try {
                String projectsId = findDriveSharedDriveByName("Projects");
                if (projectsId == null) projectsId = findDriveFolderByName(null, "Projects");
                if (projectsId == null) throw new IllegalStateException("Could not find a Projects folder or Shared Drive in this Google Drive account.");
                String growPlotId = findDriveFolderByName(projectsId, "growplot");
                if (growPlotId == null) throw new IllegalStateException("Could not find Projects/growplot in this Google Drive account.");
                String imageFeedId = findOrCreateDriveFolder(growPlotId, "Image_Feed");
                String imageFeedPath = "Google Drive: /Projects/growplot/Image_Feed";
                String emailFolder = signedInEmailFolderName();
                String userFolderId = findOrCreateDriveFolder(imageFeedId, emailFolder);
                String plotFolderName = safeFolderName(pickerDefaultFolderName);
                String defaultPlotFolderId = findOrCreateDriveFolder(userFolderId, plotFolderName);
                String defaultPath = imageFeedPath + "/" + emailFolder + "/" + plotFolderName;

                // Put this GrowPlot user's own plot folder first and checked by default.
                // Keep every other accessible folder selectable below it.
                List<DriveFeedFolderOption> options = new ArrayList<>();
                options.add(new DriveFeedFolderOption(imageFeedId, imageFeedPath));
                collectDriveFeedFolderOptions(imageFeedId, imageFeedPath, options, 0, new HashSet<>());
                List<DriveFeedFolderOption> ordered = new ArrayList<>();
                Set<String> optionIds = new HashSet<>();
                ordered.add(new DriveFeedFolderOption(defaultPlotFolderId, defaultPath));
                optionIds.add(defaultPlotFolderId);
                for (DriveFeedFolderOption option : options) {
                    if (option != null && option.id != null && optionIds.add(option.id)) ordered.add(option);
                }
                if (ordered.size() > 2) Collections.sort(ordered.subList(2, ordered.size()),
                        Comparator.comparing(option -> option.path.toLowerCase(Locale.ROOT)));
                runOnUiThread(() -> showDriveFeedFolderOptions(safePlotId(plotId), ordered));
            } catch (Exception error) {
                pendingDriveFolderBrowse = false;
                String message = "Could not list Google Drive folders: " + driveApiDiagnosticMessage(error);
                notifyLoginDriveStatus(plotId, false, true, message);
                publishStatus(makeStatus(message));
            }
        });
    }

    private void collectDriveFeedFolderOptions(String parentId, String parentPath,
                                               List<DriveFeedFolderOption> options, int depth,
                                               Set<String> visited) throws Exception {
        if (depth >= 7 || options.size() >= 500 || !visited.add(parentId)) return;
        String query = "'" + parentId.replace("'", "\\'") + "' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false";
        JSONArray children = listDriveFiles(query);
        for (int i = 0; i < children.length() && options.size() < 500; i++) {
            JSONObject child = children.optJSONObject(i);
            if (child == null) continue;
            String id = child.optString("id", "");
            String name = child.optString("name", "");
            if (id.isEmpty() || name.isEmpty()) continue;
            String path = parentPath + "/" + name;
            options.add(new DriveFeedFolderOption(id, path));
            collectDriveFeedFolderOptions(id, path, options, depth + 1, visited);
        }
    }

    private void showDriveFeedFolderOptions(String plotId, List<DriveFeedFolderOption> options) {
        if (isFinishing()) return;
        if (options == null || options.isEmpty()) {
            pendingDriveFolderBrowse = false;
            String message = "No folders were found under Projects/growplot/Image_Feed.";
            publishStatus(makeStatus(message));
            return;
        }
        String[] labels = new String[options.size()];
        for (int i = 0; i < options.size(); i++) labels[i] = options.get(i).path;
        final int[] selectedIndex = {0};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Choose Google Drive folder")
                .setMessage("The selected default is this GrowPlot user's plot folder. Other accessible folders remain available.")
                .setSingleChoiceItems(labels, 0, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("Connect", (dialog, which) ->
                        saveSelectedDriveFeedFolder(plotId, options.get(selectedIndex[0])))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    pendingDriveFolderBrowse = false;
                    publishStatus(makeStatus("Google Drive folder selection cancelled."));
                })
                .show();
    }

    private void saveSelectedDriveFeedFolder(String plotId, DriveFeedFolderOption option) {
        if (option == null || option.id == null || option.id.isEmpty()) return;
        farmDataWorker.execute(() -> {
            try {
                listDriveFiles("'" + option.id.replace("'", "\\'") + "' in parents and trashed=false");
                String firebaseIdentity = signedInEmailFolderName();
                String driveAccountEmail = getDriveAccountEmail();
                String path = option.path;
                JSONObject config = new JSONObject(getPrefs().getString("data_feed_config_" + plotId, "{}"));
                config.put("plotId", plotId);
                config.put("enabled", true);
                config.put("driveApiFolderId", option.id);
                getPrefs().edit()
                        .putString(driveApiFolderKey(plotId), option.id)
                        .putString(DRIVE_API_EMAIL_KEY_PREFIX + plotId, firebaseIdentity)
                        .putString(DRIVE_API_ACCOUNT_EMAIL_PREFIX + plotId, driveAccountEmail)
                        .putString(DRIVE_API_FOLDER_NAME_KEY_PREFIX + plotId, option.path.substring(option.path.lastIndexOf('/') + 1))
                        .putString(feedLocationKey(plotId), path)
                        .putString("data_feed_config_" + plotId, config.toString())
                        .putString(PREF_ACTIVE_PLOT_ID, plotId)
                        .apply();
                pendingDriveFolderBrowse = false;
                pickerCreatesDefaultFeed = false;
                notifyFeedSettingsSaved(plotId, path);
                notifyLoginDriveStatus(plotId, true, true, path);
                publishStatus(makeStatus("Connected to " + path));
                scanSnapshotFolder(plotId);
            } catch (Exception error) {
                pendingDriveFolderBrowse = false;
                String message = "Could not connect this Google Drive folder: " + safeErrorMessage(error);
                notifyLoginDriveStatus(plotId, false, true, message);
                publishStatus(makeStatus(message));
            }
        });
    }

    private boolean hasVertexAccessToken() {
        String token = vertexAccessToken;
        return token != null && !token.trim().isEmpty()
                && System.currentTimeMillis() - vertexTokenIssuedAt < 50L * 60L * 1000L;
    }

    private void authorizeVertexForPendingDiagnosis() {
        try {
            AuthorizationRequest.Builder requestBuilder = new AuthorizationRequest.Builder()
                    .setRequestedScopes(Collections.singletonList(new Scope(VERTEX_API_SCOPE)));
            com.google.firebase.auth.FirebaseUser firebaseUser =
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            String email = firebaseUser == null ? null : firebaseUser.getEmail();
            if (email != null && !email.trim().isEmpty()) {
                requestBuilder.setAccount(new Account(email.trim(), "com.google"));
            }
            Identity.getAuthorizationClient(this).authorize(requestBuilder.build())
                    .addOnSuccessListener(this, authorization -> finishVertexAuthorization(authorization, null))
                    .addOnFailureListener(this, error -> finishVertexAuthorization(null,
                            "Google Cloud authorization failed: " + safeErrorMessage(error)));
        } catch (Exception error) {
            finishVertexAuthorization(null, "Google Cloud authorization could not start: " + safeErrorMessage(error));
        }
    }

    private void finishVertexAuthorization(AuthorizationResult authorization, String failureMessage) {
        if (authorization != null && authorization.hasResolution()) {
            try {
                startIntentSenderForResult(authorization.getPendingIntent().getIntentSender(),
                        REQUEST_VERTEX_AUTHORIZATION, null, 0, 0, 0);
            } catch (Exception error) {
                finishVertexAuthorization(null, "Google Cloud permission prompt could not open: " + safeErrorMessage(error));
            }
            return;
        }
        String token = authorization == null ? null : authorization.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            vertexAuthorizationFailedOnce = true;
            if (failureMessage != null && !failureMessage.trim().isEmpty()) {
                final int camera = pendingVertexCamera;
                final String plot = pendingVertexPlotId;
                if (camera > 0) sendVertexResult(camera, plot, vertexStatus("not_authorized", failureMessage));
            }
        } else {
            vertexAccessToken = token;
            vertexTokenIssuedAt = System.currentTimeMillis();
            vertexAuthorizationFailedOnce = false;
        }
        vertexAuthorizationInProgress = false;
        final int camera = pendingVertexCamera;
        final String plotId = pendingVertexPlotId;
        final String species = pendingVertexSpecies;
        final String symptom = pendingVertexSymptom;
        final String metrics = pendingVertexPixelMetrics;
        SnapshotBridge bridge = pendingVertexBridge;
        pendingVertexBridge = null;
        if (camera > 0 && bridge != null) bridge.diagnosePhotosWithPlantNet(camera, plotId, species, symptom, metrics);
    }

    private String findDriveSharedDriveByName(String name) throws Exception {
        String escapedName = name.replace("\\", "\\\\").replace("'", "\\'");
        String query = "name='" + escapedName + "'";
        String pageToken = null;
        String fallbackId = null;
        do {
            String path = "/drive/v3/drives?q=" + URLEncoder.encode(query, "UTF-8")
                    + "&pageSize=100&fields=nextPageToken,drives(id,name)"
                    + (pageToken == null ? "" : "&pageToken=" + URLEncoder.encode(pageToken, "UTF-8"));
            JSONObject response = driveApiJsonRequest("GET", path, null);
            JSONArray drives = response.optJSONArray("drives");
            if (drives != null) {
                for (int i = 0; i < drives.length(); i++) {
                    JSONObject drive = drives.optJSONObject(i);
                    if (drive == null || !name.equals(drive.optString("name"))) continue;
                    String id = drive.optString("id", "");
                    if (id.isEmpty()) continue;
                    String growPlotId = findDriveFolderByName(id, "growplot");
                    if (growPlotId == null) continue;
                    if (findDriveFolderByName(growPlotId, "Image_Feed") != null) return id;
                    if (fallbackId == null) fallbackId = id;
                }
            }
            pageToken = response.optString("nextPageToken", null);
        } while (pageToken != null && !pageToken.isEmpty());
        return fallbackId;
    }

    private void connectDefaultFeedWithDriveApi(String plotId, String folderName) throws Exception {
        // "Projects" may be a folder shared into My Drive, or an actual Shared Drive.
        // Drive API exposes Shared Drives through drives.list rather than files.list.
        String projectsId = findDriveSharedDriveByName("Projects");
        if (projectsId == null) projectsId = findDriveFolderByName(null, "Projects");
        if (projectsId == null) {
            throw new IllegalStateException("Could not find a Projects folder or Shared Drive in the authorized Google Drive account. Check that this Google account has access to Projects.");
        }
        String growPlotId = findDriveFolderByName(projectsId, "growplot");
        if (growPlotId == null) {
            throw new IllegalStateException("Could not find Projects/growplot in the authorized Google Drive account.");
        }
        String imageFeedId = findOrCreateDriveFolder(growPlotId, "Image_Feed");
        String userFolder = signedInEmailFolderName();
        String userFolderId = findOrCreateDriveFolder(imageFeedId, userFolder);
        String plotFolderId = findOrCreateDriveFolder(userFolderId, folderName);
        listDriveFiles("'" + plotFolderId.replace("'", "\\'") + "' in parents and trashed=false");
        String path = "Google Drive: /Projects/growplot/Image_Feed/" + userFolder + "/" + folderName;
        JSONObject config = new JSONObject(getPrefs().getString("data_feed_config_" + plotId, "{}"));
        config.put("plotId", plotId);
        config.put("enabled", true);
        config.put("driveApiFolderId", plotFolderId);
        getPrefs().edit()
                .putString(driveApiFolderKey(plotId), plotFolderId)
                .putString(DRIVE_API_EMAIL_KEY_PREFIX + plotId, userFolder)
                .putString(DRIVE_API_FOLDER_NAME_KEY_PREFIX + plotId, folderName)
                .putString(feedLocationKey(plotId), path)
                .putString("data_feed_config_" + plotId, config.toString())
                .putString(PREF_ACTIVE_PLOT_ID, plotId)
                .apply();
        String relative = "Image_Feed/" + userFolder + "/" + folderName;
        if (accountApiBridge != null) accountApiBridge.saveImageFeedPath(plotId, path, relative);
        pickerCreatesDefaultFeed = false;
        notifyFeedSettingsSaved(plotId, path);
        notifyLoginDriveStatus(plotId, true, true, path);
        scanSnapshotFolder(plotId);
    }

    private void notifyLoginDriveStatus(String plotId, boolean connected, boolean isFinal, String messageOrPath) {
        JSONObject result = new JSONObject();
        try {
            result.put("plotId", safePlotId(plotId));
            result.put("connected", connected);
            result.put("final", isFinal);
            if (connected) result.put("path", messageOrPath);
            else result.put("message", messageOrPath);
        } catch (Exception ignored) { }
        String json = result.toString();
        runOnUiThread(() -> {
            if (web != null) web.evaluateJavascript(
                    "window.GrowPlotLoginImageFeedStatus&&window.GrowPlotLoginImageFeedStatus(" + json + ")", null);
        });
    }

    private String safeErrorMessage(Exception error) {
        String message = error == null ? null : error.getMessage();
        return message == null || message.trim().isEmpty() ? "Unknown error" : message;
    }

    private void recordDriveApiResponse(String method, String path, int status, byte[] response) {
        try {
            String body = new String(response, StandardCharsets.UTF_8);
            if (body.length() > 900) body = body.substring(0, 900) + "…[response truncated]";
            StringBuilder trace = driveApiTrace.get();
            String line = method + " " + path + " | HTTP " + status + " | " + body + "\n";
            if (trace.length() < 6000) trace.append(line, 0, Math.min(line.length(), 6000 - trace.length()));
            if (trace.length() >= 6000) trace.append("\n[Drive API trace truncated]");
        } catch (Exception ignored) { }
    }

    private String driveApiDiagnosticMessage(Exception error) {
        String message = safeErrorMessage(error);
        StringBuilder trace = driveApiTrace.get();
        return trace == null || trace.length() == 0 ? message
                : message + "\n\nGoogle Drive API responses (raw):\n" + trace.toString();
    }

    private JSONObject driveApiJsonRequest(String method, String path, JSONObject body) throws Exception {
        byte[] response = driveApiRequest(method, path,
                body == null ? null : body.toString().getBytes(StandardCharsets.UTF_8),
                body == null ? null : "application/json; charset=utf-8");
        return new JSONObject(new String(response, StandardCharsets.UTF_8));
    }

    private byte[] driveApiRequest(String method, String path, byte[] requestBody, String contentType) throws Exception {
        String token = googleDriveAccessToken;
        if (token == null || token.trim().isEmpty())
            throw new IllegalStateException("Google Drive authorization is not active. Sign in again to reconnect.");
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL("https://www.googleapis.com" + path).openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            if (requestBody != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", contentType);
                connection.setFixedLengthStreamingMode(requestBody.length);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(requestBody);
                    output.flush();
                }
            }
            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (stream != null) {
                try (InputStream input = stream) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                }
            }
            byte[] response = output.toByteArray();
            recordDriveApiResponse(method, path, responseCode, response);
            if (responseCode < 200 || responseCode >= 300) {
                String details = new String(response, StandardCharsets.UTF_8);
                if (details.length() > 500) details = details.substring(0, 500);
                throw new IllegalStateException("Drive API HTTP " + responseCode + (details.isEmpty() ? "" : ": " + details));
            }
            return response;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private JSONArray listDriveFiles(String query) throws Exception {
        JSONArray files = listDriveFiles(query, "allDrives");
        // Items inside a directly shared My Drive folder may be absent from the allDrives
        // corpus even though the authorized user can access them. Retry parent-scoped
        // listings in the user's corpus so nested folder discovery and feed scans can see them.
        if (files.length() > 0 || !query.contains(" in parents")) return files;
        return listDriveFiles(query, "user");
    }

    private JSONArray listDriveFiles(String query, String corpora) throws Exception {
        JSONArray files = new JSONArray();
        String pageToken = null;
        do {
            String path = "/drive/v3/files?q=" + URLEncoder.encode(query, "UTF-8")
                    + "&pageSize=1000&fields=nextPageToken,files(id,name,mimeType,modifiedTime,size,parents)"
                    + "&supportsAllDrives=true&includeItemsFromAllDrives=true"
                    + "&corpora=" + URLEncoder.encode(corpora, "UTF-8")
                    + (pageToken == null ? "" : "&pageToken=" + URLEncoder.encode(pageToken, "UTF-8"));
            JSONObject response = driveApiJsonRequest("GET", path, null);
            JSONArray page = response.optJSONArray("files");
            if (page != null) for (int i = 0; i < page.length(); i++) files.put(page.opt(i));
            pageToken = response.optString("nextPageToken", null);
        } while (pageToken != null && !pageToken.isEmpty() && files.length() < MAX_FEED_FILES);
        return files;
    }

    private String findDriveFolderByName(String parentId, String name) throws Exception {
        String escapedName = name.replace("\\", "\\\\").replace("'", "\\'");
        String query = "name='" + escapedName + "' and mimeType='application/vnd.google-apps.folder' and trashed=false";
        if (parentId != null) query += " and '" + parentId.replace("'", "\\'") + "' in parents";
        JSONArray candidates = listDriveFiles(query);
        if (parentId != null) {
            return findExactDriveFolderId(candidates, name);
        }

        // A directly shared Projects folder can be absent from allDrives. Also consider every
        // same-named root and prefer the one that actually contains GrowPlot/Image_Feed; a
        // personal, unrelated Projects folder must not mask the shared project tree.
        JSONArray sharedWithMe = listDriveFiles("sharedWithMe and " + query, "user");
        Set<String> seen = new HashSet<>();
        String fallbackId = null;
        for (int pass = 0; pass < 2; pass++) {
            JSONArray files = pass == 0 ? candidates : sharedWithMe;
            for (int i = 0; i < files.length(); i++) {
                JSONObject file = files.optJSONObject(i);
                if (file == null || !name.equals(file.optString("name"))) continue;
                String id = file.optString("id", "");
                if (id.isEmpty() || !seen.add(id)) continue;
                if (!"Projects".equals(name)) return id;
                String growPlotId = findDriveFolderByName(id, "growplot");
                if (growPlotId == null) continue;
                if (findDriveFolderByName(growPlotId, "Image_Feed") != null) return id;
                if (fallbackId == null) fallbackId = id;
            }
        }
        return fallbackId;
    }

    private String findExactDriveFolderId(JSONArray files, String name) {
        for (int i = 0; i < files.length(); i++) {
            JSONObject file = files.optJSONObject(i);
            if (file != null && name.equals(file.optString("name"))) return file.optString("id", null);
        }
        return null;
    }

    private String findOrCreateDriveFolder(String parentId, String name) throws Exception {
        String existing = findDriveFolderByName(parentId, name);
        if (existing != null && !existing.isEmpty()) return existing;
        JSONObject metadata = new JSONObject();
        metadata.put("name", name);
        metadata.put("mimeType", "application/vnd.google-apps.folder");
        JSONArray parents = new JSONArray();
        parents.put(parentId);
        metadata.put("parents", parents);
        JSONObject created = driveApiJsonRequest("POST", "/drive/v3/files?supportsAllDrives=true&fields=id", metadata);
        String id = created.optString("id", "");
        if (id.isEmpty()) throw new IllegalStateException("Google Drive did not return the new folder ID for " + name);
        return id;
    }

    private List<FeedDocument> queryDriveImageFeed(String folderId, String plotId) throws Exception {
        List<FeedDocument> images = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        collectDriveImageFeed(folderId, plotId, images, visited, 0, 0);
        return images;
    }

    private void collectDriveImageFeed(String parentId, String plotId, List<FeedDocument> images,
                                       Set<String> visited, int depth, int inheritedCamera) throws Exception {
        if (depth > 6 || images.size() >= MAX_FEED_FILES || !visited.add(parentId)) return;
        String query = "'" + parentId.replace("'", "\\'") + "' in parents and trashed=false";
        JSONArray children = listDriveFiles(query);
        for (int i = 0; i < children.length() && images.size() < MAX_FEED_FILES; i++) {
            JSONObject item = children.optJSONObject(i);
            if (item == null) continue;
            String id = item.optString("id", "");
            String name = item.optString("name", "");
            String mime = item.optString("mimeType", "");
            if (id.isEmpty() || name.isEmpty()) continue;
            int cameraHint = cameraNumber(name);
            if (cameraHint == 0) cameraHint = quadrantNumber(name);
            if (cameraHint == 0) cameraHint = inheritedCamera;
            if ("application/vnd.google-apps.folder".equals(mime)) {
                collectDriveImageFeed(id, plotId, images, visited, depth + 1, cameraHint);
            } else if ("image/jpeg".equalsIgnoreCase(mime)
                    || name.toLowerCase(Locale.US).endsWith(".jpg")
                    || name.toLowerCase(Locale.US).endsWith(".jpeg")) {
                String modified = item.optString("modifiedTime", "0").replaceAll("[^0-9]", "");
                File cacheDirectory = new File(new File(getCacheDir(), "drive_image_feed"), plotId);
                if (!cacheDirectory.exists() && !cacheDirectory.mkdirs())
                    throw new IllegalStateException("Cannot create the Drive image cache");
                File cached = new File(cacheDirectory,
                        id.replaceAll("[^A-Za-z0-9_-]", "_") + "_" + modified + ".jpg");
                if (!cached.isFile() || cached.length() == 0) {
                    byte[] photo = driveApiRequest("GET",
                            "/drive/v3/files/" + URLEncoder.encode(id, "UTF-8") + "?alt=media&supportsAllDrives=true", null, null);
                    try (OutputStream output = new FileOutputStream(cached)) { output.write(photo); }
                }
                images.add(new FeedDocument(Uri.fromFile(cached), name, cameraHint));
            }
        }
    }

    private void uploadDriveApiPhoto(String folderId, String fileName, Bitmap bitmap) throws Exception {
        ByteArrayOutputStream photo = new ByteArrayOutputStream();
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, photo))
            throw new IllegalStateException("Could not convert the snapshot to JPEG");
        String boundary = "growplot-" + System.currentTimeMillis();
        JSONObject metadata = new JSONObject();
        metadata.put("name", fileName);
        JSONArray parents = new JSONArray();
        parents.put(folderId);
        metadata.put("parents", parents);
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(("--" + boundary + "\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n"
                + metadata.toString() + "\r\n--" + boundary + "\r\nContent-Type: image/jpeg\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8));
        photo.writeTo(body);
        body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        driveApiRequest("POST", "/upload/drive/v3/files?uploadType=multipart&supportsAllDrives=true&fields=id",
                body.toByteArray(), "multipart/related; boundary=" + boundary);
    }

    private void connectDefaultFeedInSavedTree(String plotId, Uri treeUri) throws Exception {
        String growPlotId = resolveGrowPlotDocumentId(treeUri);
        if (growPlotId == null) throw new IllegalStateException("The saved Drive permission no longer points to Projects or GrowPlot");
        String imageFeedId = findOrCreateChildFolder(treeUri, growPlotId, "Image_Feed");
        String userFolder = signedInEmailFolderName();
        String userFolderId = findOrCreateChildFolder(treeUri, imageFeedId, userFolder);
        String plotFolder = safeFolderName(pickerDefaultFolderName);
        String plotFolderId = findOrCreateChildFolder(treeUri, userFolderId, plotFolder);
        String relativePath = "Image_Feed/" + userFolder + "/" + plotFolder;
        String locationLabel = "Google Drive: /Projects/growplot/" + relativePath;
        JSONObject config = new JSONObject(getPrefs().getString("data_feed_config_" + plotId, "{}"));
        config.put("plotId", plotId);
        config.put("enabled", true);
        config.put("folderUri", treeUri.toString());
        getPrefs().edit()
                .putString(feedTreeKey(plotId), treeUri.toString())
                .putString(feedFolderDocumentKey(plotId), plotFolderId)
                .putString(feedLocationKey(plotId), locationLabel)
                .putString("data_feed_config_" + plotId, config.toString())
                .putString(PREF_ACTIVE_PLOT_ID, plotId)
                .apply();
        rememberGrowPlotTreeIfScoped(treeUri);
        saveDriveStorageConfig(treeUri);
        if (accountApiBridge != null)
            accountApiBridge.saveImageFeedPath(plotId, locationLabel, relativePath);
        pickerCreatesDefaultFeed = false;
        notifyFeedSettingsSaved(plotId, locationLabel);
        scanSnapshotFolder(plotId);
    }

    private void openImageFeedFolderPicker() {
        openImageFeedFolderPicker(false);
    }

    private void openImageFeedFolderPicker(boolean forcePicker) {
        if (isFinishing()) return;
        if (!forcePicker && pickerCreatesDefaultFeed) {
            String savedTree = getPrefs().getString(feedTreeKey(pickerPlotId), null);
            String savedFolder = getPrefs().getString(feedFolderDocumentKey(pickerPlotId), null);
            // Reuse any previously granted GrowPlot/Projects tree even when this plot
            // has no per-plot folder mapping yet; connectDefaultFeed creates that path.
            if (savedTree == null || savedFolder == null) {
                Uri scopedTree = findGrowPlotScopedTreeUri();
                if (scopedTree != null) {
                    try {
                        connectDefaultFeedInSavedTree(pickerPlotId, scopedTree);
                    } catch (Exception error) {
                        String reason = error.getMessage() == null ? "Drive folder could not be created" : error.getMessage();
                        publishStatus(makeStatus("Expected Google Drive path: " + expectedFeedPathLabel(pickerPlotId)
                                + " | Folder status: unavailable — " + reason));
                    }
                    return;
                }
            }
            if (savedTree != null && savedFolder != null) {
                Uri savedTreeUri = Uri.parse(savedTree);
                if (hasPersistedReadWriteGrant(savedTreeUri)) {
                    try {
                        connectDefaultFeedInSavedTree(pickerPlotId, savedTreeUri);
                    } catch (Exception error) {
                        String reason = error.getMessage() == null ? "Drive folder could not be created" : error.getMessage();
                        publishStatus(makeStatus("Expected Google Drive path: " + expectedFeedPathLabel(pickerPlotId)
                                + " | Folder status: unavailable — " + reason));
                    }
                    return;
                }
            }
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(Intent.EXTRA_TITLE, pickerCreatesDefaultFeed
                ? "Choose " + pickerDefaultFolderName + " in Projects/growplot/Image_Feed"
                : "Choose this plot's image feed folder");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQUEST_IMAGE_FOLDER);
        } catch (Exception error) {
            publishStatus(makeStatus("This device does not provide a folder picker. Choose the data feed location using the system Files app."));
        }
    }

    private void loadMainPage() {
        try (InputStream input = getAssets().open("index.html");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            String html = output.toString(StandardCharsets.UTF_8.name())
                    .replace("{{GROWPLOT_VERSION}}", "v" + BuildConfig.VERSION_NAME);
            // Inline the packaged CV engine so WebView never needs to fetch leaf-cv.js.
            try (InputStream cvInput = getAssets().open("leaf-cv.js");
                 ByteArrayOutputStream cvOutput = new ByteArrayOutputStream()) {
                byte[] cvBuffer = new byte[8192];
                int cvRead;
                while ((cvRead = cvInput.read(cvBuffer)) != -1) cvOutput.write(cvBuffer, 0, cvRead);
                String cvScript = cvOutput.toString(StandardCharsets.UTF_8.name());
                String cvTag = "<script>" + cvScript + "</script>";
                if (html.contains("</head>")) html = html.replace("</head>", cvTag + "</head>");
                else if (html.contains("</body>")) html = html.replace("</body>", cvTag + "</body>");
                else throw new IllegalStateException("Dashboard HTML missing head/body closing tag");
            }
            web.loadDataWithBaseURL("https://appassets.androidplatform.net/", html,
                    "text/html", "UTF-8", null);
        } catch (Exception error) {
            web.loadData("GrowPlot could not load its dashboard.", "text/plain", "UTF-8");
        }
    }

    private synchronized void scanSnapshotFolder() {
        scanSnapshotFolder(null);
    }

    private synchronized void scanSnapshotFolder(String requestedPlotId) {
        if (isFinishing()) return;
        if (drivePathRestorePending) return;
        if (scanInProgress) {
            rescanRequested = true;
            return;
        }
        final String plotId = safePlotId(requestedPlotId == null
                ? getPrefs().getString(PREF_ACTIVE_PLOT_ID, DEFAULT_PLOT_ID) : requestedPlotId);
        String savedApiFolder = driveApiFolderForSignedInUser(plotId);
        if (savedApiFolder != null && !savedApiFolder.isEmpty()
                && (googleDriveAccessToken == null || System.currentTimeMillis() - googleDriveTokenIssuedAt > 45 * 60_000L)
                && com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (!driveAuthorizationInProgress) {
                String savedName = getPrefs().getString(DRIVE_API_FOLDER_NAME_KEY_PREFIX + plotId, pickerDefaultFolderName);
                pendingDriveReconnectOnly = true;
                authorizeAndConnectDriveFeed(plotId, savedName);
            }
            return;
        }
        String savedTree = getPrefs().getString(feedTreeKey(plotId), null);
        if (savedTree == null && DEFAULT_PLOT_ID.equals(plotId)) savedTree = getPrefs().getString(PREF_IMAGE_FEED_TREE_URI, null);
        String savedFiles = getPrefs().getString(PREF_SELECTED_URIS, null);
        final String apiFolderId = driveApiFolderForSignedInUser(plotId);
        final Uri treeUri = savedTree == null ? null : Uri.parse(savedTree);
        final List<Uri> selected = new ArrayList<>();
        if (treeUri == null && savedFiles != null) {
            try {
                JSONArray array = new JSONArray(savedFiles);
                for (int i = 0; i < array.length(); i++) selected.add(Uri.parse(array.getString(i)));
            } catch (Exception ignored) { }
        }
        if ((apiFolderId == null || apiFolderId.isEmpty()) && treeUri == null && selected.isEmpty()) {
            JSONObject status = makeStatus("No Google Drive folder is connected for this plot on this device. Tap Connect to folder above to grant access.");
            try { status.put("plotId", plotId); } catch (Exception ignored) { }
            publishStatus(status);
            return;
        }
        scanInProgress = true;
        scanWorker.execute(() -> {
            try {
                boolean folderSource = treeUri != null || (apiFolderId != null && !apiFolderId.isEmpty());
                List<FeedDocument> images = new ArrayList<>();
                if (apiFolderId != null && !apiFolderId.isEmpty()) {
                    images.addAll(queryDriveImageFeed(apiFolderId, plotId));
                } else if (treeUri != null) {
                    String rootDocumentId = getPrefs().getString(feedFolderDocumentKey(plotId), null);
                    if (rootDocumentId == null || rootDocumentId.isEmpty()) rootDocumentId = DocumentsContract.getTreeDocumentId(treeUri);
                    images.addAll(queryImageFeedFolder(treeUri, rootDocumentId));
                }
                else for (Uri uri : selected) images.add(new FeedDocument(uri, getDisplayName(uri), 0));
                scanSelectedImages(images, folderSource, plotId);
            } catch (Exception error) {
                String message = apiFolderId != null && !apiFolderId.isEmpty()
                        ? "Could not scan this plot's Google Drive feed. Sign in again or check the Drive API authorization."
                        : treeUri != null
                        ? "Could not scan this plot's data feed. Tap Connect to folder above and grant access again."
                        : "Could not read the previously selected photos. Tap Connect to folder above and choose the data feed folder.";
                JSONObject status = makeStatus(message);
                try { status.put("error", error.getClass().getSimpleName()); status.put("plotId", plotId); } catch (Exception ignored) { }
                publishStatus(status);
            } finally {
                scanInProgress = false;
                if (rescanRequested && !isFinishing()) {
                    rescanRequested = false;
                    mainHandler.post(MainActivity.this::scanSnapshotFolder);
                }
            }
        });
    }

    private List<FeedDocument> queryImageFeedFolder(Uri treeUri, String rootDocumentId) throws Exception {
        List<FeedDocument> images = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        collectImageFeedDocuments(treeUri, rootDocumentId, images, visited, 0, 0);
        return images;
    }

    private void collectImageFeedDocuments(Uri treeUri, String parentDocumentId,
                                           List<FeedDocument> images, Set<String> visited,
                                           int depth, int inheritedCamera) throws Exception {
        if (depth > 6 || images.size() >= MAX_FEED_FILES || !visited.add(parentDocumentId)) return;
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId);
        String[] columns = {DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE};
        try (Cursor cursor = getContentResolver().query(childrenUri, columns, null, null, null)) {
            if (cursor == null) throw new IllegalStateException("selected feed folder listing unavailable");
            int idColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            int nameColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            int mimeColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);
            if (idColumn < 0 || nameColumn < 0) throw new IllegalStateException("Folder provider omitted document names");
            while (cursor.moveToNext() && images.size() < MAX_FEED_FILES) {
                String documentId = cursor.getString(idColumn);
                String name = cursor.getString(nameColumn);
                String mime = mimeColumn < 0 || cursor.isNull(mimeColumn) ? "" : cursor.getString(mimeColumn);
                if (documentId == null || name == null) continue;
                int folderCamera = cameraNumber(name);
                if (folderCamera == 0) folderCamera = quadrantNumber(name);
                if (folderCamera == 0) folderCamera = cameraFolderNumber(name);
                int cameraHint = folderCamera > 0 ? folderCamera : inheritedCamera;
                if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
                    collectImageFeedDocuments(treeUri, documentId, images, visited, depth + 1, cameraHint);
                } else if ("image/jpeg".equalsIgnoreCase(mime)
                        || name.toLowerCase(Locale.US).endsWith(".jpg")
                        || name.toLowerCase(Locale.US).endsWith(".jpeg")) {
                    images.add(new FeedDocument(DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId), name, cameraHint));
                }
            }
        }
    }

    private int quadrantNumber(String name) {
        Matcher matcher = CAMERA_LABEL.matcher(name == null ? "" : name);
        if (!matcher.find()) return 0;
        try { return Integer.parseInt(matcher.group(1)); } catch (NumberFormatException ignored) { return 0; }
    }

    private int cameraFolderNumber(String name) {
        return quadrantNumber(name);
    }

    private String getDocumentName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri,
                new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
                if (column >= 0 && !cursor.isNull(column)) return cursor.getString(column);
            }
        } catch (Exception ignored) { }
        return "Selected folder";
    }

    private String getStorageLabel(Uri uri) {
        String authority = uri == null ? "" : uri.getAuthority();
        if (authority == null) return "Storage";
        String normalized = authority.toLowerCase(Locale.ROOT);
        if (normalized.contains("google.android.apps.docs")) return "Google Drive";
        if (normalized.contains("externalstorage")) return "Device storage";
        if (normalized.contains("downloads")) return "Downloads";
        return authority.replace(".documents", "").replace("com.android.providers.", "");
    }

    private boolean hasLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestDeviceLocationPermission(String plotId) {
        if (isFinishing()) return;
        locationPermissionPlotId = plotId;
        if (hasLocationPermission()) {
            requestDeviceLocationForPlot(plotId);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (locationPermissionRequestInFlight) return;
            locationPermissionRequestInFlight = true;
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_DEVICE_LOCATION);
        } else sendDeviceLocation(plotId, null, "Device location is unavailable.");
    }

    private void requestDeviceLocationForPlot(String plotId) {
        if (locationRequestInFlight) {
            locationLookupPlotId = plotId;
            return;
        }
        locationRequestInFlight = true;
        locationLookupPlotId = plotId;
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (manager == null) {
            locationRequestInFlight = false;
            sendDeviceLocation(plotId, null, "Device location is unavailable.");
            return;
        }
        try {
            Location cached = null;
            for (String provider : new String[]{LocationManager.NETWORK_PROVIDER,
                    LocationManager.GPS_PROVIDER, LocationManager.PASSIVE_PROVIDER}) {
                try {
                    Location candidate = manager.getLastKnownLocation(provider);
                    if (candidate != null && (cached == null || candidate.getTime() > cached.getTime())) cached = candidate;
                } catch (SecurityException | IllegalArgumentException ignored) { }
            }
            if (cached != null && System.currentTimeMillis() - cached.getTime() <= 10 * 60_000L) {
                locationRequestInFlight = false;
                sendDeviceLocation(plotId, cached, "");
                return;
            }
            locationListener = new LocationListener() {
                @Override public void onLocationChanged(Location location) {
                    LocationListener listener = locationListener;
                    locationListener = null;
                    if (listener != null) manager.removeUpdates(listener);
                    locationRequestInFlight = false;
                    sendDeviceLocation(locationLookupPlotId, location, "");
                }
                @Override public void onStatusChanged(String provider, int status, android.os.Bundle extras) { }
                @Override public void onProviderEnabled(String provider) { }
                @Override public void onProviderDisabled(String provider) { }
            };
            boolean requested = false;
            for (String provider : new String[]{LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER}) {
                try {
                    if (manager.isProviderEnabled(provider)) {
                        manager.requestSingleUpdate(provider, locationListener, Looper.getMainLooper());
                        requested = true;
                    }
                } catch (SecurityException | IllegalArgumentException ignored) { }
            }
            final Location fallbackLocation = cached;
            if (!requested) finishDeviceLocationLookup(manager, fallbackLocation);
            else mainHandler.postDelayed(() -> finishDeviceLocationLookup(manager, fallbackLocation), 15_000L);
        } catch (SecurityException error) {
            locationRequestInFlight = false;
            sendDeviceLocation(plotId, null, "Allow location access to center the map on this device.");
        }
    }

    private void finishDeviceLocationLookup(LocationManager manager, Location fallback) {
        if (!locationRequestInFlight) return;
        LocationListener listener = locationListener;
        locationListener = null;
        if (listener != null) manager.removeUpdates(listener);
        locationRequestInFlight = false;
        sendDeviceLocation(locationLookupPlotId, fallback,
                fallback == null ? "Could not read the device location. Turn on Location or enter the plot address." : "");
    }

    private void sendDeviceLocation(String plotId, Location location, String message) {
        JSONObject result = new JSONObject();
        try {
            result.put("plotId", safePlotId(plotId));
            result.put("found", location != null);
            result.put("message", message == null ? "" : message);
            if (location != null) {
                result.put("latitude", location.getLatitude());
                result.put("longitude", location.getLongitude());
            }
        } catch (Exception ignored) { }
        String json = result.toString();
        runOnUiThread(() -> {
            if (web != null) web.evaluateJavascript("window.onDeviceLocationAvailable && window.onDeviceLocationAvailable(" + json + ");", null);
        });
    }

    private void notifyFeedSettingsSaved(String plotId, String locationLabel) {
        JSONObject result = new JSONObject();
        try {
            result.put("plotId", safePlotId(plotId));
            result.put("path", locationLabel == null ? "" : locationLabel);
            result.put("saved", true);
        } catch (Exception ignored) { }
        String json = result.toString();
        runOnUiThread(() -> {
            if (web != null) web.evaluateJavascript("window.onFeedSettingsSaved && window.onFeedSettingsSaved(" + json + ");", null);
        });
    }

    private void sendAddressGeocode(String plotId, String query, Address address) {
        JSONObject result = new JSONObject();
        try {
            result.put("plotId", plotId);
            result.put("query", query);
            result.put("found", address != null && address.hasLatitude() && address.hasLongitude());
            if (address != null && address.hasLatitude() && address.hasLongitude()) {
                result.put("latitude", address.getLatitude());
                result.put("longitude", address.getLongitude());
                result.put("label", address.getAddressLine(0) == null ? query : address.getAddressLine(0));
            }
        } catch (Exception ignored) { }
        String json = result.toString();
        runOnUiThread(() -> {
            if (web != null) web.evaluateJavascript("window.onPlotAddressGeocoded && window.onPlotAddressGeocoded(" + json + ");", null);
        });
    }

    private String getDocumentPathLabel(Uri treeUri, String documentId) {
        String provider = getStorageLabel(treeUri);
        List<String> names = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId);
                DocumentsContract.Path path = DocumentsContract.findDocumentPath(getContentResolver(), documentUri);
                if (path != null && path.getPath() != null) {
                    for (String id : path.getPath()) {
                        String name = getDocumentName(DocumentsContract.buildDocumentUriUsingTree(treeUri, id));
                        if (name != null && !name.isEmpty() && !"Selected folder".equals(name)) names.add(name);
                    }
                }
            } catch (Exception ignored) { }
        }
        if (names.isEmpty()) {
            String rootName = getDocumentName(treeUri);
            if ("Google Drive".equals(provider)) {
                if ("GrowPlot".equalsIgnoreCase(rootName)) {
                    names.add("My Drive"); names.add("Projects"); names.add(rootName);
                } else {
                    names.add("My Drive"); names.add(rootName);
                }
            } else names.add(rootName);
        }
        StringBuilder path = new StringBuilder();
        for (String name : names) path.append('/').append(name);
        if (path.length() == 0) path.append('/').append(getDocumentName(treeUri));
        return "Google Drive".equals(provider) ? "Google Drive: " + path : provider + ": " + path;
    }

    private String findOrCreateChildFolder(Uri treeUri, String parentDocumentId, String folderName) throws Exception {
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId);
        String[] columns = {DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE};
        try (Cursor cursor = getContentResolver().query(childrenUri, columns, null, null, null)) {
            if (cursor == null) throw new IllegalStateException("Folder provider cannot list this location");
            int idColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            int nameColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            int mimeColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);
            while (cursor.moveToNext()) {
                if (idColumn < 0 || nameColumn < 0 || mimeColumn < 0) continue;
                if (folderName.equals(cursor.getString(nameColumn))
                        && DocumentsContract.Document.MIME_TYPE_DIR.equals(cursor.getString(mimeColumn))) {
                    return cursor.getString(idColumn);
                }
            }
        }
        Uri parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentDocumentId);
        Uri created = DocumentsContract.createDocument(getContentResolver(), parentUri,
                DocumentsContract.Document.MIME_TYPE_DIR, folderName);
        if (created == null) throw new IllegalStateException("Folder provider refused to create " + folderName);
        return DocumentsContract.getDocumentId(created);
    }

    private String safeFolderName(String value) {
        String safe = value == null ? "" : value.trim().replaceAll("[^A-Za-z0-9_-]+", "_");
        return safe.isEmpty() ? "1" : safe.substring(0, Math.min(32, safe.length()));
    }

    private String safePlotId(String plotId) {
        return plotId != null && plotId.matches("[A-Za-z0-9_-]{1,64}") ? plotId : DEFAULT_PLOT_ID;
    }

    private String feedTreeKey(String plotId) { return "data_feed_tree_uri_" + safePlotId(plotId); }

    private String feedFolderDocumentKey(String plotId) { return "data_feed_folder_document_" + safePlotId(plotId); }

    private String feedLocationKey(String plotId) { return "data_feed_location_label_" + safePlotId(plotId); }

    private String expectedFeedPathLabel(String plotId) {
        String id = safePlotId(plotId);
        String folderName = getPrefs().getString(DRIVE_API_FOLDER_NAME_KEY_PREFIX + id, null);
        if (folderName == null && pickerCreatesDefaultFeed && id.equals(safePlotId(pickerPlotId)))
            folderName = pickerDefaultFolderName;
        return expectedFeedPathLabel(id, folderName);
    }

    private String expectedFeedPathLabel(String plotId, String folderName) {
        try {
            String path = "Google Drive: /Projects/growplot/Image_Feed/" + signedInEmailFolderName();
            String folder = folderName == null || folderName.trim().isEmpty() ? null : safeFolderName(folderName);
            return folder == null ? path : path + "/" + folder;
        } catch (Exception ignored) {
            return "Google Drive: /Projects/growplot/Image_Feed";
        }
    }

    private String feedLocationLabel(String plotId) {
        String id = safePlotId(plotId);
        String label = getPrefs().getString(feedLocationKey(id), "");
        String savedDriveFolder = getPrefs().getString(driveApiFolderKey(id), null);
        String savedGrowPlotUser = getPrefs().getString(DRIVE_API_EMAIL_KEY_PREFIX + id, null);
        if (savedDriveFolder != null && savedGrowPlotUser != null) {
            try {
                if (!savedGrowPlotUser.equals(signedInEmailFolderName()))
                    return expectedFeedPathLabel(id);
            } catch (Exception ignored) { }
        }
        if (label != null && label.contains(": /")) return label;
        if (pickerCreatesDefaultFeed && id.equals(safePlotId(pickerPlotId)))
            return expectedFeedPathLabel(id);
        if (label != null && !label.isEmpty()) {
            if (label.startsWith("Google Drive/")) {
                String suffix = label.substring("Google Drive/".length());
                return "Google Drive: /" + suffix;
            }
            int separator = label.indexOf('/');
            if (separator > 0) return label.substring(0, separator) + ": /" + label.substring(separator + 1);
            return label;
        }
        String tree = getPrefs().getString(feedTreeKey(id), null);
        if (tree == null && DEFAULT_PLOT_ID.equals(id)) tree = getPrefs().getString(PREF_IMAGE_FEED_TREE_URI, null);
        String documentId = getPrefs().getString(feedFolderDocumentKey(id), null);
        if (tree != null && documentId != null) return getDocumentPathLabel(Uri.parse(tree), documentId);
        return tree == null ? "" : getStorageLabel(Uri.parse(tree)) + ": /" + getDocumentName(Uri.parse(tree));
    }

    private long photoFreshness(Photo photo) {
        // Rank captures by when they were taken, not when Drive synced or modified the file.
        return photo.capturedAt;
    }

    private JSONObject compareCameraFrames(Photo current, Photo previous) throws JSONException {
        JSONObject result = new JSONObject();
        try {
            String currentArchive = archivePhoto(current);
            String previousArchive = archivePhoto(previous);
            File historyDirectory = new File(new File(getFilesDir(), "photo_history"), safePlotId(current.plotId));
            FrameFeatures now = analyzeFrameColors(currentArchive == null ? current.uri
                    : Uri.fromFile(new File(historyDirectory, currentArchive)));
            FrameFeatures before = analyzeFrameColors(previousArchive == null ? previous.uri
                    : Uri.fromFile(new File(historyDirectory, previousArchive)));
            double greenDelta = now.greenPercent - before.greenPercent;
            double brightnessDelta = now.brightness - before.brightness;
            double magnitude = Math.abs(greenDelta);
            String change;
            if (greenDelta >= 4.0) {
                change = String.format(Locale.US, "More green is visible (+%.1f percentage points).", greenDelta);
            } else if (greenDelta <= -4.0) {
                change = String.format(Locale.US, "Less green is visible (−%.1f percentage points).", magnitude);
            } else {
                change = String.format(Locale.US, "Visible green is similar (%+.1f percentage points).", greenDelta);
            }
            String lighting;
            if (Math.abs(brightnessDelta) >= 12.0) {
                lighting = String.format(Locale.US, " Frame brightness also changed by %+.0f%%, which can affect the color estimate.", brightnessDelta);
            } else {
                lighting = " Frame brightness was broadly similar.";
            }
            String summary = change + lighting;
            String explanation = String.format(Locale.US,
                    "Compared %s with %s. Estimated green pixels: %.1f%% now vs %.1f%% before; average brightness: %.0f%% vs %.0f%%. This is a rough pixel-color comparison, not proof of growth or a disease diagnosis; framing, shadows, and lighting can change the result.",
                    formatClock(previous.capturedAt), formatClock(current.capturedAt), now.greenPercent,
                    before.greenPercent, now.brightness, before.brightness);
            result.put("frameInsight", summary);
            result.put("frameExplanation", explanation);
            result.put("greenChangePercentPoints", greenDelta);
            result.put("currentGreenPercent", now.greenPercent);
            result.put("previousGreenPercent", before.greenPercent);
            result.put("currentBrightnessPercent", now.brightness);
            result.put("previousBrightnessPercent", before.brightness);
            result.put("comparedCurrentAt", current.capturedAt);
            result.put("comparedPreviousAt", previous.capturedAt);
        } catch (Exception error) {
            result.put("frameInsight", "Could not compare these two snapshots.");
            result.put("frameExplanation", "Check that both saved photos can still be read from the selected image-feed folder.");
        }
        return result;
    }

    private FrameFeatures analyzeFrameColors(Uri uri) throws Exception {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) throw new IllegalStateException("Photo is unavailable");
            BitmapFactory.decodeStream(input, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw new IllegalStateException("Photo is unreadable");
        int sampleSize = 1;
        while (Math.max(bounds.outWidth, bounds.outHeight) / sampleSize > 128) sampleSize *= 2;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        Bitmap decoded;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) throw new IllegalStateException("Photo is unavailable");
            decoded = BitmapFactory.decodeStream(input, null, options);
        }
        if (decoded == null) throw new IllegalStateException("Photo is unreadable");
        FrameFeatures features = new FrameFeatures();
        long green = 0;
        long brightness = 0;
        int total = decoded.getWidth() * decoded.getHeight();
        int[] pixels = new int[total];
        decoded.getPixels(pixels, 0, decoded.getWidth(), 0, 0, decoded.getWidth(), decoded.getHeight());
        for (int color : pixels) {
            int red = Color.red(color), g = Color.green(color), blue = Color.blue(color);
            brightness += (int) Math.round(0.299 * red + 0.587 * g + 0.114 * blue);
            if (g >= 45 && g > red * 1.12 && g > blue * 1.06 && g - red >= 12) green++;
        }
        features.greenPercent = total == 0 ? 0 : green * 100.0 / total;
        features.brightness = total == 0 ? 0 : brightness * 100.0 / (total * 255.0);
        decoded.recycle();
        return features;
    }

    private void scanSelectedImages(List<FeedDocument> selectedUris, boolean folderSource, String plotId) throws Exception {
        List<Photo> photos = new ArrayList<>();
        Map<Integer, Photo> latestByCamera = new HashMap<>();
        int imageCount = 0;
        int unmatchedCount = 0;
        for (FeedDocument document : selectedUris) {
            Uri fileUri = document.uri;
            String name = document.name == null ? getDisplayName(fileUri) : document.name;
            String mime = getContentResolver().getType(fileUri);
            if (name == null || !("image/jpeg".equalsIgnoreCase(mime)
                    || name.toLowerCase(Locale.US).endsWith(".jpg")
                    || name.toLowerCase(Locale.US).endsWith(".jpeg"))) continue;
            imageCount++;
            int camera = cameraNumber(name);
            if (camera == 0) camera = document.cameraHint;
            if (camera == 0) camera = knownTimestampCamera(name);
            if (camera == 0) unmatchedCount++;
            long size = getDocumentSize(fileUri);
            long modifiedAt = getDocumentLastModified(fileUri);
            String sourceKey = fileUri + "|" + size + "|" + modifiedAt;
            Photo photo;
            try {
                photo = readPhotoMetadata(fileUri, name, camera, modifiedAt, sourceKey);
            } catch (Exception unavailablePhoto) {
                continue;
            }
            photo.plotId = plotId;
            photos.add(photo);
            if (camera > 0) {
                Photo current = latestByCamera.get(camera);
                if (current == null || photoFreshness(photo) > photoFreshness(current)) latestByCamera.put(camera, photo);
            }
        }

        File latestDirectory = new File(new File(getFilesDir(), "latest_images"), plotId);
        if (!latestDirectory.exists() && !latestDirectory.mkdirs()) throw new IllegalStateException("Cannot create image cache");
        for (Map.Entry<Integer, Photo> entry : latestByCamera.entrySet()) {
            Photo photo = entry.getValue();
            copyToPrivateCache(photo.uri, new File(latestDirectory, cameraFileName(entry.getKey())));
        }

        JSONObject status = new JSONObject();
        status.put("connected", true);
        status.put("plotId", plotId);
        status.put("feedLocation", feedLocationLabel(plotId));
        Map<Integer, List<Photo>> photosByCamera = new HashMap<>();
        for (Photo photo : photos) {
            if (photo.camera > 0) {
                if (!photosByCamera.containsKey(photo.camera)) photosByCamera.put(photo.camera, new ArrayList<>());
                photosByCamera.get(photo.camera).add(photo);
            }
        }
        status.put("intervalMinutes", refreshIntervalMinutes);
        status.put("checkedAt", formatClock(System.currentTimeMillis()));
        status.put("unmatchedPhotoCount", unmatchedCount);
        int updatedCameraCount = latestByCamera.size();
        String statusText;
        if (imageCount == 0) {
            statusText = folderSource ? "Selected data feed folder is connected, but no JPEG photos were found."
                    : "No JPEG photos in the saved photo selection.";
        } else if (updatedCameraCount == 0) {
            statusText = "Found " + imageCount + " JPG photos, but none matched a camera. Add CCTV01–CCTV06, CAM1–CAM6, Q1–Q6, or Quadrant 1–6 to each filename or folder name.";
        } else {
            statusText = (folderSource ? "Scanned this plot’s data feed" : "Checked saved photos")
                    + " and updated " + updatedCameraCount + " quadrant feed(s) for this plot.";
            if (unmatchedCount > 0) statusText += " Ignored " + unmatchedCount
                    + " JPG photo(s) without a camera mapping. Add CCTV01–CCTV06, CAM1–CAM6, Q1–Q6, or Quadrant 1–6 to the filename or folder.";
            if (!folderSource) statusText += " Tap Connect to folder above to discover new uploads.";
        }
        status.put("statusText", statusText);
        JSONArray currentPhotos = new JSONArray();
        for (int camera = 1; camera <= 6; camera++) {
            Photo photo = latestByCamera.get(camera);
            if (photo == null) continue;
            JSONObject item = new JSONObject();
            item.put("camera", camera);
            item.put("name", photo.name);
            item.put("timestamp", photo.capturedAt);
            item.put("displayTime", photo.displayTime);
            item.put("cacheKey", Long.toString(photo.capturedAt) + "-" + Long.toString(System.currentTimeMillis()));
            currentPhotos.put(item);
        }
        status.put("currentPhotos", currentPhotos);

        JSONArray cameraStats = new JSONArray();
        for (int camera = 1; camera <= 6; camera++) {
            List<Photo> group = photosByCamera.get(camera);
            JSONObject item = new JSONObject();
            item.put("camera", camera);
            int count = group == null ? 0 : group.size();
            item.put("photoCount", count);
            item.put("similarPhotoCount", count > 1 ? count : 0);
            item.put("frameInsight", count > 1
                    ? "No distinct previous snapshot is available to compare."
                    : count == 1 ? "Waiting for a second snapshot to compare." : "No snapshots are available to compare.");
            item.put("frameExplanation", "The comparison estimates visible green pixels and brightness. It cannot confirm growth or diagnose plant health.");
            if (count == 0) {
                item.put("earliestTime", "—");
                item.put("latestTime", "—");
                item.put("earliestTimestamp", 0L);
                item.put("latestTimestamp", 0L);
                item.put("elapsedDays", 0);
                item.put("elapsedHours", 0);
                item.put("elapsedRemainder", "—");
            } else {
                Photo earliest = group.get(0);
                Photo latest = group.get(0);
                for (Photo photo : group) {
                    if (photo.capturedAt < earliest.capturedAt) earliest = photo;
                    if (photo.capturedAt > latest.capturedAt) latest = photo;
                }
                if (count > 1) {
                    List<Photo> ordered = new ArrayList<>(group);
                    ordered.sort((a, b) -> {
                        int byCapture = Long.compare(photoFreshness(b), photoFreshness(a));
                        return byCapture != 0 ? byCapture : String.valueOf(b.name).compareTo(String.valueOf(a.name));
                    });
                    Photo current = ordered.get(0);
                    Photo previous = null;
                    for (int i = 1; i < ordered.size(); i++) {
                        if (!ordered.get(i).uri.equals(current.uri)) {
                            previous = ordered.get(i);
                            break;
                        }
                    }
                    if (previous != null) {
                        JSONObject comparison = compareCameraFrames(current, previous);
                        item.put("frameInsight", comparison.optString("frameInsight"));
                        item.put("frameExplanation", comparison.optString("frameExplanation"));
                        item.put("frameCurrentGreenPercent", comparison.optDouble("currentGreenPercent", 0));
                        item.put("framePreviousGreenPercent", comparison.optDouble("previousGreenPercent", 0));
                        item.put("frameCurrentBrightnessPercent", comparison.optDouble("currentBrightnessPercent", 0));
                        item.put("framePreviousBrightnessPercent", comparison.optDouble("previousBrightnessPercent", 0));
                    }
                }
                long elapsed = Math.max(0, latest.capturedAt - earliest.capturedAt) / 1000;
                item.put("earliestTime", earliest.displayTime);
                item.put("latestTime", latest.displayTime);
                item.put("earliestTimestamp", earliest.capturedAt);
                item.put("latestTimestamp", latest.capturedAt);
                item.put("elapsedDays", elapsed / 86400);
                item.put("elapsedHours", (elapsed % 86400) / 3600);
                item.put("elapsedRemainder", ((elapsed % 3600) / 60) + " min " + (elapsed % 60) + " sec");
            }
            cameraStats.put(item);
        }
        status.put("cameraStats", cameraStats);
        status.put("timelineInterval", timelineInterval);
        status.put("timelinePhotos", makeTimelinePhotos(photosByCamera));
        publishStatus(status);
    }

    private JSONArray makeTimelinePhotos(Map<Integer, List<Photo>> photosByCamera) throws Exception {
        JSONArray timelines = new JSONArray();
        // Anchor the four buckets to the latest capture in the selected plot's configured
        // Drive folder, rather than today's date. Historical Drive photos must remain visible.
        long newestCapture = 0L;
        for (List<Photo> cameraPhotos : photosByCamera.values()) {
            if (cameraPhotos == null) continue;
            for (Photo photo : cameraPhotos) {
                if (photo.capturedAt > newestCapture && photo.capturedAt <= System.currentTimeMillis() + 86_400_000L)
                    newestCapture = photo.capturedAt;
            }
        }
        long anchorTime = newestCapture > 0 ? newestCapture : System.currentTimeMillis();
        List<TimelineBuckets.Period> periods = TimelineBuckets.latestFour(timelineInterval, anchorTime);
        String periodName = "daily".equals(timelineInterval) ? "Day" : "monthly".equals(timelineInterval) ? "Month"
                : "yearly".equals(timelineInterval) ? "Year" : "Week";
        for (int camera = 1; camera <= 6; camera++) {
            List<Photo> group = photosByCamera.get(camera);
            JSONObject timeline = new JSONObject();
            timeline.put("camera", camera);
            timeline.put("interval", timelineInterval);
            JSONArray slots = new JSONArray();
            List<Long> captureTimes = new ArrayList<>();
            if (group != null) for (Photo photo : group) captureTimes.add(photo.capturedAt);
            int[] selectedPhoto = TimelineBuckets.earliestPhotoIndices(periods, captureTimes);
            timeline.put("baselineDate", formatDay(periods.get(0).startMillis));
            for (int slotIndex = 0; slotIndex < periods.size(); slotIndex++) {
                TimelineBuckets.Period period = periods.get(slotIndex);
                String startDate = formatDay(period.startMillis);
                JSONObject slot = new JSONObject();
                slot.put("day", period.index);
                slot.put("label", periodName + " " + period.index + " (start " + startDate + ")");
                slot.put("expectedDate", startDate);
                Photo best = selectedPhoto[slotIndex] >= 0 ? group.get(selectedPhoto[slotIndex]) : null;
                if (best != null && best.historyFile == null) best.historyFile = archivePhoto(best);
                slot.put("available", best != null && best.historyFile != null);
                if (best != null && best.historyFile != null) {
                    slot.put("timestamp", best.capturedAt);
                    slot.put("capturedAt", best.displayTime);
                    slot.put("imageUrl", "https://appassets.androidplatform.net/assets/history/" + safePlotId(best.plotId) + "/" + best.historyFile);
                }
                slots.put(slot);
            }
            timeline.put("slots", slots);
            timelines.put(timeline);
        }
        return timelines;
    }

    private long startOfLocalDay(long timestamp) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"), Locale.US);
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private String formatDay(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        return format.format(new Date(timestamp));
    }

    private String getDisplayName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (column >= 0 && !cursor.isNull(column)) return cursor.getString(column);
            }
        } catch (Exception ignored) { }
        String segment = uri.getLastPathSegment();
        return segment == null ? null : Uri.decode(segment);
    }

    private long getDocumentSize(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.SIZE}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (column >= 0 && !cursor.isNull(column)) return cursor.getLong(column);
            }
        } catch (Exception ignored) { }
        return 0L;
    }

    private long getDocumentLastModified(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri,
                new String[]{"last_modified"}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex("last_modified");
                if (column >= 0 && !cursor.isNull(column)) {
                    long value = cursor.getLong(column);
                    // Some Android document providers expose epoch seconds rather than milliseconds.
                    return value > 0 && value < 100_000_000_000L ? value * 1000L : value;
                }
            }
        } catch (Exception ignored) { }
        return 0L;
    }

    private Photo readPhotoMetadata(Uri uri, String name, int camera, long modified, String sourceKey) throws Exception {
        Photo photo = new Photo();
        photo.uri = uri;
        photo.name = name;
        photo.camera = camera;
        photo.modifiedAt = modified;
        photo.sourceKey = sourceKey;
        photo.capturedAt = modified > 0 ? modified : System.currentTimeMillis();
        boolean hasExifCaptureTime = false;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input != null) {
                ExifInterface exif = new ExifInterface(input);
                String raw = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
                if (raw == null) raw = exif.getAttribute(ExifInterface.TAG_DATETIME);
                String offset = exif.getAttribute("OffsetTimeOriginal");
                if (raw != null) {
                    SimpleDateFormat parser = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US);
                    parser.setTimeZone(TimeZone.getTimeZone(offset == null ? "Asia/Kuala_Lumpur" : "GMT" + offset));
                    Date parsed = parser.parse(raw);
                    if (parsed != null) {
                        photo.capturedAt = parsed.getTime();
                        hasExifCaptureTime = true;
                    }
                }
            }
        } catch (ParseException ignored) { }
        if (!hasExifCaptureTime) {
            long fromName = timestampFromFileName(name);
            if (fromName > 0) photo.capturedAt = fromName;
            else if (modified <= 0) {
                // Legacy camera-only names have no embedded time. Use the known baseline only
                // when the provider also supplied no usable modification timestamp.
                long knownTime = knownCameraTimestamp(name);
                if (knownTime > 0) photo.capturedAt = knownTime;
            }
        }
        photo.displayTime = formatDateTime(photo.capturedAt);
        return photo;
    }

    private String archivePhoto(Photo photo) {
        String base = String.format(Locale.US, "cctv_%02d_%d_%s.jpg", photo.camera,
                photo.capturedAt, Integer.toHexString(photo.sourceKey.hashCode()));
        File directory = new File(new File(getFilesDir(), "photo_history"), safePlotId(photo.plotId));
        File destination = new File(directory, base);
        if (destination.isFile() && destination.length() > 0) return base;
        Bitmap bitmap = null;
        Bitmap oriented = null;
        try {
            if (!directory.exists() && !directory.mkdirs()) return null;
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream input = getContentResolver().openInputStream(photo.uri)) {
                if (input == null) return null;
                BitmapFactory.decodeStream(input, null, bounds);
            }
            BitmapFactory.Options decode = new BitmapFactory.Options();
            decode.inSampleSize = 1;
            while (bounds.outWidth / decode.inSampleSize > 1440
                    || bounds.outHeight / decode.inSampleSize > 1440) decode.inSampleSize *= 2;
            try (InputStream input = getContentResolver().openInputStream(photo.uri)) {
                if (input == null) return null;
                bitmap = BitmapFactory.decodeStream(input, null, decode);
            }
            if (bitmap == null) return null;
            int orientation = ExifInterface.ORIENTATION_NORMAL;
            try (InputStream input = getContentResolver().openInputStream(photo.uri)) {
                if (input != null) orientation = new ExifInterface(input).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            }
            float rotation = orientation == ExifInterface.ORIENTATION_ROTATE_90 ? 90f
                    : orientation == ExifInterface.ORIENTATION_ROTATE_180 ? 180f
                    : orientation == ExifInterface.ORIENTATION_ROTATE_270 ? 270f : 0f;
            if (rotation != 0f) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            } else oriented = bitmap;
            try (FileOutputStream output = new FileOutputStream(destination)) {
                if (!oriented.compress(Bitmap.CompressFormat.JPEG, 82, output)) return null;
                output.flush();
            }
            return destination.length() > 0 ? base : null;
        } catch (Exception ignored) {
            if (destination.exists()) destination.delete();
            return null;
        } finally {
            if (oriented != null && oriented != bitmap) oriented.recycle();
            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
        }
    }

    private long timestampFromFileName(String name) {
        String file = name == null ? "" : name;
        Matcher matcher = Pattern.compile("(\\d{8})[_-]?(\\d{6})").matcher(file);
        String capture;
        if (matcher.find()) {
            capture = matcher.group(1) + matcher.group(2);
        } else {
            // Older Plot01_demo uploads used YYYYMMDDHH without minutes/seconds.
            Matcher hourOnly = Pattern.compile("(\\d{8})[_-]?(\\d{2})(?!\\d)").matcher(file);
            if (!hourOnly.find()) return 0L;
            capture = hourOnly.group(1) + hourOnly.group(2) + "0000";
        }
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            parser.setLenient(false);
            parser.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
            Date parsed = parser.parse(capture);
            return parsed == null ? 0L : parsed.getTime();
        } catch (ParseException ignored) { return 0L; }
    }

    private long knownCameraTimestamp(String name) {
        Matcher matcher = CAMERA_FILE.matcher(name == null ? "" : name);
        if (!matcher.matches()) return 0L;
        try {
            int camera = Integer.parseInt(matcher.group(1));
            String[] captureTimes = {"20261008_175252", "20261008_175312", "20261008_175325",
                    "20261008_175337", "20261008_175346", "20261008_175355"};
            return timestampFromFileName(captureTimes[camera - 1]);
        } catch (Exception ignored) { return 0L; }
    }

    private void copyToPrivateCache(Uri source, File destination) throws Exception {
        try (InputStream input = getContentResolver().openInputStream(source);
             FileOutputStream output = new FileOutputStream(destination)) {
            if (input == null) throw new IllegalStateException("Image stream unavailable");
            byte[] buffer = new byte[32 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            output.flush();
        }
    }

    private int knownTimestampCamera(String name) {
        String normalized = name == null ? "" : name.replaceAll("(?i)\\.jpe?g$", "");
        switch (normalized) {
            case "20261008_184621": return 1;
            case "20261008_184637": return 2;
            case "20261008_184727": return 3;
            case "20261008_184644": return 4;
            case "20261008_184652": return 5;
            case "20261008_184705": return 6;
            case "20261009_104256": return 1;
            case "20261009_104315": return 2;
            case "20261009_104330": return 4;
            case "20261009_104341": return 6;
            case "20261009_104359": return 5;
            case "20261009_104416": return 3;
            default: return 0;
        }
    }

    private int cameraNumber(String fileName) {
        String candidate = fileName == null ? "" : fileName;
        Matcher matcher = CAMERA_FILE.matcher(candidate);
        if (matcher.matches()) {
            try { return Integer.parseInt(matcher.group(1)); }
            catch (NumberFormatException ignored) { return 0; }
        }
        matcher = CAMERA_LABEL.matcher(candidate);
        if (matcher.find()) {
            try { return Integer.parseInt(matcher.group(1)); }
            catch (NumberFormatException ignored) { return 0; }
        }
        String normalized = candidate.replaceAll("(?i)\\.jpe?g$", "");
        switch (normalized) {
            // These six timestamp-named uploads were visually matched to the same six plant quadrants.
            case "20261008_184621": return 1; // Andong merah / Cordyline
            case "20261008_184637": return 2; // Citrus
            case "20261008_184727": return 3; // Belimbing buluh
            case "20261008_184644": return 4; // Pandan
            case "20261008_184652": return 5; // Curcuma family, tentative
            case "20261008_184705": return 6; // Cassava
            // Visually matched 9 Oct 2026 timestamp-only uploads to the established quadrants.
            case "20261009_104256": return 1; // Andong merah / Cordyline
            case "20261009_104315": return 2; // Citrus
            case "20261009_104330": return 4; // Pandan
            case "20261009_104341": return 6; // Cassava
            case "20261009_104359": return 5; // Curcuma family, tentative
            case "20261009_104416": return 3; // Bilimbi
            default: return 0;
        }
    }

    private String cameraFileName(int camera) {
        return String.format(Locale.US, "cctv_%02d.jpg", camera);
    }

    private String sourceLabel(Photo photo) {
        return photo.camera > 0 ? "CCTV " + String.format(Locale.US, "%02d", photo.camera) : photo.name;
    }

    private String formatDateTime(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy · h:mm:ss a 'MYT'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        return format.format(new Date(timestamp));
    }

    private String formatClock(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("h:mm a 'MYT'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        return format.format(new Date(timestamp));
    }

    private JSONObject makeStatus(String message) {
        JSONObject status = new JSONObject();
        try {
            status.put("connected", false);
            status.put("cameraStats", new JSONArray());
            status.put("statusText", message);
            status.put("intervalMinutes", refreshIntervalMinutes);
            String activePlot = safePlotId(getPrefs().getString(PREF_ACTIVE_PLOT_ID, DEFAULT_PLOT_ID));
            status.put("plotId", activePlot);
            status.put("feedLocation", feedLocationLabel(activePlot));
        } catch (Exception ignored) { }
        return status;
    }

    private void publishStatus(JSONObject status) {
        final String json = status.toString();
        runOnUiThread(() -> {
            if (!pageReady || web == null) pendingPageUpdate = json;
            else sendToPage(json);
        });
    }

    private void sendToPage(String json) {
        if (web != null) web.evaluateJavascript("window.onSnapshotScan && window.onSnapshotScan(" + json + ");", null);
    }
}
