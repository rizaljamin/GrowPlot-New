const fs = require('node:fs');
const assert = require('node:assert/strict');

const java = fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java', 'utf8');
const html = fs.readFileSync('app/src/main/assets/index.html', 'utf8');
assert.match(java, /private boolean snapshotFeedCanCapture\(int camera\)[\s\S]*?frame\.getVisibility\(\) == View\.VISIBLE[\s\S]*?cctvPlaybackActive\[camera - 1\]/,
  'capture readiness is tied to an active, visible feed reported by VLC events');
assert.match(java, /private void updateCctvPlaybackFromEvent[\s\S]*?Event\.Playing[\s\S]*?Event\.EncounteredError/,
  'VLC playback events update the cached stream state');
assert.doesNotMatch(java, /isIpWebcamFrameAvailable|player\.isPlaying\(\)/,
  'status and snapshot checks do not poll the player or open a duplicate MJPEG probe');
assert.doesNotMatch(html, /ipWebcamStreamUrl|\/videofeed/,
  'the WebView does not open a second live MJPEG connection');
assert.match(java, /private long nextSnapshotCheckDelay\(int camera\)[\s\S]*?if \(!snapshotFeedCanCapture\(camera\)\) return 30_000L;[\s\S]*?if \(!isFeedFolderReady\(plotId\)\) return 60_000L;[\s\S]*?remaining<=0 \? 5000L : remaining;/,
  'inactive feed and missing folder retry, overdue captures are attempted promptly, and future captures track last success');
assert.match(java, /private final Runnable cctv01SnapshotTicker[\s\S]*?checkIpWebcamBeforeSnapshot\(1\);[\s\S]*?postDelayed\(this, nextSnapshotCheckDelay\(1\)\)/,
  'CCTV01 recurring timer retries based on capture success/due time');
assert.match(java, /private final Runnable cctv02SnapshotTicker[\s\S]*?postDelayed\(this, nextSnapshotCheckDelay\(2\)\)/,
  'CCTV02 keeps due-aware retry scheduling');
assert.match(java, /private final Runnable cctv03SnapshotTicker[\s\S]*?checkIpWebcamBeforeSnapshot\(3\);[\s\S]*?postDelayed\(this, nextSnapshotCheckDelay\(3\)\)/,
  'CCTV03 recurring timer retries based on capture success/due time');
assert.match(java, /mainHandler\.postDelayed\(cctv01SnapshotTicker, nextSnapshotCheckDelay\(1\)\)/,
  'starting or changing the interval for CCTV01 does not reset a full interval when overdue');
assert.match(java, /mainHandler\.postDelayed\(cctv03SnapshotTicker, nextSnapshotCheckDelay\(3\)\)/,
  'starting or changing the interval for CCTV03 respects the saved due time');
assert.match(java, /saveFeedPhotoToDrive\(null, frame, plotId, camera, false\)[\s\S]*?putLong\(snapshotLastSuccessKey\(camera,plotId\),System\.currentTimeMillis\(\)\)/,
  'the next interval is measured from a successful Drive save');
console.log('PASS: CCTV snapshot timers are due-aware and retry after inactive feeds, missing folder access, or failed saves.');
