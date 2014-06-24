package uk.ac.kent.jb509.shopper.adapter;

import java.util.List;

import uk.ac.kent.jb509.shopper.Product;
import uk.ac.kent.jb509.shopper.R;
import uk.ac.kent.jb509.shopper.ShopperApp;
import uk.ac.kent.jb509.shopper.utils.AppConfig;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SuggestedProductsListAdapter extends ArrayAdapter<Product> {
	public LayoutInflater inflater;
	public final int resource;
	public Context ctx;

	public SuggestedProductsListAdapter(final Context ctx,
			final int resourceId, final List<Product> objects) {
		super(ctx, resourceId, objects);
		resource = resourceId;
		inflater = LayoutInflater.from(ctx);
	}

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {

		convertView = inflater.inflate(R.layout.adapter_product_row_item,
				parent, false);
		final Product product = getItem(position);

		Holder.productName = (TextView) convertView
				.findViewById(R.id.productName);
		Holder.productName.setText(product.getProductName());

		Holder.productDesc = (TextView) convertView
				.findViewById(R.id.productDesc);
		Holder.productDesc.setText(product.getDescription());

		Holder.productPrice = (TextView) convertView
				.findViewById(R.id.productPrice);
		Holder.productPrice.setText(AppConfig.currencySymbol
				+ AppConfig.toTwoDecimalPoints(AppConfig
						.getCurrencyConversion(product.getPrice())));

		Holder.productImage = (ImageView) convertView
				.findViewById(R.id.productImage);
		product.loadImage(Holder.productImage, 128, 128);
		return convertView;
	}

	public static class Holder {
		public static TextView productName = null;
		public static TextView productDesc = null;
		public static TextView productPrice = null;
		public static ImageView productImage = null;
	}

}
