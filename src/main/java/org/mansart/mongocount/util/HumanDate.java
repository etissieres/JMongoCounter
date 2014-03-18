package org.mansart.mongocount.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class HumanDate {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public static String format(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }

    public static String now() {
        return HumanDate.format(System.currentTimeMillis());
    }
}
