package pb.gtd.ui;

import java.util.ArrayList;
import java.util.Date;

import pb.gtd.Constants;
import pb.gtd.R;
import pb.gtd.Tag;
import pb.gtd.service.GTDService;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ItemListActivity extends Activity implements OnItemClickListener,
		OnEditorActionListener {

	public static final String ARG_TAG = "tag";

	private boolean syncing;

	private String tag;
	private ArrayAdapter<ItemListItem> itemList;

	BroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tag = getIntent().getStringExtra(ARG_TAG);

		itemList = new ItemListAdapter(this,
				android.R.layout.simple_list_item_1,
				new ArrayList<ItemListItem>());

		View root = getLayoutInflater().inflate(R.layout.activity_item_list,
				null);
		if (tag.equals("tickler")) {
			// adding to tickler directly doesn't make sense
			((ViewGroup) root).removeViewAt(0);
		}

		setContentView(root);
		setupActionBar();

		ListView v = (ListView) findViewById(R.id.item_list_view);
		v.setOnItemClickListener(this);
		v.setAdapter(itemList);

		if (!tag.equals("tickler")) {
			((EditText) findViewById(R.id.stuff_input_item))
					.setOnEditorActionListener(this);
		}

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				setSyncState(false);
				if (intent.getAction().equals(Constants.ACTION_SYNCDONE)) {
					loadItems();
				} else if (intent.getAction()
						.equals(Constants.ACTION_SYNCERROR)) {
					Toast toast = Toast.makeText(getApplicationContext(),
							intent.getStringExtra("msg"), Toast.LENGTH_SHORT);
					toast.show();

				}
			}
		};
		IntentFilter ifl = new IntentFilter();
		ifl.addAction(Constants.ACTION_SYNCDONE);
		ifl.addAction(Constants.ACTION_SYNCERROR);
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, ifl);

		loadItems();
	}

	public void loadItems() {
		String today = DateFormat.format("yyyy-MM-dd", new Date()).toString();

		itemList.setNotifyOnChange(false);
		itemList.clear();
		itemList.addAll(GTDService.instance.getItems(Tag.displayTag(tag, today)));
		itemList.notifyDataSetChanged();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(tag);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (tag.equals("inbox")) {
			getMenuInflater().inflate(R.menu.item_list_inbox, menu);
		} else {
			getMenuInflater().inflate(R.menu.item_list, menu);
		}
		return true;
	}

	protected synchronized boolean setSyncState(boolean sync) {
		if (sync && syncing) {
			return false;
		} else {
			if (sync) {
				GTDService.instance.requestSync();
				// start animation
			} else {
				// TODO stop animation
			}

			syncing = sync;
			return true;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final CharSequence[] items = {
				tag.equals("inbox") ? "Process" : "To inbox", "Delete", "Edit" };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Action");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			Context ctx;
			int num;

			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					if (!tag.equals("inbox")) {
						GTDService.instance.deleteTag(num);
						Toast.makeText(getApplicationContext(),
								"Moved to inbox.", Toast.LENGTH_SHORT).show();
						requestDelayedSync();
					} else {
						Intent i = new Intent(ctx, ProcessActivity.class);
						i.putExtra("num", num);
						startActivity(i);

					}
				} else if (item == 1) {
					GTDService.instance.actionDeleteItem(num);
					Toast.makeText(getApplicationContext(), "Deleted.",
							Toast.LENGTH_SHORT).show();
					requestDelayedSync();
				} else if (item == 2) {
					AlertDialog.Builder alert = new AlertDialog.Builder(ctx);

					alert.setTitle("Edit");
					final EditText input = new EditText(ctx);
					input.setText(GTDService.instance.getItemTitle(num));
					alert.setView(input);

					alert.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									GTDService.instance.actionSetTitle(num,
											input.getText().toString());
									requestDelayedSync();
								}
							});

					alert.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							});

					alert.show();
				}
			}

			public DialogInterface.OnClickListener init(int n, Context c) {
				num = n;
				ctx = c;
				return this;
			}
		}.init(itemList.getItem(position).num, getActionBar()
				.getThemedContext()));
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// NavUtils.navigateUpFromSameTask(this);
			Intent intent = new Intent(this, TagListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			return true;

		case R.id.action_sync:
			setSyncState(true);
			return true;

		case R.id.action_process:
			if (GTDService.instance.getItems("inbox").size() > 0) {
				startActivity(new Intent(this, ProcessActivity.class));
			}
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		TextView tv = (TextView) findViewById(R.id.stuff_input_item);
		String title = tv.getText().toString();

		if (title.length() > 0) {
			GTDService.instance.actionAddItem(title, tag);

			tv.setText("");
			tv.requestFocus();

			requestDelayedSync();
		}

		return true;
	}

	private void requestDelayedSync() {
		GTDService.instance.requestSync(Constants.SYNC_DELAY);
	}
}
