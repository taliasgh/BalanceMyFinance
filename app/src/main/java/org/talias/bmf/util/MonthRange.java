package org.talias.bmf.util;

import java.util.Calendar;

/**
 * Converts calendar year/month into millisecond ranges for Room queries.
 * Months are 1–12 (January = 1), matching month spinners in the UI.
 */
public final class MonthRange {

    private MonthRange() {
    }

    /** First instant of the month (inclusive), in milliseconds since epoch. */
    public static long startOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month - 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /** First instant of the next month (exclusive end: dateMillis is before this value). */
    public static long startOfNextMonth(int year, int month) {
        if (month == 12) {
            return startOfMonth(year + 1, 1);
        }
        return startOfMonth(year, month + 1);
    }
}
