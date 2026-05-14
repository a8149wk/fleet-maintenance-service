package com.fms.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class NumberUtils {

    private static final DecimalFormatSymbols ID_SYMBOLS = new DecimalFormatSymbols(new Locale("id", "ID"));
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0", ID_SYMBOLS);

    private NumberUtils() {
    }

    public static String formatRupiah(BigDecimal value) {
        if (value == null) return "Rp 0";
        return "Rp " + MONEY.format(value.setScale(0, RoundingMode.HALF_UP));
    }

    public static BigDecimal nullSafe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
