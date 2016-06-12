package com.example.chukc.pulltorefreshlistview.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chukc.pulltorefreshlistview.R;


/**
 * 如果不需要下拉刷新直接在canPullDown中返回false，这里的自动加载和下拉刷新没有冲突，通过增加在尾部的footerview实现自动加载，
 * 所以在使用中不要再动footerview了
 */
public class PullableListView extends ListView implements Pullable {
    public static final int INIT = 0;
    public static final int LOADING = 1;
    private OnLoadListener mOnLoadListener;
    private ImageView mLoadingView;
    private TextView mStateTextView;
    private int state = INIT;
    private boolean canLoad = true;
    private boolean isFinishde = false; //數據是否全部加載完成
    private RelativeLayout footer;
    protected Scroller mScroller; // used for scroll back
    protected final static int SCROLL_DURATION = 400; // scroll back duration
    // 均匀旋转动画
    private RotateAnimation refreshingAnimation;
    private View view;
    private int FooterVisibleHeight = 120;
    private boolean isNoData; //是否没有数据
//    private AnimationDrawable mLoadAnim;

    public PullableListView(Context context) {
        super(context);
        init(context);
    }

    public PullableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context, new DecelerateInterpolator());
        view = LayoutInflater.from(context).inflate(R.layout.refresh_more,
                null);
        refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.rotating);
        footer = (RelativeLayout) view.findViewById(R.id.rl_footer);
        mLoadingView = (ImageView) footer.findViewById(R.id.loading_icon);
        mStateTextView = (TextView) footer.findViewById(R.id.loadstate_tv);
        addFooterView(view, null, false);
    }

//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        super.onLayout(changed, l, t, r, b);
//        // make sure XListViewFooter is the last footer view, and only add once.
//        if (mIsFooterReady == false) {
//            // if not inflate screen ,footerview not add
//            if (getAdapter() != null) {
//                if (getLastVisiblePosition() != (getAdapter().getCount() - 1)) {
//                    mIsFooterReady = true;
//                    addFooterView(view, null, false);
//                }
//            }
//
//        }
//    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            setBottomMargin(mScroller.getCurrY());
            postInvalidate();
        } else {
        }
        super.computeScroll();
    }

    public void setBottomMargin(int height) {
        if (height < 0) return;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) footer.getLayoutParams();
        lp.bottomMargin = height;
        footer.setLayoutParams(lp);
    }

    protected void resetFooterHeight() {
        int bottomMargin = getBottomMargin();
        if (bottomMargin > 0) {
            mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
            invalidate();
        }
    }


    public int getBottomMargin() {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) footer.getLayoutParams();
        return lp.bottomMargin;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // 按下的时候禁止自动加载
                canLoad = false;
                break;
            case MotionEvent.ACTION_UP:
                // 松开手判断是否自动加载
                canLoad = true;
                isFinishde = false;
                checkLoad();
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // 在滚动中判断是否满足自动加载条件
            checkLoad();
    }

    /**
     * 判断是否满足自动加载条件
     */
    private void checkLoad() {
//        if (footer.getHeight() < FooterVisibleHeight)
            showFooter();
        if (reachBottom() && mOnLoadListener != null && state != LOADING
                && canLoad && !isFinishde) {
            Toast.makeText(getContext(),"底部自动加载",Toast.LENGTH_LONG).show();
            isNoData=false;
            changeState(LOADING);
            mOnLoadListener.onLoad(this);
            resetFooterHeight();
        }
    }

    private void changeState(int state) {
        this.state = state;
        switch (state) {
            case INIT:
                mLoadingView.clearAnimation();
                mLoadingView.setVisibility(View.INVISIBLE);
                if (mStateTextView != null)
                    mStateTextView.setVisibility(View.VISIBLE);
                mStateTextView.setText("数据已加载完");
                break;

            case LOADING:
                mLoadingView.setVisibility(View.VISIBLE);
                mLoadingView.startAnimation(refreshingAnimation);
                if (mStateTextView != null)
                    mStateTextView.setVisibility(View.GONE);
                mStateTextView.setText("加载中");
                break;
        }
    }

    /**
     * 完成加载
     */
    public void finishLoading() {
        changeState(INIT);
    }

    @Override
    public boolean canPullDown() {
        if (getCount() == 0) {
            // 没有item的时候也可以下拉刷新
            return true;
        } else if (getFirstVisiblePosition() == 0
                && getChildAt(0).getTop() >= 0) {
            // 滑到ListView的顶部了
            return true;
        } else
            return false;
    }

    @Override
    public boolean canPullUp() {
        if (getCount() == 0) {
            // 没有item的时候也可以上拉加载
            return true;
        } else if (getLastVisiblePosition() == (getCount() - 1)) {
            // 滑到底部了
            if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null
                    && getChildAt(
                    getLastVisiblePosition()
                            - getFirstVisiblePosition()).getBottom() <= getMeasuredHeight()&&!isNoData)
                return true;
        }
        return false;
    }


    public void setOnLoadListener(OnLoadListener listener) {
        this.mOnLoadListener = listener;
    }

    /**
     * @return footerview可见时返回true，否则返回false
     */
    public boolean reachBottom() {
        if (getCount() == 0) {
            // 没有item的时候也可以上拉加载
            return true;
        } else if (getLastVisiblePosition() == (getCount() - 1)) {
            // 滑到底部了
            if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null
                    && getChildAt(
                    getLastVisiblePosition()
                            - getFirstVisiblePosition()).getTop() < getMeasuredHeight())
                return true;
        }
        return false;
    }

    public void setFinishedFooter() {
        this.state = INIT;
        isFinishde = true;
        hideFooter();
    }


    public void hideFooter() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) footer.getLayoutParams();
        params.height = 0;
        footer.setLayoutParams(params);
    }

    private void showFooter() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) footer.getLayoutParams();
        params.height = FooterVisibleHeight;
        footer.setLayoutParams(params);
    }

    public void setFinishedView() {
        mLoadingView.setVisibility(View.INVISIBLE);
        mStateTextView.setText("数据全部加载完成");
    }

    public void removeFooter() {
//        removeFooterView(view);
        footer.setVisibility(View.GONE);
        isNoData=true;
    }

    public boolean getIsNoData(){
        return  isNoData;
    }
    public void addFooter() {
        addFooterView(view, null, false);
    }

    public interface OnLoadListener {
        void onLoad(PullableListView pullableListView);
    }
}

