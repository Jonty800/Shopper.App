/** Shopper application
 * Copyright (C) <2013> Jonathan Baker, University of Kent
 * All rights reserved
 */

package uk.ac.kent.jb509.shopper.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import uk.ac.kent.jb509.shopper.MainActivity;
import uk.ac.kent.jb509.shopper.Product;
import uk.ac.kent.jb509.shopper.R;
import uk.ac.kent.jb509.shopper.utils.AppConfig;
import uk.ac.kent.jb509.shopper.utils.ListViewLoader;
import uk.ac.kent.jb509.shopper.utils.SliderCache;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

public class ProductListFragment extends BaseFragment implements
		SearchView.OnQueryTextListener, SearchView.OnCloseListener,
		Button.OnClickListener {

	public class ProductFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			// TODO Auto-generated method stub

			List<Product> data = new ArrayList<Product>();
			try {
				data = loader.get();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} catch (final ExecutionException e) {
				e.printStackTrace();
			}

			constraint = constraint.toString().toLowerCase();

			final FilterResults newFilterResults = new FilterResults();

			if (constraint != null && constraint.length() > 0) {

				final List<Product> temp = new ArrayList<Product>();

				for (int i = 0; i < data.size(); i++) {
					if (data.get(i).getProductName().toLowerCase()
							.contains(constraint)) {
						temp.add(data.get(i));
					}
				}

				newFilterResults.count = temp.size();
				newFilterResults.values = temp;
			} else {
				newFilterResults.count = data.size();
				newFilterResults.values = data;
			}

			return newFilterResults;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(final CharSequence constraint,
				final FilterResults results) {

			ArrayList<Product> resultData = new ArrayList<Product>();

			resultData = (ArrayList<Product>) results.values;

			final ProductListAdapter adapter = new ProductListAdapter(context);
			adapter.itemList = resultData;
			listView.setAdapter(adapter);

			adapter.notifyDataSetChanged();
		}

	}

	public class ProductListAdapter extends BaseAdapter implements Filterable {

		private final Context context;
		private List<Product> itemList;

		public ProductListAdapter(final Context c) {
			context = c;
		}

		@Override
		public int getCount() {
			if (itemList == null) {
				return 0;
			} else {
				return itemList.size();
			}
		}

		@Override
		public Filter getFilter() {
			Filter filter = null;

			if (filter == null) {
				filter = new ProductFilter();
			}
			return filter;
		}

		@Override
		public Object getItem(final int position) {
			if (itemList == null) {
				return null;
			} else {
				return itemList.get(position);
			}
		}

		@Override
		public long getItemId(final int position) {
			if (itemList == null) {
				return 0;
			} else {
				return itemList.get(position).hashCode();
			}
		}

		public List<Product> getItemList() {
			return itemList;
		}

		@Override
		public View getView(final int position, View convertView,
				final ViewGroup parent) {

			final LayoutInflater inflater = ((Activity) context)
					.getLayoutInflater();
			convertView = inflater.inflate(R.layout.adapter_product_list,
					parent, false);

			final Product p = itemList.get(position);

			// set title
			TextView textView = (TextView) convertView
					.findViewById(R.id.product_title);
			textView.setText(p.getProductName());

			// set price
			textView = (TextView) convertView.findViewById(R.id.product_info);
			textView.setText("Price: "
					+ AppConfig.currencySymbol
					+ AppConfig.toTwoDecimalPoints(AppConfig
							.getCurrencyConversion(p.getPrice())));

			// set image
			final ImageView imageView = (ImageView) convertView
					.findViewById(R.id.product_image);
			p.loadImage(imageView, 0, 256);
			// convertView.setBackgroundResource(R.drawable.list_item_divider);
			return convertView;
		}

		public void setItemList(final List<Product> itemList) {
			this.itemList = itemList;
		}
	}

	public static final int[] productIcons = { 0, R.drawable.books,
			R.drawable.films, R.drawable.music, R.drawable.games };

	Activity act;

	private ProductListAdapter adapter;

	public int categoryId;

	Context context;

	ListView listView;
	private ListViewLoader loader;

	private ProgressDialog progDialog;

	View view;

	@Override
	public void onClick(final View view) {
		if (view == ((MainActivity) getActivity()).mCloseButton) {
			((MainActivity) getActivity()).mSearchView.setIconified(true);
		} else if (view == ((MainActivity) getActivity()).mOpenButton) {
			((MainActivity) getActivity()).mSearchView.setIconified(false);
		}
	}

	@Override
	public boolean onClose() {
		return false;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_product_list, container,
				false);
		SliderCache.view = view;
		super.onCreateView(inflater, container, savedInstanceState);
		context = this.getActivity();
		super.onCreate(savedInstanceState);

		getActivity().getActionBar().show();
		/**
		 * Load search menu item
		 */
		final View instance = view;
		view = inflater.inflate(R.layout.adapter_searchview_actionbar,
				container, false);

		final MenuInflater inflater1 = ((MainActivity) getActivity())
				.getMenuInflater();
		if (inflater1 != null) {
			if (((MainActivity) getActivity()).menu != null) {
				inflater1.inflate(R.menu.searchview_in_menu,
						((MainActivity) getActivity()).menu);
				((MainActivity) getActivity()).mSearchView = (SearchView) ((MainActivity) getActivity()).menu
						.findItem(R.id.action_search).getActionView();
				setupSearchView();

				((MainActivity) getActivity()).mOpenButton = (Button) view
						.findViewById(R.id.open_button);
				((MainActivity) getActivity()).mCloseButton = (Button) view
						.findViewById(R.id.close_button);
				((MainActivity) getActivity()).mOpenButton
						.setOnClickListener(this);
				((MainActivity) getActivity()).mCloseButton
						.setOnClickListener(this);
			}
		}
		/* -=------------- */

		view = instance;

		// setContentView(R.layout.activity_product_list);

		categoryId = Integer.parseInt(AppConfig.SELECTED_CATEGORY);

		adapter = new ProductListAdapter(getActivity());
		listView = (ListView) view.findViewById(android.R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> arg0, final View arg1,
					final int arg2, final long arg3) {
				final Product p = (Product) adapter.getItem(arg2);
				AppConfig.SELECTED_CATEGORY = Integer.toString(p
						.getCategoryId());
				AppConfig.SELECTED_PRODUCT_ID = Integer.toString(p
						.getProductId());
				AppConfig.SELECTED_PRODUCT_PRICE = Double.toString(p.getPrice());
				AppConfig.SELECTED_PRODUCT_NAME = p.getProductName();
				final Fragment fragment = new ProductFragment();

				AppConfig.transitionFragment(getFragmentManager(), fragment);

			}

		});
		listView.setAdapter(adapter);
		final String category = getResources().getStringArray(
				R.array.categories)[categoryId - 1];

		if (category != null) {
			getActivity().setTitle(category);
		}

		loader = new ListViewLoader(adapter, categoryId) {
			@Override
			protected void onPostExecute(final List<Product> result) {
				super.onPostExecute(result);
				if (progDialog.isShowing()) {
					progDialog.dismiss();
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progDialog = new ProgressDialog(context, R.style.dialogTheme);

				progDialog.setMessage("Please wait...");
				progDialog.setIndeterminate(false);
				progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progDialog.setCancelable(true);
				progDialog.show();
			}
		};
		loader.execute(String.format(AppConfig.WEBSERVER_GETLIST, categoryId));
		return view;
	}

	@Override
	public boolean onQueryTextChange(final String newText) {
		adapter.getFilter().filter(newText);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(final String query) {
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		super.updateCartCount();
	}

	private void setupSearchView() {

		((MainActivity) getActivity()).mSearchView.setIconifiedByDefault(true);

		((MainActivity) getActivity()).mSearchView.setOnQueryTextListener(this);
		((MainActivity) getActivity()).mSearchView.setOnCloseListener(this);
		int searchImgId = getResources().getIdentifier(
				"android:id/search_button", null, null);
		ImageView v = (ImageView) ((MainActivity) getActivity()).mSearchView
				.findViewById(searchImgId);
		v.setImageResource(R.drawable.ic_search);
		v.setScaleY(0.75f);
		v.setScaleX(0.75f);
		int id = ((MainActivity) getActivity()).mSearchView.getContext()
				.getResources()
				.getIdentifier("android:id/search_src_text", null, null);
		TextView textView = (TextView) ((MainActivity) getActivity()).mSearchView
				.findViewById(id);

		textView.setTextColor(getResources().getColor(R.color.text));
	}
}
