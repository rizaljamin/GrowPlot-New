const fs = require('node:fs');
const assert = require('node:assert/strict');

const html = fs.readFileSync('app/src/main/assets/index.html', 'utf8');
const login = html.match(/<form id="loginForm">([\s\S]*?)<\/form>/)?.[1] || '';
const create = html.match(/<form id="createForm">([\s\S]*?)<\/form>/)?.[1] || '';
const inputs = (form) => [...form.matchAll(/<input\b[^>]*\bname="([^"]+)"/g)].map((m) => m[1]);
assert.deepEqual(inputs(login), ['email', 'password'], 'login uses email and password only');
assert.deepEqual(inputs(create), ['email', 'password', 'confirmPassword'], 'registration keeps confirm password and removes username');
assert.match(html, /id="googleRegisterBtn"/, 'registration offers Google sign-in');

const account = fs.readFileSync('app/src/main/assets/account-login.js', 'utf8');
assert.match(account, /form\.elements\.password\.value!==form\.elements\.confirmPassword\.value/, 'registration checks password confirmation');
assert.match(account, /api\.startGoogleSignIn\(\)/, 'Google button calls the native Firebase bridge');
assert.match(account, /className='growplot-password-toggle'/, 'password fields get eye visibility controls');
assert.match(account, /id='forgotPasswordBtn'/, 'login has a password-reset action');
assert.match(account, /This ID is already a registered member/, 'duplicate email shows the requested guidance');
assert.match(account, /registrationForgotPasswordBtn/, 'duplicate-email guidance offers password reset');
assert.match(account, /Registration date/,'My Account displays registration date');
assert.match(account, /Last login/,'My Account displays last login');
assert.match(account, /Payment method/,'My Account has a payment-method placeholder');
assert.match(account, /callback\(saved,response\.error\|\|''\)/, 'cloud-save completion is reported to onboarding');

// Build 26 behavior: an authenticated Firebase session survives a Firestore profile error.
assert.match(account, /response\.action==='profile'&&currentUser/, 'profile failure is handled separately after authentication');
assert.match(account, /Signed in, but your Firebase cloud profile could not be loaded/, 'profile failure does not look like bad credentials');
assert.match(account, /response\.action==='accountWarning'/, 'best-effort profile-write warnings stay separate from sign-in');

const onboarding = fs.readFileSync('app/src/main/assets/new-user-onboarding.js', 'utf8');
assert.match(onboarding, /document\.createElement\('dialog'\)/, 'first-plot prompt is a native modal dialog');
assert.match(onboarding, /prompt\.showModal\(\)/, 'first-plot prompt enters the modal top layer');
assert.match(onboarding, /function openPlotManagement\(\)/, 'Create Plot has a dedicated Plot Management route');
assert.match(onboarding, /settings\.click\(\)/, 'Create Plot opens Settings/Plot Management');
assert.match(onboarding, /function showDemoHome\(\)/, 'Home has a dedicated route to demo home');
assert.match(onboarding, /GrowPlotShowDemoHome/, 'Home selects and returns to the demo screen');
assert.match(onboarding, /hidePrompt\(\)/, 'both actions close the first-plot prompt before navigating');

const bridge = fs.readFileSync('app/src/main/java/com/growplot/farm/AccountApiBridge.java', 'utf8');
assert.match(bridge, /@JavascriptInterface public void startGoogleSignIn\(\)/, 'Android bridge exposes Google sign-in');
assert.match(bridge, /GetSignInWithGoogleOption/, 'explicit Google button uses direct Google sign-in');
assert.match(bridge, /GoogleAuthProvider\.getCredential\(google\.getIdToken\(\),null\)/, 'Google ID token is exchanged for Firebase Auth credentials');
assert.match(bridge, /auth\.signInWithEmailAndPassword\(email,password\)/, 'email/password login submits the entered credentials to Firebase');
assert.match(bridge, /if\("passwordReset"\.equals\(action\)\)/, 'native Firebase bridge handles password reset');
assert.match(bridge, /duplicateEmail/, 'native registration identifies a duplicate email');
assert.match(bridge, /registrationDate/,'Firebase account metadata includes registration date');
assert.match(bridge, /lastLoginDate/,'Firebase account metadata includes last login date');

// Auth success is returned before the optional Firestore profile read/write.
const authMethod = bridge.slice(bridge.indexOf('private void accountResponse('), bridge.indexOf('private void reportAccountWarning('));
assert.ok(authMethod.includes('respond(action,true'), 'Firebase-authenticated user is returned as a successful login');
assert.doesNotMatch(authMethod, /FirebaseFirestore|\.get\(\)/, 'auth response does not wait for Firestore');
assert.match(bridge, /accountResponse\("googleSignIn",user\);[\s\S]*?\.get\(\)/, 'Google sign-in responds before optional Firestore profile setup');
assert.match(bridge, /reportAccountWarning\("Account created, but the cloud profile could not be saved:/, 'registration profile failures are reported as warnings');

console.log('PASS: Firebase auth isolation, duplicate-email recovery, account details, Google sign-in, and first-plot modal.');
