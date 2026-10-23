# GrowPlot Firebase Functions

## Monthly public crop prices

`getMonthlyRetailCropPrices` is a Firebase callable in `us-central1`. It reads the monthly Malaysia PriceCatcher CSV and the official item lookup from `storage.data.gov.my/pricecatcher`, converts supported kg/gram observations to RM/kg, and returns matching crop item means, observation counts, and the direct source URL. It requires Firebase authentication. Monthly summaries are cached in Firestore; the current month refreshes every six hours, while completed months remain cached.

PriceCatcher is a public retail-price observation dataset, not a FAMA farm-gate quote. GrowPlot labels its output as Malaysia retail and displays the observation count and source attribution. The function does not write user crop data into the cache. Deploy it with:

```sh
firebase deploy --only functions:getMonthlyRetailCropPrices --project growplot --non-interactive
```

## Vertex image diagnosis

`vertexImageDiagnosis` is a Firebase callable in `us-central1`. It requires a signed-in Firebase user, accepts 1–5 JPEG/PNG/WebP photos (combined base64 payload up to 7 MB), sends the images to Gemini `gemini-3.5-flash` on Vertex AI using the documented `https://aiplatform.us.rep.googleapis.com` endpoint, and returns a JSON visual assessment. The callable region (`us-central1`) is separate from the model serving location (`us`). It does not persist the submitted images.

The function runs as `growplot-vertex-api@growplot.iam.gserviceaccount.com`, using the Cloud Run service identity and ADC. Grant this runtime service account **Agent Platform User** (`roles/aiplatform.user`) on project `growplot`. Do not create or upload a service-account key. Vertex AI API and project billing must be enabled.

Deploy only this callable with:

```sh
firebase deploy --only functions:vertexImageDiagnosis --project growplot --non-interactive
```

GitHub Actions deploys Firebase callables from `main` when function source changes, using the existing `firebase-production` environment and deploy credential. The Android app must include the callable bridge before installed builds can use the monthly price lookup.

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

**Build service identity:** Cloud Build uses the project's configured build service account. Google provisions service agents for deployment. Do not grant service-agent roles to the GitHub principal.
