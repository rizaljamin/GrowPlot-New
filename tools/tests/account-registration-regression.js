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
assert.match(bridge, /new GetSignInWithGoogleOption\.Builder\(webClientId\)\.build\(\)/, 'explicit Google button uses the Sign in with Google button flow');
assert.doesNotMatch(bridge, /new GetGoogleIdOption\.Builder/, 'explicit Google button does not use the account-sheet option');
assert.match(bridge, /e instanceof NoCredentialException/, 'no-credential errors receive an actionable message');
assert.match(bridge, /add or select a Google account in the sign-in window/i, 'no-credential guidance explains how to retry');
assert.match(bridge, /GoogleAuthProvider\.getCredential\(google\.getIdToken\(\),null\)/, 'Google ID token is exchanged for Firebase Auth credentials');
assert.match(bridge, /ensureGoogleProfile\(user\)/, 'first Google sign-in creates a Firestore user profile');

// Firebase authentication success is independent from Firestore profile availability.
const authMethod = bridge.slice(bridge.indexOf('private void accountResponse('), bridge.indexOf('private void reportAccountWarning('));
assert.ok(authMethod.includes('respond(action,true'), 'Firebase-authenticated user is returned as a successful login');
assert.doesNotMatch(authMethod, /FirebaseFirestore|\.get\(\)/, 'login response does not wait for a Firestore read');
assert.match(bridge, /accountResponse\("googleSignIn",user\);[\s\S]*?\.get\(\)/, 'Google sign-in responds before optional Firestore profile setup');
assert.match(bridge, /reportAccountWarning\("Account created, but the cloud profile could not be saved:/, 'registration reports profile-save failure without converting authentication to failure');
assert.match(bridge, /reportAccountWarning\("Signed in with Google, but the cloud profile could not be checked:/, 'Google profile-read failure is reported separately from sign-in');

assert.match(account, /response\.action==='accountWarning'/, 'cloud-profile warnings are presented separately from auth failures');
assert.match(account, /response\.action==='profile'&&currentUser/, 'a profile read failure does not reject an authenticated session');
assert.match(account, /saveNotice\('Signed in, but your Firebase cloud profile could not be loaded:/, 'profile failure explains that sign-in succeeded but cloud data is unavailable');
console.log('PASS: email registration fields, Google account path, login-screen first-plot prompt, and Firebase save confirmation.');
