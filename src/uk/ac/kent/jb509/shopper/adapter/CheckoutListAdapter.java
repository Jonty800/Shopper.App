package uk.ac.kent.jb509.shopper.adapter;

import java.util.List;

import uk.ac.kent.jb509.shopper.Product;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class CheckoutListAdapter extends ArrayAdapter<Product> {
	public LayoutInflater inflater;
	public CheckoutListAdapter instance;
	public final int resource;
	public boolean showButton;

	public CheckoutListAdapter(final Context ctx, final int resourceId,
			final List<Product> objects, final boolean showButton) {
		super(ctx, resourceId, objects);
		resource = resourceId;
		inflater = LayoutInflater.from(ctx);
		this.showButton = showButton;
	}

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {
		convertView = inflater.inflate(resource, null);
		instance = this;
		return convertView;
	}
}
