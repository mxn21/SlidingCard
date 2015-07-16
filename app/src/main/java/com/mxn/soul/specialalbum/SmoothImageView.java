package com.mxn.soul.specialalbum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by cys on 15/7/16.
 */
public class SmoothImageView extends ImageView {

    private PaintFlagsDrawFilter pfd;
    private Paint mPaint = new Paint();
    private Matrix matrix = new Matrix();;


    public SmoothImageView(Context context) {
        super(context);
        initialize();
    }

    public SmoothImageView(Context context , AttributeSet attrs){
        super(context,attrs);
        initialize();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    private void initialize() {
        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        mPaint.setAntiAlias(true);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.p
        canvas.setDrawFilter(pfd);
        super.onDraw(canvas);
    }
}
