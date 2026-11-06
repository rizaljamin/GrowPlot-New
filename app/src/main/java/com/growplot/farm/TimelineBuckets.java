package com.growplot.farm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    /** Returns the latest four calendar periods that actually contain saved photos. */
    static List<Period> latestSavedPeriods(String interval, List<Long> captureTimes) {
        Map<Long, Period> periodsByStart = new HashMap<>();
        for (long capturedAt : captureTimes) {
            Calendar start = Calendar.getInstance(ZONE, Locale.US);
            start.setTimeInMillis(capturedAt);
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            if ("monthly".equals(interval)) {
                start.set(Calendar.DAY_OF_MONTH, 1);
            } else if ("yearly".equals(interval)) {
                start.set(Calendar.DAY_OF_YEAR, 1);
            } else if (!"daily".equals(interval)) {
                int daysSinceMonday = (start.get(Calendar.DAY_OF_WEEK) + 5) % 7;
                start.add(Calendar.DAY_OF_YEAR, -daysSinceMonday);
            }
            Calendar end = (Calendar) start.clone();
            if ("monthly".equals(interval)) end.add(Calendar.MONTH, 1);
            else if ("yearly".equals(interval)) end.add(Calendar.YEAR, 1);
            else if (!"daily".equals(interval)) end.add(Calendar.WEEK_OF_YEAR, 1);
            else end.add(Calendar.DAY_OF_YEAR, 1);
            long startMillis = start.getTimeInMillis();
            periodsByStart.put(startMillis, new Period(0, startMillis, end.getTimeInMillis()));
        }
        List<Period> periods = new ArrayList<>(periodsByStart.values());
        periods.sort(Comparator.comparingLong(period -> period.startMillis));
        int first = Math.max(0, periods.size() - 4);
        List<Period> latest = new ArrayList<>(periods.subList(first, periods.size()));
        List<Period> indexed = new ArrayList<>(latest.size());
        for (int i = 0; i < latest.size(); i++) {
            Period period = latest.get(i);
            indexed.add(new Period(i + 1, period.startMillis, period.endMillis));
        }
        return indexed;
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

    static int[] latestPhotoIndices(List<Period> periods, List<Long> captureTimes) {
        int[] selected = new int[periods.size()];
        long[] latest = new long[periods.size()];
        for (int i = 0; i < selected.length; i++) {
            selected[i] = -1;
            latest[i] = Long.MIN_VALUE;
        }
        for (int photoIndex = 0; photoIndex < captureTimes.size(); photoIndex++) {
            long capturedAt = captureTimes.get(photoIndex);
            for (int periodIndex = 0; periodIndex < periods.size(); periodIndex++) {
                Period period = periods.get(periodIndex);
                if (capturedAt >= period.startMillis && capturedAt < period.endMillis
                        && capturedAt > latest[periodIndex]) {
                    latest[periodIndex] = capturedAt;
                    selected[periodIndex] = photoIndex;
                }
            }
        }
        return selected;
    }

    /** Returns the latest four saved photos in capture-time order, oldest first. */
    static int[] latestFourPhotoIndices(List<Long> captureTimes) {
        List<Integer> sorted = new ArrayList<>();
        for (int i = 0; i < captureTimes.size(); i++) sorted.add(i);
        sorted.sort(Comparator.comparingLong(captureTimes::get));
        int first = Math.max(0, sorted.size() - 4);
        int[] selected = new int[sorted.size() - first];
        for (int i = 0; i < selected.length; i++) selected[i] = sorted.get(first + i);
        return selected;
    }
}
