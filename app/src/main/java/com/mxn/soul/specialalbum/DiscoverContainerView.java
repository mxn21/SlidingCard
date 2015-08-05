package com.mxn.soul.specialalbum;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class DiscoverContainerView extends RelativeLayout implements
        SlidingCard.OnPageChangeListener {

    private Activity activity;

    private List<PhotoContent> dataList = new ArrayList<>();

    private ContainerInterface containerInterface;

    private int count = 0;

    //如果放在ViewPager或者ListView中需要解决滑动冲突
//    private ViewPager mPager;
//    private ListView listView;

//    public void setPagerAndListView(ViewPager mPager, ListView listView) {
//        this.mPager = mPager;
//        this.listView = listView;
//    }

    public DiscoverContainerView(Context context) {
        super(context);
    }

    public DiscoverContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addToView(View child) {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(child, 0, layoutParams);
    }

    public void initCardView(final Activity activity, List<PhotoContent> dataList) {
        this.activity = activity;
        this.dataList = dataList;
        if (dataList != null && dataList.size() > 0) {
            for (int i = 0; i < 3; i++) {
                PhotoContent userVo = dataList.get(i);
                if (userVo != null) {
                    SlidingCard mSlidingCard = new SlidingCard(this.activity);
                    mSlidingCard.setContent(R.layout.sliding_card_item);
                    mSlidingCard.setUserVo(userVo);
                    View contentView = mSlidingCard.getContentView();
                    if (i == 1) {
                        contentView.setRotation(4);
                    }
                    if (i == 2) {
                        contentView.setRotation(-3);
                    }
//                    postInvalidate();
                    mSlidingCard.setListIndex(i);
                    mSlidingCard.setCurrentItem(1, false);
                    mSlidingCard.setOnPageChangeListener(this);
                    addToView(mSlidingCard);

                }
            }
        }
    }

    public SlidingCard getCurrentView() {
        if (getChildCount() > 0) {
            return (SlidingCard) getChildAt(getChildCount() - 1);
        }
        return null;
    }

    public SlidingCard getNextView() {
        if (getChildCount() - 1 > 0) {
            return (SlidingCard) getChildAt(getChildCount() - 2);
        }
        return null;
    }

    @Override
    public void onPageScrolled(SlidingCard v, int position,
                               float positionOffset, int positionOffsetPixels) {
        if (positionOffset == 0f) {
            positionOffset = 1f;
        }
        SlidingCard slidingCard = getNextView();
        if (slidingCard != null) {
            if (Math.abs(positionOffsetPixels) != 0) {
                View contentView = slidingCard.getContentView();
                LayoutParams params = new LayoutParams(
                        contentView.getLayoutParams());
                params.topMargin = (int) ((1 - Math.abs(positionOffset)) *
                        getResources()
                                .getDimensionPixelSize(R.dimen.card_item_margin));
                params.leftMargin = (int) ((2 - Math.abs(positionOffset)) *
                        getResources()
                                .getDimensionPixelSize(R.dimen.card_item_margin));
                params.rightMargin = (int) ((2 - Math.abs(positionOffset)) *
                        getResources()
                                .getDimensionPixelSize(R.dimen.card_item_margin));
                contentView.setLayoutParams(params);
                contentView.setRotation(0);
                postInvalidate();
            }
        }
    }

    @Override
    public void onPageSelectedAfterAnimation(SlidingCard v, int prevPosition,
                                             int curPosition) {
        if (activity != null) {
            removeViewAt(getChildCount() - 1);
            //UserVo userVo = dataList.get(curPosition);
            if (containerInterface != null) {
                PhotoContent item = dataList.get(0);
                dataList.remove(0);
              //  containerInterface.onFeelOperat(count);
                addNew(item);
            }
            PhotoContent userVo = dataList.get(curPosition);
            if (userVo != null) {
                SlidingCard mSlidingCard = new SlidingCard(activity);
                mSlidingCard.setContent(R.layout.sliding_card_item);
                mSlidingCard.setUserVo(userVo);
                View contentView = mSlidingCard.getContentView();

                setRotation(contentView);
                mSlidingCard.setCurrentItem(1, false);
                mSlidingCard.setOnPageChangeListener(this);
                addToView(mSlidingCard);
            }
//			if (containerInterface != null) {
//				dataList.wremove(0);
//				containerInterface.onFeelOperat(count);
//			}
            Log.e("test", "onPageSelectedAfterAnimation:" + curPosition + ","
                    + getChildCount());
        }
    }

    @Override
    public void onPageSelected(SlidingCard v, int prevPosition, int curPosition) {
        Log.e("test", "onPageSelected:" + curPosition);
    }

    @Override
    public void onPageScrollStateChanged(SlidingCard v, int state) {
        Log.e("test", "state change:" + state);
    }

    public List<PhotoContent> getDataList() {
        return dataList;
    }

    public void setDataList(List<PhotoContent> dataList) {
        this.dataList = dataList;
    }

    public interface ContainerInterface {

        void onFeelOperat(int count);

    }


    public void setContainerInterface(ContainerInterface containerInterface) {
        this.containerInterface = containerInterface;
    }

    public void addNew(PhotoContent u) {
        dataList.add(u);
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


    //如果放在ViewPager或者ListView中需要重写下面三个方法解决滑动冲突
//    @Override
//    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
//        mPager.requestDisallowInterceptTouchEvent(true);
//        listView.requestDisallowInterceptTouchEvent(true);
//        return super.dispatchTouchEvent(ev);
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        mPager.requestDisallowInterceptTouchEvent(true);
//        listView.requestDisallowInterceptTouchEvent(true);
//        return super.onInterceptTouchEvent(ev);
//    }
//
//    @Override
//    public boolean onTouchEvent(@NonNull MotionEvent event) {
//        mPager.requestDisallowInterceptTouchEvent(true);
//        listView.requestDisallowInterceptTouchEvent(true);
//        return super.onTouchEvent(event);
//    }


}
