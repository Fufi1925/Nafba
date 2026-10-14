package de.fufi.nafba;
import org.junit.Test;
import static org.junit.Assert.*;
public class NativePolicyTest {
    @Test public void xiaomiNeedsEveryCapability() {
        assertTrue(NativePolicy.xiaomiReady(3,true,true));
        assertFalse(NativePolicy.xiaomiReady(2,true,true));
        assertFalse(NativePolicy.xiaomiReady(3,false,true));
        assertFalse(NativePolicy.xiaomiReady(3,true,false));
    }
    @Test public void publicTimerRequiresAndroid16AndPermission() {
        assertFalse(NativePolicy.timerReady(false,3500000,true));
        assertFalse(NativePolicy.timerReady(false,3600000,true));
        assertFalse(NativePolicy.timerReady(false,3600001,false));
        assertTrue(NativePolicy.timerReady(false,3600001,true));
        assertTrue(NativePolicy.timerReady(true,3500000,false));
    }
    @Test public void batteryAlertCannotMasqueradeAsLiveUpdate() {
        assertFalse(NativePolicy.lowBatteryNativeReady(false));
        assertTrue(NativePolicy.lowBatteryNativeReady(true));
    }
}
