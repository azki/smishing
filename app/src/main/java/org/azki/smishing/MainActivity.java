package org.azki.smishing;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;
import java.util.Vector;

public class MainActivity extends Activity {

    private LayoutInflater mInflater;
    private SharedPreferences pref;
    private CustomAdapter mAdaptor;
    private ListView blockedListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPref();

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
        mAdaptor = new CustomAdapter(this, android.R.layout.simple_list_item_1,
                dataList);
        blockedListView.setAdapter(mAdaptor);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoProVersion();
            }
        });

        SmishingApplication.showAd(this);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initPref() {
        pref = getSharedPreferences("pref", Context.MODE_MULTI_PROCESS);
    }

    @Override
    public void onStart() {
        super.onStart();
        GA.sendView("org.azki.smishing.MainActivity");
    }

    @Override
    public void onStop() {
        super.onStop();
        GA.sendView(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list_clear:
                clearBlockedList();
                break;
            case R.id.action_pro_version:
                gotoProVersion();
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

    private void gotoProVersion() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri
                    .parse("market://details?id=org.azki.smishing.pro"));
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri
                        .parse("https://play.google.com/store/apps/details?id=org.azki.smishing.pro"));
                startActivity(intent);
            } catch (Exception e2) {
                Toast.makeText(this, "Error : " + e, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class CustomAdapter extends ArrayAdapter<RowData> {
        public CustomAdapter(Context context, int textViewResourceId,
                             List<RowData> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView,
                            final ViewGroup parent) {
            final RowData rowData = getItem(position);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item, null);
            }
            TextView dateTextView = (TextView) convertView
                    .findViewById(R.id.dateTextView);
            TextView bodyTextView = (TextView) convertView
                    .findViewById(R.id.bodyTextView);
            TextView senderTextView = (TextView) convertView
                    .findViewById(R.id.senderTextView);

            dateTextView.setText(rowData.when);
            bodyTextView.setText(rowData.body);
            senderTextView.setText(rowData.sender);
            return convertView;
        }
    }
}
