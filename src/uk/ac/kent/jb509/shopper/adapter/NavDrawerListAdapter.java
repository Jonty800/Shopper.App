package uk.ac.kent.jb509.shopper.adapter;

import java.util.ArrayList;

import uk.ac.kent.jb509.shopper.R;
import uk.ac.kent.jb509.shopper.utils.AppConfig;
import uk.ac.kent.jb509.shopper.utils.NavDrawerItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavDrawerListAdapter extends BaseAdapter {

	private final String[] catDescs = new String[] {
			"Return to the homepage where you can view the available categories",
			"View the books we have for sale",
			"Browse the films we have, perhaps make an order?",
			"What music do we sell? Click here to find out!",
			"Looking for games? You've come to the right place ",
			"View the checkout and purchase your order", };
	private final Context context;

	private final ArrayList<NavDrawerItem> navDrawerItems;

	public NavDrawerListAdapter(final Context context,
			final ArrayList<NavDrawerItem> navDrawerItems) {
		this.context = context;
		this.navDrawerItems = navDrawerItems;
	}

	@Override
	public int getCount() {
		return navDrawerItems.size();
	}

	@Override
	public Object getItem(final int position) {
		return navDrawerItems.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {
		if (convertView == null) {
			final LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.adapter_menu_row_item,
					null);
		}

		TextView textView = (TextView) convertView
				.findViewById(R.id.categoryDesc);
		textView.setText(catDescs[position]);
		final ImageView imageView = (ImageView) convertView
				.findViewById(R.id.categoryImage);
		imageView.setImageResource(navDrawerItems.get(position).getIcon());

		textView = (TextView) convertView.findViewById(R.id.categoryName);
		textView.setText(navDrawerItems.get(position).getTitle());
		// displaying count
		// check whether it set visible or not
		// if(navDrawerItems.get(position).getCounterVisibility()){
		// textView.setText(navDrawerItems.get(position).getCount());
		// }else{
		// hide the counter view
		// textView.setVisibility(View.GONE);
		// }

		return convertView;
	}

}
