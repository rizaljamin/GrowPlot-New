# GrowPlot diagnosis and YOLO services

The diagnosis proxy keeps provider keys server-side, accepts up to five diagnosis photos, and calls Kindwise **crop.health**, Pl@ntNet **diseases/identify**, and the YOLO-World detector separately. YOLO-World locates broad `plant`, `leaf`, `flower`, and `fruit` regions. It is not trained to identify GrowPlot crop species or diseases. Symptoms and GrowPlot's pixel summary are report context; the provider calls analyze submitted images.

## Configure and deploy

1. Build and deploy `backend/yolo` as a separate HTTPS Cloud Run service. The model downloads its pretrained weights when the container starts. Set a long random `GROWPLOT_YOLO_SERVICE_TOKEN` and use at least 4 GiB memory; first startup can take longer while weights load.
2. Deploy this directory as the diagnosis proxy. Set `KINDWISE_CROP_HEALTH_API_KEY`, `PLANTNET_API_KEY`, `GROWPLOT_PROXY_TOKEN`, `GROWPLOT_YOLO_SERVICE_URL` (the YOLO service base URL), and the same `GROWPLOT_YOLO_SERVICE_TOKEN`. Keep the YOLO service token on the server only.
3. In GitHub Actions secrets, configure `GROWPLOT_DIAGNOSIS_API_URL` as the proxy URL ending in `/v1/diagnose` and `GROWPLOT_DIAGNOSIS_PROXY_TOKEN` as the app-to-proxy gate. The Android build passes these settings into the app; they are not provider keys. The app makes a `/v1/yolo` request automatically when **Run Diagnosis** is tapped.

Deploy the diagnosis proxy after deploying the model service and replacing the URL placeholder with its HTTPS base URL:

```sh
gcloud run deploy growplot-diagnosis \
  --source backend \
  --region asia-southeast1 \
  --allow-unauthenticated \
  --memory 1Gi \
  --set-env-vars GROWPLOT_YOLO_SERVICE_URL=https://YOUR-YOLO-SERVICE.run.app \
  --set-secrets GROWPLOT_PROXY_TOKEN=growplot-proxy-token:latest,GROWPLOT_YOLO_SERVICE_TOKEN=growplot-yolo-service-token:latest
```

Create a Secret Manager secret for the YOLO service token before deployment. Give the diagnosis proxy service account access to that secret. The Cloud Run example below uses that secret (review project billing and compute costs before deploying):

```sh
gcloud run deploy growplot-yolo \
  --source backend/yolo \
  --region asia-southeast1 \
  --allow-unauthenticated \
  --memory 4Gi \
  --cpu 2 \
  --timeout 300 \
  --set-secrets GROWPLOT_YOLO_SERVICE_TOKEN=growplot-yolo-service-token:latest
```

The HTTP service is reachable publicly for the diagnosis proxy, but its inference route rejects requests without the server-side token. Use a separate token from the app-to-proxy gate and never put this service token in the APK.

Example local Gradle properties:

```properties
growplotDiagnosisApiUrl=https://YOUR_PRIVATE_SERVICE/v1/diagnose
growplotDiagnosisProxyToken=YOUR_APP_TO_PROXY_TOKEN
```

## Run locally

```sh
npm install
cp .env.example .env
npm start
```

Set secrets in the shell/environment before starting. Do not commit `.env` or API keys. `GET /healthz` is the non-secret health check; `POST /v1/diagnose` requires `Authorization: Bearer <GROWPLOT_PROXY_TOKEN>` and multipart `photos` fields plus a `context` JSON field.

The proxy and YOLO service accept JPEG/PNG only, up to five photos and 10 MB each. They do not persist uploaded photos. YOLO-World uses a generic open-vocabulary detector and is not a custom-trained crop-disease model. Model detections, species suggestions, and provider responses are preliminary outputs, not human expert diagnoses. Pl@ntNet disease recognition supports a limited set of host species/pathologies; Kindwise crop.health focuses on its listed crop coverage. A provider can return no candidates or an unsupported/failed status.
