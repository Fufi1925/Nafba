package de.fufi.nafba;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

public final class MainActivity extends Activity {
    private final int ink = Color.rgb(235,245,242), muted = Color.rgb(161,185,187), mint = Color.rgb(163,244,220);
    private LinearLayout content;
    private Switch enabled;
    private TextView permission;
    private IslandView preview;
    private boolean syncing;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if("de.fufi.nafba.STOP".equals(getIntent().getAction())) stopIsland();
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        if (Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        scroll.setOnApplyWindowInsetsListener((v,insets) -> {
            if(Build.VERSION.SDK_INT >= 30) {
                Insets safe = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                v.setPadding(safe.left,safe.top,safe.right,safe.bottom);
            } else v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        scroll.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xFF152F38,0xFF101820,0xFF192D2B}));
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(24),dp(24),dp(24),dp(36)); scroll.addView(content); setContentView(scroll);
        text(content,"NAFBA  /  V1",12,mint,18);
        text(content,"Kleine Island.\nGroßes Gefühl.",36,ink,12);
        text(content,"Dein Android. Ein Hauch Glas. Alles an deinem Platz.",15,muted,24);
        LinearLayout hero = card();
        TextView tag = text(hero,"LIVE-VORSCHAU",11,muted,12);
        preview = new IslandView(this); hero.addView(preview,new LinearLayout.LayoutParams(-1,dp(82))); preview.update(76,false,false); preview.setOnClickListener(v -> preview.toggle());
        text(hero,"Tippe auf die Island, um sie zu öffnen.",12,muted,0);
        LinearLayout activation = card();
        enabled = toggle(activation,"Island aktivieren","Über deinen Apps sichtbar",Prefs.flag(this,"enabled",false));
        enabled.setOnCheckedChangeListener((button,on) -> {
            if(syncing) return;
            if(on) {
                if(!Settings.canDrawOverlays(this)) {
                    syncing=true; enabled.setChecked(false); syncing=false;
                    new android.app.AlertDialog.Builder(this).setTitle("Platz für deine Island")
                        .setMessage("Erlaube Nafba, über anderen Apps angezeigt zu werden. Kehre danach hierher zurück und aktiviere die Island.")
                        .setPositiveButton("Einstellungen öffnen",(d,w) -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()))))
                        .setNegativeButton("Später",null).show();
                    return;
                }
                if(Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},10);
                refreshService(true);
            } else stopIsland();
        });
        permission = text(activation,"",12,muted,0);
        LinearLayout position = card(); text(position,"Deine Form",21,ink,12);
        slider(position,"Breite","width",220,340,280," dp");
        slider(position,"Abstand von oben","top",0,120,28," dp");
        slider(position,"Seitliche Position","offset",-100,100,0," dp");
        text(position,"Passe die Position an deine Frontkamera an. Die Island kann Statussymbole überdecken.",12,muted,0);
        LinearLayout battery = card(); text(battery,"Akku-Momente",21,ink,12);
        Switch low = toggle(battery,"Low Battery Bar","Warnung nur unter deiner Grenze",Prefs.flag(this,"low",true));
        low.setOnCheckedChangeListener((b,on) -> { Prefs.get(this).edit().putBoolean("low",on).apply(); refreshService(false); });
        slider(battery,"Warnen unter","threshold",5,50,20," %");
        Button demo = button("Akkuwarnung ansehen"); battery.addView(demo); demo.setOnClickListener(v -> { preview.update(19,false,true); scroll.smoothScrollTo(0,0); });
        Button reset = button("Normale Vorschau"); battery.addView(reset); reset.setOnClickListener(v -> preview.update(76,false,false));
        LinearLayout feel = card(); text(feel,"Das Gefühl",21,ink,12);
        Switch motion = toggle(feel,"Sanfte Animationen","Deaktivieren für weniger Bewegung",Prefs.flag(this,"motion",true));
        motion.setOnCheckedChangeListener((b,on) -> { Prefs.get(this).edit().putBoolean("motion",on).apply(); refreshService(false); preview.update(76,false,false); });
        text(feel,"Glas-Verläufe, feine Lichtkanten und ruhige Mint-Töne. Kein Zugriff auf deine Benachrichtigungen, kein Konto, keine Werbung.",13,muted,0);
        LinearLayout device = card(); text(device,"Für dein POCO / Redmi",21,ink,10);
        text(device,"Falls HyperOS oder MIUI die Island beendet: Öffne die App-Info und prüfe Hintergrundaktivität, Akku-Einstellungen und die Erlaubnis für schwebende Fenster. Die Menünamen unterscheiden sich je nach Gerät.",13,muted,12);
        Button info = button("App-Einstellungen öffnen"); device.addView(info); info.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()))));
        text(content,"NAFBA  ·  Made for your everyday\nV1.0.0  ·  Von Fufi1925",12,muted,0);
    }
    @Override protected void onResume() { super.onResume(); if(enabled != null) { boolean active=IslandService.isRunning(); Prefs.get(this).edit().putBoolean("enabled",active).apply(); syncing=true; enabled.setChecked(active); syncing=false; permission.setText(Settings.canDrawOverlays(this) ? "Overlay-Erlaubnis erteilt" : "Overlay-Erlaubnis noch erforderlich"); } }
    private void refreshService(boolean start) {
        if(start || Prefs.flag(this,"enabled",false)) {
            try { startForegroundService(new Intent(this,IslandService.class)); }
            catch(RuntimeException e) { stopIsland(); Toast.makeText(this,"Island konnte nicht starten. Prüfe die App-Berechtigungen.",Toast.LENGTH_LONG).show(); }
        }
    }
    private void stopIsland() { stopService(new Intent(this,IslandService.class)); Prefs.get(this).edit().putBoolean("enabled",false).apply(); }
    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(20),dp(20),dp(20),dp(20));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xAA30464C,0xAA202E34}); bg.setCornerRadius(dp(28)); bg.setStroke(dp(1),0x30FFFFFF); box.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(16); content.addView(box,p); return box;
    }
    private TextView text(LinearLayout box,String value,int size,int color,int bottom) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); t.setFontFeatureSettings("kern");
        t.setTypeface(Typeface.create(size>=21 ? "sans-serif-medium" : "sans-serif",Typeface.NORMAL));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(bottom); box.addView(t,p); return t;
    }
    private Switch toggle(LinearLayout box,String title,String subtitle,boolean checked) {
        Switch sw = new Switch(this); sw.setText(title); sw.setTextColor(ink); sw.setTextSize(16); sw.setMinHeight(dp(48)); sw.setChecked(checked); box.addView(sw);
        text(box,subtitle,12,muted,10); return sw;
    }
    private void slider(LinearLayout box,String label,String key,int min,int max,int fallback,String suffix) {
        int current=Prefs.number(this,key,fallback); TextView title=text(box,label+"  ·  "+current+suffix,14,ink,0);
        SeekBar s = new SeekBar(this); s.setMax(max-min); s.setProgress(current-min); s.setContentDescription(label); box.addView(s,new LinearLayout.LayoutParams(-1,dp(48)));
        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar,int p,boolean user) { title.setText(getString(R.string.setting_value,label,p+min,suffix)); if(user) Prefs.get(MainActivity.this).edit().putInt(key,p+min).apply(); }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) { refreshService(false); }
        });
    }
    private Button button(String title) {
        Button b = new Button(this); b.setText(title); b.setAllCaps(false); b.setTextColor(mint); b.setTextSize(14); b.setMinHeight(dp(48));
        GradientDrawable bg=new GradientDrawable(); bg.setColor(0x2220AB8A); bg.setCornerRadius(dp(16)); bg.setStroke(dp(1),0x35A3F4DC); b.setBackground(bg);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48)); p.topMargin=dp(8); b.setLayoutParams(p); return b;
    }
}
