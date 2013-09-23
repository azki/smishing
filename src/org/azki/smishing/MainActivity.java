package org.azki.smishing;

import java.util.List;
import java.util.Vector;

import net.daum.adam.publisher.AdView;
import net.daum.adam.publisher.AdView.OnAdFailedListener;
import net.daum.adam.publisher.AdView.OnAdLoadedListener;
import net.daum.adam.publisher.impl.AdError;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;

public class MainActivity extends Activity {

	private LayoutInflater mInflater;
	private SharedPreferences pref;
	private CustomAdapter mAdaptor;
	private ListView blockedListView;
	private AdView adView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		pref = getSharedPreferences("pref", Context.MODE_MULTI_PROCESS);
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		blockedListView = (ListView) findViewById(R.id.blockedListView);
		TextView emptyView = (TextView) findViewById(R.id.emptyListTextView);
		blockedListView.setEmptyView(emptyView);

		Vector<RowData> dataList = new Vector<RowData>();
		Gson gson = new Gson();
		int blockedCount = pref.getInt("blockedCount", 0);
		for (int i = 0; i < blockedCount; i++) {
			String rawJson = pref.getString("blocked" + i, null);
			if (rawJson != null) {
				dataList.add(gson.fromJson(rawJson, RowData.class));
			}
		}
		mAdaptor = new CustomAdapter(this, android.R.layout.simple_list_item_1, dataList);
		blockedListView.setAdapter(mAdaptor);

		initAdam();
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (adView != null) {
			adView.pause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (adView != null) {
			adView.resume();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (adView != null) {
			adView.destroy();
			adView = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_list_clear:
			clearBlockedList();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void clearBlockedList() {
		int blockedCount = pref.getInt("blockedCount", 0);
		Editor editor = pref.edit();
		for (int i = 0; i < blockedCount; i++) {
			editor.remove("blocked" + i);
		}
		editor.remove("blockedCount");
		editor.commit();
		mAdaptor.clear();
		mAdaptor.notifyDataSetChanged();
	}

	private class CustomAdapter extends ArrayAdapter<RowData> {
		public CustomAdapter(Context context, int textViewResourceId, List<RowData> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, final ViewGroup parent) {
			final RowData rowData = getItem(position);
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item, null);
			}
			TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
			TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
			TextView senderTextView = (TextView) convertView.findViewById(R.id.senderTextView);

			dateTextView.setText(rowData.when);
			bodyTextView.setText(rowData.body);
			senderTextView.setText(rowData.sender);
			return convertView;
		}
	}

	private void reMarginContentForAd() {
		View contentAreaView = findViewById(R.id.blockedListView);
		RelativeLayout.LayoutParams plControl = (RelativeLayout.LayoutParams) contentAreaView.getLayoutParams();
		plControl.bottomMargin = adView.getHeight();
		contentAreaView.setLayoutParams(plControl);
	}

	private void initAdam() {
		adView = (AdView) findViewById(R.id.adview);
		// 2. 광고 내려받기 실패했을 경우에 실행할 리스너
		adView.setOnAdFailedListener(new OnAdFailedListener() {
			@Override
			public void OnAdFailed(AdError error, String message) {
				reMarginContentForAd();
			}
		});
		// 3. 광고를 정상적으로 내려받았을 경우에 실행할 리스너
		adView.setOnAdLoadedListener(new OnAdLoadedListener() {
			@Override
			public void OnAdLoaded() {
				reMarginContentForAd();
			}
		});
		boolean isNoBanner = pref.getBoolean("isNoBanner", false);
		if (isNoBanner) {
			adView.pause();
			adView.setVisibility(View.GONE);
		} else {
			adView.resume();
			adView.setVisibility(View.VISIBLE);
		}
		reMarginContentForAd();
	}
}
