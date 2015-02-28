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
 * PullListView ����ˢ��/�������ظ���
 * 
 * ӵ��Ԥ���ع��ܣ���ListView�����������ʱ���Զ����ظ���
 * ��ֻ��Ҫ���� {@link setPrestrain()}���ɣ�
 * @author Doots
 *
 */
public class PullListView extends ListView implements OnScrollListener {
	
	/** TAG */
	protected final String TAG = getClass().getName();

	// ��̬����
	private final static int SCROLL_DURATION = 400; 		// ����ʱ��
	private final static int PULL_LOAD_MORE_DELTA = 100;	// ������С�߶�
	private final static float OFFSET_RADIO = 1.8f; 		// ����ϵ��
	
	// ����ͷ�����ߵײ�
	private int mScrollBack = SCROLLBACK_HEADER;			// ��ǰ����״̬
	private final static int SCROLLBACK_HEADER = 0;			// ����ͷ��
	private final static int SCROLLBACK_FOOTER = 1;			// ���صײ�
	
	private boolean mIsPrestrain = false;		// �Ƿ�Ԥ����
	
	private float mLastY = -1;					// ��������y����
	private Scroller mScroller;					// ʹ��scroll����
	private OnScrollListener mScrollListener;	// scroll����
	
	// Header View
	private PullListViewHeader mHeaderView;		// ͷ����ˢ��
	private boolean mEnablePullRefresh = true;	// �Ƿ�����
	private boolean mPullRefreshing = false;	// �Ƿ�����ˢ��

	// Footer View
	private PullListViewFooter mFooterView;		// �ײ������ظ���
	private boolean mEnablePullLoad = true;		// �Ƿ�����
	private boolean mPullLoading = false;		// �Ƿ�������
	private boolean mIsFooterReady = false;		// �Ƿ�������

	// ��Item��
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
		// ���ù�������
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
	 * �����Ƿ�Ԥ����
	 * @param prestrain
	 */
	public void setPrestrain(boolean prestrain) {
		mIsPrestrain = prestrain;
	}
	
	/**
	 * �����Ѿ�����
	 */
	public void setTheEnd() {
		mFooterView.setState(PullListViewFooter.STATE_END);
	}
	
	/**
	 * ����ͷ��HeadView�Ƿ�����
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
	 * ���õײ�FooterView�Ƿ�����
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
	 * ֹͣˢ��
	 */
	public void stopRefresh() {
		if (mPullRefreshing) {
			mPullRefreshing = false;
			resetHeaderHeight();
		}
	}
	
	/**
	 * ֹͣ���ظ���
	 */
	public void stopLoadMore() {
		if (mPullLoading) {
			mPullLoading = false;
			mFooterView.setState(PullListViewFooter.STATE_NORMAL);
		}
	}
	
	/**
	 * ���ظ���
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
	 * ��������
	 */
	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnPullScrollListener) {
			OnPullScrollListener l = (OnPullScrollListener) mScrollListener;
			l.onPullScrolling(this);
		}
	}
	
	/**
	 * ˢ��ͷ���߶�
	 */
	private void updateHeaderHeight(float delta) {
		mHeaderView.setVisiableHeight((int) delta + mHeaderView.getVisiableHeight());
		if (mEnablePullRefresh && !mPullRefreshing) {
			// δ����ˢ��״̬�����¼�ͷ
			if (mHeaderView.getVisiableHeight() > mHeaderView.getContentHeight()) {
				mHeaderView.setState(PullListViewHeader.STATE_READY);
			} else {
				mHeaderView.setState(PullListViewHeader.STATE_NORMAL);
			}
		}
		// �ع�������
		setSelection(0);
	}
	
	/**
	 * ����HeaderView�߶�
	 */
	private void resetHeaderHeight() {
		int height = mHeaderView.getVisiableHeight();
		if (height == 0) return;	// û�пɼ�����
		
		// ����ˢ�»��������߶�С��ͷ���߶�
		if (mPullRefreshing && (height <= mHeaderView.getContentHeight())) {
			return;
		}
		
		// ���ո߶�
		int finalHeight = 0;
		// ����ˢ�»��������߶ȴ���ͷ���߶�
		if (mPullRefreshing && (height > mHeaderView.getContentHeight())) {
			finalHeight = mHeaderView.getContentHeight();
		}
		
		mScrollBack = SCROLLBACK_HEADER;
		mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
		invalidate();
	}
	
	/**
	 * ����FooterView�߶�
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
	 * ����FooterView�߶�
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
	 * ��ʼˢ��
	 */
	private void startRefresh() {
		mPullRefreshing = true;
		mHeaderView.setState(PullListViewHeader.STATE_REFRESHING);
		if (mOnPullListViewListener != null) {
			mOnPullListViewListener.onRefresh();
		}
	}
	
	
	/**
	 * ��ʼ���ظ���
	 */
	private void startLoadMore() {
		mPullLoading = true;
		mFooterView.setState(PullListViewFooter.STATE_LOADING);
		if (mOnPullListViewListener != null) {
			mOnPullListViewListener.onLoadMore();
		}
	}
	
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
			if (getFirstVisiblePosition() == 0 && (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
				// �ڶ������ҿɼ��߶Ȼ����ƶ��������0
				updateHeaderHeight(deltaY / OFFSET_RADIO);
				invokeOnScrolling();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1 && (mFooterView.getBottomPadding() > 0 || deltaY < 0)) {
				// �ڵײ�������׵ľ�������ƶ��������0
				updateFooterHeight(-deltaY / OFFSET_RADIO);
			}
			break;
		default:
			mLastY = -1; // ����
			if (getFirstVisiblePosition() == 0) {
				// ˢ��
				if (mEnablePullRefresh && mHeaderView.getVisiableHeight() > mHeaderView.getContentHeight() && !mPullRefreshing) {
					startRefresh();
				}
				resetHeaderHeight();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1) {
				// ���ظ���
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
		// ȷ��FooterView���������棬����ֻ���һ��
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
		
		// Ԥ����
		if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount != 0 && totalItemCount != getHeaderViewsCount() + getFooterViewsCount()) {
			if (mIsPrestrain && mEnablePullLoad && !mPullLoading && mFooterView.getState() != PullListViewFooter.STATE_END) {
				startLoadMore();
			}
		}
	}
	

	// ================================
	// OnPullScrollListener ��������
	// ================================
	public interface OnPullScrollListener extends OnScrollListener {
		/** ���ڻ���  */
		public void onPullScrolling(View v);
	}
	
	// ================================
	// PullListView ����
	// ================================
	public interface OnPullListViewListener {
		/** ���� */
		public void onRefresh();
		/** ���ظ��� */
		public void onLoadMore();
	}
	private OnPullListViewListener mOnPullListViewListener;
	public void setOnPullListViewListener(OnPullListViewListener l) {
		mOnPullListViewListener = l;
	}

}
