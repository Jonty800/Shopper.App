/** Shopper application
 * Copyright (C) <2013> Jonathan Baker, University of Kent
 * All rights reserved
 */

package uk.ac.kent.jb509.shopper.fragments;

import uk.ac.kent.jb509.shopper.MainActivity;
import uk.ac.kent.jb509.shopper.Product;
import uk.ac.kent.jb509.shopper.R;
import uk.ac.kent.jb509.shopper.ShopperApp;
import uk.ac.kent.jb509.shopper.adapter.CheckoutListAdapter;
import uk.ac.kent.jb509.shopper.utils.AppConfig;
import uk.ac.kent.jb509.shopper.utils.SliderCache;
import uk.ac.kent.jb509.shopper.utils.SlidingUpPanelLayout;
import uk.ac.kent.jb509.shopper.utils.SlidingUpPanelLayout.PanelSlideListener;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class BaseFragment extends Fragment {
	View view;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		view = SliderCache.view;
		setupSlider(false);
		ShopperApp.setFragmentInstance(this);
		return view;
	}

	public void removeSlider() {
		((LinearLayout) view.findViewById(R.id.sliding_clickable))
				.setVisibility(View.GONE);
		SliderCache.layout.setPanelSlideListener(null);
		SliderCache.layout.enabled = false;
	}

	public void setupSlider(final boolean showButton) {
		SliderCache.layout = (SlidingUpPanelLayout) view
				.findViewById(R.id.slider_panel);
		if (((ShopperApp) getActivity().getApplication()).getCartSize() == 0) {
			removeSlider();
			return;
		}
		SliderCache.layout.setShadowDrawable(getResources().getDrawable(
				R.drawable.above_shadow));

		TextView textView = (TextView) view
				.findViewById(R.id.num_items_in_basket);
		final int products = ((ShopperApp) getActivity().getApplication())
				.getCartSize();
		final String prodWord = products == 1 ? "product" : "products";
		final String areWord = products == 1 ? "is" : "are";
		textView.setText("There " + areWord + " " + products + " " + prodWord
				+ " in your cart");
		textView = (TextView) view.findViewById(R.id.num_items_desc);
		textView.setText("Click here to see items currently in your basket");

		textView = (TextView) view.findViewById(R.id.total_text_field);
		textView.setText("Total: "
				+ ((ShopperApp) getActivity().getApplication())
						.getCartTotalString());
		textView.setBackgroundColor(new ColorDrawable(Color.argb(208, 0, 0, 0))
				.getColor());
		showSlider(showButton);
	}

	public void showSlider(final boolean showButton) {
		((LinearLayout) view.findViewById(R.id.sliding_clickable))
				.setVisibility(View.VISIBLE);
		SliderCache.layout.setPanelSlideListener(new PanelSlideListener() {

			@Override
			public void onPanelAnchored(final View panel) {

			}

			@Override
			public void onPanelCollapsed(final View panel) {
				final TextView textView = (TextView) view
						.findViewById(R.id.num_items_desc);
				textView.setText("Click here view the items in your basket");
			}

			@Override
			public void onPanelExpanded(final View panel) {
				final TextView textView = (TextView) view
						.findViewById(R.id.num_items_desc);
				textView.setText("Click or drag to close this dialog");
			}

			@Override
			public void onPanelSlide(final View panel, final float slideOffset) {

			}
		});
		SliderCache.layout.enabled = true;
		updateSliderList(showButton);
	}

	public void updateCartCount() {
		if (((MainActivity) getActivity()).shoppingCartTextView != null) {

			final TextView tv = ((MainActivity) getActivity()).shoppingCartTextView;
			if (tv != null) {
				tv.setText(""
						+ ((ShopperApp) getActivity().getApplication())
								.getCartSize());
			}
		}

		if (((MainActivity) getActivity()).shoppingCartTextView != null) {
			String s = "";
			if (((ShopperApp) getActivity().getApplication()).getCartSize() > 0) {
				s += ((ShopperApp) getActivity().getApplication())
						.getCartSize();
			}
			((MainActivity) getActivity()).shoppingCartTextView.setText(s);
			((MainActivity) getActivity()).shoppingCartTextView
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(final View v) {
							final Fragment fragment = new ShoppingCartFragment();
							if (fragment != null) {
								AppConfig.transitionFragment(
										getFragmentManager(), fragment);
							}
						}
					});
		}
	}

	public void updateSliderList(final boolean showButton) {

		TextView textView = (TextView) view
				.findViewById(R.id.num_items_in_basket);
		final int products = ((ShopperApp) getActivity().getApplication())
				.getCartSize();
		final String inWord = products == 1 ? "is" : "are";
		final String prodWord = products == 1 ? "product" : "products";
		if (textView != null) {
			textView.setText("There " + inWord + " " + products + " "
					+ prodWord + " in your cart");
		}
		textView = (TextView) view.findViewById(R.id.num_items_desc);
		if (textView != null) {
			textView.setText("Click here to see items currently in your basket");
		}

		if (((ShopperApp) getActivity().getApplication()).getCartSize() == 0
				&& SliderCache.layout != null) {
			removeSlider();
		}

		SliderCache.sliderList = (ListView) view
				.findViewById(R.id.shopping_list);
		SliderCache.sliderList.setAdapter(new CheckoutListAdapter(
				getActivity(), R.layout.adapter_product_row_item,
				((ShopperApp) getActivity().getApplication()).getCart(),
				showButton) {

			@Override
			public View getView(final int position, View convertView,
					final ViewGroup parent) {
				super.getView(position, convertView, parent);
				convertView = instance.inflater
						.inflate(instance.resource, null);
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

				final ImageView productImage = (ImageView) convertView
						.findViewById(R.id.productImage);
				product.loadImage(productImage, 128, 128);

				final Button button = (Button) convertView
						.findViewById(R.id.delete_button);
				if (button != null) {
					button.setText("Remove");
					if (!instance.showButton) {
						button.setVisibility(View.INVISIBLE);
					}
				}
				return convertView;
			}
		});
		textView = (TextView) view.findViewById(R.id.total_text_field);
		textView.setText("Total: "
				+ ((ShopperApp) getActivity().getApplication())
						.getCartTotalString());
	}

	public static class Holder {
		public static TextView productName;
		public static TextView productDesc;
		public static TextView productPrice;
	}
}
