package com.mxn.soul.slidingcard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import java.lang.reflect.Field;


/**
 * 每一个卡片布局
 */
public class SlidingCard extends LinearLayout {

    private static final boolean USE_CACHE = true;

    public static final int MAX_SETTLE_DURATION = 600; // ms
    //最小快速滑动距离
    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips

    public static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    private View mContent;

    private int mPrevItem = DEFAULT_ITEM;

    private int mCurItem = DEFAULT_ITEM;

    private static final int DEFAULT_ITEM = 0;

    private boolean mFirstLayout = true;

    private Scroller mScroller;

    private boolean mScrollingCacheEnabled;

    private boolean mScrolling;

    private boolean mIsBeingDragged;

    private boolean mIsUnableToDrag;

    private int mTouchSlop;

    //手指刚按下的初始点
    private float mInitialMotionX;

    /**
     * 最后一次motion event的位置.
     */
    private float mLastMotionX;

    private float mLastMotionY;

    /**
     * 活动的触摸pointer的ID. 用于当多点拖动的时候保持一致性
     */
    protected int mActivePointerId = INVALID_POINTER;

    /**
     * 表示当前没有活动的点
     * {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;

    /**
     * 确定滚动的速度
     */
    protected VelocityTracker mVelocityTracker;

    private int mMinimumVelocity;

    protected int mMaximumVelocity;

    private int mFlingDistance;

    private OnPageChangeListener mOnPageChangeListener;

    private View contentView;

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    /**
     * 空闲状态，view完整显示，动画处于执行完毕状态
     */
    public static final int SCROLL_STATE_IDLE = 0;

    /**
     * 表示正在被拖动
     */
    public static final int SCROLL_STATE_DRAGGING = 1;

    /**
     * 表示view正在自动移动到最终的位置
     */
    public static final int SCROLL_STATE_SETTLING = 2;

    private int mScrollState = SCROLL_STATE_IDLE;

    /**
     * 滑动的回调接口
     */
    public interface OnPageChangeListener {

        /**
         * 当前page滚动的时候调用,只要位置有移动就会调用
         * @param position             当前Position等于0, 左滑position等于1,右滑position等于-1
         * @param positionOffset       范围是[0, 1) 表示从position的偏移
         * @param positionOffsetPixels 从position的偏移的像素
         */
        void onPageScrolled(SlidingCard v, int position,
                            float positionOffset, int positionOffsetPixels);

        /**
         * 当新page被选中的时候调用
         * 动画并不一定完成
         */
        void onPageSelected(SlidingCard v, int prevPosition,
                            int curPosition);

        /**
         * 当动画结束后，新页面被选中时调用
         */
         void onPageSelectedAfterAnimation(SlidingCard v,
                                          int prevPosition, int curPosition);

        /**
         * 当滑动状态改变时调用，用来观察：拖动状态，自动移动状态，和停止状态。
         *
         * @param state 滑动状态.
         * @see SlidingCard#SCROLL_STATE_IDLE
         * @see SlidingCard#SCROLL_STATE_DRAGGING
         * @see SlidingCard#SCROLL_STATE_SETTLING
         */
        void onPageScrollStateChanged(SlidingCard v, int state);

    }


    public SlidingCard(Context context) {
        this(context, null);
    }

