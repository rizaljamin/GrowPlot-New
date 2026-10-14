#!/usr/bin/env python3
"""Copy an existing GitHub Release's three verified assets to a Drive folder."""
import hashlib
import io
import json
import os
import pathlib
import re
import sys

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload, MediaIoBaseDownload

folder = pathlib.Path(sys.argv[1])
assets = list(folder.iterdir())
apks = [p for p in assets if p.name.endswith(".apk")]
zips = [p for p in assets if p.name.endswith("-source.zip")]
manifests = [p for p in assets if "manifest" in p.name.lower() and p.suffix == ".txt"]
if len(apks) != 1 or len(zips) != 1 or len(manifests) != 1 or len(assets) != 3:
    sys.exit("Expected exactly one APK, one -source.zip and one manifest; refusing ambiguous release")

def digest(path):
    h = hashlib.sha256()
    with path.open("rb") as f:
        for block in iter(lambda: f.read(1024 * 1024), b""):
            h.update(block)
    return h.hexdigest()

manifest = manifests[0].read_text(encoding="utf-8")
for p in (apks[0], zips[0]):
    if p.name not in manifest or digest(p) not in manifest.lower():
        sys.exit(f"Manifest does not contain filename and SHA-256 for {p.name}")
if "com.growplot.farm" not in manifest:
    sys.exit("Manifest missing expected application ID")
if not re.search(r"versionCode:\s*\d+", manifest):
    sys.exit("Manifest missing versionCode")

credentials = service_account.Credentials.from_service_account_info(
    json.loads(os.environ["GDRIVE_CREDENTIALS"]),
    scopes=["https://www.googleapis.com/auth/drive.file"],
)
drive = build("drive", "v3", credentials=credentials, cache_discovery=False)
parent = os.environ["GDRIVE_FOLDER_ID"]
# This folder must be explicitly shared with the service account.
drive.files().get(fileId=parent, fields="id,name,mimeType", supportsAllDrives=True).execute()

def list_matching(name):
    safe = name.replace("'", "\\'")
    q = f"'{parent}' in parents and name = '{safe}' and trashed = false"
    result = drive.files().list(q=q, fields="nextPageToken,files(id,name,size,md5Checksum)",
                                supportsAllDrives=True, includeItemsFromAllDrives=True).execute()
    if result.get("nextPageToken"):
        sys.exit("Ambiguous Drive listing pagination; refusing upload")
    return result.get("files", [])

for path in (apks[0], zips[0], manifests[0]):
    matches = list_matching(path.name)
    if len(matches) > 1:
        sys.exit(f"Duplicate Drive files: {path.name}")
    if matches:
        remote = matches[0]
        stream = io.BytesIO()
        downloader = MediaIoBaseDownload(stream, drive.files().get_media(
            fileId=remote["id"], supportsAllDrives=True))
        done = False
        while not done:
            _, done = downloader.next_chunk()
        remote_hash = hashlib.sha256(stream.getvalue()).hexdigest()
        if remote_hash != digest(path):
            sys.exit(f"Drive file differs; refusing overwrite: {path.name}")
        print(f"Verified existing: {path.name} SHA-256 {remote_hash}")
        continue
    mime = "application/vnd.android.package-archive" if path.suffix == ".apk" else (
        "application/zip" if path.suffix == ".zip" else "text/plain")
    created = drive.files().create(
        body={"name": path.name, "parents": [parent]},
        media_body=MediaFileUpload(str(path), mimetype=mime, resumable=True),
        fields="id,name", supportsAllDrives=True,
    ).execute()
    print(f"Uploaded: {created['name']} id={created['id']} SHA-256 {digest(path)}")
print("Drive synchronization completed; source-to-release reproducibility and device testing are separate checks.")
