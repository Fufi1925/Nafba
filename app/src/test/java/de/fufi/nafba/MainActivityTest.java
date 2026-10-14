package de.fufi.nafba;

import android.Manifest;
import android.graphics.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.*;
import java.io.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w393dp-h852dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MainActivityTest {
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    @Before public void start() { controller=Robolectric.buildActivity(MainActivity.class).setup();activity=controller.get(); }
    @After public void stop() { controller.pause().stop().destroy(); }
    private View root() { return ((android.widget.FrameLayout)activity.findViewById(android.R.id.content)).getChildAt(0); }
    private void capture(String file) throws IOException {
        View v=root();v.measure(View.MeasureSpec.makeMeasureSpec(786,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1704,View.MeasureSpec.EXACTLY));v.layout(0,0,786,1704);
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        Bitmap b=Bitmap.createBitmap(786,1704,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));
        File out=new File("build/outputs/screenshots/"+file);out.getParentFile().mkdirs();try(FileOutputStream stream=new FileOutputStream(out)) { assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,stream)); }
    }
    @Test public void fourBottomTabsNavigateAndRestore() {
        for(int i=0;i<4;i++) assertNotNull(root().findViewWithTag("tab_"+i));
        root().findViewWithTag("tab_3").performClick();assertTrue(root().findViewWithTag("tab_3").isSelected());
        controller.recreate();activity=controller.get();assertTrue(root().findViewWithTag("tab_3").isSelected());
    }
    @Test public void apkDeclaresNoOverlayPermission() throws Exception {
        PackageInfo p=activity.getPackageManager().getPackageInfo(activity.getPackageName(),PackageManager.GET_PERMISSIONS);
        assertFalse(Arrays.asList(p.requestedPermissions).contains(Manifest.permission.SYSTEM_ALERT_WINDOW));
    }
    @Test public void renderRealAndroidOverviewAndGallery() throws Exception {
        activity.getSharedPreferences("island",0).edit().putBoolean("motion",false).commit();
        capture("overview.png");root().findViewWithTag("tab_1").performClick();capture("island.png");root().findViewWithTag("tab_3").performClick();capture("settings.png");
    }
}
