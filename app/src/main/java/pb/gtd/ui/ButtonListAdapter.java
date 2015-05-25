package pb.gtd.ui;

import pb.gtd.R;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

public class ButtonListAdapter<T> extends ArrayAdapter<T> {

	public interface OnClickListener {
		public void onClick(int pos);
	}

	protected OnClickListener listener;

	public void setListener(OnClickListener l) {
		listener = l;
	}

	public ButtonListAdapter(ProcessActivity processActivity) {
		super(processActivity, R.layout.tag_button, R.layout.tag_button);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Button b;
		if (convertView == null) {
			b = new Button(getContext());
		} else {
			b = (Button) convertView;
		}
		b.setText(getItem(position).toString());
		b.setTag(position);
		b.setOnClickListener(new View.OnClickListener() {
			private ButtonListAdapter<T> adapter;

			@Override
			public void onClick(View v) {
				adapter.onInternalClick((Integer) v.getTag());
			}

			public View.OnClickListener setAdapter(ButtonListAdapter<T> a) {
				adapter = a;
				return this;
			}
		}.setAdapter(this));
		return b;
	}

	public void onInternalClick(int pos) {
		if (listener != null) {
			listener.onClick(pos);
		}
	}
}
