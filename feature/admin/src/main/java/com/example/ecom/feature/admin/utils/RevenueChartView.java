package com.example.ecom.feature.admin.utils;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.models.ChartPoint;
import java.text.DecimalFormat;
import java.util.*;
/** Lightweight line / horizontal bar chart. No external chart library is required. */
public class RevenueChartView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<ChartPoint> points=new ArrayList<>();
    private boolean bars;
    public RevenueChartView(Context c) {
        super(c);
    }
    public RevenueChartView(Context c,AttributeSet a) {
        super(c,a);
    }
    public RevenueChartView(Context c,AttributeSet a,int style) {
        super(c,a,style);
    }
    public void setPoints(List<ChartPoint> values,boolean bars) {
        points.clear();
        points.addAll(values);
        this.bars=bars;
        StringBuilder description=new StringBuilder();
        for(ChartPoint p:points)description.append(p.label).append(": ").append(AdminUi.money(p.value)).append("; ");
        setContentDescription(getContext().getString(R.string.admin_chart_accessibility,description));
        invalidate();
    }
    private float d(float v) {
        return v*getResources().getDisplayMetrics().density;
    }
    private int color(int id) {
        return ContextCompat.getColor(getContext(),id);
    }
    private String label(long v) {
        if(v>=1000000)return new DecimalFormat("0.#").format(v/1000000d)+"M";
        if(v>=1000)return new DecimalFormat("0.#").format(v/1000d)+"k";
        return String.valueOf(v);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
        paint.setTextSize(d(10));
        paint.setStyle(Paint.Style.FILL);
        long largest=0;
        for(ChartPoint p:points)largest=Math.max(largest,p.value);
        if(largest==0) {
            paint.setColor(color(R.color.admin_text_secondary));
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(getContext().getString(R.string.admin_chart_empty),getWidth()/2f,getHeight()/2f,paint);
            return;
        }
        if(bars) {
            drawBars(canvas,largest);
            return;
        }
        float left=d(34),right=getWidth()-d(10),top=d(10),bottom=getHeight()-d(30),width=right-left,height=bottom-top;
        double unit=Math.pow(10,Math.floor(Math.log10(largest)));
        long max=(long)(Math.ceil(largest/unit)*unit);
        if(max<4)max=4;
        paint.setStrokeWidth(d(1));
        for(int i=0;i<=4;i++) {
            float y=bottom-height*i/4;
            paint.setColor(color(R.color.admin_border));
            canvas.drawLine(left,y,right,y,paint);
            paint.setColor(color(R.color.admin_text_secondary));
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(label(max*i/4),left-d(8),y+d(3),paint);
        }
        Path line=new Path(),fill=new Path();
        float first=left;
        for(int i=0;i<points.size();i++) {
            float x=points.size()==1?(left+right)/2:left+width*i/(points.size()-1);
            float y=bottom-height*points.get(i).value/max;
            if(i==0) {
                line.moveTo(x,y);
                fill.moveTo(x,bottom);
                fill.lineTo(x,y);
                first=x;
            }
            else {
                line.lineTo(x,y);
                fill.lineTo(x,y);
            }
        }
        float last=points.size()==1?first:right;
        fill.lineTo(last,bottom);
        fill.close();
        paint.setColor(color(R.color.admin_chart_fill));
        canvas.drawPath(fill,paint);
        paint.setColor(color(R.color.admin_primary));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(d(2.5f));
        paint.setStrokeJoin(Paint.Join.ROUND);
        canvas.drawPath(line,paint);
        paint.setStyle(Paint.Style.FILL);
        int step=Math.max(1,(int)Math.ceil(points.size()/7d));
        for(int i=0;i<points.size();i++) {
            float x=points.size()==1?(left+right)/2:left+width*i/(points.size()-1),y=bottom-height*points.get(i).value/max;
            paint.setColor(Color.WHITE);
            canvas.drawCircle(x,y,d(4.7f),paint);
            paint.setColor(color(R.color.admin_primary));
            canvas.drawCircle(x,y,d(3.5f),paint);
            if(i%step==0||i==points.size()-1) {
                paint.setColor(color(R.color.admin_text_secondary));
                paint.setTextAlign(i==0?Paint.Align.LEFT:i==points.size()-1?Paint.Align.RIGHT:Paint.Align.CENTER);
                canvas.drawText(points.get(i).label,x,bottom+d(20),paint);
            }
        }
    }
    private void drawBars(Canvas canvas,long max) {
        float row=(getHeight()-d(8))/points.size(),left=d(83),width=getWidth()-left-d(53);
        for(int i=0;i<points.size();i++) {
            ChartPoint p=points.get(i);
            float y=i*row+d(5);
            paint.setColor(color(R.color.admin_text_secondary));
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(p.label,0,y+row*.5f+d(3),paint);
            paint.setColor(color(R.color.admin_primary_light));
            canvas.drawRoundRect(left,y+row*.25f,left+width,y+row*.75f,d(4),d(4),paint);
            paint.setColor(color(R.color.admin_primary));
            canvas.drawRoundRect(left,y+row*.25f,left+width*p.value/max,y+row*.75f,d(4),d(4),paint);
            paint.setColor(color(R.color.admin_text));
            canvas.drawText(label(p.value),left+width+d(7),y+row*.5f+d(3),paint);
        }
    }
}
