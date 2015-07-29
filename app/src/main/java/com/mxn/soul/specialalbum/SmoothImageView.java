package com.mxn.soul.specialalbum;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by cys on 15/7/16.
 * 去锯齿的ImageView
 */
public class SmoothImageView extends ImageView {

    public SmoothImageView(Context context) {
        super(context);
        initialize();
    }
    public SmoothImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
        if(android.os.Build.VERSION.SDK_INT>=11)
        {
           setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
    }
}
