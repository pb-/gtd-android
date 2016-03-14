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
        return super.getView(position, convertView, parent);
    }

}
