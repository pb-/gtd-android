package pb.gtd.ui;

import java.util.ArrayList;

import pb.gtd.Constants;
import pb.gtd.R;
import pb.gtd.Tag;
import pb.gtd.Util;
import pb.gtd.service.GTDService;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class TagListActivity extends Activity implements OnItemClickListener,
		OnEditorActionListener {

	private ArrayAdapter<Tag> tagList;
	private BroadcastReceiver receiver;
	private boolean syncing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("main", "oncreate");

		setContentView(R.layout.activity_tag_list);

		tagList = new ArrayAdapter<Tag>(this,
				android.R.layout.simple_list_item_1, new ArrayList<Tag>());

		ListView v = (ListView) findViewById(R.id.tag_list_view);
		v.setAdapter(tagList);
		v.setOnItemClickListener(this);

		((EditText) findViewById(R.id.stuff_input))
				.setOnEditorActionListener(this);

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				try {
					setSyncState(false);
					if (intent.getAction().equals(Constants.ACTION_SYNCDONE)) {
						loadTagList();
					} else if (intent.getAction().equals(
							Constants.ACTION_SYNCERROR)) {
						Toast toast = Toast.makeText(getApplicationContext(),
								intent.getStringExtra("msg"),
								Toast.LENGTH_SHORT);
						toast.show();

					}
				} catch (RuntimeException re) {
					Log.d("ui", "ooops");
					Util.writeCrashReport(re);
				}
			}
		};
		IntentFilter ifl = new IntentFilter();
		ifl.addAction(Constants.ACTION_SYNCDONE);
		ifl.addAction(Constants.ACTION_SYNCERROR);
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, ifl);

		Intent intent = new Intent(this, GTDService.class);
		if (startService(intent) == null) {
			Log.w("service", "failed to find service");
		}

		if (GTDService.instance != null) {
			// loadTagList();
			if (savedInstanceState == null) {
				Log.i("ui", "requesting sync");
				setSyncState(true);
			}
		}
	}

	protected void onResume() {
		super.onResume();

		Log.d("ui", "onResume");

		if (GTDService.instance != null) {
			loadTagList();
		}
	}

	public void loadTagList() {
		Log.d("ui", "loading tag list");
		tagList.setNotifyOnChange(false);
		tagList.clear();
		tagList.addAll(GTDService.instance.getTags());
		tagList.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tag_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_sync:
			Log.d("ui", "clicked on sync");
			setSyncState(true);
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.action_export:
		case R.id.action_export_full:
			String filename = GTDService.instance
					.export(item.getItemId() == R.id.action_export_full);
			String text;
			if (filename != null) {
				text = "Export: " + filename;
			} else {
				text = "Export failed :-(";
			}
			Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
					.show();
			return true;
		case R.id.action_process:
			if (GTDService.instance.getItems("inbox").size() > 0) {
				startActivity(new Intent(this, ProcessActivity.class));
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	protected synchronized boolean setSyncState(boolean sync) {
		if (sync && syncing) {
			Log.d("ui", "not syncing because already running");
			return false;
		} else {
			if (sync) {
				GTDService.instance.requestSync();
			}

			Log.d("ui", "sync state change: " + syncing + " --> " + sync);
			syncing = sync;
			// invalidateOptionsMenu();
			return true;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		Intent i = new Intent(this, ItemListActivity.class);
		i.putExtra(ItemListActivity.ARG_TAG, tagList.getItem(position).title);
		startActivity(i);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		TextView tv = (TextView) findViewById(R.id.stuff_input);
		String title = tv.getText().toString();

		if (title.length() > 0) {
			GTDService.instance.actionAddItem(title, "inbox");

			tv.setText("");
			tv.requestFocus();

			GTDService.instance.requestSync(Constants.SYNC_DELAY);
		}

		return true;
	}
}
