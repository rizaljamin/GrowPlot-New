package com.growplot.farm;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

/** Pure Java regression for the photo timeline. Requires no Android device. */
public final class TimelineBucketsSmokeTest {
    private static final ZoneId MYT = ZoneId.of("Asia/Kuala_Lumpur");

    private static long captured(int year, int month, int day, int hour, int minute, int second) {
        return ZonedDateTime.of(year, month, day, hour, minute, second, 0, MYT)
                .toInstant().toEpochMilli();
    }

    private static void verify(String mode, List<Long> captures, int... expectedIndices) {
        List<TimelineBuckets.Period> periods =
                TimelineBuckets.latestSavedPeriods(mode, captures);
        int[] selected = TimelineBuckets.latestPhotoIndices(periods, captures);
        if (!Arrays.equals(expectedIndices, selected))
            throw new AssertionError(mode + ": expected " + Arrays.toString(expectedIndices)
                    + " but selected " + Arrays.toString(selected));
        if (periods.size() != expectedIndices.length)
            throw new AssertionError(mode + ": unexpected number of populated periods");
        for (int i = 0; i < selected.length; i++) {
            long capturedAt = captures.get(selected[i]);
            TimelineBuckets.Period period = periods.get(i);
            if (capturedAt < period.startMillis || capturedAt >= period.endMillis)
                throw new AssertionError(mode + ": selected photo outside its period");
        }
    }

    public static void main(String[] args) {
        // CCTV01 screenshot: five nonconsecutive dates with multiple images per day.
        List<Long> cctv01 = Arrays.asList(
                captured(2026,10,26,14,51,57),
                captured(2026,10,31,8,34,55),
                captured(2026,10,31,12,41,7),
                captured(2026,11,1,22,22,10),
                captured(2026,11,1,22,45,9),
                captured(2026,11,2,16,59,0),
                captured(2026,11,2,21,15,48),
                captured(2026,11,4,10,4,41));
        // Daily intentionally bypasses TimelineBuckets: it lists all source photos.
        verify("weekly", cctv01, 4, 7);       // weeks of Oct26, Nov2
        verify("monthly", cctv01, 2, 7);      // Oct and Nov
        verify("yearly", cctv01, 7);          // 2026

        // Earlier Demo Plot photos must yield three distinct days, not four files.
        List<Long> oldDemo = Arrays.asList(
                captured(2026,10,8,18,46,21),
                captured(2026,10,8,0,0,0),
                captured(2026,10,9,10,42,56),
                captured(2026,10,10,7,18,51),
                captured(2026,10,10,10,38,21));
        verify("weekly", oldDemo, 4);
        verify("monthly", oldDemo, 4);
        System.out.println("PASS: Weekly and Monthly select newest photo per populated period.");
    }
}
