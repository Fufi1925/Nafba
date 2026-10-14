package de.fufi.nafba;

import android.app.*;
import android.content.*;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import org.json.*;

/** Official system notification paths only. Never creates a WindowManager window. */
public final class NativeBackend {
    public static final String CHANNEL = "native_activity";
    public static final int ACTIVITY_ID = 21, BATTERY_ID = 22;
    public static volatile Status current = new Status(0,false,false,false);
    public static final class Status {
        public final int protocol;
        public final boolean focusPermission, registered, promoted;
        Status(int protocol, boolean focusPermission, boolean registered, boolean promoted) {
            this.protocol=protocol; this.focusPermission=focusPermission; this.registered=registered; this.promoted=promoted;
        }
        public boolean xiaomiReady() { return NativePolicy.xiaomiReady(protocol,focusPermission,registered); }
        public boolean timerReady() { return NativePolicy.timerReady(xiaomiReady(),fullApi(),promoted); }
        public String title() {
            if(xiaomiReady()) return "HyperIsland freigegeben";
            if(promoted && Build.VERSION.SDK_INT>=36) return "Live Updates verfügbar";
            if(protocol>=3) return "HyperIsland erkannt";
            return "Native Freigabe prüfen";
        }
        public String detail() {
            if(xiaomiReady()) return "Die native Xiaomi-Anbindung ist verfügbar. Das System zeichnet deine Island.";
            if(promoted && Build.VERSION.SDK_INT>=36) return "Timer können Android Live Updates anfordern. Die Darstellung legt dein System fest.";
            if(protocol>=3) return "Für eigene Island-Meldungen fehlen Nafba noch die Xiaomi-App-/Signaturfreigabe oder die Geräteberechtigung.";
            return "Auf diesem Gerät wurde noch keine nutzbare native Island-Schnittstelle bestätigt.";
        }
    }
    /** Xiaomi documents this provider call as slow; invoke on a worker thread. */
    public static Status query(Context context) {
        int protocol=Settings.System.getInt(context.getContentResolver(),"notification_focus_protocol",0);
        boolean permission=false, promoted=false;
        if(protocol>=3) {
            try {
                Bundle request=new Bundle(); request.putString("package",context.getPackageName());
                Bundle result=context.getContentResolver().call(Uri.parse("content://miui.statusbar.notification.public"),"canShowFocus",null,request);
                permission=result!=null && result.getBoolean("canShowFocus",false);
            } catch(RuntimeException ignored) { /* No vendor permission means unavailable, never a drawn replacement. */ }
        }
        if(Build.VERSION.SDK_INT>=36 && fullApi()>=3600001) promoted=context.getSystemService(NotificationManager.class).canPostPromotedNotifications();
        Status status=new Status(protocol,permission,!BuildConfig.XIAOMI_APP_ID.isEmpty(),promoted);
        current=status; return status;
    }
    public static void channels(Context c) {
        NotificationManager m=c.getSystemService(NotificationManager.class);
        m.createNotificationChannel(new NotificationChannel(CHANNEL,"Native Aktivitäten",NotificationManager.IMPORTANCE_DEFAULT));
        m.createNotificationChannel(new NotificationChannel("monitor","Akku-Beobachtung",NotificationManager.IMPORTANCE_LOW));
    }
    public static Notification timer(Context c,long remaining,long endWallClock,PendingIntent stop) {
        channels(c);
        Notification.Builder b=base(c,"Timer läuft","Deine Zeit im Blick.")
            .setCategory(Notification.CATEGORY_STOPWATCH).setOngoing(true).setOnlyAlertOnce(true)
            .setWhen(endWallClock).setUsesChronometer(true).setChronometerCountDown(true)
            .addAction(new Notification.Action.Builder(Icon.createWithResource(c,R.drawable.ic_status),"Beenden",stop).build());
        if(Build.VERSION.SDK_INT>=36 && fullApi()>=3600001 && current.promoted) {
            // Public 36.1 extra; keep compilation on SDK 36 without using hidden APIs.
            Bundle live=new Bundle();live.putBoolean("android.requestPromotedOngoing",true);
            b.addExtras(live).setShortCriticalText(time(remaining));
        }
        if(current.xiaomiReady()) b.addExtras(xiaomi(c,"timer","Timer",time(remaining),true));
        return b.build();
    }
    public static boolean lowBattery(Context c,int percent) {
        if(!current.xiaomiReady()) return false;
        channels(c);
        Notification n=base(c,"Akku niedrig",percent+" % verbleibend")
            .setOnlyAlertOnce(true).addExtras(xiaomi(c,"battery","Akku niedrig",percent+"%",false)).build();
        // Battery alerts are NOT misrepresented as Android promoted ongoing activities.
        try { c.getSystemService(NotificationManager.class).notify(BATTERY_ID,n); return true; }
        catch(SecurityException denied) { return false; }
    }
    private static Notification.Builder base(Context c,String title,String body) {
        PendingIntent open=PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(c,CHANNEL).setSmallIcon(R.drawable.ic_status).setContentTitle(title).setContentText(body).setContentIntent(open);
    }
    /** Xiaomi Developer Guide pId=2131 + official template library: image/text component type 1. */
    private static Bundle xiaomi(Context c,String business,String title,String value,boolean ongoing) {
        Bundle extras=new Bundle(), pictures=new Bundle();
        pictures.putParcelable("miui.focus.pic_nafba",Icon.createWithResource(c,R.drawable.ic_status));
        extras.putBundle("miui.focus.pics",pictures);
        try {
            JSONObject pic=new JSONObject().put("type",1).put("pic","miui.focus.pic_nafba");
            JSONObject text=new JSONObject().put("title",title).put("content",value).put("showHighlightColor",false);
            JSONObject component=new JSONObject().put("type",1).put("picInfo",pic).put("textInfo",text);
            JSONObject island=new JSONObject().put("islandProperty",1)
                .put("bigIslandArea",new JSONObject().put("imageTextInfoLeft",component).put("picInfo",pic))
                .put("smallIslandArea",new JSONObject().put("picInfo",pic));
            JSONObject data=new JSONObject().put("protocol",1).put("business",business).put("updatable",ongoing)
                .put("enableFloat",false).put("islandFirstFloat",true).put("filterWhenNoPermission",true)
                .put("aodTitle",title+" · "+value).put("param_island",island)
                .put("baseInfo",new JSONObject().put("type",2).put("title",title).put("content",value));
            extras.putString("miui.focus.param",new JSONObject().put("param_v2",data).toString());
        } catch(JSONException invalid) { throw new IllegalStateException("Invalid native island payload",invalid); }
        return extras;
    }
    public static String time(long millis) { long s=Math.max(0,millis/1000); return String.format(java.util.Locale.ROOT,"%d:%02d",s/60,s%60); }
    private static int fullApi() { return Build.VERSION.SDK_INT>=36?Build.VERSION.SDK_INT_FULL:Build.VERSION.SDK_INT*100000; }
    public static void settings(Context c) {
        Intent i;
        i=new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,c.getPackageName());
        try { c.startActivity(i); } catch(ActivityNotFoundException unavailable) { c.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+c.getPackageName()))); }
    }
}
