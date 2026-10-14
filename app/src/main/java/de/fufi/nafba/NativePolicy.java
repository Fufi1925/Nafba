package de.fufi.nafba;

/** A model name alone never proves native island support. No simulated system fallback. */
public final class NativePolicy {
    private NativePolicy() {}
    public static boolean xiaomiReady(int protocol, boolean permission, boolean appRegistered) {
        return protocol >= 3 && permission && appRegistered;
    }
    public static boolean timerReady(boolean xiaomiReady, int fullApi, boolean promotedAllowed) {
        return xiaomiReady || (fullApi >= 3600001 && promotedAllowed);
    }
    public static boolean lowBatteryNativeReady(boolean xiaomiReady) { return xiaomiReady; }
}
