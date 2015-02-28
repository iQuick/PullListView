package me.imli.pulllistview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DataAdapter extends BaseAdapter {
	
	private List<String> mList = new ArrayList<String>();
	
	private Context mContext;
	
	public DataAdapter(Context context) {
		mContext = context;
	}
	
	public void update(List<String> list) {
		this.mList.clear();
		this.notifyDataSetChanged();
		this.mList.addAll(list);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public String getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			holder = new Holder();
			convertView = LinearLayout.inflate(mContext, R.layout.item_data, null);
			holder.tv = (TextView) convertView.findViewById(R.id.tv);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		holder.tv.setText(getItem(position));
		return convertView;
	}

	public class Holder {
		public TextView tv;
	}
}
