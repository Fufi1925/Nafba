package de.fufi.nafba;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;

/** System-owned activity/notification session. There is no overlay implementation. */
public final class NativeSessionService extends Service {
    public static final String STOP="de.fufi.nafba.STOP_NATIVE", TIMER="timer", BATTERY="battery";
    public static volatile boolean monitoring;
    public static volatile long timerDeadline;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private boolean receiverRegistered;
    private String mode;
    private final BroadcastReceiver batteryReceiver=new BroadcastReceiver() {
        @Override public void onReceive(Context c,Intent i) {
            int p=BatteryState.percent(i.getIntExtra(BatteryManager.EXTRA_LEVEL,-1),i.getIntExtra(BatteryManager.EXTRA_SCALE,-1));
            boolean charging=i.getIntExtra(BatteryManager.EXTRA_PLUGGED,0)!=0;
            if(BatteryState.isLow(p,charging,Prefs.number(c,"threshold",20),true)) NativeBackend.lowBattery(c,p);
            else getSystemService(NotificationManager.class).cancel(NativeBackend.BATTERY_ID);
        }
    };
    private final Runnable tick=new Runnable() {
        @Override public void run() {
            long left=timerDeadline-SystemClock.elapsedRealtime();
            if(left<=0) { stopSelf(); return; }
            if(NativeBackend.current.xiaomiReady()) getSystemService(NotificationManager.class).notify(NativeBackend.ACTIVITY_ID,NativeBackend.timer(NativeSessionService.this,left,System.currentTimeMillis()+left,stopIntent()));
            handler.postDelayed(this,1000);
        }
    };
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        if(intent==null || STOP.equals(intent.getAction())) { stopSelf(); return START_NOT_STICKY; }
        String requested=intent.getStringExtra("mode");
        if(requested==null) { stopSelf(); return START_NOT_STICKY; }
        if(mode!=null && !mode.equals(requested)) { stopSelf(); return START_NOT_STICKY; }
        mode=requested;
        if(!getSystemService(NotificationManager.class).areNotificationsEnabled()) { stopSelf(); return START_NOT_STICKY; }
        NativeBackend.channels(this);
        try {
            if(TIMER.equals(mode) && NativeBackend.current.timerReady()) {
                long duration=Math.max(15000,Math.min(120000,intent.getLongExtra("duration",60000)));
                timerDeadline=SystemClock.elapsedRealtime()+duration;
                Notification n=NativeBackend.timer(this,duration,System.currentTimeMillis()+duration,stopIntent());
                if(Build.VERSION.SDK_INT>=34) startForeground(NativeBackend.ACTIVITY_ID,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE);
                else startForeground(NativeBackend.ACTIVITY_ID,n);
                handler.removeCallbacks(tick); handler.post(tick);
            } else if(BATTERY.equals(mode) && NativeBackend.current.xiaomiReady()) {
                Notification n=new Notification.Builder(this,"monitor").setSmallIcon(R.drawable.ic_status)
                    .setContentTitle("Nafba beobachtet den Akku").setContentText("Native Akkuwarnung ist aktiviert.")
                    .addAction(new Notification.Action.Builder(null,"Beenden",stopIntent()).build()).setOngoing(true).build();
                if(Build.VERSION.SDK_INT>=34) startForeground(NativeBackend.ACTIVITY_ID,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
                else startForeground(NativeBackend.ACTIVITY_ID,n);
                monitoring=true;
                if(!receiverRegistered) {
                    IntentFilter filter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    if(Build.VERSION.SDK_INT>=33) registerReceiver(batteryReceiver,filter,Context.RECEIVER_NOT_EXPORTED); else registerReceiver(batteryReceiver,filter);
                    receiverRegistered=true;
                }
            } else stopSelf();
        } catch(RuntimeException denied) { stopSelf(); }
        return START_NOT_STICKY;
    }
    private PendingIntent stopIntent() { return PendingIntent.getService(this,2,new Intent(this,NativeSessionService.class).setAction(STOP),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE); }
    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null); if(receiverRegistered) unregisterReceiver(batteryReceiver);
        monitoring=false; timerDeadline=0;
        NotificationManager m=getSystemService(NotificationManager.class); m.cancel(NativeBackend.ACTIVITY_ID); m.cancel(NativeBackend.BATTERY_ID);
        super.onDestroy();
    }
    @Override public void onTimeout(int startId) { stopSelf(); }
    @Override public void onTimeout(int startId,int type) { stopSelf(); }
    @Override public IBinder onBind(Intent i) { return null; }
}
