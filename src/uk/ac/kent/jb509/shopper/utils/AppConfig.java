/** Shopper application
 * Copyright (C) <2013> Jonathan Baker, University of Kent
 * All rights reserved
 */

package uk.ac.kent.jb509.shopper.utils;

import java.text.DecimalFormat;

import uk.ac.kent.jb509.shopper.R;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Typeface;

public class AppConfig {

	public static String currencySymbol = "£";

	public static String SELECTED_CATEGORY = "uk.ac.kent.jb509.shopper.categoryId";

	public static String SELECTED_PRODUCT_ID = "uk.ac.kent.jb509.shopper.productId";
	public static String SELECTED_PRODUCT_NAME = "uk.ac.kent.jb509.shopper.productName";
	public static String SELECTED_PRODUCT_PRICE = "uk.ac.kent.jb509.shopper.productPrice";

	public final static String WEBSERVER_URL = "http://go55.student.eda.kent.ac.uk/shopper/";

	public static String WEBSERVER_GETDETAILS = WEBSERVER_URL
			+ "getProductById.php?id=%d";

	public static String WEBSERVER_GETIMAGE = WEBSERVER_URL
			+ "getImage.php?id=%d&w=%d&h=%d&str=%d";

	public static String WEBSERVER_GETLIST = WEBSERVER_URL
			+ "getProductsByCategory.php?cat=%d";
	public static String WEBSERVER_PUTORDER = WEBSERVER_URL
			+ "putOrder.php?ord=%s";

	/**
	 * 
	 * @param input
	 *            database value to be converted
	 * @return double value converted into relative currency
	 */
	public static double getCurrencyConversion(final double input) {
		if (currencySymbol == "$") {
			return input * 1.610;
		} else if (currencySymbol == "€") {
			return input * 1.190;
		} else { // must be £
			return input;
		}
	}

	public static void setCurrencySymbolEuro() {
		currencySymbol = "€";
	}

	public static void setCurrencySymbolGBP() {
		currencySymbol = "£";
	}

	public static void setCurrencySymbolUSD() {
		currencySymbol = "$";
	}

	/**
	 * Returns a double to a 2 decimal point string: 17.5 => 17.50
	 */
	public static String toTwoDecimalPoints(final double d) {
		return new DecimalFormat("#.00").format(d);
	}

	public static void transitionFragment(final FragmentManager fm,
			final Fragment fragment) {
		final FragmentTransaction ft = fm.beginTransaction();

		ft.setCustomAnimations(R.anim.enter, R.anim.exit);
		ft.replace(R.id.frame_container, fragment);
		// Start the animated transition.
		ft.addToBackStack("tag").commit();
	}
}
