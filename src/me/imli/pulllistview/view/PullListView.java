package me.imli.pulllistview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Scroller;

/**
 * PullListView 下拉刷新/卡拉加载更多
 * 
 * 拥有预加载功能，即ListView滑动到最后几行时会自动加载更多
 * （只需要设置 {@link setPrestrain()}即可）
 * @author Doots
 *
 */
public class PullListView extends ListView implements OnScrollListener {
	
	/** TAG */
	protected final String TAG = getClass().getName();

	// 静态参数
	private final static int SCROLL_DURATION = 400; 		// 返回时间
	private final static int PULL_LOAD_MORE_DELTA = 100;	// 上拉最小高度
	private final static float OFFSET_RADIO = 1.8f; 		// 滑动系数
	
	// 返回头部或者底部
	private int mScrollBack = SCROLLBACK_HEADER;			// 当前返回状态
	private final static int SCROLLBACK_HEADER = 0;			// 返回头部
	private final static int SCROLLBACK_FOOTER = 1;			// 返回底部
	
	private boolean mIsPrestrain = false;		// 是否预加载
	
	private float mLastY = -1;					// 保存点击的y坐标
	private Scroller mScroller;					// 使用scroll返回
	private OnScrollListener mScrollListener;	// scroll监听
	
	// Header View
	private PullListViewHeader mHeaderView;		// 头部，刷新
	private boolean mEnablePullRefresh = true;	// 是否启用
	private boolean mPullRefreshing = false;	// 是否正在刷新

	// Footer View
	private PullListViewFooter mFooterView;		// 底部，加载更多
	private boolean mEnablePullLoad = true;		// 是否启用
	private boolean mPullLoading = false;		// 是否正加载
	private boolean mIsFooterReady = false;		// 是否可以添加

	// 总Item数
	private int mTotalItemCount;

	public PullListView(Context context) {
		super(context);
		init();
	}

	public PullListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PullListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	private void init() {
		mScroller = new Scroller(getContext(), new DecelerateInterpolator());
		// 设置滚动监听
		super.setOnScrollListener(this);
		
		// init header
		mHeaderView = new PullListViewHeader(getContext());
		addHeaderView(mHeaderView);
		
		// init footer
		mFooterView = new PullListViewFooter(getContext());
		setFooterDividersEnabled(true);
		mFooterView.setOnClickListener(loadMore());
	}
	
	/**
	 * 设置是否预加载
	 * @param prestrain
	 */
	public void setPrestrain(boolean prestrain) {
		mIsPrestrain = prestrain;
	}
	
	/**
	 * 设置已经到底
	 */
	public void setTheEnd() {
		mFooterView.setState(PullListViewFooter.STATE_END);
	}
	
	/**
	 * 设置头部HeadView是否启用
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) {
			mHeaderView.hide();
		} else {
			mHeaderView.show();
		}
	}
	
	/**
	 * 设置底部FooterView是否启用
	 * @param enable
	 */
	public void setPullLoadEnable(boolean enable) {
		mEnablePullLoad = enable;
		if (!mEnablePullLoad) {
			mFooterView.hide();
			mFooterView.setOnClickListener(null);
			//make sure "pull up" don't show a line in bottom when listview with one page 
			setFooterDividersEnabled(false);
		} else {
			mPullLoading = false;
			mFooterView.show();
			mFooterView.setState(PullListViewFooter.STATE_NORMAL);
			//make sure "pull up" don't show a line in bottom when listview with one page  
			setFooterDividersEnabled(true);
			// both "pull up" and "click" will invoke load more.
			mFooterView.setOnClickListener(loadMore());
		}
	}
	
	/**
	 * 停止刷新
	 */
	public void stopRefresh() {
		if (mPullRefreshing) {
			mPullRefreshing = false;
			resetHeaderHeight();
		}
	}
	
	/**
	 * 停止加载更多
	 */
	public void stopLoadMore() {
		if (mPullLoading) {
			mPullLoading = false;
			mFooterView.setState(PullListViewFooter.STATE_NORMAL);
		}
	}
	
