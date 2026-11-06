const fs = require('node:fs');
const assert = require('node:assert/strict');

const java = fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java', 'utf8');
assert.match(java, /private boolean snapshotFeedCanCapture\(int camera\)[\s\S]*?frame\.getVisibility\(\) == View\.VISIBLE[\s\S]*?player\.isPlaying\(\)/,
  'capture readiness is tied to an active, visible, playing feed');
assert.match(java, /private long nextSnapshotCheckDelay\(int camera\)[\s\S]*?if \(!snapshotFeedCanCapture\(camera\)\) return 30_000L;[\s\S]*?if \(!isFeedFolderReady\(plotId\)\) return 60_000L;[\s\S]*?remaining<=0 \? 5000L : remaining;/,
  'inactive feed and missing folder retry, overdue captures are attempted promptly, and future captures track last success');
assert.match(java, /private final Runnable cctv01SnapshotTicker[\s\S]*?checkIpWebcamBeforeSnapshot\(1\);[\s\S]*?postDelayed\(this, nextSnapshotCheckDelay\(1\)\)/,
  'CCTV01 recurring timer retries based on capture success/due time');
assert.match(java, /private final Runnable cctv02SnapshotTicker[\s\S]*?postDelayed\(this, nextSnapshotCheckDelay\(2\)\)/,
  'CCTV02 keeps due-aware retry scheduling even while VLC is temporarily absent');
assert.match(java, /private final Runnable cctv02SnapshotTicker[\s\S]*?if \(!activityResumed\) return;[\s\S]*?if \(ipWebcam2VlcPlayer != null && ipWebcam2VlcLayout != null\) checkIpWebcamBeforeSnapshot\(2\);[\s\S]*?postDelayed\(this, nextSnapshotCheckDelay\(2\)\)/,
  'CCTV02 reschedules its ticker instead of abandoning it when its player is temporarily absent');
assert.match(java, /private final Runnable cctv03SnapshotTicker[\s\S]*?checkIpWebcamBeforeSnapshot\(3\);[\s\S]*?postDelayed\(this, nextSnapshotCheckDelay\(3\)\)/,
  'CCTV03 recurring timer retries based on capture success/due time');
assert.match(java, /private final Runnable cctv03SnapshotTicker[\s\S]*?if \(ipWebcam3VlcPlayer != null && ipWebcam3VlcLayout != null\) checkIpWebcamBeforeSnapshot\(3\);[\s\S]*?postDelayed\(this, nextSnapshotCheckDelay\(3\)\)/,
  'CCTV03 reschedules its ticker while its player is temporarily absent');
assert.match(java, /mainHandler\.postDelayed\(cctv01SnapshotTicker, nextSnapshotCheckDelay\(1\)\)/,
  'starting or changing the interval for CCTV01 does not reset a full interval when overdue');
assert.match(java, /mainHandler\.postDelayed\(cctv03SnapshotTicker, nextSnapshotCheckDelay\(3\)\)/,
  'starting or changing the interval for CCTV03 respects the saved due time');
assert.match(java, /setCctvSnapshotIntervalMinutes\(double minutes\)[\s\S]*?putLong\(PREF_CCTV_SNAPSHOT_INTERVAL_MILLIS, intervalMillis\)\.apply\(\)[\s\S]*?if \(activityResumed\) \{[\s\S]*?postDelayed\(cctv01SnapshotTicker[\s\S]*?postDelayed\(cctv02SnapshotTicker[\s\S]*?postDelayed\(cctv03SnapshotTicker/,
  'interval changes persist and restart checks for every camera even before VLC reconnects');
assert.match(java, /saveFeedPhotoToDrive\(null, frame, plotId, camera, false\)[\s\S]*?putLong\(snapshotLastSuccessKey\(camera,plotId\),System\.currentTimeMillis\(\)\)/,
  'the next interval is measured from a successful Drive save');
console.log('PASS: CCTV snapshot timers are due-aware and retry after inactive feeds, missing folder access, or failed saves.');
