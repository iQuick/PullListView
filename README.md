# PullListView
===

PullListView下拉刷新/ 上拉加载更多

## 博客：[http://www.imli.me/2015/03/02/pulllistview](http://www.imli.me/2015/03/02/pulllistview/)

## 使用
用法与平常的ListView一样

>mListView = (PullListView) findViewById(R.id.listview);

>mAdapter = new DataAdapter(this);

>mListView.setAdapter(mAdapter);

>mListView.setPrestrain(true);				// 设置预加载

>mListView.setOnPullListViewListener(this);	// 设置刷新/加载监听

>mListView.setTheEnd();						// 设置已经到底

## 特色
* 增加了预加载功能
* 设置到底（即不再加载更多）
