package com.growplot.farm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/** Calendar-period boundaries used by the rolling plant-photo timeline. */
final class TimelineBuckets {
    private static final TimeZone ZONE = TimeZone.getTimeZone("Asia/Kuala_Lumpur");

    static final class Period {
        final int index;
        final long startMillis;
        final long endMillis;

        Period(int index, long startMillis, long endMillis) {
            this.index = index;
            this.startMillis = startMillis;
            this.endMillis = endMillis;
        }
    }

    private TimelineBuckets() {}

    static List<Period> latestFour(String interval, long nowMillis) {
        Calendar current = Calendar.getInstance(ZONE, Locale.US);
        current.setTimeInMillis(nowMillis);
        current.set(Calendar.HOUR_OF_DAY, 0);
        current.set(Calendar.MINUTE, 0);
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);
        if ("daily".equals(interval)) {
            // Midnight already established above.
        } else if ("monthly".equals(interval)) {
            current.set(Calendar.DAY_OF_MONTH, 1);
        } else if ("yearly".equals(interval)) {
            current.set(Calendar.DAY_OF_YEAR, 1);
        } else if (!"daily".equals(interval)) {
            int daysSinceMonday = (current.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            current.add(Calendar.DAY_OF_YEAR, -daysSinceMonday);
        }

        List<Period> periods = new ArrayList<>(4);
        Calendar start = (Calendar) current.clone();
        for (int i = 0; i < 4; i++) {
            start = (Calendar) current.clone();
            int offset = i - 3;
            if ("daily".equals(interval)) start.add(Calendar.DAY_OF_YEAR, offset);
            else if ("daily".equals(interval)) start.add(Calendar.DAY_OF_YEAR, offset);
            else if ("monthly".equals(interval)) start.add(Calendar.MONTH, offset);
            else if ("yearly".equals(interval)) start.add(Calendar.YEAR, offset);
            else start.add(Calendar.WEEK_OF_YEAR, offset);
            Calendar end = (Calendar) start.clone();
            if ("daily".equals(interval)) end.add(Calendar.DAY_OF_YEAR, 1);
            else if ("daily".equals(interval)) end.add(Calendar.DAY_OF_YEAR, 1);
            else if ("monthly".equals(interval)) end.add(Calendar.MONTH, 1);
            else if ("yearly".equals(interval)) end.add(Calendar.YEAR, 1);
            else end.add(Calendar.WEEK_OF_YEAR, 1);
            periods.add(new Period(i + 1, start.getTimeInMillis(), end.getTimeInMillis()));
        }
        return periods;
    }

    static int[] earliestPhotoIndices(List<Period> periods, List<Long> captureTimes) {
        int[] selected = new int[periods.size()];
        long[] earliest = new long[periods.size()];
        for (int i = 0; i < selected.length; i++) {
            selected[i] = -1;
            earliest[i] = Long.MAX_VALUE;
        }
        for (int photoIndex = 0; photoIndex < captureTimes.size(); photoIndex++) {
            long capturedAt = captureTimes.get(photoIndex);
            for (int periodIndex = 0; periodIndex < periods.size(); periodIndex++) {
                Period period = periods.get(periodIndex);
                if (capturedAt >= period.startMillis && capturedAt < period.endMillis
                        && capturedAt < earliest[periodIndex]) {
                    earliest[periodIndex] = capturedAt;
                    selected[periodIndex] = photoIndex;
                }
            }
        }
        return selected;
    }
}
