/** Shopper application
 * Copyright (C) <2013> Jonathan Baker, University of Kent
 * All rights reserved
 */

package uk.ac.kent.jb509.shopper.fragments;

import java.util.Random;

import uk.ac.kent.jb509.shopper.Product;
import uk.ac.kent.jb509.shopper.R;
import uk.ac.kent.jb509.shopper.ShopperApp;
import uk.ac.kent.jb509.shopper.utils.AppConfig;
import uk.ac.kent.jb509.shopper.utils.CustomToast;
import uk.ac.kent.jb509.shopper.utils.SliderCache;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ProductFragment extends BaseFragment {

	private Product product;

	View view;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_product, container, false);
		SliderCache.view = view;
		super.onCreateView(inflater, container, savedInstanceState);
		super.onCreate(savedInstanceState);
		if (((uk.ac.kent.jb509.shopper.MainActivity) getActivity()).mSearchView != null) {
			((uk.ac.kent.jb509.shopper.MainActivity) getActivity()).mSearchView
					.setVisibility(View.GONE);
		}
		getActivity().getActionBar().show();
		// collect extra info
		product = new Product();

		product.setCategoryId(Integer.parseInt(AppConfig.SELECTED_CATEGORY));
		product.setProductId(Integer.parseInt(AppConfig.SELECTED_PRODUCT_ID));
		product.setPrice(Double.parseDouble(AppConfig.SELECTED_PRODUCT_PRICE));
		product.setProductName(AppConfig.SELECTED_PRODUCT_NAME);

		TextView text = (TextView) view.findViewById(R.id.product_title);
		text.setText(product.getProductName());

		text = (TextView) view.findViewById(R.id.product_price);
		text.setText(AppConfig.currencySymbol
				+ AppConfig.toTwoDecimalPoints(AppConfig
						.getCurrencyConversion(product.getPrice())));

		text = (TextView) view.findViewById(R.id.product_description);
		text.setText(product.getDescription());

		final ImageView image = (ImageView) view
				.findViewById(R.id.product_cover);
		product.loadImage(image, 0, 480);

		// view = inflater.inflate(R.layout.sliding_panel_layout, container,
		// false);

		getActivity().setTitle(product.getProductName());

		final Button button = (Button) view.findViewById(R.id.add_to_cart);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final ShopperApp shopperApp = (ShopperApp) getActivity()
						.getApplication();
				String toastText = "Product was added to the basket!";

				// check via productId since Product object can be duplicated
				if (!shopperApp.containsProduct(product.getProductId())) {
					shopperApp.getCart().add(product);
					// notify the user
					new CustomToast(getActivity(), toastText, true).show();
					shopperApp.updateSliderProducts(false);

					Animation shake = AnimationUtils.loadAnimation(
							getActivity(), R.anim.bounce);
					getActivity().findViewById(R.id.show_cart).startAnimation(
							shake);
				} else {
					// notify the user that it is not in the basket
					toastText = "Product is already in the basket!";
					new CustomToast(getActivity(), toastText, false).show();
				}
			}
		});

		product.loadDetails(getActivity());

		return view;
	}
}
