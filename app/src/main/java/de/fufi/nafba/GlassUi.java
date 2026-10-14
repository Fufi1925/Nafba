package de.fufi.nafba;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.Build;
import android.view.*;
import android.widget.*;

/** App-owned glass, blur and light. It never samples other applications. */
final class GlassUi {
    static final int INK=0xFF22232A, MUTED=0xFF80818C, PINK=0xFFEE4265;
    static int dp(Context c,float n) { return Math.round(n*c.getResources().getDisplayMetrics().density); }
    static GradientDrawable shape(int color,float radius,Context c) { GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(c,radius)); return g; }
    static TextView text(Context c,String value,int size,int color,boolean bold) {
        TextView v=new TextView(c); v.setText(value); v.setTextColor(color); v.setTextSize(size);
        v.setTypeface(Typeface.create(bold?"sans-serif-medium":"sans-serif",Typeface.NORMAL)); v.setIncludeFontPadding(false); return v;
    }
    static class Backdrop extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient base; RadialGradient rose, lavender;
        Backdrop(Context c) { super(c); }
        @Override protected void onSizeChanged(int w,int h,int ow,int oh) {
            base=new LinearGradient(0,0,w,h,new int[]{0xFFF9F9FC,0xFFF0F0F8,0xFFFAF5F2},null,Shader.TileMode.CLAMP);
            rose=new RadialGradient(w*.94f,h*.20f,w*.62f,new int[]{0x42FFD8DE,0x00FFD8DE},null,Shader.TileMode.CLAMP);
            lavender=new RadialGradient(w*.03f,h*.70f,w*.77f,new int[]{0x48CFCDF5,0x00CFCDF5},null,Shader.TileMode.CLAMP);
        }
        @Override protected void onDraw(Canvas c) {
            if(base==null) return;
            p.setShader(base); c.drawRect(0,0,getWidth(),getHeight(),p);
            p.setShader(rose); c.drawRect(0,0,getWidth(),getHeight(),p);
            p.setShader(lavender); c.drawRect(0,0,getWidth(),getHeight(),p); p.setShader(null);
        }
    }
    static class Panel extends FrameLayout {
        final ImageView frost; final Backdrop backdrop;
        View contentBehind;
        private Bitmap snapshot;
        private final int[] mine=new int[2],origin=new int[2];
        Panel(Context c,Backdrop backdrop,float radius) {
            super(c); this.backdrop=backdrop; setClipToOutline(true);
            GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xCFFFFFFF,0x9CFFFFFF,0xC5FFFFFF});
            g.setCornerRadius(dp(c,radius)); g.setStroke(dp(c,1),0xD0FFFFFF); setBackground(g);
            setElevation(dp(c,3));
            frost=new ImageView(c); frost.setScaleType(ImageView.ScaleType.FIT_XY); frost.setAlpha(.48f);
            if(Build.VERSION.SDK_INT>=31) frost.setRenderEffect(RenderEffect.createBlurEffect(dp(c,18),dp(c,18),Shader.TileMode.CLAMP));
            addView(frost,new FrameLayout.LayoutParams(-1,-1));
            View light=new View(c);
            GradientDrawable tint=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xAFFFFFFF,0xA0FFFFFF,0xBFFFFFFF});
            tint.setCornerRadius(dp(c,radius));light.setBackground(tint);addView(light,new FrameLayout.LayoutParams(-1,-1));
        }
        void captureFrost() {
            if(getWidth()==0 || getHeight()==0 || backdrop.getWidth()==0) return;
            int scale=contentBehind==null?2:10;
            int w=Math.max(1,getWidth()/scale),h=Math.max(1,getHeight()/scale);
            if(snapshot==null || snapshot.getWidth()!=w || snapshot.getHeight()!=h) snapshot=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
            Canvas canvas=new Canvas(snapshot);canvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);canvas.scale(1f/scale,1f/scale);
            getLocationInWindow(mine);backdrop.getLocationInWindow(origin);canvas.save();canvas.translate(origin[0]-mine[0],origin[1]-mine[1]);backdrop.draw(canvas);canvas.restore();
            if(contentBehind!=null) { contentBehind.getLocationInWindow(origin);canvas.translate(origin[0]-mine[0],origin[1]-mine[1]);contentBehind.draw(canvas); }
            frost.setImageBitmap(snapshot);
        }
        @Override protected void onSizeChanged(int w,int h,int ow,int oh) {
            if(w>0 && h>0) post(this::captureFrost);
        }
    }
    static class IconView extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); final Path path=new Path();
        final int type; int color;
        IconView(Context c,int type,int color) { super(c); this.type=type; this.color=color; setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); }
        void tint(int value) { color=value; invalidate(); }
        @Override protected void onDraw(Canvas c) {
            c.save(); c.translate((getWidth()-dp(getContext(),25))/2f,(getHeight()-dp(getContext(),25))/2f); c.scale(dp(getContext(),25)/24f,dp(getContext(),25)/24f);
            p.setColor(color); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.8f); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND); path.reset();
            switch(type) {
                case 0: path.moveTo(3,10);path.lineTo(12,3);path.lineTo(21,10);path.moveTo(5,9);path.lineTo(5,21);path.lineTo(10,21);path.lineTo(10,15);path.lineTo(14,15);path.lineTo(14,21);path.lineTo(19,21);path.lineTo(19,9);c.drawPath(path,p);break;
                case 1: c.drawRoundRect(2,7,22,17,5,5,p);p.setStyle(Paint.Style.FILL);c.drawCircle(17,12,1.4f,p);break;
                case 2: c.drawCircle(12,13,8,p);c.drawLine(12,13,12,8,p);c.drawLine(12,13,16,15,p);c.drawLine(9,2,15,2,p);break;
                default:
                    c.drawCircle(12,12,6,p); c.drawCircle(12,12,2.2f,p);
                    for(int i=0;i<8;i++) { c.save();c.rotate(i*45,12,12);c.drawLine(12,2,12,4,p);c.restore(); }
            }
            c.restore();
        }
    }
    static class Toggle extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); boolean checked; int accent=PINK;
        float thumb;android.animation.ValueAnimator animation;
        Toggle(Context c,boolean checked) { super(c);this.checked=checked;thumb=checked?1:0;setMinimumWidth(dp(c,58));setMinimumHeight(dp(c,48));setFocusable(true);setClickable(true); updateState(); }
        void setChecked(boolean on) {
            if(checked==on) return;checked=on;updateState();if(animation!=null) animation.cancel();
            if(!Prefs.flag(getContext(),"motion",true)) { thumb=on?1:0;invalidate();return; }
            animation=android.animation.ValueAnimator.ofFloat(thumb,on?1:0);animation.setDuration(180);animation.setInterpolator(new android.view.animation.DecelerateInterpolator());
            animation.addUpdateListener(a -> { thumb=(float)a.getAnimatedValue();invalidate(); });animation.start();
        }
        @Override protected void onDetachedFromWindow() { if(animation!=null) animation.cancel();super.onDetachedFromWindow(); }
        private void updateState() { setContentDescription(checked?"Aktiviert":"Deaktiviert");if(Build.VERSION.SDK_INT>=30) setStateDescription(checked?"Ein":"Aus"); }
        @Override protected void onDraw(Canvas c) {
            float d=getResources().getDisplayMetrics().density,top=(getHeight()-30*d)/2;
            p.setColor(checked?accent:0xFFD8D8DF);c.drawRoundRect(4*d,top,54*d,top+30*d,15*d,15*d,p);
            p.setShadowLayer(2*d,0,d,0x22000000);p.setColor(Color.WHITE);c.drawCircle((19+20*thumb)*d,top+15*d,12*d,p);p.clearShadowLayer();
        }
    }
    static Button button(Context c,String title,int accent,boolean filled) {
        Button b=new Button(c);b.setText(title);b.setTextSize(15);b.setAllCaps(false);b.setTextColor(filled?Color.WHITE:accent);
        GradientDrawable g=shape(filled?accent:0xEFFFFFFF,17,c);
        StateListDrawable states=new StateListDrawable();states.addState(new int[]{android.R.attr.state_pressed},shape(filled?0xFFD23556:0xFFEEEEF4,17,c));states.addState(new int[]{},g);b.setBackground(states);
        b.setMinimumHeight(dp(c,50)); return b;
    }
}
