# GrowPlot Firebase Functions

## Vertex image diagnosis

`vertexImageDiagnosis` is a Firebase callable in `us-central1`. It requires a signed-in Firebase user, accepts 1–5 JPEG/PNG/WebP photos (combined base64 payload up to 7 MB), sends the images to Gemini `gemini-3.5-flash` on Vertex AI using the documented `https://aiplatform.us.rep.googleapis.com` endpoint, and returns a JSON visual assessment. The callable region (`us-central1`) is separate from the model serving location (`us`). It does not persist the submitted images.

The function runs as `growplot-vertex-api@growplot.iam.gserviceaccount.com`, using the Cloud Run service identity and ADC. Grant this runtime service account **Agent Platform User** (`roles/aiplatform.user`) on project `growplot`. Do not create or upload a service-account key. Vertex AI API and project billing must be enabled.

Deploy only this callable with:

```sh
firebase deploy --only functions:vertexImageDiagnosis --project growplot --non-interactive
```

GitHub Actions deploys this callable from `main` when the function source or package manifest changes, using the existing `firebase-production` environment and deploy credential. The callable must be wired into the Android client before installed GrowPlot builds can use it.

## Google Cloud setup for the Vertex callable

Project: `growplot`. Enable these **5 APIs** in Google Cloud Console (not Firebase Console):

| API | Service name | Purpose |
| --- | --- | --- |
| Cloud Functions | `cloudfunctions.googleapis.com` | Firebase callable deployment |
| Cloud Build | `cloudbuild.googleapis.com` | Builds the function source |
| Artifact Registry | `artifactregistry.googleapis.com` | Stores the built function image |
| Firebase Extensions | `firebaseextensions.googleapis.com` | Firebase CLI checks installed extensions during deploy |
| Vertex AI | `aiplatform.googleapis.com` | Gemini image inference at runtime |

Firebase CLI 14 checks the first four during this deployment. The Vertex AI API is required when the function calls Gemini. Billing must be enabled for Cloud Functions deployment.

### IAM principals and roles

**GitHub Actions deploy principal:** `firebase-adminsdk-fbsvc@growplot.iam.gserviceaccount.com` (the email from the service-account JSON configured in the GitHub Actions secret `GROWPLOT_FIREBASE_DEPLOY_SERVICE_ACCOUNT`). Grant these project roles:

| Role | Role ID | Why it is needed |
| --- | --- | --- |
| Firebase Admin | `roles/firebase.admin` | Firebase CLI needs to inspect/manage Extensions; the deploy log returned 403 on `firebaseextensions.instances.list`. |
| Cloud Functions Developer | `roles/cloudfunctions.developer` | Creates and updates the callable function. |
| Cloud Build Editor | `roles/cloudbuild.builds.editor` | Starts the function source build. |
| Service Usage Consumer | `roles/serviceusage.serviceUsageConsumer` | Allows the deploy principal to use enabled project APIs. |

Grant **Service Account User** (`roles/iam.serviceAccountUser`) to that same deploy principal on each service account the function deployment runs as:

- `growplot@appspot.gserviceaccount.com` (App Engine default service account)
- `growplot-vertex-api@growplot.iam.gserviceaccount.com` (the callable's configured runtime identity)

Assign this role on each service account's **Permissions / Manage access** page, with the GitHub deploy principal as the member. Do not assign it to the target service account itself. Do not grant project Owner to the GitHub deploy account.

**Vertex runtime principal:** `growplot-vertex-api@growplot.iam.gserviceaccount.com`. Grant **Agent Platform User** (`roles/aiplatform.user`) on project `growplot` so the function can invoke Gemini on Vertex AI. This is a runtime role; the GitHub deploy principal is a separate identity.

**Build service identity:** Cloud Build uses the project's configured build service account. Google provisions service agents for deployment. Do not grant service-agent roles to the GitHub principal. If Cloud Build reports a permission error, grant only the role named by that error to the specific build identity shown in the log.

### Current setup status

The deployment log has confirmed the four Firebase deployment APIs above are enabled, and the App Engine default service account impersonation check passes. The latest deployment stopped because the deploy principal lacks Firebase Extensions access; add `roles/firebase.admin` to the GitHub deploy principal, then rerun the workflow. The runtime Vertex permission and final deployment still need verification after deployment succeeds.

### References

- [Firebase Extensions IAM roles](https://firebase.google.com/docs/projects/iam/roles-predefined-all-products)
- [Deploying Cloud Run functions with Cloud Build](https://docs.cloud.google.com/build/docs/deploying-builds/deploy-functions)
- [Cloud Functions IAM roles](https://docs.cloud.google.com/functions/docs/reference/iam/roles)
- [Vertex AI access control](https://docs.cloud.google.com/iam/docs/roles-permissions/aiplatform)

## VIGI credentials

Deploy the camera credential functions after enabling billing and Cloud KMS. Create a symmetric encrypt/decrypt CryptoKey in Google Cloud KMS, grant the Functions runtime service account **Cloud KMS CryptoKey Encrypter/Decrypter** on that key, and set `GROWPLOT_KMS_KEY_NAME` to the full CryptoKey resource name for `saveCameraCredentials` and `loadCameraCredentials`. Deploy using:

```sh
firebase deploy --only functions:saveCameraCredentials,functions:loadCameraCredentials --project growplot --non-interactive
```

The functions authenticate Firebase UID and store encrypted passwords at `users/{uid}/data_feeds/CCTV03`. Do not permit direct client writes to `credentials` or `username` in Firestore rules. These callables return decrypted credentials to an authenticated owner device. Never put the KMS key or service-account credentials in the APK.