    public SlidingCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSlidingCard();

    }

    void initSlidingCard() {
        setWillNotDraw(false);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setFocusable(true);
        final Context context = getContext();
        mScroller = new Scroller(context, sInterpolator);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat
                .getScaledPagingTouchSlop(configuration);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        final float density = context.getResources().getDisplayMetrics().density;
        mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFirstLayout = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int itemHeight = getContext().getResources().getDimensionPixelSize(
                R.dimen.card_item_height);
        int width = getDefaultSize(0, widthMeasureSpec);
        setMeasuredDimension(width, itemHeight);
        final int contentWidth = getChildMeasureSpec(widthMeasureSpec, 0, width);
        mContent.measure(contentWidth, itemHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) {
            completeScroll();
            scrollTo(getDestScrollX(mCurItem), getScrollY());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;
        mContent.layout(0, 0, width, height);
        if (mFirstLayout) {
            scrollTo(getDestScrollX(mCurItem), getScrollY());
        }
        mFirstLayout = false;
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                int oldX = getScrollX();
                int oldY = getScrollY();
                int x = mScroller.getCurrX();
                int y = mScroller.getCurrY();
                if (oldX != x || oldY != y) {
                    scrollTo(x, y);
                    pageScrolled(x);
                } else {
                    setScrollState(SCROLL_STATE_IDLE);
                }
                // 继续绘图直到动画结束
                ViewCompat.postInvalidateOnAnimation(this);
                return;
            }
        }
        // scroll完成, 清除状态
        completeScroll();
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    private void pageScrolled(int xpos) {
        final int widthWithMargin = getWidth();
        final int position = xpos / widthWithMargin;
        final int offsetPixels = xpos % widthWithMargin;
        final float offset = (float) offsetPixels / widthWithMargin;

        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrolled(this, position, offset,
                    offsetPixels);
        }
    }

    private void setScrollState(int newState) {
        if (mScrollState == newState) {
            return;
        }
        mScrollState = newState;
        disableLayers();
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrollStateChanged(this, newState);
        }
    }

    private void completeScroll() {
        boolean needPopulate = mScrolling;
        if (needPopulate) {
            //滚动完成, 不再需要Cache
            setScrollingCacheEnabled(false);
            mScroller.abortAnimation();
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (oldX != x || oldY != y) {
                scrollTo(x, y);
            } else {
                setScrollState(SCROLL_STATE_IDLE);
            }
            if (mOnPageChangeListener != null && mPrevItem != mCurItem) {
                mOnPageChangeListener.onPageSelectedAfterAnimation(this,
                        mPrevItem, mCurItem);
            }
        }
        mScrolling = false;
    }

    private void setScrollingCacheEnabled(boolean enabled) {
        if (mScrollingCacheEnabled != enabled) {
            mScrollingCacheEnabled = enabled;
            if (USE_CACHE) {
                final int size = getChildCount();
                for (int i = 0; i < size; ++i) {
                    final View child = getChildAt(i);
                    if (child.getVisibility() != GONE) {
                        child.setDrawingCacheEnabled(enabled);
                    }
                }
            }
        }
    }

    //为了改变线性关系，而采用这样的中度的影响
    float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f;
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    /**
     * Like {@link View#scrollBy}, 用平滑的滚动代替瞬间到达
     *
     * @param x        X轴上移动的像素
     * @param y        Y轴上移动的像素
     * @param velocity 快速滑动时的速度(可以为0)
     */
    void smoothScrollTo(int x, int y, int velocity) {
        if (getChildCount() == 0) {
            setScrollingCacheEnabled(false);
            return;
        }
        int sx = getScrollX();
        int sy = getScrollY();
        int dx = x - sx;
        int dy = y - sy;
        if (dx == 0 && dy == 0) {
            completeScroll();
            setScrollState(SCROLL_STATE_IDLE);
            return;
        }
        setScrollingCacheEnabled(true);
        setScrollState(SCROLL_STATE_SETTLING);
        mScrolling = true;
        final int width = getWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / width);
        final float distance = halfWidth + halfWidth
                * distanceInfluenceForSnapDuration(distanceRatio);
        int duration ;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            duration = MAX_SETTLE_DURATION;
        }
        duration = Math.min(duration, MAX_SETTLE_DURATION);
        mScroller.startScroll(sx, sy, dx, dy, duration);
        invalidate();
    }


    void setCurrentItemInternal(int item, boolean smoothScroll, boolean always) {
        setCurrentItemInternal(item, smoothScroll, always, 0);
    }

    void setCurrentItemInternal(int item, boolean smoothScroll, boolean always,
                                int velocity) {
        if (!always && mCurItem == item) {
            setScrollingCacheEnabled(false);
            return;
        }
        item = getTargetPage(item);
        final boolean dispatchSelected = mCurItem != item;
        mPrevItem = mCurItem;
        mCurItem = item;
        final int destX = getDestScrollX(mCurItem);
        if (dispatchSelected && mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(this, mPrevItem, mCurItem);
        }
        if (smoothScroll) {
            smoothScrollTo(destX, 0, velocity);
        } else {
            completeScroll();
            scrollTo(destX, 0);
            if (mOnPageChangeListener != null && mPrevItem != mCurItem) {
                mOnPageChangeListener.onPageSelectedAfterAnimation(this,
                        mPrevItem, mCurItem);
            }
        }
    }

    int getTargetPage(int page) {
        page = (page > 1) ? 2 : ((page < 1) ? 0 : page);
        return page;
    }

    int getDestScrollX(int page) {
        switch (page) {
            case 0:
                return mContent.getLeft() - getCardWidth();
            case 1:
                return mContent.getLeft();
            case 2:
                return mContent.getLeft() + getCardWidth();
        }
        return 0;
    }
    public void setCurrentItem(int item, boolean smoothScroll) {
        setCurrentItemInternal(item, smoothScroll, false);
    }


    @Override
    public void addView(@NonNull View child) {
        try {
            super.removeAllViews();
        } catch (Exception e) {
            Log.e("ERROR", String.valueOf(e.getMessage()));
        }
        mContent = child;
        super.addView(child);
        disableLayers();
    }

    @Override
    public void removeView(@NonNull View view) {
        try {
            super.removeView(view);
        } catch (Exception e) {
            Log.e("ERROR", String.valueOf(e.getMessage()));
        }
        disableLayers();
    }
    public void setContent(int res) {
        setContent(LayoutInflater.from(getContext()).inflate(res, null));
    }

    public void setContent(View v) {
        addView(v);
    }


    public void initCardChildView(PhotoContent vo) {
        ImageView mImageView = (ImageView) findViewById(R.id.user_imageview);
        TextView mTextView = (TextView) findViewById(R.id.user_text);
        contentView = findViewById(R.id.sliding_card_content_view);
        if (vo != null) {
            mTextView.setText(vo.getTitle());
            mImageView.setImageResource(getResourceByReflect(vo.getUrl()));

        }
    }

    public int getResourceByReflect(String imageName) {
        Class drawable = R.drawable.class;
        Field field ;
        int r_id;
        try {
            field = drawable.getField(imageName);
            r_id = field.getInt(field.getName());
        } catch (Exception e) {
            r_id = R.drawable.img1;
            Log.e("ERROR", String.valueOf(e.getMessage()));
        }
        return r_id;
    }

    public boolean isCardClose() {
        return mCurItem == 0 || mCurItem == 2;
    }

    public int getCardWidth() {
        return mContent.getWidth();
    }

    private boolean thisSlideAllowed() {
        return !isCardClose();
    }

    private int getLeftBound() {
        return mContent.getLeft() - getCardWidth();
    }

    private int getRightBound() {
        return mContent.getLeft() + getCardWidth();
    }

    private void startDrag() {
        mIsBeingDragged = true;
        setScrollState(SCROLL_STATE_DRAGGING);
    }

    private void endDrag() {
        mIsBeingDragged = false;
        mIsUnableToDrag = false;
        mActivePointerId = INVALID_POINTER;
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void determineDrag(MotionEvent ev) {
        final int activePointerId = mActivePointerId;
        if (activePointerId == INVALID_POINTER)
            return;
        final int pointerIndex = findPointerIndex(ev, activePointerId);
        final float x = MotionEventCompat.getX(ev, pointerIndex);
        final float dx = x - mLastMotionX;
        final float xDiff = Math.abs(dx);
        final float y = MotionEventCompat.getY(ev, pointerIndex);
        final float dy = y - mLastMotionY;
        final float yDiff = Math.abs(dy);
        if (xDiff > mTouchSlop && xDiff > yDiff && thisSlideAllowed()) {
            startDrag();
            mLastMotionX = x;
            mLastMotionY = y;
            setScrollingCacheEnabled(true);
        } else if (xDiff > mTouchSlop) {
            mIsUnableToDrag = true;
        }
    }

    private int determineTargetPage(float pageOffset, int velocity, int deltaX) {
        int targetPage = mCurItem;
        if (Math.abs(deltaX) > mFlingDistance
                && Math.abs(velocity) > mMinimumVelocity) {
            if (velocity > 0 && deltaX > 0) {
                targetPage -= 1;
            } else if (velocity < 0 && deltaX < 0) {
                targetPage += 1;
            }
        } else {
            targetPage = Math.round(mCurItem + pageOffset);
        }
        return targetPage;
    }


    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return super.dispatchKeyEvent(event) ;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // 这是我们要抬起的点. 选取另外一个点代替抬起的点
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = MotionEventCompat.getX(ev, newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev,
                    newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private int findPointerIndex(MotionEvent event, int pointerId) {
        int index = MotionEventCompat.findPointerIndex(event, pointerId);
        if (index == INVALID_POINTER) {
            index = 0;
        }
        return index;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isCardClose())
            return false;
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        if (action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_UP
                || (action != MotionEvent.ACTION_DOWN && mIsUnableToDrag)) {
            endDrag();
            return false;
        }
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                determineDrag(ev);
                break;
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                completeScroll();
                mIsBeingDragged = false;
                mIsUnableToDrag = false;
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        if (!mIsBeingDragged) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        if (isCardClose())
            return false;
        final int action = ev.getAction();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //记录event的开始点
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsBeingDragged) {
                    determineDrag(ev);
                    if (mIsUnableToDrag)
                        return false;
                }
                if (mIsBeingDragged) {
                    // 跟随motion event滑动
                    final int activePointerIndex = findPointerIndex(ev,
                            mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, activePointerIndex);
                    float deltaX = mLastMotionX - x;
                    mLastMotionX = x;
                    float oldScrollX = getScrollX();
                    float scrollX = oldScrollX + deltaX;
                    final float leftBound = getLeftBound();
                    final float rightBound = getRightBound();
                    if (scrollX < leftBound) {
                        scrollX = leftBound;
                    } else if (scrollX > rightBound) {
                        scrollX = rightBound;
                    }
                    mLastMotionX += scrollX - (int) scrollX;
                    scrollTo((int) scrollX, getScrollY());
                    pageScrolled((int) scrollX);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) VelocityTrackerCompat.getXVelocity(
                            velocityTracker, mActivePointerId);
                    final int scrollX = getScrollX();
                    final float pageOffset = (float) (scrollX - getDestScrollX(mCurItem))
                            / getCardWidth();
                    final int activePointerIndex = findPointerIndex(ev,
                            mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, activePointerIndex);
                    final int totalDelta = (int) (x - mInitialMotionX);
                    int nextPage = determineTargetPage(pageOffset, initialVelocity,
                            totalDelta);
                    setCurrentItemInternal(nextPage, true, true, initialVelocity);
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    setCurrentItemInternal(mCurItem, true, true);
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int indexx = MotionEventCompat.getActionIndex(ev);
                mLastMotionX = MotionEventCompat.getX(ev, indexx);
                mActivePointerId = MotionEventCompat.getPointerId(ev, indexx);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                try {
                    mLastMotionX = MotionEventCompat.getX(ev,
                            findPointerIndex(ev, mActivePointerId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint
                .FILTER_BITMAP_FLAG);
        canvas.setDrawFilter(pfd);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child == null)
                return;
        }
        super.dispatchDraw(canvas);
    }

    private void disableLayers() {
        //view按一般方式绘制，不使用离屏缓冲．这是默认的行为
        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_NONE, null);
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewCompat.setLayerType(getChildAt(i), ViewCompat.LAYER_TYPE_NONE,
                    null);
        }
    }

    public void setUserVo(PhotoContent photoVo) {
        initCardChildView(photoVo);
    }

    public View getContentView() {
        return contentView;
    }

}
