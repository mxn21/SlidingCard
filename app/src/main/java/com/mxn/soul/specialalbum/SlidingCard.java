package com.mxn.soul.specialalbum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.KeyEventCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class SlidingCard extends LinearLayout {

    private static final String TAG = "SlidingCard";

    private static final boolean DEBUG = false;

    private static final boolean USE_CACHE = true;

    public static final int MAX_SETTLE_DURATION = 600; // ms

    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips

    private PhotoContent photoVo;

    public static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    private boolean mEnabled = true;

    private List<View> mIgnoredViews = new ArrayList<>();

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

    private float mInitialMotionX;

    // variables for drawing
    private float mScrollX = 0.0f;

    /**
     * Position of the last motion event.
     */
    private float mLastMotionX;

    private float mLastMotionY;

    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    protected int mActivePointerId = INVALID_POINTER;

    /**
     * Sentinel value for no current active pointer. Used by
     * {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;

    /**
     * Determines speed during touch scrolling
     */
    protected VelocityTracker mVelocityTracker;

    private int mMinimumVelocity;

    protected int mMaximumVelocity;

    private int mFlingDistance;

    private OnPageChangeListener mOnPageChangeListener;

    private int listIndex;

    private SmoothImageView headImageView;
    private TextView headTextView;

    private View contentView;

    public void setListIndex(int listIndex) {
        this.listIndex = listIndex;
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    /**
     * Indicates that the pager is in an idle, settled state. The current page
     * is fully in view and no animation is in progress.
     */
    public static final int SCROLL_STATE_IDLE = 0;

    /**
     * Indicates that the pager is currently being dragged by the user.
     */
    public static final int SCROLL_STATE_DRAGGING = 1;

    /**
     * Indicates that the pager is in the process of settling to a final
     * position.
     */
    public static final int SCROLL_STATE_SETTLING = 2;

    private int mScrollState = SCROLL_STATE_IDLE;

    /**
     * Callback interface for responding to changing state of the selected page.
     */
    public interface OnPageChangeListener {

        /**
         * This method will be invoked when the current page is scrolled, either
         * as part of a programmatically initiated smooth scroll or a user
         * initiated touch scroll.
         *
         * @param position             Position index of the first page currently being
         *                             displayed. Page position+1 will be visible if
         *                             positionOffset is nonzero.
         * @param positionOffset       Value from [0, 1) indicating the offset from the page at
         *                             position.
         * @param positionOffsetPixels Value in pixels indicating the offset from position.
         */
        public void onPageScrolled(SlidingCard v, int position,
                                   float positionOffset, int positionOffsetPixels);

        /**
         * This method will be invoked when a new page becomes selected.
         * Animation is not necessarily complete.
         * <p/>
         * Position index of the new selected page.
         */
        public void onPageSelected(SlidingCard v, int prevPosition,
                                   int curPosition);

        /**
         * This method will be invoked when a new page becomes selected. after
         * animation has completed.
         * <p/>
         * Position index of the new selected page.
         */
        public void onPageSelectedAfterAnimation(SlidingCard v,
                                                 int prevPosition, int curPosition);

        /**
         * Called when the scroll state changes. Useful for discovering when the
         * user begins dragging, when the pager is automatically settling to the
         * current page, or when it is fully stopped/idle.
         *
         * @param state The new scroll state.
         * @see SlidingCard#SCROLL_STATE_IDLE
         * @see SlidingCard#SCROLL_STATE_DRAGGING
         * @see SlidingCard#SCROLL_STATE_SETTLING
         */
        public void onPageScrollStateChanged(SlidingCard v, int state);

    }


    public SlidingCard(Context context) {
        this(context, null);
    }

    public SlidingCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSlidingCard();
        setContent(new FrameLayout(context));

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
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, itemHeight);
        final int contentWidth = getChildMeasureSpec(widthMeasureSpec, 0, width);
        final int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0,
                height);
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

                // Keep on drawing until the animation has finished.
                ViewCompat.postInvalidateOnAnimation(this);
                return;
            }
        }

        // Done with scroll, clean up state.
        completeScroll();
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        mScrollX = x;
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
            // Done with scroll, no longer want to cache view drawing.
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
                        if (enabled && isLowQuality) {
                            child.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
                            child.setDrawingCacheQuality(DRAWING_CACHE_QUALITY_LOW);
                        }
                    }
                }
            }
        }
    }

    float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) FloatMath.sin(f);
    }

    /**
     * Like {@link View#scrollBy}, but scroll smoothly instead of immediately.
     *
     * @param x the number of pixels to scroll by on the X axis
     * @param y the number of pixels to scroll by on the Y axis
     */
    void smoothScrollTo(int x, int y) {
        smoothScrollTo(x, y, 0);
    }

    /**
     * Like {@link View#scrollBy}, but scroll smoothly instead of immediately.
     *
     * @param x        the number of pixels to scroll by on the X axis
     * @param y        the number of pixels to scroll by on the Y axis
     * @param velocity the velocity associated with a fling, if applicable. (0
     *                 otherwise)
     */
    void smoothScrollTo(int x, int y, int velocity) {
        if (getChildCount() == 0) {
            // Nothing to do.
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

        int duration = 0;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            final float pageDelta = (float) Math.abs(dx) / width;
            duration = (int) ((pageDelta + 1) * 100);
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
    public void addView(View child) {
        try {
            super.removeAllViews();
        } catch (Exception e) {
            Log.e("lq", String.valueOf(e.getMessage()));
        }
        mContent = child;
        super.addView(child);
        disableLayers();
    }

    @Override
    public void removeView(View view) {
        try {
            super.removeView(view);
        } catch (Exception e) {
            Log.e("lq", String.valueOf(e.getMessage()));
        }
        disableLayers();
    }

    public void setContent(int res) {
        setContent(LayoutInflater.from(getContext()).inflate(res, null));
    }

    public void setContent(View v) {
        addView(v);
    }


    public void initCardChildView(PhotoContent userVo) {
        headImageView = (SmoothImageView) findViewById(R.id.user_imageview);
        headTextView = (TextView) findViewById(R.id.user_text);
        contentView = findViewById(R.id.sliding_card_content_view);
        if (userVo != null) {
            initImageLoad(userVo, headImageView);
            initTextView(userVo, headTextView);

        }
    }

    private void initTextView(PhotoContent vo, TextView textView) {

        switch (Integer.valueOf(vo.getId())) {
            case 1:
                textView.setText("当红小花旦越来越惊艳了");
                break;
            case 2:
                textView.setText("早秋长袖连衣裙刮起了唯美浪漫风");
                break;
            case 3:
                textView.setText("高品质裙装美照让你一次看过瘾");
                break;


        }
    }

    private void initImageLoad(PhotoContent vo, SmoothImageView imageView) {
        switch (Integer.valueOf(vo.getId())) {
            case 1:
                imageView.setImageResource(R.drawable.img1);
                break;
            case 2:
                imageView.setImageResource(R.drawable.img2);
                break;
            case 3:
                imageView.setImageResource(R.drawable.img3);
                break;


        }
    }


    public boolean isCardClose() {
        return mCurItem == 0 || mCurItem == 2;
    }

    public int getCardWidth() {
        return mContent.getWidth();
    }

    private void getHitRect(View v, Rect rect) {
        v.getHitRect(rect);
        ViewParent parent = v.getParent();
        while (parent != null && parent != this) {
            View _parent = (View) parent;
            Rect parentRect = new Rect();
            _parent.getHitRect(parentRect);
            rect.left += parentRect.left;
            rect.right += parentRect.left;
            rect.top += parentRect.top;
            rect.bottom += parentRect.top;
            parent = parent.getParent();
        }
    }

    private boolean isInIgnoredView(MotionEvent ev) {
        Rect rect = new Rect();
        for (View v : mIgnoredViews) {
            getHitRect(v, rect);
            if (rect.contains((int) ev.getX(), (int) ev.getY()))
                return true;
        }
        return false;
    }

    private boolean thisTouchAllowed(MotionEvent ev) {
        int x = (int) (ev.getX() + mScrollX);
        if (!isCardClose()) {

            return !isInIgnoredView(ev);

        }
        return false;
    }

    private boolean thisSlideAllowed(float dx) {
        if (!isCardClose()) {

            return true;

        }
        return false;
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
        if (xDiff > mTouchSlop && xDiff > yDiff && thisSlideAllowed(dx)) {
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
            targetPage = (int) Math.round(mCurItem + pageOffset);
        }
        return targetPage;
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Let the focused view and/or our descendants get the key first
        return super.dispatchKeyEvent(event) || executeKeyEvent(event);
    }

    /**
     * You can call this function yourself to have the scroll view perform
     * scrolling from a key event, just as if the event had been dispatched to
     * it by the view hierarchy.
     *
     * @param event The key event to execute.
     * @return Return true if the event was handled, else false.
     */
    public boolean executeKeyEvent(KeyEvent event) {
        boolean handled = false;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    handled = arrowScroll(FOCUS_LEFT);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    handled = arrowScroll(FOCUS_RIGHT);
                    break;
                case KeyEvent.KEYCODE_TAB:
                    if (Build.VERSION.SDK_INT >= 11) {
                        // The focus finder had a bug handling FOCUS_FORWARD and
                        // FOCUS_BACKWARD
                        // before Android 3.0. Ignore the tab key on those
                        // devices.
                        if (KeyEventCompat.hasNoModifiers(event)) {
                            handled = arrowScroll(FOCUS_FORWARD);
                        } else if (KeyEventCompat.hasModifiers(event,
                                KeyEvent.META_SHIFT_ON)) {
                            handled = arrowScroll(FOCUS_BACKWARD);
                        }
                    }
                    break;
            }
        }
        return handled;
    }

    public boolean arrowScroll(int direction) {
        View currentFocused = findFocus();
        if (currentFocused == this)
            currentFocused = null;

        boolean handled = false;

        View nextFocused = FocusFinder.getInstance().findNextFocus(this,
                currentFocused, direction);
        if (nextFocused != null && nextFocused != currentFocused) {

                handled = nextFocused.requestFocus();

        } else if ( direction == FOCUS_BACKWARD) {
            // Trying to move left and nothing there; try to page.
            handled = pageNext();
        }
        if (handled) {
            playSoundEffect(SoundEffectConstants
                    .getContantForFocusDirection(direction));
        }
        return handled;
    }

    boolean pageNext() {
        if (mCurItem > 0) {
            setCurrentItem(mCurItem - 1, true);
            return true;
        }
        return false;
    }



    private void onSecondaryPointerUp(MotionEvent ev) {
        if (DEBUG)
            Log.v(TAG, "onSecondaryPointerUp called");
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
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

        if (!mEnabled)
            return true;

        if (isCardClose())
            return false;

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

        if (DEBUG)
            if (action == MotionEvent.ACTION_DOWN)
                Log.v(TAG, "Received ACTION_DOWN");

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
                if (thisTouchAllowed(ev)) {
                    completeScroll();
                    mIsBeingDragged = false;
                    mIsUnableToDrag = false;
                } else {
                    mIsUnableToDrag = true;
                }
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
    public boolean onTouchEvent(MotionEvent ev) {

        if (!mEnabled)
            return true;

        if (isCardClose())
            return false;

        if (!mIsBeingDragged && !thisTouchAllowed(ev))
            return false;

        // if (!mIsBeingDragged && !mQuickReturn)
        // return false;

        final int action = ev.getAction();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
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
                    // Scroll to follow the motion event
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
                    // Don't lose the rounded component
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
                }
                break;
        }
        return true;
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {

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

    private boolean isLowQuality = false;


    private void disableLayers() {
        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_NONE, null);
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewCompat.setLayerType(getChildAt(i), ViewCompat.LAYER_TYPE_NONE,
                    null);
        }
    }

    public PhotoContent getUserVo() {
        return photoVo;
    }

    public void setUserVo(PhotoContent photoVo) {
        this.photoVo = photoVo;
        initCardChildView(photoVo);
    }


    public View getContentView() {
        return contentView;
    }


}
