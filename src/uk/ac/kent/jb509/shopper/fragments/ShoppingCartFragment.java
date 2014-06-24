package uk.ac.kent.jb509.shopper.fragments;

import uk.ac.kent.jb509.shopper.MainActivity;
import uk.ac.kent.jb509.shopper.Product;
import uk.ac.kent.jb509.shopper.R;
import uk.ac.kent.jb509.shopper.ShopperApp;
import uk.ac.kent.jb509.shopper.adapter.CheckoutListAdapter;
import uk.ac.kent.jb509.shopper.utils.AppConfig;
import uk.ac.kent.jb509.shopper.utils.AutoResizeTextView;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ShoppingCartFragment extends Fragment {

	private CheckoutListAdapter mAdapter;
	private ListView mListView;
	ShopperApp shopperApp = null;

	TextView total_text_view;
	View view;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_shopping_cart, container,
				false);

		super.onCreateView(inflater, container, savedInstanceState);
		super.onCreate(savedInstanceState);

		getActivity().getActionBar().show();
		if (((uk.ac.kent.jb509.shopper.MainActivity) getActivity()).mSearchView != null) {
			((uk.ac.kent.jb509.shopper.MainActivity) getActivity()).mSearchView
					.setVisibility(View.GONE);
		}

		shopperApp = (ShopperApp) getActivity().getApplication();

		getActivity().setTitle("Shopping Cart");

		mListView = (ListView) view.findViewById(R.id.list_view_catalog);
		mAdapter = new CheckoutListAdapter(getActivity(),
				R.layout.adapter_product_row_item, shopperApp.getCart(), true) {

			@Override
			public View getView(final int position, View convertView,
					final ViewGroup parent) {
				super.getView(position, convertView, parent);
				convertView = instance.inflater
						.inflate(instance.resource, null);
				final Product product = getItem(position);

				final TextView productName = (TextView) convertView
						.findViewById(R.id.productName);
				productName.setText(product.getProductName());

				final TextView productDesc = (TextView) convertView
						.findViewById(R.id.productDesc);
				productDesc.setText(product.getDescription());

				final TextView productPrice = (TextView) convertView
						.findViewById(R.id.productPrice);
				productPrice.setText(AppConfig.currencySymbol
						+ AppConfig.toTwoDecimalPoints(AppConfig
								.getCurrencyConversion(product.getPrice())));

				final ImageView productImage = (ImageView) convertView
						.findViewById(R.id.productImage);
				product.loadImage(productImage, 128, 128);

				final Button button = (Button) convertView
						.findViewById(R.id.delete_button);
				if (button != null) {
					button.setText("Remove");
					if (!instance.showButton) {
						button.setVisibility(View.INVISIBLE);
					} else {
						button.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(final View v) {
								// Perform action on click
								instance.remove(instance.getItem(position));
								notifyDataSetChanged();
								updateTotalField(); // update total textview
								updateCartCount();
							}
						});

					}
				}

				return convertView;
			}
		};
		mListView.setAdapter(mAdapter);
		total_text_view = (TextView) view.findViewById(R.id.total_textField);

		mListView.setSelector(android.R.color.transparent);

		final Button b = (Button) view.findViewById(R.id.proceed_to_checkout);
		b.setOnClickListener(new OnClickListener() {
			DialogInterface.OnClickListener dialogClickListener;

			@Override
			public void onClick(final View v) {
				if (shopperApp.getCartSize() == 0) {
					dialogClickListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int position) {
							switch (position) {
							case DialogInterface.BUTTON_POSITIVE:
								break;
							}
						}
					};
				} else {
					dialogClickListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int position) {
							switch (position) {
							case DialogInterface.BUTTON_POSITIVE:
								// Yes button clicked

								final Fragment fragment = new CheckoutSubmitFragment();
								if (fragment != null) {
									AppConfig.transitionFragment(
											getFragmentManager(), fragment);
								}
								break;

							case DialogInterface.BUTTON_NEGATIVE:
								// No button clicked
								break;
							}
						}
					};
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				if (shopperApp.getCartSize() != 0) {
					builder.setMessage(
							"Are you sure you want to place an order?")
							.setNegativeButton("No", dialogClickListener)
							.setPositiveButton("Yes", dialogClickListener)
							.show();
				} else {
					builder.setMessage("Error: There are no items in your cart")
							.setNegativeButton("OK", dialogClickListener)
							.show();
				}

			}
		});

		updateTotalField();
		return view;
	}

	public void updateCartCount() {

		final TextView tv = ((MainActivity) getActivity()).shoppingCartTextView;
		if (tv != null) {
			tv.setText(""
					+ ((ShopperApp) getActivity().getApplication())
							.getCartSize());
		}
	}

	public void updateTotalField() {
		if (shopperApp.getCartSize() == 0) { // set text to format £0.00
			total_text_view.setText("Total: " + AppConfig.currencySymbol
					+ "0.00");
		} else {
			total_text_view
					.setText("Total: " + shopperApp.getCartTotalString());
		}
		updateCartCount();
	}

}