	/**
	 * 加载更多
	 * @return
	 */
	private OnClickListener loadMore() {
		return new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mFooterView.getState() != PullListViewFooter.STATE_END) {
					startLoadMore();
				}
			}
		};
	}
	
	/**
	 * 滑动监听
	 */
	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnPullScrollListener) {
			OnPullScrollListener l = (OnPullScrollListener) mScrollListener;
			l.onPullScrolling(this);
		}
	}
	
	/**
	 * 刷新头部高度
	 */
	private void updateHeaderHeight(float delta) {
		mHeaderView.setVisiableHeight((int) delta + mHeaderView.getVisiableHeight());
		if (mEnablePullRefresh && !mPullRefreshing) {
			// 未处于刷新状态，更新箭头
			if (mHeaderView.getVisiableHeight() > mHeaderView.getContentHeight()) {
				mHeaderView.setState(PullListViewHeader.STATE_READY);
			} else {
				mHeaderView.setState(PullListViewHeader.STATE_NORMAL);
			}
		}
		// 回滚到顶部
		setSelection(0);
	}
	
	/**
	 * 重置HeaderView高度
	 */
	private void resetHeaderHeight() {
		int height = mHeaderView.getVisiableHeight();
		if (height == 0) return;	// 没有可见部分
		
		// 正在刷新或者下拉高度小于头部高度
		if (mPullRefreshing && (height <= mHeaderView.getContentHeight())) {
			return;
		}
		
		// 最终高度
		int finalHeight = 0;
		// 正在刷新或者下拉高度大于头部高度
		if (mPullRefreshing && (height > mHeaderView.getContentHeight())) {
			finalHeight = mHeaderView.getContentHeight();
		}
		
		mScrollBack = SCROLLBACK_HEADER;
		mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
		invalidate();
	}
	
	/**
	 * 更新FooterView高度
	 * @param delta
	 */
	private void updateFooterHeight(float delta) {
		int height = mFooterView.getBottomPadding() + (int) delta;
		if (mEnablePullLoad && !mPullLoading && mFooterView.getState() != PullListViewFooter.STATE_END) {
			if (height > PULL_LOAD_MORE_DELTA) {
				mFooterView.setState(PullListViewFooter.STATE_READY);
			} else {
				mFooterView.setState(PullListViewFooter.STATE_NORMAL);
			}
		}
		mFooterView.setBottomPadding(height);
	}
	
	/**
	 * 重置FooterView高度
	 */
	private void resetFooterHeight() {
		int bottomMargin = mFooterView.getBottomPadding();
		if (bottomMargin > 0) {
			mScrollBack = SCROLLBACK_FOOTER;
			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
			invalidate();
		}
	}
	
	/**
	 * 开始刷新
	 */
	private void startRefresh() {
		mPullRefreshing = true;
		mHeaderView.setState(PullListViewHeader.STATE_REFRESHING);
		if (mOnPullListViewListener != null) {
			mOnPullListViewListener.onRefresh();
		}
	}
	
	
	/**
	 * 开始加载更多
	 */
	private void startLoadMore() {
		mPullLoading = true;
		mFooterView.setState(PullListViewFooter.STATE_LOADING);
		if (mOnPullListViewListener != null) {
			mOnPullListViewListener.onLoadMore();
		}
	}
	
	private int mRefreshOrLoad = 0;
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}
		
		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			if (getFirstVisiblePosition() == 0 && (mHeaderView.getVisiableHeight() > 0 || deltaY > 0) && mRefreshOrLoad !=2) {
				// 在顶部并且可见高度或者移动距离大于0
				updateHeaderHeight(deltaY / OFFSET_RADIO);
				invokeOnScrolling();
				mRefreshOrLoad = 1;
			} else if (getLastVisiblePosition() == mTotalItemCount - 1 && (mFooterView.getBottomPadding() > 0 || deltaY < 0) && mRefreshOrLoad != 1) {
				// 在底部并且与底的距离或者移动距离大于0
				updateFooterHeight(-deltaY / OFFSET_RADIO);
				mRefreshOrLoad = 2;
			}
			break;
		default:
			mLastY = -1; // 重置
			if (getFirstVisiblePosition() == 0 && mRefreshOrLoad == 1) {
				// 刷新
				if (mEnablePullRefresh && mHeaderView.getVisiableHeight() > mHeaderView.getContentHeight() && !mPullRefreshing) {
					startRefresh();
				}
				resetHeaderHeight();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1 && mRefreshOrLoad == 2) {
				// 加载更多
				if (mEnablePullLoad && mFooterView.getBottomPadding() > PULL_LOAD_MORE_DELTA && !mPullLoading && mFooterView.getState() != PullListViewFooter.STATE_END) {
					startLoadMore();
				}
				resetFooterHeight();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	@Override
	public void setAdapter(ListAdapter adapter) {
		// 确保FooterView是在最下面，并且只添加一次
		if (mIsFooterReady == false) {
			mIsFooterReady = true;
			addFooterView(mFooterView);
		}
		super.setAdapter(adapter);
	}
	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				mHeaderView.setVisiableHeight(mScroller.getCurrY());
			} else if (mScrollBack == SCROLLBACK_FOOTER) {
				mFooterView.setBottomPadding(mScroller.getCurrY());
			}
			postInvalidate();
			invokeOnScrolling();
		}
		super.computeScroll();
	}
	
	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
		
		// 预加载
		if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount != 0 && totalItemCount != getHeaderViewsCount() + getFooterViewsCount()) {
			if (mIsPrestrain && mEnablePullLoad && !mPullLoading && mFooterView.getState() != PullListViewFooter.STATE_END) {
				startLoadMore();
			}
		}
	}
	

	// ================================
	// OnPullScrollListener 滑动监听
	// ================================
	public interface OnPullScrollListener extends OnScrollListener {
		/** 正在滑动  */
		public void onPullScrolling(View v);
	}
	
	// ================================
	// PullListView 监听
	// ================================
	public interface OnPullListViewListener {
		/** 更新 */
		public void onRefresh();
		/** 加载更多 */
		public void onLoadMore();
	}
	private OnPullListViewListener mOnPullListViewListener;
	public void setOnPullListViewListener(OnPullListViewListener l) {
		mOnPullListViewListener = l;
	}

}
