package com.mxn.soul.specialalbum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by cys on 15/7/16.
 */
public class SmoothImageView extends ImageView {
    public SmoothImageView(Context context) {
        super(context);
    }

    public SmoothImageView(Context context , AttributeSet attrs){
        super(context,attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        //设置该View大小为 80 80
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec) ;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
    }
}
