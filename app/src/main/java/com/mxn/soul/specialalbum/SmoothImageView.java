package com.mxn.soul.specialalbum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by cys on 15/7/16.
 */
public class SmoothImageView extends ImageView {

    private PaintFlagsDrawFilter pfd;
    private Paint mPaint = new Paint();
    private Matrix matrix = new Matrix();
    private Bitmap bmp;
    private PorterDuffXfermode xfermode=new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);
    private Rect bitmapRect=new Rect();
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
        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setXfermode(xfermode);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.img1);
    }


    @Override
    protected void onDraw(Canvas canvas) {
     //   canvas.setDrawFilter(pfd);
//        canvas.drawBitmap(bmp, matrix, mPaint);
       // super.onDraw(canvas);

        bitmapRect.set(0, 0, getWidth(), getHeight());

        canvas.save();
        canvas.setDrawFilter(pfd);
//        mPath.reset();
//        canvas.clipPath(mPath); // makes the clip empty
//        mPath.addCircle(getWidth()/2, getWidth()/2, getHeight()/2, Path.Direction.CCW);
//        canvas.clipPath(mPath, Region.Op.REPLACE);
        canvas.drawBitmap(bmp, null, bitmapRect, mPaint);
        canvas.restore();
    }
}
