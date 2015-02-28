package me.imli.pulllistview;

import java.util.ArrayList;
import java.util.List;

import me.imli.pulllistview.view.PullListView;
import me.imli.pulllistview.view.PullListView.OnPullListViewListener;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class MainActivity extends Activity implements OnPullListViewListener {
	
	private List<String> mList;
	private PullListView mListView;
	private DataAdapter mAdapter;
	
	private Handler mHandler;
	
	private int count = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mHandler = new Handler();
		
		mListView = (PullListView) findViewById(R.id.listview);
		mAdapter = new DataAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setPrestrain(true);
		mListView.setOnPullListViewListener(this);
		
		mList = new ArrayList<String>();
		for (int i = 0; i < 20; i++) {
			mList.add("Item " + i);
		}
		mAdapter.update(mList);
	}

	@Override
	public void onRefresh() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {

				Log.i("aaaaa", "onRefresh");
				mList.add(0, "new Item");
				mAdapter.update(mList);
				mListView.stopRefresh();
			}
		}, 2000);
	}

	@Override
	public void onLoadMore() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {

				Log.i("aaaaa", "onLoadMore");
				int size = mList.size();
				for (int i = size; i < size + 10; i++) {
					Log.i("aaa", i + "");
					mList.add("Item " + i);
				}
				mAdapter.update(mList);
				mListView.stopLoadMore();
				if (count++ >= 5) {
					mListView.setTheEnd();
				}
			}
		}, 2000);
	}

}
