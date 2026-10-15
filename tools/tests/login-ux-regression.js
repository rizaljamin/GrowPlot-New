const fs = require('node:fs');
const assert = require('node:assert/strict');

const account = fs.readFileSync('app/src/main/assets/account-login.js', 'utf8');
assert.match(account, /function addPasswordVisibilityToggles\(\)/, 'password fields receive visibility controls');
assert.match(account, /field\.type=visible\?'text':'password'/, 'visibility control switches only the selected password field');
assert.match(account, /aria-pressed/, 'visibility state is accessible');
assert.match(account, /const eyeOpen='<svg viewBox="0 0 24 24"/, 'password visibility uses a familiar eye icon');
assert.match(account, /toggle\.setAttribute\('aria-label','Show '/, 'eye icon has an accessible action label');
assert.match(account, /toggle\.innerHTML=visible\?eyeClosed:eyeOpen/, 'eye icon reflects whether the password is visible');
assert.match(account, /id='forgotPasswordBtn'/, 'login exposes a password-reset action');
assert.match(account, /request\('passwordReset',\{email:email\.value\.trim\(\)\}\)/, 'reset action uses the entered email');
assert.match(account, /If a GrowPlot account matches that address/, 'reset feedback does not confirm whether an account exists');
assert.match(account, /function showRegistrationReset\(\)/, 'duplicate-email guidance offers recovery during registration');
assert.match(account, /This ID is already a registered member\. Click Forgot password to update new password\./, 'duplicate-email copy is explicit');
assert.match(account, /registrationForgotPasswordBtn/, 'registration recovery has a distinct button');
assert.match(account, /submit\.insertAdjacentElement\('afterend',target\)/, 'registration feedback is moved directly under Create account');
assert.match(account, /\['Registration date',formatDate\(currentUser\.registrationDate\)\]/, 'My Account includes registration date');
assert.match(account, /\['Last login',formatDate\(currentUser\.lastLoginDate\)\]/, 'My Account includes last login');
assert.match(account, /\['Payment method','Not configured'\]/, 'My Account shows the payment placeholder');

const bridge = fs.readFileSync('app/src/main/java/com/growplot/farm/AccountApiBridge.java', 'utf8');
const resetStart = bridge.indexOf('if("passwordReset".equals(action))');
const authLookup = bridge.indexOf('FirebaseUser user=auth.getCurrentUser()');
assert(resetStart >= 0 && resetStart < authLookup, 'password reset runs before the active-user requirement');
assert.match(bridge, /auth\.sendPasswordResetEmail\(email\)/, 'Firebase Auth sends the reset email');
assert.match(bridge, /e instanceof com\.google\.firebase\.auth\.FirebaseAuthInvalidUserException/, 'unknown emails receive the same success response to prevent account discovery');
assert.match(bridge, /Could not send a reset email/, 'reset failure has generic feedback');
assert.match(bridge, /GetSignInWithGoogleOption\.Builder\(webClientId\)/, 'explicit Google button uses the direct sign-in option');
assert.match(bridge, /NoCredentialException/, 'missing Google credentials receive actionable guidance');
assert.match(bridge, /reauthenticate it in Android Settings/, 'Google credential guidance explains the device-account recovery step');
assert.match(bridge, /FirebaseAuthUserCollisionException/, 'registration detects duplicate Firebase email');
assert.match(bridge, /ERROR_EMAIL_ALREADY_IN_USE/, 'registration detects Firebase duplicate-email error code');
assert.match(bridge, /getCreationTimestamp\(\)/, 'account response includes Firebase registration time');
assert.match(bridge, /getLastSignInTimestamp\(\)/, 'account response includes Firebase last login time');

const onboarding = fs.readFileSync('app/src/main/assets/new-user-onboarding.js', 'utf8');
assert.match(onboarding, /Create your first plot/, 'plotless users get the first-plot prompt');
assert.match(onboarding, /textContent='Create Plot'/, 'prompt offers the Create Plot action');
assert.match(onboarding, /textContent='Home'/, 'prompt offers the Home action');
assert.match(onboarding, /settings\.click\(\)/, 'Create Plot opens Settings');
assert.match(onboarding, /plotManagementSection/, 'Create Plot opens Plot Management');
assert.match(onboarding, /GrowPlotShowDemoHome/, 'Home opens the demo home screen');
assert.match(onboarding, /document\.createElement\('dialog'\)/, 'first-plot choice uses a native modal dialog');
assert.match(onboarding, /prompt\.showModal\(\)/, 'first-plot dialog enters the browser top layer');
assert.match(onboarding, /prompt\.close\(\)/, 'dialog actions close the modal before navigation');
assert.match(onboarding, /else \{prompt\.hidden=true;prompt\.style\.display='none';\}/, 'fallback overlay is hidden explicitly');

console.log('PASS: password, account details, duplicate-email recovery, and first-plot UX checks.');
