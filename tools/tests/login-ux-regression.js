const fs = require('node:fs');
const assert = require('node:assert/strict');

const account = fs.readFileSync('app/src/main/assets/account-login.js', 'utf8');
assert.match(account, /function addPasswordVisibilityToggles\(\)/, 'password fields receive visibility controls');
assert.match(account, /field\.type=visible\?'text':'password'/, 'visibility control switches only the selected password field');
assert.match(account, /aria-pressed/, 'visibility state is accessible');
assert.match(account, /id='forgotPasswordBtn'/, 'login exposes a password-reset action');
assert.match(account, /request\('passwordReset',\{email:email\.value\.trim\(\)\}\)/, 'reset action uses the entered email');
assert.match(account, /If a GrowPlot account matches that address/, 'reset feedback does not confirm whether an account exists');

const bridge = fs.readFileSync('app/src/main/java/com/growplot/farm/AccountApiBridge.java', 'utf8');
const resetStart = bridge.indexOf('if("passwordReset".equals(action))');
const authLookup = bridge.indexOf('FirebaseUser user=auth.getCurrentUser()');
assert(resetStart >= 0 && resetStart < authLookup, 'password reset runs before the active-user requirement');
assert.match(bridge, /auth\.sendPasswordResetEmail\(email\)/, 'Firebase Auth sends the reset email');
assert.match(bridge, /Could not send a reset email/, 'reset failure has generic feedback');

console.log('PASS: password visibility and Firebase password-reset UX checks.');
