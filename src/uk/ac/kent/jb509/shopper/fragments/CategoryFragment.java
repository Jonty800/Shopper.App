/** Shopper application
 * Copyright (C) <2013> Jonathan Baker, University of Kent
 * All rights reserved
 */

package uk.ac.kent.jb509.shopper.fragments;

import uk.ac.kent.jb509.shopper.R;
import uk.ac.kent.jb509.shopper.utils.AppConfig;
import uk.ac.kent.jb509.shopper.utils.SliderCache;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

// Originally named MainActivity.java
public class CategoryFragment extends BaseFragment {

	public class CategoryAdapter extends BaseAdapter {

		private final String[] categoryDescriptions = new String[] {
				"Books are a fantastic way for you to enter the world of adventrue. We have everything - from childrens books to adult novels. Click here to see our latest offers.",
				"Discover our huge range of high definition DVD titles and save on the hottest new releases. Expand your collection, upgrade your favourites and browse our fantastic range.",
				"Whatever your taste in music we have you covered, with thousands of CDs from all genres. Whether it’s listening to the latest in pop, getting an earful of rock.",
				"We have a selection the best video games online across all platforms, as well as some great accessories and consoles including the PS3, Xbox 360, Wii, and Wii U." };

		private final Integer[] categoryIcons = { R.drawable.books,
				R.drawable.films, R.drawable.music, R.drawable.games };

		private final String[] categoryTitles;

		private final Context context;

		private CategoryAdapter(final Context c) {
			context = c;
			final Resources res = getResources();
			categoryTitles = res.getStringArray(R.array.categories);
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public Object getItem(final int position) {
			return position;
		}

		@Override
		public long getItemId(final int position) {
			return position;
		}

		@Override
		public View getView(final int position, final View convertView,
				final ViewGroup parent) {
			View cell = convertView;

			if (cell == null) { // get from mobile.xml
				final LayoutInflater inflater = ((Activity) context)
						.getLayoutInflater();
				cell = inflater.inflate(R.layout.adapter_category, parent,
						false);
			}
			TextView textView = (TextView) cell
					.findViewById(R.id.category_title);
			textView.setText(categoryTitles[position]);
			textView.setTextColor(getResources().getColor(R.color.text));
			textView.setShadowLayer(1, 2, 2, R.color.black);

			textView = (TextView) cell.findViewById(R.id.descriptiontext);
			textView.setText(categoryDescriptions[position]);

			final ImageView imageView = (ImageView) cell
					.findViewById(R.id.category_image);
			imageView.setImageResource(categoryIcons[position]);

			cell.setBackgroundResource(R.drawable.list_item_divider);

			return cell;
		}
	}

	private final int[] categoryIds = { 1, 2, 3, 4 };

	Context context;

	public CategoryFragment() {
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_category,
				container, false);
		SliderCache.view = rootView;
		super.onCreateView(inflater, container, savedInstanceState);
		context = this.getActivity();
		super.onCreate(savedInstanceState);
		if (((uk.ac.kent.jb509.shopper.MainActivity) getActivity()).mSearchView != null) {
			((uk.ac.kent.jb509.shopper.MainActivity) getActivity()).mSearchView
					.setVisibility(View.GONE);
		}
		getActivity().getActionBar().show();

		final GridView gridView = (GridView) rootView
				.findViewById(R.id.categories_grid);
		gridView.setAdapter(new CategoryAdapter(this.getActivity()));
		gridView.setSelector(R.color.selector);

		// attach click event
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				final Fragment fragment = new ProductListFragment();
				if (fragment != null) {
					AppConfig.SELECTED_CATEGORY = Integer
							.toString(categoryIds[position]);
					AppConfig
							.transitionFragment(getFragmentManager(), fragment); // todo
				}
			}
		});

		this.getActivity().setTitle("Shopper.net");
		return rootView;
	}
}