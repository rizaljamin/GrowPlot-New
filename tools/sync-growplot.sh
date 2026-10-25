#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

fail() { printf 'GrowPlot sync stopped: %s\n' "$*" >&2; exit 1; }

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || fail "this folder is not a Git checkout"
REMOTE_URL="$(git remote get-url origin 2>/dev/null)" || fail "origin remote is missing"
[[ "$REMOTE_URL" == *"rizaljamin/GrowPlot-New"* ]] || fail "unexpected origin: $REMOTE_URL"
[[ -z "$(git status --porcelain)" ]] || fail "worktree has local changes; preserve and review them before syncing"

git fetch --prune origin main || fail "could not fetch origin/main"
git switch main >/dev/null 2>&1 || fail "could not switch to local main safely"
git merge --ff-only origin/main >/dev/null || fail "local main diverged from origin/main; preserve commits and reconcile manually"

HEAD_SHA="$(git rev-parse HEAD)"
REMOTE_SHA="$(git rev-parse origin/main)"
[[ "$HEAD_SHA" == "$REMOTE_SHA" ]] || fail "HEAD ($HEAD_SHA) does not match origin/main ($REMOTE_SHA)"
VERSION_NAME="$(sed -n 's/^versionName=//p' version.properties | head -n1)"
VERSION_CODE="$(sed -n 's/^versionCode=//p' version.properties | head -n1)"
[[ -n "$VERSION_NAME" && "$VERSION_CODE" =~ ^[1-9][0-9]*$ ]] || fail "version.properties is missing a valid app version"
LATEST_LOG="$(head -n1 VERSION-LOG.txt)"
[[ "$LATEST_LOG" == *"$VERSION_NAME"* && "$LATEST_LOG" == *"versionCode $VERSION_CODE"* ]] || \
  fail "version.properties ($VERSION_NAME / $VERSION_CODE) disagrees with the newest VERSION-LOG.txt entry: $LATEST_LOG"

printf 'GrowPlot synced\n  repository: %s\n  branch: main\n  commit: %s\n  version: %s (versionCode %s)\n' \
  "$REMOTE_URL" "$HEAD_SHA" "$VERSION_NAME" "$VERSION_CODE"
