package de.fufi.nafba;
import org.junit.Test;
import static org.junit.Assert.*;
public class BatteryStateTest {
    @Test public void strictThreshold() { assertTrue(BatteryState.isLow(19,false,20,true)); assertFalse(BatteryState.isLow(20,false,20,true)); }
    @Test public void chargingAndDisabledSuppressWarning() { assertFalse(BatteryState.isLow(5,true,20,true)); assertFalse(BatteryState.isLow(5,false,20,false)); }
    @Test public void unknownBatteryIsNotLow() { assertEquals(-1,BatteryState.percent(10,0)); assertEquals(-1,BatteryState.percent(-1,100)); assertFalse(BatteryState.isLow(-1,false,20,true)); }
    @Test public void convertsAndClamps() { assertEquals(50,BatteryState.percent(100,200)); assertEquals(100,BatteryState.percent(250,200)); assertTrue(BatteryState.isLow(0,false,20,true)); }
}
