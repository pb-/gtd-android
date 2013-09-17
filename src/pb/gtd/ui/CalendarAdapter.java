package pb.gtd.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class CalendarAdapter extends ButtonListAdapter<String> {

	public CalendarAdapter(ProcessActivity processActivity) {
		super(processActivity);
		add("Mo");
		add("Tu");
		add("We");
		add("Th");
		add("Fr");
		add("Sa");
		add("Su");
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position < 7 || getItem(position).length() == 0) {
			Button b;
			b = (Button) super.getView(position, convertView, parent);
			b.setBackground(null);
			return b;
		} else {
			return super.getView(position, null, parent);
		}
	}

	@Override
	public void onInternalClick(int pos) {
		if (listener != null) {
			// please disregard what you've seen here and walk away.
			String s = getItem(pos);

			try {
				listener.onClick(Integer.parseInt(s));
			} catch (NumberFormatException nfe) {

			}
		}
	}

}
