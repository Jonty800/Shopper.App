package uk.ac.kent.jb509.shopper.fragments;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;

import uk.ac.kent.jb509.shopper.MainActivity;
import uk.ac.kent.jb509.shopper.Product;
import uk.ac.kent.jb509.shopper.R;
import uk.ac.kent.jb509.shopper.ShopperApp;
import uk.ac.kent.jb509.shopper.adapter.NavDrawerListAdapter;
import uk.ac.kent.jb509.shopper.adapter.SuggestedProductsListAdapter;
import uk.ac.kent.jb509.shopper.utils.AppConfig;
import uk.ac.kent.jb509.shopper.utils.AutoResizeTextView;
import uk.ac.kent.jb509.shopper.utils.ListViewLoader;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CheckoutSubmitFragment extends Fragment {
	List<Product> cartCache = new ArrayList<Product>();
	Menu menu;

	ProgressDialog progDialog;
	View view;

	public void checkoutOrder() {
		final String url = String.format(AppConfig.WEBSERVER_PUTORDER,
				((ShopperApp) getActivity().getApplication()).getOrderString());

		int id = new Random().nextInt(4);
		if (id == 0)
			id = 1;
		ListViewLoader lvl = new ListViewLoader(null, id) {

			@Override
			protected void onPostExecute(List<Product> result) {
				List<Product> products = pickatRandom(result, 4);
				final ListView listView = (ListView) view
						.findViewById(android.R.id.list);

				listView.setAdapter(new SuggestedProductsListAdapter(
						getActivity(), R.layout.adapter_product_row_item,
						products) {

					@Override
					public View getView(final int position, View convertView,
							ViewGroup parent) {
						// TODO Auto-generated method stub
						convertView = super.getView(position, convertView,
								parent);
						final Button button = (Button) convertView
								.findViewById(R.id.delete_button);
						if (button != null) {
							button.setText("Add");
							button.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(final View v) {
									// Perform action on click
									((ShopperApp) getActivity()
											.getApplication()).getCart().add(
											getItem(position));
									remove(getItem(position));
									notifyDataSetChanged();
									updateCartCount();
								}
							});

						}
						return convertView;
					}

				});
			}
		};
		lvl.execute(String.format(AppConfig.WEBSERVER_GETLIST, id));

		final AsyncTask<String, Void, JSONObject> task = new AsyncTask<String, Void, JSONObject>() {
			@Override
			protected JSONObject doInBackground(final String... params) {
				JSONObject obj = null;

				try {
					final URL u = new URL(params[0]);
					// open connection
					final HttpURLConnection conn = (HttpURLConnection) u
							.openConnection();
					conn.connect();
					if (conn.getResponseCode() != 200) {
						return null;
					}
					// if all is ok
					final InputStream is = conn.getInputStream();

					// read the stream
					final int bufferSize = 1024;
					final byte[] buffer = new byte[bufferSize];
					final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

					while (is.read(buffer) != -1) {
						byteStream.write(buffer);
					}

					// Extract the JSON
					final String jsonStr = new String(byteStream.toByteArray());
					obj = new JSONObject(jsonStr);
				} catch (final Throwable t) {
					t.printStackTrace();
				}
				return obj;
			}

			@Override
			protected void onPostExecute(final JSONObject result) {
				final TextView aTextView = (TextView) view
						.findViewById(R.id.result_field);
				if (result != null) {
					aTextView.setText("Your order was successful!");
					((ShopperApp) getActivity().getApplication()).getCart()
							.clear();
					updateCartCount();
					// empty the cart
				} else {
					aTextView.setText("An error occured");
				}
				if (progDialog.isShowing()) {
					progDialog.cancel();
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progDialog = new ProgressDialog(view.getContext(),
						R.style.dialogTheme);
				progDialog.setMessage("Submitting your order\nPlease wait...");
				progDialog.setIndeterminate(false);
				progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progDialog.setCancelable(false);
				progDialog.show();
			}
		};
		task.execute(url);
	}

	public static List<Product> pickatRandom(List<Product> lst, int n) {
		List<Product> copy = new LinkedList<Product>(lst);
		Collections.shuffle(copy);
		return copy.subList(0, n);
	}

	public void updateCartCount() {

		final TextView tv = ((MainActivity) getActivity()).shoppingCartTextView;
		if (tv != null) {
			tv.setText(""
					+ ((ShopperApp) getActivity().getApplication())
							.getCartSize());
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_checkout_submit, container,
				false);
		super.onCreateView(inflater, container, savedInstanceState);
		super.onCreate(savedInstanceState);
		getActivity().getActionBar().show();
		if (((uk.ac.kent.jb509.shopper.MainActivity) getActivity()).mSearchView != null) {
			((uk.ac.kent.jb509.shopper.MainActivity) getActivity()).mSearchView
					.setVisibility(View.GONE);
		}
		final ShopperApp shopperApp = (ShopperApp) getActivity()
				.getApplication();
		checkoutOrder();
		updateCartCount();
		return view;
	}

}
