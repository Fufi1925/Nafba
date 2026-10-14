package de.fufi.nafba;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** A drawn glass surface; no screenshots, accessibility scraping or wallpaper access. */
public final class IslandView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final Matrix shaderMatrix = new Matrix();
    private final LinearGradient glass = new LinearGradient(0,0,0,1,new int[]{0xFF34464C,0xFA10191E},null,Shader.TileMode.CLAMP);
    private final Typeface medium = Typeface.create("sans-serif-medium",Typeface.NORMAL);
    private String batteryText = "76%";
    private int battery = 76;
    private boolean low, charging, expanded;
    private float reveal;
    private ValueAnimator animator;
    public IslandView(Context context) { super(context); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    public void update(int percent, boolean isCharging, boolean isLow) {
        battery = percent; charging = isCharging; low = isLow;
        batteryText = percent < 0 ? "—" : percent+"%";
        setContentDescription(isLow ? "Akku niedrig: " + percent + " Prozent" : "Nafba Akku: " + percent + " Prozent");
        animateReveal(expanded || low || charging);
    }
    public void toggle() { expanded = !expanded; animateReveal(expanded || low || charging); }
    private void animateReveal(boolean open) {
        float target = open ? 1 : 0;
        if (animator != null) animator.cancel();
        if (!Prefs.flag(getContext(), "motion", true)) { reveal = target; invalidate(); return; }
        animator = ValueAnimator.ofFloat(reveal, target); animator.setDuration(360);
        animator.setInterpolator(new DecelerateInterpolator(2));
        animator.addUpdateListener(a -> { reveal = (float) a.getAnimatedValue(); invalidate(); }); animator.start();
    }
    @Override protected void onDetachedFromWindow() { if (animator != null) animator.cancel(); super.onDetachedFromWindow(); }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float d = getResources().getDisplayMetrics().density;
        float w = getWidth() - 12*d, h = getHeight() - 12*d;
        float compact = Math.min(w, 112*d), actualW = compact + (w-compact)*reveal;
        float actualH = Math.min(h, 34*d + 28*d*reveal);
        r.set((getWidth()-actualW)/2, 4*d, (getWidth()+actualW)/2, 4*d+actualH);
        int accent = low ? Color.rgb(255,172,125) : Color.rgb(163,244,220);
        paint.setShader(null); paint.setColor(Color.rgb(16,26,31));
        paint.setShadowLayer(4*d,0,2*d,0x35000000); c.drawRoundRect(r,actualH/2,actualH/2,paint); paint.clearShadowLayer();
        shaderMatrix.setScale(1,actualH); shaderMatrix.postTranslate(0,r.top); glass.setLocalMatrix(shaderMatrix); paint.setShader(glass);
        c.drawRoundRect(r,actualH/2,actualH/2,paint); paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(d); paint.setColor(0x55FFFFFF); c.drawRoundRect(r,actualH/2,actualH/2,paint); paint.setStyle(Paint.Style.FILL);
        if (reveal < .4f) {
            paint.setColor(0xBBE4EEE9); c.drawRoundRect(r.left+18*d,r.centerY()-2*d,r.left+45*d,r.centerY()+2*d,2*d,2*d,paint);
            paint.setColor(accent); c.drawCircle(r.right-20*d,r.centerY(),3*d,paint);
        } else {
            paint.setTypeface(medium); paint.setTextSize(13*d); paint.setColor(accent);
            String title = low ? "Akku niedrig" : charging ? "Wird geladen" : "Dein Akku";
            c.drawText(title,r.left+20*d,r.top+23*d,paint);
            paint.setColor(0xBBE4EEE9); paint.setTextSize(11*d); c.drawText(low ? "Bitte laden." : "Alles im Blick.",r.left+20*d,r.top+42*d,paint);
            paint.setColor(accent); paint.setTextSize(18*d); paint.setTextAlign(Paint.Align.RIGHT);
            c.drawText(batteryText,r.right-19*d,r.top+31*d,paint); paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(0x35FFFFFF); c.drawRoundRect(r.right-54*d,r.top+39*d,r.right-19*d,r.top+42*d,2*d,2*d,paint);
            paint.setColor(accent); c.drawRoundRect(r.right-54*d,r.top+39*d,r.right-54*d+35*d*Math.max(0,battery)/100f,r.top+42*d,2*d,2*d,paint);
        }
    }
}
