# GrowPlot Vision API — Cloud Run deployment

This optional service accepts a JPEG image from GrowPlot and calls the OpenAI vision API. Its separate files are in `backend/openai-vision` so the root `backend` directory can build the diagnosis proxy.
Do not store OPENAI_API_KEY in GitHub or inside the APK.

Prerequisites: Google Cloud project with billing, Cloud Run and Cloud Build APIs enabled,
Secret Manager API enabled, and permissions to deploy Cloud Run and access a secret.

From the repository root:

```bash
gcloud config set project YOUR_PROJECT_ID
gcloud services enable run.googleapis.com cloudbuild.googleapis.com secretmanager.googleapis.com artifactregistry.googleapis.com
printf '%s' 'YOUR_OPENAI_API_KEY' | gcloud secrets create growplot-openai-api-key --data-file=-
gcloud run deploy growplot-vision \
  --source backend/openai-vision \
  --region asia-southeast1 \
  --allow-unauthenticated \
  --set-secrets OPENAI_API_KEY=growplot-openai-api-key:latest \
  --set-env-vars GROWPLOT_ALLOWED_ORIGIN=https://appassets.androidplatform.net
```

**Security warning:** `--allow-unauthenticated` is suitable only for a short, rate-limited
development test. CORS origin checks do not authenticate clients and can be spoofed by
non-browser callers. Before production, require Firebase ID token verification on every
request, rate-limit by user, set OpenAI spending limits, and restrict ingress as appropriate.
The deployed Cloud Run service account must have Secret Manager Secret Accessor permission
on the secret. Grant it explicitly if deployment reports a permission error.

Copy the deployed HTTPS service URL (without /api/plant-analysis) into
GrowPlot > Run diagnosis > OpenAI vision backend settings. Do not paste an API key into GrowPlot.

This deployment has not been executed or verified by ChatGPT.
