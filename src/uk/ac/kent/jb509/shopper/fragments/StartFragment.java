/** Shopper application
 * Copyright (C) <2013> Jonathan Baker, University of Kent
 * All rights reserved
 */

package uk.ac.kent.jb509.shopper.fragments;

import java.util.List;

import uk.ac.kent.jb509.shopper.Product;
import uk.ac.kent.jb509.shopper.R;
import uk.ac.kent.jb509.shopper.adapter.CheckoutListAdapter;
import uk.ac.kent.jb509.shopper.utils.AppConfig;
import uk.ac.kent.jb509.shopper.utils.CustomToast;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class StartFragment extends Fragment {

	Activity act = getActivity();

	public String selectedCurrency = null;
	View view;

	public StartFragment() {

	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.fragment_start, container, false);
		super.onCreate(savedInstanceState);
		getActivity().getActionBar().hide();
		if (((uk.ac.kent.jb509.shopper.MainActivity) getActivity()).mSearchView != null) {
			((uk.ac.kent.jb509.shopper.MainActivity) getActivity()).mSearchView
					.setVisibility(View.GONE);
		}

		LinearLayout layout = (LinearLayout) view
				.findViewById(R.id.poundlayout);
		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectedCurrency = "British Pounds";
				checkAndLaunchIconFragment();
			}
		});
		layout = (LinearLayout) view.findViewById(R.id.eurolayout);
		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectedCurrency = "Euros";
				checkAndLaunchIconFragment();
			}
		});
		layout = (LinearLayout) view.findViewById(R.id.dollarlayout);
		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectedCurrency = "U.S Dollar";
				checkAndLaunchIconFragment();
			}
		});
		return view;

	}

	void checkAndLaunchIconFragment() {
		if (selectedCurrency.startsWith("British")) { // make £
			AppConfig.setCurrencySymbolGBP();
		} else if (selectedCurrency.startsWith("Euro")) { // Make EUR
			AppConfig.setCurrencySymbolEuro();
		} else if (selectedCurrency.startsWith("U.S")) { // Make $
			AppConfig.setCurrencySymbolUSD();
		} else {
			// shouldnt happen
			final String toastText = "No currency was selected";
			final CustomToast toast = new CustomToast(getActivity(), toastText,
					false);
			toast.show();
			return; // nope.mp4
		}
		final Fragment fragment = new CategoryFragment();
		if (fragment != null) {
			AppConfig.transitionFragment(getFragmentManager(), fragment);

		}
	}

	public void onItemSelected(final AdapterView<?> parent, final View view,
			final int pos, final long id) {
		// An item was selected. You can retrieve the selected item using
		// parent.getItemAtPosition(pos)
		selectedCurrency = (String) parent.getItemAtPosition(pos);
	}

	public void onNothingSelected(final AdapterView<?> parent) {
		// Another interface callback
	}

}