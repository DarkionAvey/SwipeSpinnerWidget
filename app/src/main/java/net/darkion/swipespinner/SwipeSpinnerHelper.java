package net.darkion.swipespinner;

import android.animation.TimeInterpolator;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.annotation.NonNull;
import android.support.v4.math.MathUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * A spinner which allows the user to scroll the whole list through a single
 * dragging gesture.
 * This is a helper class which binds to an existing recycler
 * view. The use of recycler view ensures better performance and
 * better long-term support.
 * <p>
 * This view supports both vertical and horizontal scrolling;
 * set the orientation by using {@link LinearLayoutManager#setOrientation(int)}
 * <p>
 * Customization is not part of this class; check the companion demo app to see
 * how to add indication arrows and for other tricks.
 *
 * @author code by Darkion Avey;
 * original UX design by Oleg Frolov
 */
public class SwipeSpinnerHelper {
    //set to 1 to make scrolling linear; >1 to make it exponential
    private static final double SCROLL_EXPONENT = 2.8;
    private static final boolean DEBUG = false;
    private RecyclerView mRecyclerView;
    private final static String TAG = "SwipeSpinnerHelper";
    private ScrollCallbacks mScrollCallbacks;
    private float mInitXY;
    private float xyDifference;
    private long mDownTime;
    private int mTouchSlop;
    private boolean mDraggingUp = true;
    private PagerSnapHelper mPagerSnapHelper = new PagerSnapHelper();
    private Runnable mScrollByRunnable = new Runnable() {
        @Override
        public void run() {
            float translation = (isVertical() ? mRecyclerView.getTranslationY() : mRecyclerView.getTranslationX());
            float fraction = MathUtils.clamp(Math.abs((translation / getDragThreshold())), 0f, 1f);
            int scroll = (int) (0f - translation * Math.pow(fraction, SCROLL_EXPONENT));
            if (Math.abs(scroll) > 0 && (isVertical() && mRecyclerView.canScrollVertically(scroll) || !isVertical() && mRecyclerView.canScrollHorizontally(scroll))) {
                if (mScrollCallbacks != null)
                    mScrollCallbacks.onScrolled((translation > 0 ? 1f : -1) * fraction);
                mRecyclerView.scrollBy(isVertical() ? 0 : scroll, isVertical() ? scroll : 0);
            }
            mRecyclerView.post(mScrollByRunnable);
        }
    };

    public static SwipeSpinnerHelper bindRecyclerView(@NonNull RecyclerView recyclerView) {
        return new SwipeSpinnerHelper(recyclerView);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    private SwipeSpinnerHelper(@NonNull final RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (!(recyclerView.getLayoutManager() instanceof LinearLayoutManager))
                    throw new RuntimeException("Layout manager should not be null and should be instance of LinearLayoutManager");
                mPagerSnapHelper.attachToRecyclerView(mRecyclerView);
                ViewConfiguration viewConfiguration = ViewConfiguration.get(mRecyclerView.getContext());
                mTouchSlop = viewConfiguration.getScaledTouchSlop();
                mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                        return handleTouchEvent(e);
                    }

                    @Override
                    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                        handleTouchEvent(e);
                    }

                    @Override
                    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

                    }
                });
            }
        });
    }

    /**
     * Use this method to register callbacks to scrolling and
     * reset events. See {@link ScrollCallbacks}
     *
     * @param scrollCallbacks the callbacks object that you want to register
     */
    public void setScrollCallbacks(ScrollCallbacks scrollCallbacks) {
        this.mScrollCallbacks = scrollCallbacks;
    }

    private LinearLayoutManager getLinearLayoutManager() {
        return (LinearLayoutManager) mRecyclerView.getLayoutManager();
    }

    /**
     * Convenience method to check if attached linear layout manager
     * is vertical or horizontal. This potentially throws NullPointerException
     *
     * @return if LinearLayoutManager is vertical
     */
    public boolean isVertical() {
        return getLinearLayoutManager().getOrientation() == RecyclerView.VERTICAL;
    }

    /**
     * This is used in both onInterceptTouchEvent and onTouchEvent
     * because of the RecyclerView.OnItemTouchListener works. If
     * you were to create a class extending RecyclerView, you can
     * use this only in onTouchEvent, and return true to touch interception
     */
    private boolean handleTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xyDifference = 0;
                //for a lousy fling detection
                mDownTime = System.currentTimeMillis();
                mInitXY = isVertical() ? event.getRawY() : event.getRawX();
                return true;
            case MotionEvent.ACTION_MOVE:
                float currentXY = isVertical() ? event.getRawY() : event.getRawX();
                float translation = xyDifference = currentXY - mInitXY;
                if (Math.abs(xyDifference) > mTouchSlop) {
                    mRecyclerView.removeCallbacks(mScrollByRunnable);
                    mDraggingUp = currentXY < mInitXY;
                    float interpolatedProgress = getInterpolatedProgress(translation, mDraggingUp);
                    if (isVertical())
                        mRecyclerView.setTranslationY(interpolatedProgress);
                    else mRecyclerView.setTranslationX(interpolatedProgress);

                    int direction = (int) (-translation + mRecyclerView.getTranslationY());
                    if (isVertical() && mRecyclerView.canScrollVertically(direction) || !isVertical() && mRecyclerView.canScrollHorizontally(direction))
                        mRecyclerView.post(mScrollByRunnable);
                    return true;

                }
                return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mRecyclerView.removeCallbacks(mScrollByRunnable);
                boolean isFling = System.currentTimeMillis() - mDownTime <= 250 && Math.abs(xyDifference) > mTouchSlop;
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();


                //not using fling detection would result in displacement
                //that is not large enough to move the item (due to interpolation)
                //hence we use try to detect fling and manually shift the item
                if (isFling) {
                    int position = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                    View view = linearLayoutManager.findViewByPosition(position);
                    if (view == null) view = mRecyclerView;
                    int widthHeight = isVertical() ? view.getHeight() : view.getWidth();
                    if (!mDraggingUp) widthHeight *= -1;
                    final int finalWidthHeight = widthHeight;
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.smoothScrollBy(isVertical() ? 0 : finalWidthHeight, isVertical() ? finalWidthHeight : 0);
                        }
                    });

                } else {
                    snapToItem();
                }

                //using spring animation after flinging is distracting
                //hence we opt for eye-friendly DecelerateInterpolator
                if (Math.abs(isVertical() ? mRecyclerView.getTranslationY() : mRecyclerView.getTranslationX()) < getDragThreshold() / 2f)
                    mRecyclerView.animate().setInterpolator(LogDecelerateInterpolator.LOG_DECELERATE_INTERPOLATOR).translationX(0).translationY(0).start();
                else {
                    final SpringAnimation springAnim = new SpringAnimation(mRecyclerView, isVertical() ? DynamicAnimation.TRANSLATION_Y : DynamicAnimation.TRANSLATION_X, 0);
                    springAnim.getSpring().setStiffness(500f);
                    springAnim.start();
                }
                if (mScrollCallbacks != null) mScrollCallbacks.onResetScroll();
                xyDifference = 0;
                return true;
        }
        return true;
    }

    /**
     * Manual invocation of SnapHelper since it doesn't work
     * when using {@link RecyclerView#scrollBy(int, int)}; it
     * only works with touch gestures, which is not the case here.
     */
    private void snapToItem() {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int id = mDraggingUp ? linearLayoutManager.findLastCompletelyVisibleItemPosition() : linearLayoutManager.findFirstCompletelyVisibleItemPosition();
        if (id == -1)
            id = mDraggingUp ? linearLayoutManager.findLastVisibleItemPosition() : linearLayoutManager.findFirstVisibleItemPosition();
        if (id == -1)
            id = mDraggingUp ? linearLayoutManager.findFirstCompletelyVisibleItemPosition() : linearLayoutManager.findLastCompletelyVisibleItemPosition();
        if (id == -1)
            id = mDraggingUp ? linearLayoutManager.findFirstVisibleItemPosition() : linearLayoutManager.findLastVisibleItemPosition();

        if (id != -1) {
            View view = linearLayoutManager.findViewByPosition(id);
            if (view != null) {
                int[] values = mPagerSnapHelper.calculateDistanceToFinalSnap(linearLayoutManager, view);
                if (values != null) mRecyclerView.smoothScrollBy(values[0], values[1]);
                else if (DEBUG) Log.w(TAG, "snapToItem: values[] is null");
            } else if (DEBUG) Log.w(TAG, "snapToItem: view is null");
        } else if (DEBUG) Log.w(TAG, "snapToItem: ID is -1");
    }

    /**
     * Interpolate drag action to prevent over-drag beyond threshold
     *
     * @author Plaid@Github
     */
    private float getInterpolatedProgress(float translation, boolean draggingUp) {
        float dragFraction = (float) Math.log10(1 + (Math.abs(translation) / getDragThreshold()));
        float dragTo = dragFraction * getDragThreshold();
        if (draggingUp) {
            dragTo *= -1;
        }
        return dragTo;
    }

    /**
     * @return how many pixel this view can be dragged
     */
    private float getDragThreshold() {
        return isVertical() ? mRecyclerView.getHeight() : mRecyclerView.getWidth() * (isVertical() ? 1.5f : 0.9f);
    }

    /**
     * @author Launcher3
     */
    private static class LogDecelerateInterpolator implements TimeInterpolator {
        int mBase;
        int mDrift;
        final float mLogScale;
        static final LogDecelerateInterpolator LOG_DECELERATE_INTERPOLATOR = new LogDecelerateInterpolator(80, 0);

        LogDecelerateInterpolator(int base, int drift) {
            mBase = base;
            mDrift = drift;
            mLogScale = 1f / computeLog(1, mBase, mDrift);
        }

        static float computeLog(float t, int base, int drift) {
            return (float) -Math.pow(base, -t) + 1 + (drift * t);
        }

        @Override
        public float getInterpolation(float t) {
            return Float.compare(t, 1f) == 0 ? 1f : computeLog(t, mBase, mDrift) * mLogScale;
        }
    }

    /**
     * Interface that allows detection of scrolling events.
     * You can use directedInterpolationFraction to detect the
     * scrolling direction as well as the amount.
     * directedInterpolationFraction value is between -1 & 1
     */
    public interface ScrollCallbacks {
        void onScrolled(float directedInterpolationFraction);

        void onResetScroll();
    }
}
