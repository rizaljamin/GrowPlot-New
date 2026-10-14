# GrowPlot crop-planning navigation and summary — 14 October 2026

Status: approved requirement; implementation in progress. Preserve v0.6.01 Build 9 CCTV baseline. Do not merge unfinished crop-planning changes to main.

## Required navigation
1. Manage Farm > Crop Planning displays a **Select Crop** button with crop icon, not an expanded catalogue.
2. Tapping Select Crop opens a separate selection view containing the existing crop-photo grid, search, and choices, with a Back control.
3. Selecting a crop opens a crop summary view before planting assignment.
4. Summary includes common name, scientific species name, cultivar/variety when known, expected time to maturity or first harvest with reference point, and typical healthy fruit size and weight where applicable. Clearly label ranges as indicative and variety-dependent; use Not applicable for non-fruit crops.
5. Continue opens the existing planting form (plot, greenhouse, section/all sections, plant count, dates), retaining Google Drive persistence and existing edit/delete behavior.
6. Do not change CCTV, streaming, snapshot logic, AI Health or provider integrations.

## Acceptance checks
- Initial Crop Planning tab contains Select Crop button; no expanded grid.
- Select Crop > select item > summary > continue to planting works; back navigation works.
- No crop is saved merely by viewing its summary.
- Existing saved planting records can still be viewed and edited.
- No Build 10 APK or merge to main until validation.
