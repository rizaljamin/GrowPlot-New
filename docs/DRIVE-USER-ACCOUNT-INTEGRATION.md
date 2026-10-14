# Google Drive account persistence — integration status

Desired owner workflow: GrowPlot accounts persist under `GrowPlot/user/` in the owner's Drive, survive reinstall, and are checked on every login. Admin enters the existing Demo Plot directly. First-time standard users choose Create My Plot or Show Me the Demo Plot.

## Implemented
- `DriveUserAccount`: portable JSON account schema, PBKDF2-HMAC-SHA256 salted password verifier, role/plot fields.
- `LoginLandingPolicy`: target routing contract.
- New-user onboarding UI preview.

## NOT IMPLEMENTED — security and functional blockers
- The Android application has **no authenticated Drive API client for centrally reading/writing the owner's private user directory**. Existing Android Storage Access Framework grants apply to the current device and cannot serve as an independent shared login backend.
- Usernames/passwords cannot safely be validated by downloading an owner-controlled Drive account file directly to arbitrary clients. A client able to read/write all account files could impersonate administrators, modify roles or replace hashes. A trusted backend or similarly trusted identity/authorization layer is required.
- There is no production session, central plot ownership enforcement, cloud password reset, or reliable multi-device conflict handling.
- Existing `AdminOtpDemo` remains a device-local demonstration. Do **not** represent it as cloud login or production-ready.

## Integration plan
1. Connect a trusted authenticated identity service for login and role claims, with least-privilege server-side access to the owner Drive folder. Keep all Drive refresh tokens and administrative write credentials off the APK.
2. Store user profile, plot ownership and farm settings in Drive; use stable user IDs and conditional writes/revision checks.
3. Make successful login fetch user profile from trusted service before routing. Admin goes straight to current Demo Plot and all modules. New user sees Create My Plot / Show Me the Demo Plot; returning user opens last selected plot.
4. On reinstall, authenticate again, load profile and plots from Drive, then request any device-specific SAF grants needed for media feeds.
5. Validate with two devices, reinstall, password changes, unauthorized role edits, and Drive outage cases.
