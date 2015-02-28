package me.imli.pulllistview.view;

import me.imli.pulllistview.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * PullListViewHeader
 * @author Doots
 *
 */
public class PullListViewHeader extends LinearLayout implements View.OnClickListener {
	
	/** TAG */
	protected final String TAG = getClass().getName();
	
	// 状态枚举
	public final static int STATE_NORMAL = 0;		// 正常
	public final static int STATE_READY = 1;		// 准备
	public final static int STATE_REFRESHING = 2;	// 正在刷新
	private int mState = STATE_NORMAL;
	
	private int mContentHeight = 0;
	
	// 动画
	private final int ROTATE_ANIM_DURATION = 180;
	private Animation mRotateUpAnim;		// 箭头向上旋转
	private Animation mRotateDownAnim;		// 箭头向下旋转
	
	private ViewGroup mRoot;				// Root
	private ViewGroup mContent;				// 内容
	private ImageView mIvArrow;				// 箭头
	private ProgressBar mPB;				// 进度条
	private TextView mTvTip;				// 提示文字
	

	public PullListViewHeader(Context context) {
		super(context);
		init();
	}

	public PullListViewHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		// init layout
		LinearLayout.inflate(getContext(), R.layout.layout_listview_header, this);
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		setLayoutParams(params);
		setOnClickListener(this);
		
		// init control
		mIvArrow = (ImageView) findViewById(R.id.layout_listview_header_arrow);
		mTvTip = (TextView) findViewById(R.id.layout_listview_header_hint_textview);
		mPB = (ProgressBar) findViewById(R.id.layout_listview_header_progressbar);

		mContent = (ViewGroup) findViewById(R.id.layout_listview_header_content);
		mContent.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mContentHeight = mContent.getHeight();
			}
		});
		
		mRoot = (ViewGroup) findViewById(R.id.layout_listview_header_parent);
		LayoutParams lp = (LayoutParams) mRoot.getLayoutParams();
		lp.height = 0;
		mRoot.setLayoutParams(lp);
		
		// init anim
		mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateUpAnim.setFillAfter(true);
		
		mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateDownAnim.setFillAfter(true); 
	}
	
	/**
	 * 设置状态
	 * @param state
	 */
	public void setState(int state) {
		if (state == mState) return;
		
		if (state == STATE_REFRESHING) {
			// 正在刷新, 隐藏箭头
			mIvArrow.clearAnimation();
			mIvArrow.setVisibility(View.INVISIBLE);
			mPB.setVisibility(View.VISIBLE);
		} else {
			// 显示箭头
			mIvArrow.setVisibility(View.VISIBLE);
			mPB.setVisibility(View.INVISIBLE);
		}
		
		switch (state) {
		case STATE_NORMAL:
			if (mState == STATE_READY) {
				mIvArrow.startAnimation(mRotateDownAnim);
			} else if (mState == STATE_REFRESHING) {
				mIvArrow.clearAnimation();
			}
			mTvTip.setText(R.string.pull_header_hint_normal);
			break;
		case STATE_READY:
			if (mState != STATE_READY) {
				mIvArrow.clearAnimation();
				mIvArrow.startAnimation(mRotateUpAnim);
				mTvTip.setText(R.string.pull_header_hint_ready);
			}
			break;
		case STATE_REFRESHING:
			mTvTip.setText(R.string.pull_header_hint_loading);
			break;
		default:
			break;
		}
		
		// 更改状态
		mState = state;
	}
	
	/**
	 * 获取状态
	 * @return
	 */
	public int getState() {
		return mState;
	}
	
	/**
	 * 设置可见高度
	 * @param height
	 */
	public void setVisiableHeight(int height) {
		if (height < 0) height = 0;
		// 设置高度
		LayoutParams params = (LayoutParams) mRoot.getLayoutParams();
		params.height = height;
		mRoot.setLayoutParams(params);
	}
	
	/**
	 * 获取可见高度
	 * @return
	 */
	public int getVisiableHeight() {
		return mRoot.getHeight();
	}
	
	/**
	 * 获取内容高度
	 * @return
	 */
	public int getContentHeight() {
		return mContentHeight;
	}
	
	/**
	 * 显示
	 */
	public void show() {
		setVisibility(View.VISIBLE);
	}
	
	/**
	 * 隐藏
	 */
	public void hide() {
		setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		return;
	}
}
