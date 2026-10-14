const fs = require('node:fs');
const assert = require('node:assert/strict');

const html = fs.readFileSync('app/src/main/assets/index.html', 'utf8');
const login = html.match(/<form id="loginForm">([\s\S]*?)<\/form>/)?.[1] || '';
const create = html.match(/<form id="createForm">([\s\S]*?)<\/form>/)?.[1] || '';
const inputs = (form) => [...form.matchAll(/<input\b[^>]*\bname="([^"]+)"/g)].map((m) => m[1]);
assert.deepEqual(inputs(login), ['email', 'password'], 'login uses email and password only');
assert.deepEqual(inputs(create), ['email', 'password', 'confirmPassword'], 'registration retains email, password and confirm password, with no username');
assert.match(html, /id="googleRegisterBtn"/, 'registration offers Google sign-in');

const account = fs.readFileSync('app/src/main/assets/account-login.js', 'utf8');
assert.match(account, /form\.elements\.password\.value!==form\.elements\.confirmPassword\.value/, 'registration checks password confirmation');
assert.match(account, /api\.startGoogleSignIn\(\)/, 'Google button calls the native Firebase bridge');
assert.match(account, /response\.action==='saveProfile'/, 'Firebase save response is handled');
assert.match(account, /callback\(saved,response\.error\|\|''\)/, 'cloud-save completion is reported to onboarding');

const onboarding = fs.readFileSync('app/src/main/assets/new-user-onboarding.js', 'utf8');
assert.match(onboarding, /card\.appendChild\(view\)/, 'first-plot prompt is inside the login card');
assert.doesNotMatch(onboarding, /insertBefore\(section,dash\)|mainDashboard/, 'first-plot prompt is not inserted on the main dashboard');
assert.match(onboarding, /GrowPlotSaveCloudProfile\(\(ok,error\)=>/, 'first plot waits for a Firebase save result');

const bridge = fs.readFileSync('app/src/main/java/com/growplot/farm/AccountApiBridge.java', 'utf8');
assert.match(bridge, /@JavascriptInterface public void startGoogleSignIn\(\)/, 'Android bridge exposes Google sign-in');
assert.match(bridge, /GoogleAuthProvider\.getCredential\(google\.getIdToken\(\),null\)/, 'Google ID token is exchanged for Firebase Auth credentials');
assert.match(bridge, /ensureGoogleProfile\(user\)/, 'first Google sign-in creates a Firestore user profile');
console.log('PASS: email registration fields, Google account path, login-screen first-plot prompt, and Firebase save confirmation.');
