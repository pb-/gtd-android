package pb.gtd.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import pb.gtd.Constants;
import pb.gtd.R;
import pb.gtd.Tag;
import pb.gtd.service.GTDService;

public class ProcessActivity extends Activity implements
        ButtonListAdapter.OnClickListener, OnClickListener,
        OnEditorActionListener {

    private ButtonListAdapter<Tag> tags;
    private ArrayList<ItemListItem> queue;
    private Calendar month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tags = new ButtonListAdapter<Tag>(this);
        refreshTagList();

        queue = GTDService.instance.getItems("inbox");
        Intent in = getIntent();
        if (in != null && in.hasExtra("num")) {
            int num = in.getIntExtra("num", -1);
            for (int i = queue.size() - 1; i >= 0; i--) {
                if (queue.get(i).num != num) {
                    queue.remove(i);
                }
            }
        }

        setContentView(R.layout.activity_process);
        setupActionBar();

        refreshTitle();

        GridView gv = (GridView) findViewById(R.id.tag_list_grid);
        gv.setAdapter(tags);
        tags.setListener(this);

        ((EditText) findViewById(R.id.add_tag_input))
                .setOnEditorActionListener(this);

        ((Button) findViewById(R.id.month_prev)).setOnClickListener(this);
        ((Button) findViewById(R.id.month_next)).setOnClickListener(this);

        month = new GregorianCalendar();
        refreshCalendar();

        processTopItem();
    }

    private void processTopItem() {
        ItemListItem it = queue.get(0);

        ((TextView) findViewById(R.id.item_title)).setText("Processing "
                + it.title);
    }

    private void nextItem() {
        queue.remove(0);

        if (queue.size() > 0) {
            refreshTagList();
            refreshTitle();

            processTopItem();
        } else {
            GTDService.instance.requestSync(Constants.SYNC_DELAY);
            finish();
        }
    }

    private void refreshTitle() {
        if (queue.size() == 1) {
            setTitle("Processing one item");
        } else {
            setTitle("Processing " + queue.size() + " items");
        }
    }

    private void refreshTagList() {
        tags.setNotifyOnChange(false);
        tags.clear();
        ArrayList<Tag> ts = GTDService.instance.getTags();
        for (int i = ts.size() - 1; i >= 0; i--) {
            if (ts.get(i).title.equals("inbox")
                    || ts.get(i).title.equals("tickler")) {
                ts.remove(i);
            } else {
                ts.get(i).count = 0;
            }
        }
        tags.addAll(ts);
        tags.notifyDataSetChanged();
    }

    private void refreshCalendar() {
        Calendar cal = (Calendar) month.clone();

        cal.set(Calendar.DAY_OF_MONTH, 1);
        Calendar nextMonth = (Calendar) cal.clone();
        nextMonth.add(Calendar.MONTH, 1);
        // final Calendar today = new GregorianCalendar();

        TextView tv = (TextView) findViewById(R.id.month_name);
        tv.setText(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US)
                + " " + cal.get(Calendar.YEAR));

        // start on monday, possibly in the previous month
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        CalendarAdapter days = new CalendarAdapter(this);
        days.setListener(new ButtonListAdapter.OnClickListener() {
            @Override
            public void onClick(int day) {
                int mon = month.get(Calendar.MONTH) + 1;
                String d = day < 10 ? "0" + day : "" + day;
                String m = mon < 10 ? "0" + mon : "" + mon;
                String tag = "$" + month.get(Calendar.YEAR) + "-" + m + "-" + d;

                GTDService.instance.actionTagItem(queue.get(0).num, tag);
                nextItem();
            }
        });

        // render one month
        while (cal.compareTo(nextMonth) < 0) {
            for (int i = 0; i < 7; i++) {
                if (cal.get(Calendar.MONTH) != month.get(Calendar.MONTH)) {
                    // not in this month
                    days.add("");
                } else {
                    days.add(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
                }

                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        GridView gv = (GridView) findViewById(R.id.calendar_grid);
        gv.setAdapter(days);
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.process, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, TagListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(int position) {
        GTDService.instance.actionTagItem(queue.get(0).num,
                tags.getItem(position).title);
        nextItem();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.month_prev:
                month.add(Calendar.MONTH, -1);
                refreshCalendar();
                break;
            case R.id.month_next:
                month.add(Calendar.MONTH, +1);
                refreshCalendar();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String tag = ((TextView) findViewById(R.id.add_tag_input)).getText()
                .toString();

        if (tag.length() > 0) {
            GTDService.instance.actionTagItem(queue.get(0).num, tag);
            nextItem();
        }

        return true;
    }
}
