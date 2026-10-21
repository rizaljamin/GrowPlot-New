# GrowPlot quality checks

Use this checklist before publishing any app change. A fix in one area must not silently remove a working flow in another.

## Core functions to protect

- Home: all six CCTV cards, live feeds where configured, and current snapshots.
- A.I Health: camera selection, diagnosis, diagnosis history, plant identity choices, and confirmation.
- Photo history: daily, weekly, monthly, and yearly intervals; photos remain scoped to the selected plot. Verify every rendered photo URL resolves through the Android WebView asset handler, including bundled fallbacks.
- Data Feed: connect or reconnect to the selected plot folder, scan photos, and report connection or scan errors clearly.
- Manage Farm: Plot, Crop Planning, Irrigation & Fertiliser, and Maintenance sections.
- Plot settings: plot selection and management, camera settings, map location, and save behavior.
- Main navigation and account screen.

## Checks before a build is ready

1. Start from the current GitHub `main` commit and record the commit and build identity.
2. State which core function is being changed and which existing flows depend on it.
3. Keep the source diff within that scope. Review all changed files and confirm unrelated screens, controls, bridge methods, and event handlers remain present.
4. Add or update a regression test for the reported failure. Run the complete regression suite and JavaScript syntax checks through `tools/build-apk.sh`.
5. Build the ARM64 APK from the exact source commit. Verify package/version, signature, alignment, artifact hashes, and build manifest.
6. Test the changed flow on the Android tablet when available. If that check has not happened, label device behavior pending and do not call it verified.
7. Report in plain language: what changed, what tests passed, what still needs tablet testing, and the APK link if one was built.

## Regression test rule

When editing a core function that has no automated regression test yet, add the test in the same source change before the next APK build. Do not replace a whole screen or module to repair a smaller behavior unless the user asks for that redesign.
