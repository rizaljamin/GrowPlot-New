#!/usr/bin/env node
const fs = require('fs');
const assert = require('assert');
const source = fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java', 'utf8');
const listMethod = source.slice(source.indexOf('private JSONArray listDriveFiles(String query)'), source.indexOf('private String findDriveFolderByName'));
const findMethod = source.slice(source.indexOf('private String findDriveFolderByName'), source.indexOf('private String findOrCreateDriveFolder'));
assert(listMethod.includes('listDriveFiles(query, "allDrives")'), 'default Drive search should include shared drives');
assert(listMethod.includes('corpora=') && listMethod.includes('URLEncoder.encode(corpora, "UTF-8")'), 'Drive corpus must be selectable');
assert(findMethod.includes('sharedWithMe and " + query'), 'root folder lookup must retry in Shared with me');
assert(findMethod.includes('listDriveFiles("sharedWithMe and " + query, "user")'), 'Shared with me retry must use the user corpus');
assert(findMethod.includes('parentId != null) return folderId'), 'nested child lookups must remain parent-scoped');
console.log('Drive folder discovery regression checks passed.');
