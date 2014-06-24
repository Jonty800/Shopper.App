/** Shopper application
 * Copyright (C) <2013> Jonathan Baker, University of Kent
 * All rights reserved
 */

package uk.ac.kent.jb509.shopper.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import uk.ac.kent.jb509.shopper.Product;
import uk.ac.kent.jb509.shopper.fragments.ProductListFragment.ProductListAdapter;
import android.os.AsyncTask;

public class ListViewLoader extends AsyncTask<String, Void, List<Product>> {

	protected ProductListAdapter adapter;
	protected int categoryId;

	public ListViewLoader(final ProductListAdapter a, final int catId) {
		adapter = a;
		categoryId = catId;
	}

	@Override
	protected List<Product> doInBackground(final String... params) {
		// Output is a list of products
		final List<Product> result = new ArrayList<Product>();

		try {
			final URL u = new URL(params[0]);

			// open connection
			final HttpURLConnection conn = (HttpURLConnection) u
					.openConnection();
			conn.connect();
			if (conn.getResponseCode() != 200) {
				return null;
			}
			final InputStream is = conn.getInputStream(); // if all is ok

			// read the stream
			final int bufferSize = 1024;
			final byte[] buffer = new byte[bufferSize];
			final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

			while (is.read(buffer) != -1) {
				byteStream.write(buffer);
			}

			// Extract the JSON
			final String jsonStr = new String(byteStream.toByteArray());
			final JSONArray arr = new JSONArray(jsonStr);
			// get each product from the json string list
			for (int i = 0; i < arr.length(); i++) {
				final Product prod = new Product();
				prod.setCategoryId(categoryId);
				prod.updateFromJSON(arr.getJSONObject(i));
				result.add(prod);
			}
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		return result;
	}

	@Override
	protected void onPostExecute(final List<Product> result) {
		adapter.setItemList(result);
		adapter.notifyDataSetChanged();
	}
}
