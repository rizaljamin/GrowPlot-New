const fs = require('node:fs');
const assert = require('node:assert/strict');

const html = fs.readFileSync('app/src/main/assets/index.html', 'utf8');
const login = html.match(/<form id="loginForm">([\s\S]*?)<\/form>/)?.[1] || '';
const create = html.match(/<form id="createForm">([\s\S]*?)<\/form>/)?.[1] || '';
const inputs = (form) => [...form.matchAll(/<input\b[^>]*\bname="([^"]+)"/g)].map((m) => m[1]);
assert.deepEqual(inputs(login), ['email', 'password'], 'login uses email and password only');
assert.deepEqual(inputs(create), ['email', 'password', 'confirmPassword'], 'registration keeps confirm password and removes username');
assert.match(html, /id="googleRegisterBtn"/, 'registration offers Google sign-in');
assert.match(html, /id="adminTestLoginBtn"[^>]*hidden/, 'admin test-login button starts hidden until native debug-build confirmation');

const account = fs.readFileSync('app/src/main/assets/account-login.js', 'utf8');
assert.match(account, /form\.elements\.password\.value!==form\.elements\.confirmPassword\.value/, 'registration checks password confirmation');
assert.match(account, /api\.startGoogleSignIn\(\)/, 'Google button calls the native Firebase bridge');
assert.match(account, /className='growplot-password-toggle'/, 'password fields get eye visibility controls');
assert.match(account, /wrapper\.appendChild\(toggle\)/, 'eye visibility controls are placed inside the password field');
assert.match(account, /id='forgotPasswordBtn'/, 'login has a password-reset action');
assert.match(account, /api\.isDevelopmentBuild\(\)/, 'admin test-login button is revealed only for a native development build');
assert.match(account, /verifyLocalAdminTestLogin\(\)/, 'admin test login asks the native bridge to validate local development access');
assert.match(account, /GrowPlotAdminVerified\(\)/, 'successful local test login opens the existing local demo-admin session');
assert.match(account, /This ID is already a registered member/, 'duplicate email shows the requested guidance');
assert.match(account, /registrationForgotPasswordBtn/, 'duplicate-email guidance offers password reset');
assert.match(account, /Registration date/,'My Account displays registration date');
assert.match(account, /id='growplotUserManagementCard'/, 'My Account content is contained in a compact card');
assert.match(account, /max-width:480px/, 'My Account width is constrained to the GrowPlot viewport');
assert.match(account, /max-height:calc\(100vh - 32px\);overflow:auto/, 'My Account fits short screens and scrolls internally');
assert.match(account, /Last login/,'My Account displays last login');
assert.match(account, /Payment method/,'My Account has a payment-method placeholder');
assert.match(account, /callback\(saved,response\.error\|\|''\)/, 'cloud-save completion is reported to onboarding');

// Build 26 behavior: an authenticated Firebase session survives a Firestore profile error.
assert.match(account, /response\.action==='profile'&&currentUser/, 'profile failure is handled separately after authentication');
assert.match(account, /Signed in, but your Firebase cloud profile could not be loaded/, 'profile failure does not look like bad credentials');
assert.match(account, /response\.action==='accountWarning'/, 'best-effort profile-write warnings stay separate from sign-in');

const onboarding = fs.readFileSync('app/src/main/assets/new-user-onboarding.js', 'utf8');
assert.match(onboarding, /id='growplotAddPlotBtn'/, 'Home displays the Add Plot button');
assert.match(onboarding, /Add Plot/, 'Add Plot uses the requested label');
assert.match(onboarding, /action\.addEventListener\('click',openPlotSetup\)/, 'Add Plot opens plot setup');
assert.match(onboarding, /manageFarm\.click\(\)/, 'Add Plot opens Manage Farm instead of Settings');
assert.match(onboarding, /plotTab\.click\(\)/, 'Add Plot selects the Plot tab');
assert.match(onboarding, /dismissLegacyPrompt\(\)/, 'obsolete onboarding popup is removed');
assert.doesNotMatch(onboarding, /document\.createElement\('dialog'\)|\.showModal\(\)/, 'onboarding does not create a blocking modal');
assert.match(account, /function closeUserPage\(\)/, 'My Account has explicit cleanup');
assert.match(account, /back\.onclick=\(\)=>\{closeUserPage\(\);goHome\(\);\}/, 'Back removes the My Account overlay');

