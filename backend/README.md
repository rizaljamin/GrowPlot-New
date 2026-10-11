# GrowPlot Kindwise + Pl@ntNet proxy

This service keeps provider keys server-side and accepts up to five diagnosis photos. It calls Kindwise **crop.health** and Pl@ntNet **diseases/identify** concurrently and returns the providers separately. Symptoms and GrowPlot's pixel summary are retained as report context; the documented provider calls are image-based, so the proxy does not claim those fields influenced either model.

## Configure and deploy

1. Create a Kindwise account, enable the crop.health product, and create a crop.health API key. Create a Pl@ntNet developer account and obtain its API key.
2. Deploy this folder as a private HTTPS service (Google Cloud Run is suitable). Set `KINDWISE_CROP_HEALTH_API_KEY`, `PLANTNET_API_KEY`, and a random `GROWPLOT_PROXY_TOKEN` as server-side secrets. Restrict the service to the user's authenticated identity where possible; the shared token is only an additional app gate.
3. Set `growplotDiagnosisApiUrl` and `growplotDiagnosisProxyToken` in a local, untracked Gradle properties file, then build the APK. The provider API keys are never included in the app.

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

The service sends JPEG/PNG only, up to five photos and 10 MB each. It does not persist uploaded photos. Provider responses are AI model outputs, not human expert diagnoses. Pl@ntNet disease recognition supports only a limited set of host species/pathologies; Kindwise crop.health focuses on its listed crop coverage. A provider can return no candidates or an unsupported/failed status.
