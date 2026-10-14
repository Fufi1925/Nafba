package de.fufi.nafba;

/** Pure battery policy: strictly below the configured threshold, never while charging. */
public final class BatteryState {
    private BatteryState() {}
    public static int percent(int level, int scale) {
        if (level < 0 || scale <= 0) return -1;
        return Math.max(0, Math.min(100, Math.round(100f * level / scale)));
    }
    public static boolean isLow(int percent, boolean charging, int threshold, boolean enabled) {
        return enabled && !charging && percent >= 0 && percent < threshold;
    }
}
