package pb.gtd.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class ItemListAdapter extends ArrayAdapter<ItemListItem> {

    public ItemListAdapter(Context context, int resource,
                           ArrayList<ItemListItem> objects) {
        super(context, resource, objects);
        // TODO Auto-generated constructor stub
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        /*
		 * if (convertView == null) { Log.d("ui", "convert is null"); }
		 * Log.d("ui", "VIEW: " + position + " " + v);
		 */
        return v;
    }

}
