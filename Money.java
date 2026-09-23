package kebabshop;

import java.util.Locale;

/**
 * Tiny helper that formats prices as Australian dollars, e.g. "$13.90".
 * Locale.ENGLISH is used so the decimal separator is always a full stop,
 * whatever the computer's regional settings are.
 */
public final class Money {

    private Money() {
        // Utility class - no instances needed.
    }

    public static String format(double amount) {
        return String.format(Locale.ENGLISH, "$%.2f", amount);
    }
}
