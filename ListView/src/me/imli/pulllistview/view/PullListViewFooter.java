package me.imli.pulllistview.view;

import me.imli.pulllistview.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * PullListViewFooter
 * @author Doots
 *
 */
public class PullListViewFooter extends LinearLayout {
	
	/** TAG */
	protected final String TAG = getClass().getName();
	
	// ״̬ö��
	private int mState = STATE_NORMAL;
	public final static int STATE_NORMAL = 0;		// ����
	public final static int STATE_READY = 1;		// ׼��
	public final static int STATE_LOADING = 2;		// ���ڼ���
	public final static int STATE_END = 3;			// �Ѿ�����
	
	private TextView mTvTip;		// ��ʾ����
	private ProgressBar mPB;		// ������

	public PullListViewFooter(Context context) {
		super(context);
		init();
	}

	public PullListViewFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		// init layout
		LinearLayout.inflate(getContext(), R.layout.layout_listview_footer, this);
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		setLayoutParams(params);
		setGravity(Gravity.TOP);
		
		// init resource
		mPB = (ProgressBar) findViewById(R.id.layout_listview_footer_progressbar);
		mTvTip = (TextView) findViewById(R.id.layout_listview_footer_hint_textview);
	}
	
	/**
	 * ����״̬
	 * @param state
	 */
	public void setState(int state) {
		mState = state;
		mTvTip.setVisibility(View.INVISIBLE);
		mPB.setVisibility(View.INVISIBLE);
		
		if (state == STATE_READY) {
			mTvTip.setVisibility(View.VISIBLE);
			mTvTip.setText(R.string.pull_footer_hint_ready);
		} else if (state == STATE_LOADING) {
			mPB.setVisibility(View.VISIBLE);
		} else if (state == STATE_NORMAL) {
			mTvTip.setVisibility(View.VISIBLE);
			mTvTip.setText(R.string.pull_footer_hint_normal);
		} else if (state == STATE_END) {
			mTvTip.setVisibility(View.VISIBLE);
			mTvTip.setText(R.string.pull_footer_hint_end);
		}
	}
	
	/**
	 * ��ȡ״̬
	 * @return
	 */
	public int getState() {
		return mState;
	}
	
	/**
	 * ���õײ�����
	 * @param height
	 */
	public void setBottomPadding(int height) {
		if (height < 0) return;
		setPadding(0, 0, 0, height);
	}
	
	/**
	 * ��ȡ�ײ�����
	 */
	public int getBottomPadding() {
		return getPaddingBottom();
	}
	
	/**
	 * normal status
	 */
	public void normal() {
		mTvTip.setVisibility(View.VISIBLE);
		mTvTip.setText(R.string.pull_footer_hint_normal);
		mPB.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * loading status
	 */
	public void loading() {
		mTvTip.setVisibility(View.INVISIBLE);
		mPB.setVisibility(View.VISIBLE);
	}
	
	/**
	 * show footer
	 */
	public void show() {
		AbsListView.LayoutParams params = (AbsListView.LayoutParams) getLayoutParams();
		params.height = 0;
		setLayoutParams(params);
	}
	
	/**
	 * hide footer
	 */
	public void hide() {
		AbsListView.LayoutParams params = (AbsListView.LayoutParams) getLayoutParams();
		params.height = LayoutParams.WRAP_CONTENT;
		setLayoutParams(params);
	}
}
