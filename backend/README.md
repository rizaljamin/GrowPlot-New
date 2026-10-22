# GrowPlot diagnosis and YOLO services

The diagnosis proxy keeps provider keys server-side, accepts up to five diagnosis photos, and calls Kindwise **crop.health**, Pl@ntNet **diseases/identify**, the YOLO-World detector, and Gemini image analysis on Gemini Enterprise Agent Platform (formerly Vertex AI). YOLO-World locates broad `plant`, `leaf`, `flower`, and `fruit` regions. It is not trained to identify GrowPlot crop species or diseases. Gemini returns cautious visual findings and possible species/cause suggestions; it does not replace the separate HSV pixel calculation or YOLO detections.

## Configure and deploy

1. Build and deploy `backend/yolo` as a separate HTTPS Cloud Run service. The model downloads its pretrained weights when the container starts. Set a long random `GROWPLOT_YOLO_SERVICE_TOKEN` and use at least 4 GiB memory; first startup can take longer while weights load.
2. Deploy this directory as the diagnosis proxy. Set `KINDWISE_CROP_HEALTH_API_KEY`, `PLANTNET_API_KEY`, `GROWPLOT_PROXY_TOKEN`, `GROWPLOT_YOLO_SERVICE_URL` (the YOLO service base URL), and the same `GROWPLOT_YOLO_SERVICE_TOKEN`. Keep provider keys and service tokens on the server only.
3. For Gemini image analysis, assign the user-managed service account `growplot-vertex-api@growplot.iam.gserviceaccount.com` as the Cloud Run service identity. It needs the **Agent Platform User** role (`roles/aiplatform.user`) on project `growplot`; the Agent Platform API must be enabled. Set `GROWPLOT_VERTEX_ENABLED=true`, `GOOGLE_CLOUD_PROJECT=growplot`, `GOOGLE_CLOUD_LOCATION=global`, and `VERTEX_MODEL_ID=gemini-3.5-flash`. The backend gets short-lived OAuth access tokens from the Cloud Run metadata server; no service-account key or Vertex credential goes in the APK.
4. In GitHub Actions secrets, configure `GROWPLOT_DIAGNOSIS_API_URL` as the proxy URL ending in `/v1/diagnose` and `GROWPLOT_DIAGNOSIS_PROXY_TOKEN` as the app-to-proxy gate. The Android build passes these settings into the app; they are not provider keys. The app makes a `/v1/yolo` request automatically when **Run Diagnosis** is tapped.

Deploy the diagnosis proxy after deploying the model service and replacing the URL placeholder with its HTTPS base URL:

```sh
gcloud run deploy growplot-diagnosis \\
  --source backend \\
  --region asia-southeast1 \\
  --service-account growplot-vertex-api@growplot.iam.gserviceaccount.com \\
  --allow-unauthenticated \\
  --memory 1Gi \\
  --set-env-vars GOOGLE_CLOUD_PROJECT=growplot,GOOGLE_CLOUD_LOCATION=global,VERTEX_MODEL_ID=gemini-3.5-flash,GROWPLOT_VERTEX_ENABLED=true,GROWPLOT_YOLO_SERVICE_URL=https://YOUR-YOLO-SERVICE.run.app \\
  --set-secrets GROWPLOT_PROXY_TOKEN=growplot-proxy-token:latest,GROWPLOT_YOLO_SERVICE_TOKEN=growplot-yolo-service-token:latest
```

Create a Secret Manager secret for the YOLO service token before deployment. Give the diagnosis proxy service account access to that secret. Review project billing and compute costs before deploying YOLO; Gemini calls are made only when the user runs a diagnosis and the Vertex flag is enabled.

The HTTP service is reachable publicly for the diagnosis proxy, but its inference routes reject requests without the server-side token. Use a separate token from the app-to-proxy gate and never put this service token in the APK.

Example local Gradle properties:

```properties
growplotDiagnosisApiUrl=https://YOUR_PRIVATE_SERVICE/v1/diagnose
growplotDiagnosisProxyToken=YOUR_APP_TO_PROXY_TOKEN
```

## Run and test locally

```sh
npm install
npm test
cp .env.example .env
npm start
```

Set provider secrets in the environment before starting. The test suite mocks Cloud Run metadata and the Agent Platform response; it does not make a billable live model request. Do not commit `.env` or API keys. `GET /healthz` is the non-secret health check; `POST /v1/diagnose` requires `Authorization: Bearer <GROWPLOT_PROXY_TOKEN>` and multipart `photos` fields plus a `context` JSON field.

The proxy and YOLO service accept JPEG/PNG only, up to five photos and 10 MB each. They do not persist uploaded photos. YOLO-World uses a generic open-vocabulary detector and is not a custom-trained crop-disease model. Model detections, species suggestions, and provider responses are preliminary outputs, not human expert diagnoses. Pl@ntNet disease recognition supports a limited set of host species/pathologies; Kindwise crop.health focuses on its listed crop coverage. A provider can return no candidates or an unsupported/failed status.
