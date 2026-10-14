package de.fufi.nafba;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.graphics.PixelFormat;
import android.os.*;
import android.provider.Settings;
import android.view.*;

public final class IslandService extends Service {
    private static volatile boolean running;
    static boolean isRunning() { return running; }
    private WindowManager manager;
    private IslandView island;
    private int battery = -1;
    private boolean charging;
    private boolean registered;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                battery = BatteryState.percent(intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1),intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1));
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
                charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
                render();
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) { if(island != null) island.setVisibility(View.GONE); }
            else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) { if(island != null) island.setVisibility(View.VISIBLE); }
        }
    };
    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return START_NOT_STICKY; }
        try {
            startNotice();
            if (island == null) {
                manager = (WindowManager)getSystemService(WINDOW_SERVICE);
                island = new IslandView(this);
                island.setOnClickListener(v -> island.toggle());
                manager.addView(island, params());
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                filter.addAction(Intent.ACTION_SCREEN_OFF); filter.addAction(Intent.ACTION_USER_PRESENT);
                if(Build.VERSION.SDK_INT >= 33) registerReceiver(receiver,filter,Context.RECEIVER_NOT_EXPORTED); else registerReceiver(receiver,filter);
                registered = true;
                KeyguardManager keyguard = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
                if(keyguard.isKeyguardLocked()) island.setVisibility(View.GONE);
            } else manager.updateViewLayout(island,params());
            running = true; Prefs.get(this).edit().putBoolean("enabled",true).apply(); render();
        } catch (RuntimeException error) {
            Prefs.get(this).edit().putBoolean("enabled",false).apply(); stopSelf();
        }
        return START_NOT_STICKY;
    }
    private WindowManager.LayoutParams params() {
        int width = Math.max(220,Math.min(340,Prefs.number(this,"width",280)));
        int height = 74;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int pixelWidth = Math.min(dp(width),Math.max(dp(180),screenWidth-dp(24)));
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(pixelWidth,dp(height),WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        p.y = dp(Math.max(0,Math.min(120,Prefs.number(this,"top",28))));
        int maxOffset = Math.max(0,(screenWidth-pixelWidth)/2);
        p.x = Math.max(-maxOffset,Math.min(maxOffset,dp(Prefs.number(this,"offset",0))));
        p.alpha = .95f;
        if(Build.VERSION.SDK_INT >= 28) p.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        return p;
    }
    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private void render() { if(island != null) island.update(battery,charging,BatteryState.isLow(battery,charging,Prefs.number(this,"threshold",20),Prefs.flag(this,"low",true))); }
    private void startNotice() {
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel("island","Nafba Island",NotificationManager.IMPORTANCE_LOW));
        Intent open = new Intent(this,MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Intent stop = new Intent(this,MainActivity.class).setAction("de.fufi.nafba.STOP");
        PendingIntent stopPi = PendingIntent.getActivity(this,1,stop,PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n = new Notification.Builder(this,"island").setSmallIcon(de.fufi.nafba.R.drawable.ic_status)
            .setContentTitle("Nafba ist aktiv").setContentText("Tippe zum Anpassen deiner Island.").setContentIntent(pi).setOngoing(true)
            .addAction(new Notification.Action.Builder(null,"Ausschalten",stopPi).build()).build();
        if(Build.VERSION.SDK_INT >= 34) startForeground(1,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE); else startForeground(1,n);
    }
    @Override public void onDestroy() {
        running = false;
        if(registered) unregisterReceiver(receiver);
        if(island != null) { try { manager.removeView(island); } catch(IllegalArgumentException ignored) {} island = null; }
        Prefs.get(this).edit().putBoolean("enabled",false).apply(); super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public void onConfigurationChanged(android.content.res.Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if(island != null) { try { manager.updateViewLayout(island,params()); } catch(RuntimeException e) { stopSelf(); } }
    }
}
