# User hierarchy and access — development branch

Baseline: GrowPlot v0.6.01 Build 5, versionCode 132.

Implemented: `app/src/main/assets/js/user-access.js` defines owner, farm_admin, farm_manager, supervisor, agronomist, worker and viewer; role permissions; organization, farm, plot and section scope checks; restricted user administration.

Important: these are frontend/domain helpers, NOT secure authorization enforcement. Before release, implement authenticated identities, server-side enforcement, tenant isolation, invitation and account lifecycle, audit trail, and secure sync. Do not store plaintext passwords or authentication secrets in Google Drive.

Pending: integrate with `index.html` and native authentication, create UI, persistence, tests and device validation. No APK generated.