const bridge = fs.readFileSync('app/src/main/java/com/growplot/farm/AccountApiBridge.java', 'utf8');
assert.match(bridge, /@JavascriptInterface public void startGoogleSignIn\(\)/, 'Android bridge exposes Google sign-in');
assert.match(bridge, /@JavascriptInterface public boolean isDevelopmentBuild\(\)\{return BuildConfig\.DEBUG;\}/, 'admin test-login availability follows the Android debug build flag');
assert.match(bridge, /@JavascriptInterface public boolean verifyLocalAdminTestLogin\(\)\{return BuildConfig\.DEBUG;\}/, 'local admin test access is available only in development builds');
assert.match(bridge, /GetSignInWithGoogleOption/, 'explicit Google button uses direct Google sign-in');
assert.match(bridge, /GoogleAuthProvider\.getCredential\(google\.getIdToken\(\),null\)/, 'Google ID token is exchanged for Firebase Auth credentials');
assert.match(bridge, /auth\.signInWithEmailAndPassword\(email,password\)/, 'email/password login submits the entered credentials to Firebase');
assert.match(bridge, /if\("passwordReset"\.equals\(action\)\)/, 'native Firebase bridge handles password reset');
assert.match(bridge, /duplicateEmail/, 'native registration identifies a duplicate email');
assert.match(bridge, /registrationDate/,'Firebase account metadata includes registration date');
assert.match(bridge, /lastLoginDate/,'Firebase account metadata includes last login date');

// Google sign-in is not returned to onboarding until a GrowPlot user record is present.
const authMethod = bridge.slice(bridge.indexOf('private void accountResponse('), bridge.indexOf('private void reportAccountWarning('));
assert.ok(authMethod.includes('respond(action,true'), 'Firebase-authenticated user is returned as a successful login');
assert.doesNotMatch(authMethod, /FirebaseFirestore|\.get\(\)/, 'auth response does not wait for Firestore');
const googleProfile = bridge.slice(bridge.indexOf('private void ensureGoogleProfile('), bridge.indexOf('@JavascriptInterface public void request('));
assert.match(googleProfile, /db\.runTransaction\(transaction->\{\s*if\(transaction\.get\(profileRef\)\.exists\(\)\)return false;/, 'Google registration atomically checks for an existing GrowPlot user profile');
assert.match(googleProfile, /transaction\.set\(profileRef,profile\)/, 'a missing Google profile is created in the registration transaction');
assert.match(googleProfile, /profile\.put\("role","USER"\)/, 'new Google profiles receive the standard user role');
assert.match(googleProfile, /profile\.put\("authProvider","google\.com"\)/, 'new profiles record Google as the sign-in provider');
assert.match(googleProfile, /profile\.put\("displayName"/, 'new profiles include the Google display name');
assert.match(googleProfile, /\}\)\.addOnSuccessListener\(created->accountResponse\("googleSignIn",user\)\)/, 'Google login is returned only after the profile check/create transaction succeeds');
assert.match(googleProfile, /addOnFailureListener\(e->failGoogleUserRegistration/, 'profile transaction failures block incomplete Google registration');
assert.match(googleProfile, /auth\.signOut\(\)/, 'failed profile registration clears the partial Firebase session');
assert.match(bridge, /reportAccountWarning\("Account created, but the cloud profile could not be saved:/, 'registration profile failures are reported as warnings');

console.log('PASS: Firebase auth isolation, duplicate-email recovery, account details, Google sign-in, and nonmodal Add Plot onboarding.');
