# GrowPlot authentication service — deployment contract

The Android application must call a trusted HTTPS service for administrator activation.
The ChatGPT Gmail connector can send a message during an interactive ChatGPT session,
but it is **not** an email API or reusable credential for the installed Android app.
Never put a Gmail access token, SMTP password, or provider secret in the APK.

## Owner activation (Issue #8)

- Reserved identity: `growplot_admin`
- Fixed delivery destination (server-side): `growplot2026@gmail.com`
- `POST /v1/admin/activation/request`: issue a random six-digit code with a 10-minute expiry, hash it server-side, throttle requests and email it via a server-side transactional email API.
- `POST /v1/admin/activation/complete`: accept code and new password (8–128 characters) over HTTPS; atomically consume the code and create the single administrator with creator-equivalent authority.
- Maximum five verification attempts per activation challenge; rate-limit by IP/account and protect against replay.
- Store the password using Argon2id (or a comparably strong adaptive password hash), never plaintext or reversible encryption.
- Do not grant privileges until the server verifies and consumes the activation code.
- On subsequent logins, server validates credentials and issues a short-lived authenticated session.
- Account management must be authorized by verified server-side role and immutable user ID, not client-supplied username/role.
- Production must not expose open administrator bootstrap indefinitely; require owner-controlled deployment authorization and an auditable recovery procedure.

## Email delivery

Choose a transactional provider (e.g. Resend, SendGrid, or Amazon SES) with a verified sender domain.
Keep provider API credentials in server-side secret storage. Use an authenticated sender and avoid
Gmail-account passwords in app settings. A Gmail connector available to ChatGPT is not suitable
for unattended activation requests from arbitrary installed APKs.

## Deployment readiness

This file specifies the integration contract only. No server, provider account, deployed
endpoint, or delivery test is claimed. Do not publish a functional admin activation screen
until the backend exists and real end-to-end activation has been tested.
