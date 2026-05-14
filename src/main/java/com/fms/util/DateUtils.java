package com.fms.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private DateUtils() {
    }

    public static String format(LocalDateTime dt) {
        return dt == null ? "" : DATETIME.format(dt);
    }

    public static String format(LocalDate d) {
        return d == null ? "" : DATE.format(d);
    }
}
