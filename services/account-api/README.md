# GrowPlot account API (development scaffold)

This service is not yet deployed. It is NOT connected to the Android login screen.
The existing Android admin demo is still device-local.

## Intended Google Drive folders (verified 2026-10-14)
- GrowPlot/user: 1tmynJPMW5-mXMqshOgswqGpThQKyYJc5
- GrowPlot/user/accounts: 1iE_P6SKYex9J6EYWhEDydL9BV-Od51ka
- GrowPlot/user/userdata: 1c8OtLU1zM702_2rFjznDtXFS9T3ZGeZZ

## API
POST /v1/register {username,password}: creates USER account in Drive.
POST /v1/login {username,password}: verifies account against Drive each login.
GET /v1/me (Bearer token): reads plot/settings profile from Drive.
PUT /v1/me (Bearer token): saves profile to Drive.
GET /v1/admin/stats (Bearer token, ADMIN only): totalUsers, standardUsers, administrators, loggedInLast7Days.

## Deployment requirements
1. Deploy Node 20+ service with `npm install express googleapis`, configure HTTPS, `GOOGLE_APPLICATION_CREDENTIALS`, `GROWPLOT_ACCOUNTS_FOLDER_ID`, `GROWPLOT_USERDATA_FOLDER_ID`.
2. Grant the backend service account access to **only** the required Drive directories; never put service-account keys or OAuth refresh tokens in the APK.
3. Implement durable sessions, rate limits, anti-enumeration, account-registration concurrency protection, password reset and audit logs before production.
4. Provision administrator account through a restricted server-side setup procedure, NOT public registration. Password reset/first-login rotation remains to be built.
5. Wire Android login/create forms to the deployed HTTPS endpoint; remove the local admin preview; route admin straight to Demo Plot and new users to onboarding.
6. Test account creation, Drive file persistence, login on a different device, reinstall, admin-only statistics and failure on Drive outage.

## Known security and reliability limitations
This initial implementation uses in-memory sessions, so sessions expire on service restart. Profile writes are not yet revision-checked and can overwrite concurrent changes. Username uniqueness checks are not atomic. No production deployment or end-to-end verification has been performed.
