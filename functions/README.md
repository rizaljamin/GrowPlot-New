# GrowPlot Firebase Functions

## Vertex image diagnosis

`vertexImageDiagnosis` is a Firebase callable in `us-central1`. It requires a signed-in Firebase user, accepts 1–5 JPEG/PNG/WebP photos (combined base64 payload up to 7 MB), sends the images to Gemini `gemini-2.5-flash` on Vertex AI, and returns a JSON visual assessment. It does not persist the submitted images.

The function runs as `growplot-vertex-api@growplot.iam.gserviceaccount.com`, using the Cloud Run service identity and ADC. Grant this runtime service account **Agent Platform User** (`roles/aiplatform.user`) on project `growplot`. Do not create or upload a service-account key. Vertex AI API and project billing must be enabled.

Deploy only this callable with:

```sh
firebase deploy --only functions:vertexImageDiagnosis --project growplot --non-interactive
```

GitHub Actions deploys this callable from `main` when the function source or package manifest changes, using the existing `firebase-production` environment and deploy credential. The callable must be wired into the Android client before installed GrowPlot builds can use it.

## VIGI credentials

Deploy the camera credential functions after enabling billing and Cloud KMS. Create a symmetric encrypt/decrypt CryptoKey in Google Cloud KMS, grant the Functions runtime service account **Cloud KMS CryptoKey Encrypter/Decrypter** on that key, and set `GROWPLOT_KMS_KEY_NAME` to the full CryptoKey resource name for `saveCameraCredentials` and `loadCameraCredentials`. Deploy using:

```sh
firebase deploy --only functions:saveCameraCredentials,functions:loadCameraCredentials --project growplot --non-interactive
```

The functions authenticate Firebase UID and store encrypted passwords at `users/{uid}/data_feeds/CCTV03`. Do not permit direct client writes to `credentials` or `username` in Firestore rules. These callables return decrypted credentials to an authenticated owner device. Never put the KMS key or service-account credentials in the APK.
