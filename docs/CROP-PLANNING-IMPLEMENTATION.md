# Crop Planning — development branch

Baseline: GrowPlot v0.6.01 Build 5, versionCode 132.

Implemented: `app/src/main/assets/js/crop-planning.js` supplies a Malaysian crop starter catalog, validates organization/farm/plot/section planting plans, growth stages, counts, dates, and assigned user IDs. CommonJS export supports Node tests; browser global is `GrowPlotCropPlanning`.

Pending: load module into `index.html`, design crop-planning UI, persist by organization/plot/section, Google Drive synchronization, images, user authorization, and on-device testing. Harvest dates are user-entered, not fabricated crop-duration estimates.

Do not merge until integrated and regression-tested. No APK generated.
