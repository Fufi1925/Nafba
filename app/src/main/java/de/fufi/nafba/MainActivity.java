package de.fufi.nafba;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.concurrent.*;

public final class MainActivity extends Activity {
    private FrameLayout root;
    private GlassUi.Backdrop backdrop;
    private GlassUi.Panel navigation;
    private ScrollView scroll;
    private LinearLayout page,tabRow;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private TextView nativeTitle,nativeDetail,timerLabel;
    private Button timerStart;
    private GlassUi.Toggle monitorToggle;
    private IslandSceneView liveBattery,timerPreview;
    private int activeTab=0,accent=GlassUi.PINK,battery=-1,duration=60,topInset,bottomInset;
    private boolean charging,registered;
    private final String[] tabs={"Übersicht","Island","Aktivitäten","Einstellungen"};
    private final BroadcastReceiver batteryReceiver=new BroadcastReceiver() {
        @Override public void onReceive(Context c,Intent i) {
            battery=BatteryState.percent(i.getIntExtra(BatteryManager.EXTRA_LEVEL,-1),i.getIntExtra(BatteryManager.EXTRA_SCALE,-1));
            charging=i.getIntExtra(BatteryManager.EXTRA_PLUGGED,0)!=0; updateBattery();
        }
    };
    private final Runnable clock=new Runnable() {
        @Override public void run() {
            long left=NativeSessionService.timerDeadline-SystemClock.elapsedRealtime();
            if(timerLabel!=null) timerLabel.setText(left>0?NativeBackend.time(left):"Deine nächste kleine Pause.");
            if(timerPreview!=null) timerPreview.time(left>0?NativeBackend.time(left):"0:21");
            if(monitorToggle!=null) monitorToggle.setChecked(NativeSessionService.monitoring);
            handler.postDelayed(this,1000);
        }
    };
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); activeTab=state==null?0:state.getInt("tab",0);
        Prefs.get(this).edit().remove("enabled").apply();
        if(Build.VERSION.SDK_INT>=30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        root=new FrameLayout(this);backdrop=new GlassUi.Backdrop(this);root.addView(backdrop,new FrameLayout.LayoutParams(-1,-1));
        scroll=new ScrollView(this);scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(false);root.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);scroll.addView(page);
        buildNavigation();root.setOnApplyWindowInsetsListener((v,insets) -> {
            if(Build.VERSION.SDK_INT>=30) {
                Insets safe=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());topInset=safe.top;bottomInset=safe.bottom;
                scroll.setPadding(safe.left,topInset,safe.right,0);
            } else { topInset=insets.getSystemWindowInsetTop();bottomInset=insets.getSystemWindowInsetBottom();scroll.setPadding(0,topInset,0,0); }
            positionNavigation(); return insets;
        });
        scroll.setOnScrollChangeListener((v,x,y,oldX,oldY) -> navigation.captureFrost());
        setContentView(root);renderTab(false);
    }
    @Override protected void onSaveInstanceState(Bundle b) { b.putInt("tab",activeTab);super.onSaveInstanceState(b); }
    @Override protected void onStart() {
        super.onStart();IntentFilter f=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        if(Build.VERSION.SDK_INT>=33) registerReceiver(batteryReceiver,f,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(batteryReceiver,f);registered=true;
        handler.post(clock);
    }
    @Override protected void onResume() { super.onResume();checkNative(); }
    @Override protected void onStop() { if(registered) { unregisterReceiver(batteryReceiver);registered=false; }handler.removeCallbacks(clock);super.onStop(); }
    @Override protected void onDestroy() { worker.shutdownNow();handler.removeCallbacksAndMessages(null);super.onDestroy(); }
    private int dp(float n) { return GlassUi.dp(this,n); }
    private void buildNavigation() {
        navigation=new GlassUi.Panel(this,backdrop,37);navigation.setTag("bottom_navigation");
        navigation.setBackground(GlassUi.shape(0xFFF8F8FC,37,this));
        navigation.contentBehind=scroll;
        tabRow=new LinearLayout(this);tabRow.setGravity(Gravity.CENTER);tabRow.setPadding(dp(5),dp(5),dp(5),dp(5));navigation.addView(tabRow,new FrameLayout.LayoutParams(-1,-1));
        for(int i=0;i<tabs.length;i++) {
            final int target=i;LinearLayout item=new LinearLayout(this);item.setOrientation(LinearLayout.VERTICAL);item.setGravity(Gravity.CENTER);item.setTag("tab_"+i);item.setContentDescription(tabs[i]);item.setFocusable(true);
            item.addView(new GlassUi.IconView(this,i,GlassUi.MUTED),new LinearLayout.LayoutParams(-1,dp(31)));
            TextView label=GlassUi.text(this,tabs[i],10,GlassUi.MUTED,false);label.setGravity(Gravity.CENTER);label.setSingleLine(true);item.addView(label,new LinearLayout.LayoutParams(-1,-2));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);p.setMargins(dp(2),0,dp(2),0);tabRow.addView(item,p);
            item.setOnClickListener(v -> { if(activeTab==target) { scroll.smoothScrollTo(0,0);return; }activeTab=target;renderTab(true); });
        }
        root.addView(navigation);positionNavigation();
    }
    private void positionNavigation() {
        FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,dp(74),Gravity.BOTTOM);p.setMargins(dp(18),0,dp(18),bottomInset+dp(12));navigation.setLayoutParams(p);
    }
    private void renderTab(boolean animate) {
        page.removeAllViews();page.setPadding(dp(24),dp(20),dp(24),bottomInset+dp(114));
        nativeTitle=null;nativeDetail=null;monitorToggle=null;timerLabel=null;timerStart=null;timerPreview=null;liveBattery=null;
        label(page,"NAFBA",11,GlassUi.MUTED,12);
        TextView heading=label(page,tabs[activeTab],34,GlassUi.INK,8);heading.setTypeface(android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.BOLD));
        String[] intros={"Deine Island. Deine kleinen Momente.","Vertraute Formen. Vom System gezeichnet.","Zeit, Akku und das, was gerade zählt.","Ganz nach deinem Gefühl."};
        label(page,intros[activeTab],14,GlassUi.MUTED,25);
        if(activeTab==0) overview();else if(activeTab==1) islandGallery();else if(activeTab==2) activities();else settings();
        label(page,"Nafba 2.0 · Native Edition",11,GlassUi.MUTED,0);
        scroll.scrollTo(0,0);updateTabs();updateNative();
        navigation.post(navigation::captureFrost);
        if(animate && Prefs.flag(this,"motion",true)) { page.setAlpha(.2f);page.setTranslationY(dp(7));page.animate().alpha(1).translationY(0).setDuration(230).start(); }
        else { page.setAlpha(1);page.setTranslationY(0); }
    }
    private void updateTabs() {
        for(int i=0;i<tabRow.getChildCount();i++) {
            LinearLayout item=(LinearLayout)tabRow.getChildAt(i);boolean selected=i==activeTab;item.setSelected(selected);
            item.setBackground(selected?GlassUi.shape(0xDFFFFFFF,29,this):null);
            ((GlassUi.IconView)item.getChildAt(0)).tint(selected?accent:0xFF85858F);
            ((TextView)item.getChildAt(1)).setTextColor(selected?accent:0xFF85858F);
        }
        navigation.frost.setAlpha(Prefs.flag(this,"glass",true)?.48f:0);
    }
    private void overview() {
        LinearLayout hero=card();label(hero,"DEINE ISLAND",11,GlassUi.MUTED,10);
        liveBattery=new IslandSceneView(this,IslandSceneView.BATTERY);add(hero,liveBattery,76,0);updateBattery();
        label(hero,"Ein Blick genügt.",24,GlassUi.INK,8);
        label(hero,"Akku und Aktivitäten in vertrauten, ruhigen Formen.",13,GlassUi.MUTED,0);
        nativeCard();
        LinearLayout sample=card();label(sample,"DEINE MOMENTE",11,GlassUi.MUTED,7);
        IslandSceneView t=new IslandSceneView(this,IslandSceneView.TIMER);add(sample,t,86,5);
        label(sample,"Zeit für dich",20,GlassUi.INK,6);label(sample,"Starte einen kurzen Timer als native Aktivität.",13,GlassUi.MUTED,12);
        action(sample,"Zu den Aktivitäten",false,() -> selectTab(2));
        LinearLayout styles=card();label(styles,"Alle Formen entdecken",21,GlassUi.INK,8);label(styles,"Musik, Anruf, Fokus und mehr — als Design-Vorschau in Nafba.",13,GlassUi.MUTED,12);action(styles,"Island ansehen",false,() -> selectTab(1));
    }
    private void islandGallery() {
        LinearLayout notice=card();label(notice,"Deine native Island",21,GlassUi.INK,7);
        label(notice,"Diese Ansichten sind Vorschauen innerhalb von Nafba. Größe, Bewegung und Darstellung außerhalb der App übernimmt dein System.",13,GlassUi.MUTED,0);
        for(int i=0;i<8;i++) {
            int scene=i;LinearLayout box=card();LinearLayout title=new LinearLayout(this);title.setGravity(Gravity.CENTER_VERTICAL);
            TextView name=GlassUi.text(this,IslandSceneView.label(i),19,GlassUi.INK,true);title.addView(name,new LinearLayout.LayoutParams(0,-2,1));
            TextView pill=GlassUi.text(this,(i==0||i==1)?"NATIV / VORSCHAU":"SYSTEM / VORSCHAU",9,GlassUi.MUTED,false);title.addView(pill);add(box,title,-2,8);
            IslandSceneView view=new IslandSceneView(this,i);add(box,view,i==IslandSceneView.MUSIC?192:i==IslandSceneView.CALL?88:80,7);
            String[] details={"Low Battery unter deiner eingestellten Grenze.","Ein kurzer Countdown für den nächsten Moment.","Albumcover, Titel, Fortschritt und Wiedergabe.","Kontakt und die vertrauten Anruf-Aktionen.","Richtung, Entfernung und das nächste Ziel.","Ein ruhiger Hinweis auf deinen Fokus.","Aufnahmezeit und der letzte gespeicherte Moment.","Ladestatus mit einer klaren grünen Akzentfarbe."};
            label(box,details[i],12,GlassUi.MUTED,0);
            view.setOnClickListener(v -> {
                if(scene==0||scene==1) selectTab(2);
                else new android.app.AlertDialog.Builder(this).setTitle(IslandSceneView.label(scene)).setMessage("Design-Vorschau. Diese Aktivität wird von der jeweiligen System- oder Drittanbieter-App bereitgestellt. Nafba greift nicht in fremde Anrufe, Aufnahmen oder Musik ein.").setPositiveButton("Verstanden",null).show();
            });
        }
    }
    private void activities() {
        LinearLayout box=card();label(box,"Timer",24,GlassUi.INK,7);
        timerPreview=new IslandSceneView(this,IslandSceneView.TIMER);add(box,timerPreview,88,8);timerLabel=label(box,"Deine nächste kleine Pause.",13,GlassUi.MUTED,16);
        LinearLayout times=new LinearLayout(this);for(int seconds:new int[]{30,60,120}) {
            Button b=GlassUi.button(this,seconds<60?"30 Sek.":seconds/60+" Min.",accent,false);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),1);p.setMargins(dp(3),0,dp(3),0);times.addView(b,p);
            b.setOnClickListener(v -> { duration=seconds;timerLabel.setText("Ausgewählt: "+NativeBackend.time(seconds*1000L)); });
        }add(box,times,46,12);
        timerStart=action(box,"Timer starten",true,() -> begin(NativeSessionService.TIMER));
        action(box,"Aktivität beenden",false,this::stopSession);
        label(box,"Die native Anzeige setzt eine Freigabe durch dein System voraus.",12,GlassUi.MUTED,0);
        LinearLayout low=card();label(low,"Akku",24,GlassUi.INK,9);
        IslandSceneView preview=new IslandSceneView(this,IslandSceneView.BATTERY);add(low,preview,76,12);
        monitorToggle=toggle(low,"Automatische Akkuwarnung",NativeSessionService.monitoring,on -> { if(on) begin(NativeSessionService.BATTERY);else stopSession(); });
        label(low,"Die Akkuwarnung verwendet ausschließlich die freigegebene Xiaomi-HyperIsland-Anbindung.",12,GlassUi.MUTED,12);
        slider(low); nativeCard();
    }
    private void settings() {
        LinearLayout look=card();label(look,"Darstellung",22,GlassUi.INK,16);
        toggle(look,"Sanfte Übergänge",Prefs.flag(this,"motion",true),on -> Prefs.get(this).edit().putBoolean("motion",on).apply());
        divider(look);
        toggle(look,"Glas-Effekt",Prefs.flag(this,"glass",true),on -> { Prefs.get(this).edit().putBoolean("glass",on).apply();updateTabs(); });
        label(look,"Der Glas-Effekt verwendet nur Inhalte aus Nafba.",12,GlassUi.MUTED,0);
        LinearLayout batteryBox=card();label(batteryBox,"Akkuwarnung",22,GlassUi.INK,12);slider(batteryBox);
        nativeCard();
        LinearLayout device=card();label(device,"Dein Gerät",22,GlassUi.INK,12);
        label(device,Build.MANUFACTURER+" "+Build.MODEL,16,GlassUi.INK,8);
        label(device,"Android "+Build.VERSION.RELEASE+" · "+Build.VERSION.INCREMENTAL,12,GlassUi.MUTED,14);
        action(device,"Geräteinfos kopieren",false,() -> {
            getSystemService(ClipboardManager.class).setPrimaryClip(ClipData.newPlainText("Nafba Gerät",deviceInfo()));Toast.makeText(this,"Geräteinfos kopiert",Toast.LENGTH_SHORT).show();
        });
        LinearLayout about=card();label(about,"Privat. Lokal. Dein.",22,GlassUi.INK,10);
        label(about,"Kein Konto, keine Werbung und kein Zugriff auf andere Bildschirme. Nafba fordert keine Erlaubnis zum Zeichnen über anderen Apps an.",13,GlassUi.MUTED,0);
    }
    private String deviceInfo() { return Build.MANUFACTURER+" "+Build.MODEL+"\nAndroid "+Build.VERSION.RELEASE+"\nBuild "+Build.VERSION.INCREMENTAL+"\nXiaomi-Protokoll "+NativeBackend.current.protocol; }
    private void nativeCard() {
        LinearLayout nativeBox=card();label(nativeBox,"NATIVE VERBINDUNG",11,GlassUi.MUTED,9);
        nativeTitle=label(nativeBox,NativeBackend.current.title(),21,GlassUi.INK,8);nativeDetail=label(nativeBox,NativeBackend.current.detail(),13,GlassUi.MUTED,12);
        action(nativeBox,"Gerät erneut prüfen",false,this::checkNative);action(nativeBox,"Systemeinstellungen",false,() -> NativeBackend.settings(this));
    }
    private void checkNative() {
        if(worker.isShutdown()) return;
        if(nativeTitle!=null) nativeTitle.setText("Gerät wird geprüft …");
        worker.execute(() -> { try { NativeBackend.query(getApplicationContext()); } catch(RuntimeException unavailable) { /* keep unknown rather than claim support */ }
            handler.post(() -> { if(!isDestroyed()) updateNative(); }); });
    }
    private void updateNative() {
        if(nativeTitle!=null) nativeTitle.setText(NativeBackend.current.title());if(nativeDetail!=null) nativeDetail.setText(NativeBackend.current.detail());
        if(timerStart!=null) { timerStart.setEnabled(NativeBackend.current.timerReady());timerStart.setAlpha(NativeBackend.current.timerReady()?1:.4f); }
        if(monitorToggle!=null) { monitorToggle.setEnabled(NativeBackend.current.xiaomiReady());monitorToggle.setAlpha(NativeBackend.current.xiaomiReady()?1:.4f); }
    }
    private void begin(String mode) {
        boolean allowed=NativeSessionService.BATTERY.equals(mode)?NativeBackend.current.xiaomiReady():NativeBackend.current.timerReady();
        if(!allowed) { Toast.makeText(this,"Native Freigabe fehlt. Prüfe die Verbindung in den Einstellungen.",Toast.LENGTH_LONG).show();return; }
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},9);Toast.makeText(this,"Erlaube Benachrichtigungen und starte danach erneut.",Toast.LENGTH_LONG).show();return;
        }
        if((NativeSessionService.monitoring && NativeSessionService.TIMER.equals(mode)) || (NativeSessionService.timerDeadline>0 && NativeSessionService.BATTERY.equals(mode))) {
            Toast.makeText(this,"Beende zuerst die laufende Nafba-Aktivität.",Toast.LENGTH_LONG).show();return;
        }
        try { startForegroundService(new Intent(this,NativeSessionService.class).putExtra("mode",mode).putExtra("duration",duration*1000L)); }
        catch(RuntimeException blocked) { Toast.makeText(this,"Das System hat die Aktivität nicht freigegeben.",Toast.LENGTH_LONG).show(); }
    }
    private void stopSession() { stopService(new Intent(this,NativeSessionService.class));NativeSessionService.timerDeadline=0;NativeSessionService.monitoring=false; }
    private void selectTab(int tab) { activeTab=tab;renderTab(true); }
    private void updateBattery() { if(liveBattery!=null) liveBattery.battery(battery,BatteryState.isLow(battery,charging,Prefs.number(this,"threshold",20),true)); }
    private TextView label(LinearLayout parent,String value,int size,int color,int bottom) {
        TextView t=GlassUi.text(this,value,size,color,size>=19);add(parent,t,-2,bottom);return t;
    }
    private void add(LinearLayout parent,View view,int height,int bottom) { LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,height<0?height:dp(height));p.bottomMargin=dp(bottom);parent.addView(view,p); }
    private LinearLayout card() {
        GlassUi.Panel panel=new GlassUi.Panel(this,backdrop,25);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(20),dp(20),dp(20),dp(20));
        panel.addView(box,new FrameLayout.LayoutParams(-1,-2));add(page,panel,-2,18);return box;
    }
    private Button action(LinearLayout parent,String title,boolean filled,Runnable run) { Button b=GlassUi.button(this,title,accent,filled);add(parent,b,50,10);b.setOnClickListener(v -> run.run());return b; }
    private GlassUi.Toggle toggle(LinearLayout parent,String title,boolean value,java.util.function.Consumer<Boolean> listener) {
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView label=GlassUi.text(this,title,15,GlassUi.INK,false);row.addView(label,new LinearLayout.LayoutParams(0,-2,1));
        GlassUi.Toggle toggle=new GlassUi.Toggle(this,value);toggle.setContentDescription(title+(value?" · Ein":" · Aus"));row.addView(toggle,new LinearLayout.LayoutParams(dp(58),dp(48)));add(parent,row,52,7);
        toggle.setOnClickListener(v -> { boolean next=!toggle.checked;toggle.setChecked(next);listener.accept(next); });return toggle;
    }
    private void slider(LinearLayout parent) {
        TextView value=label(parent,getString(R.string.setting_value,"Warnen unter",Prefs.number(this,"threshold",20)," %"),15,GlassUi.INK,0);
        SeekBar seek=new SeekBar(this);seek.setContentDescription("Akku-Grenze");seek.setMax(45);seek.setProgress(Prefs.number(this,"threshold",20)-5);
        seek.setProgressTintList(android.content.res.ColorStateList.valueOf(accent));seek.setThumbTintList(android.content.res.ColorStateList.valueOf(accent));add(parent,seek,48,5);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s,int progress,boolean user) { value.setText(getString(R.string.setting_value,"Warnen unter",progress+5," %"));if(user) Prefs.get(MainActivity.this).edit().putInt("threshold",progress+5).apply(); }
            public void onStartTrackingTouch(SeekBar s) {} public void onStopTrackingTouch(SeekBar s) {}
        });
    }
    private void divider(LinearLayout box) { View line=new View(this);line.setBackgroundColor(0x1180818C);add(box,line,1,7); }
}
