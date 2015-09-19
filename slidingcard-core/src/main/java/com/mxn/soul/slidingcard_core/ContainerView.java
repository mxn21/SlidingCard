package com.mxn.soul.slidingcard_core;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


/**
 * 一组照片的布局
 */
public class ContainerView extends RelativeLayout implements
        SlidingCard.OnPageChangeListener {

    private Context context;
    private ContainerInterface containerInterface;

    private int count = 0;

    private float nextRotation ;

    private int rootId ;
    private int layoutId ;

    private int cardItemHeight ;
    private int cardItemMargin ;

    private ViewGroup scrollableGroups[];

    public void setScrollableGroups(ViewGroup...args) {
        this.scrollableGroups = args;
    }

    public ContainerView(Context context) {
        super(context);
        this.context = context ;
    }

    public ContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context ;
        TypedArray types = context.obtainStyledAttributes(attrs,
                R.styleable.ContainerView);
        final int count = types.getIndexCount();
        for (int i = 0; i < count; ++i) {
            int attr = types.getIndex(i);
            if (attr == R.styleable.ContainerView_card_item_height) {
                cardItemHeight = types.getDimensionPixelSize(attr, 0);
            } else if (attr == R.styleable.ContainerView_card_item_margin) {
                cardItemMargin = types.getDimensionPixelSize(attr, 10);

            }
        }
        types.recycle();
    }


    public void addToView(View child) {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(child, 0, layoutParams);
    }

    public void initCardView(final ContainerInterface containerInterface,int layoutId,int rootId) {
        this.containerInterface = containerInterface;
        this.rootId = rootId ;
        this.layoutId = layoutId ;
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
        for (int i = 0; i < 3; i++) {
                SlidingCard mSlidingCard = new SlidingCard(context);
                mSlidingCard.setContent(layoutId);
                mSlidingCard.setCardHeight(cardItemHeight);
                containerInterface.initCard(mSlidingCard, i) ;
                View contentView = mSlidingCard.findViewById(rootId);
                if (i == 1) {
                    contentView.setRotation(4);
                }
                if (i == 2) {
                    contentView.setRotation(-3);
                }
                mSlidingCard.setCurrentItem(1, false);
                mSlidingCard.setOnPageChangeListener(this);
                addToView(mSlidingCard);
        }
    }

    public SlidingCard getNextView() {
        if (getChildCount() - 1 > 0) {
            return (SlidingCard) getChildAt(getChildCount() - 2);
        }
        return null;
    }

    @Override
    public synchronized void onPageScrolled(SlidingCard v, int position,
                               float positionOffset, int positionOffsetPixels) {
//        Log.e("test", "onPageScrolled:" + position + "," +positionOffset +","
//                + positionOffsetPixels);
        SlidingCard slidingCard = getNextView();
        if (slidingCard != null) {
            if (Math.abs(positionOffsetPixels) != 0) {
                View contentView = slidingCard.findViewById(rootId);
                LayoutParams params = new LayoutParams(
                        contentView.getLayoutParams());
                params.topMargin = (int) ( Math.abs(positionOffset) * cardItemMargin);
                params.leftMargin = (int) (Math.abs(positionOffset) * cardItemMargin);
                params.rightMargin = (int) ( Math.abs(positionOffset) * cardItemMargin);
                contentView.setLayoutParams(params);
                contentView.setRotation((int) ( (1 - Math.abs(positionOffset)) * nextRotation));
                postInvalidate();
            }
        }
    }

    @Override
    public synchronized void onPageSelectedAfterAnimation(SlidingCard v, int prevPosition,
                                             int curPosition) {
        if (context != null) {
            removeViewAt(getChildCount() - 1);
            containerInterface.exChangeCard();
            SlidingCard mSlidingCard = new SlidingCard(context);
            mSlidingCard.setContent(layoutId);
            mSlidingCard.setCardHeight(cardItemHeight);
            containerInterface.initCard(mSlidingCard, 2) ;
            View contentView = mSlidingCard.findViewById(rootId);
            setRotation(contentView);
            mSlidingCard.setCurrentItem(1, false);
            mSlidingCard.setOnPageChangeListener(this);
            addToView(mSlidingCard);
//            Log.e("test", "onPageSelectedAfterAnimation:" + curPosition + ","
//                    + getChildCount());
        }
    }

    @Override
    public synchronized void onPageSelected(SlidingCard v, int prevPosition, int curPosition) {
       // Log.e("test", "onPageSelected:" + curPosition);
    }

    @Override
    public synchronized void onPageScrollStateChanged(SlidingCard v, int state) {
     //   Log.e("test", "state change:" + state);
        if(state==1){
            SlidingCard slidingCard = getNextView();
            if (slidingCard != null) {
                View contentView = slidingCard.findViewById(rootId) ;
                nextRotation = contentView.getRotation() ;
            }
        }else if(state==0){
            SlidingCard.sScrolling = false ;
        }
    }

    private void setRotation(View v) {
        if (count % 3 == 1) {
            v.setRotation(4);
        } else if (count % 3 == 2) {
            v.setRotation(-3);
        }
        postInvalidate();
        count++;
    }

    public interface ContainerInterface{
        void initCard(SlidingCard card, int index) ;
        void exChangeCard() ;

    }

    //如果放在ViewPager或者ListView中需要重写下面三个方法解决滑动冲突
    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        if(scrollableGroups!=null && scrollableGroups.length>0)
        for(ViewGroup group: scrollableGroups){
            group.requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(scrollableGroups!=null && scrollableGroups.length>0)
            for(ViewGroup group: scrollableGroups){
                group.requestDisallowInterceptTouchEvent(true);
            }
        return super.onInterceptTouchEvent(ev);
    }
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if(scrollableGroups!=null && scrollableGroups.length>0)
            for(ViewGroup group: scrollableGroups){
                group.requestDisallowInterceptTouchEvent(true);
            }
        return super.onTouchEvent(event);
    }

}
