/** Shopper application
 * Copyright (C) <2013> Jonathan Baker, University of Kent
 * All rights reserved
 */
package uk.ac.kent.jb509.shopper;

import java.util.ArrayList;
import java.util.List;

import uk.ac.kent.jb509.shopper.fragments.BaseFragment;
import uk.ac.kent.jb509.shopper.utils.AppConfig;
import uk.ac.kent.jb509.shopper.utils.SliderCache;
import android.app.Application;

public class ShopperApp extends Application {

	public static BaseFragment fragmentInstance;

	public static BaseFragment getFragmentInstance() {
		return fragmentInstance;
	}

	public static void setFragmentInstance(final BaseFragment fragmentInstance) {
		ShopperApp.fragmentInstance = fragmentInstance;
	}

	private final List<Product> cart = new ArrayList<Product>();

	public boolean containsProduct(final int id) {
		for (final Product p : getCart()) {
			if (p.getProductId() == id) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Finds a product in the cart array and returns it. Returns null of no
	 * result was found
	 */
	public Product findInCart(final int id) {
		for (int i = 0; i < cart.size(); i++) {
			final Product p = cart.get(i);
			if (p.getProductId() == id) {
				return p;
			}
		}
		return null;
	}

	public List<Product> getCart() {
		return cart;
	}

	public int getCartSize() {
		return getCart().size();
	}

	/**
	 * Iterates through the cart array and adds together the prices
	 */
	public double getCartTotal() {
		double total = 0;
		for (final Product p : getCart()) {
			total += p.getPrice();
		}
		return AppConfig.getCurrencyConversion(total);
	}

	public String getCartTotalString() {
		final double total = getCartTotal();
		if (getCartSize() == 0) {
			return AppConfig.currencySymbol + "0.00";
		}
		return AppConfig.currencySymbol + AppConfig.toTwoDecimalPoints(total);
	}

	public String getOrderString() {
		String order = new String();
		for (final Product p : getCart()) {
			order += p.getProductId() + ",";
		}
		return order;
	}

	public void removeItemFromBasket(final int id) {
		cart.remove(id);
		if (getCartSize() == 0 && SliderCache.layout != null) {
			// act.removeSlider();
			getFragmentInstance().removeSlider();
		}
		getFragmentInstance().updateCartCount();
	}

	public void updateSliderProducts(final boolean showButton) {
		getFragmentInstance().setupSlider(showButton);
		getFragmentInstance().updateCartCount();
	}
}
