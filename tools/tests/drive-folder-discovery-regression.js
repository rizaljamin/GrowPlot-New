#!/usr/bin/env node
const fs = require('fs');
const assert = require('assert');
const source = fs.readFileSync('app/src/main/java/com/growplot/farm/MainActivity.java', 'utf8');
const listMethod = source.slice(source.indexOf('private JSONArray listDriveFiles(String query)'), source.indexOf('private JSONArray listDriveFiles(String query, String corpora)'));
const findMethod = source.slice(source.indexOf('private String findDriveFolderByName'), source.indexOf('private String findExactDriveFolderId'));
const sharedDriveMethod = source.slice(source.indexOf('private String findDriveSharedDriveByName'), source.indexOf('private void connectDefaultFeedWithDriveApi'));
assert(listMethod.includes('listDriveFiles(query, "allDrives")'), 'default Drive search should include shared drives');
assert(listMethod.includes('if (files.length() > 0 || !query.contains(" in parents")) return files'), 'empty parent-scoped allDrives listings should retry in the user corpus');
assert(listMethod.includes('return listDriveFiles(query, "user")'), 'nested Drive listing fallback should use the user corpus');
assert(findMethod.includes('sharedWithMe and " + query'), 'root folder lookup must retry in Shared with me');
assert(findMethod.includes('listDriveFiles(query)'), 'root Projects lookup should enumerate accessible Drive folders');
assert(findMethod.includes('findDriveFolderByName(id, "GrowPlot")'), 'same-named Projects candidates should be checked for GrowPlot');
assert(findMethod.includes('findDriveFolderByName(growPlotId, "Image_Feed")'), 'prefer the Projects candidate containing Image_Feed');
assert(sharedDriveMethod.includes('findDriveFolderByName(id, "GrowPlot")'), 'same-named Shared Drives should be checked for GrowPlot');
assert(sharedDriveMethod.includes('findDriveFolderByName(growPlotId, "Image_Feed")'), 'prefer the Shared Drive containing Image_Feed');
console.log('Drive folder discovery regression checks passed.');
