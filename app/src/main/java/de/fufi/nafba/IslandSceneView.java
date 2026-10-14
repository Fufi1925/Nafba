package de.fufi.nafba;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Reference-inspired previews INSIDE Nafba. The OS owns every external island. */
public final class IslandSceneView extends View {
    public static final int BATTERY=0,TIMER=1,MUSIC=2,CALL=3,NAVIGATION=4,DND=5,RECORDING=6,CHARGING=7;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r=new RectF();
    private final Typeface medium=Typeface.create("sans-serif-medium",Typeface.NORMAL);
    private int scene,percent=19; private String title="Low Battery",time="0:21";
    private LinearGradient cover;
    public IslandSceneView(Context c,int scene) { super(c);this.scene=scene;setLayerType(LAYER_TYPE_SOFTWARE,null);setContentDescription(label(scene)+" · Vorschau in Nafba"); }
    public static String label(int scene) { return new String[]{"Akku","Timer","Musik","Anruf","Navigation","Fokus","Aufnahme","Laden"}[scene]; }
    public void battery(int percent,boolean low) { this.percent=percent;title=low?"Low Battery":"Battery";invalidate(); }
    public void time(String value) { time=value;invalidate(); }
    @Override protected void onSizeChanged(int w,int h,int ow,int oh) { cover=new LinearGradient(0,0,0,92,new int[]{0xFFFFAD57,0xFFEF607E,0xFF9C8CEF},null,Shader.TileMode.CLAMP); }
    private void text(Canvas c,String value,float x,float y,float size,int color) { p.setShader(null);p.setColor(color);p.setTextSize(size);p.setTypeface(medium);p.setStyle(Paint.Style.FILL);c.drawText(value,x,y,p); }
    @Override protected void onDraw(Canvas canvas) {
        float d=getResources().getDisplayMetrics().density;
        canvas.save();canvas.scale(d,d);
        float w=getWidth()/d,h=getHeight()/d,top=7,bottom=h-7;
        float radius=scene==MUSIC?32:Math.min(38,(bottom-top)/2);
        r.set(5,top,w-5,bottom);p.setColor(0xFF101112);p.setShadowLayer(5,0,3,0x26000000);canvas.drawRoundRect(r,radius,radius,p);p.clearShadowLayer();
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.6f);p.setColor(0x26FFFFFF);canvas.drawRoundRect(r,radius,radius,p);p.setStyle(Paint.Style.FILL);
        float mid=(top+bottom)/2;
        switch(scene) {
            case BATTERY:
                text(canvas,title,23,mid+5,14,0xFFECECF1);text(canvas,percent<0?"—":percent+"%",w-80,mid+5,14,0xFFC9586C);
                p.setColor(0xFFC9586C);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);canvas.drawRoundRect(w-47,mid-7,w-23,mid+7,3,3,p);p.setStyle(Paint.Style.FILL);canvas.drawRoundRect(w-44,mid-4,w-40+17*Math.max(0,percent)/100f,mid+4,1,1,p);canvas.drawRect(w-22,mid-3,w-20,mid+3,p);break;
            case TIMER:
                p.setColor(0xFF5D451B);canvas.drawCircle(37,mid,17,p);p.setColor(0xFFF7BC4B);canvas.drawRoundRect(31,mid-7,35,mid+7,2,2,p);canvas.drawRoundRect(39,mid-7,43,mid+7,2,2,p);
                text(canvas,"Timer",66,mid+5,13,0xFF9C7950);text(canvas,time,w-106,mid+12,32,0xFFF5BC7D);break;
            case MUSIC:
                p.setColor(Color.WHITE);p.setShader(cover);canvas.drawRoundRect(24,top+18,80,top+74,13,13,p);p.setShader(null);
                text(canvas,"Glow",96,top+42,19,0xFFF0F0F2);text(canvas,"Echo",96,top+62,14,0xFF97979F);
                p.setColor(0xFFEF8F99);for(int i=0;i<5;i++) canvas.drawRoundRect(w-54+i*5,top+31-i%3*3,w-52+i*5,top+50+i%3*3,1,1,p);
                p.setColor(0xFF434347);canvas.drawRoundRect(24,bottom-57,w-24,bottom-54,2,2,p);p.setColor(0xFFDDDDDF);canvas.drawRoundRect(24,bottom-57,w*.48f,bottom-54,2,2,p);
                text(canvas,"1:20",24,bottom-37,10,0xFF85858D);text(canvas,"−2:15",w-54,bottom-37,10,0xFF85858D);
                text(canvas,"◀◀",w/2-70,bottom-13,21,Color.WHITE);text(canvas,"Ⅱ",w/2-7,bottom-13,27,Color.WHITE);text(canvas,"▶▶",w/2+42,bottom-13,21,Color.WHITE);break;
            case CALL:
                p.setColor(0xFFB1A4B5);canvas.drawCircle(34,mid,17,p);text(canvas,"F",29,mid+6,17,Color.WHITE);
                text(canvas,"Anruf",60,mid-3,10,0xFF8F8F96);text(canvas,"Fufi",60,mid+14,16,Color.WHITE);
                p.setColor(0xFFE9474F);canvas.drawCircle(w-74,mid,20,p);text(canvas,"×",w-81,mid+7,25,Color.WHITE);
                p.setColor(0xFF54C966);canvas.drawCircle(w-30,mid,20,p);text(canvas,"✓",w-38,mid+7,22,Color.WHITE);break;
            case NAVIGATION:
                text(canvas,"↱",23,mid+11,38,Color.WHITE);text(canvas,"900 ft",64,mid,20,Color.WHITE);text(canvas,"Page St",64,mid+21,14,0xFF96969E);break;
            case DND:
                p.setColor(0xFFB9A4F2);canvas.drawCircle(34,mid,12,p);p.setColor(0xFF101112);canvas.drawCircle(40,mid-5,11,p);text(canvas,"Do Not Disturb",59,mid+5,13,Color.WHITE);text(canvas,"On",w-46,mid+5,13,0xFFB3A0F0);break;
            case RECORDING:
                p.setColor(0xFFF35269);canvas.drawCircle(29,mid,5,p);text(canvas,"0:42",43,mid-1,13,0xFFF35269);text(canvas,"Recording saved",25,mid+17,12,Color.WHITE);break;
            default:
                text(canvas,"Charging",24,mid+5,14,Color.WHITE);text(canvas,"76%",w-96,mid+5,18,0xFF83DD91);p.setColor(0xFF83DD91);canvas.drawRoundRect(w-43,mid-11,w-25,mid+11,4,4,p);break;
        }
        canvas.restore();
    }
}
