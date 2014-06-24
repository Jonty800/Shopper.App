/** Shopper application
 * Copyright (C) <2013> Jonathan Baker, University of Kent
 * All rights reserved
 */

package uk.ac.kent.jb509.shopper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.kent.jb509.shopper.fragments.ProductListFragment;
import uk.ac.kent.jb509.shopper.utils.AppConfig;
import uk.ac.kent.jb509.shopper.utils.BitmapWorkerTask;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

public class Product {
	Activity activity;
	ImageView imgView;
	private double price;
	private int productId, categoryId;
	private String productName, description;

	ProgressDialog progDialog;

	public boolean selected = false;

	public int getCategoryId() {
		return categoryId;
	}

	public String getDescription() {
		return description;
	}

	public double getPrice() {
		return price;
	}

	public int getProductId() {
		return productId;
	}

	public String getProductName() {
		return productName;
	}

	public void loadDetails(final Activity act) {
		activity = act;

		final String url = String.format(AppConfig.WEBSERVER_GETDETAILS,
				productId);

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
				updateFromJSON(result);

				if (activity != null) {
					final TextView text = (TextView) activity
							.findViewById(R.id.product_description);
					text.setText(getDescription());
					if (progDialog.isShowing()) {
						progDialog.dismiss();
					}
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progDialog = new ProgressDialog(activity, R.style.dialogTheme);
				progDialog.setMessage("Please wait...");
				progDialog.setIndeterminate(false);
				progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progDialog.setCancelable(true);
				progDialog.show();
			}

		};
		task.execute(url);
	}

	public void loadImage(final ImageView view, final int width,
			final int height) {
		imgView = view;
		final String url = String.format(AppConfig.WEBSERVER_GETIMAGE,
				productId, width, height, 0);

		final BitmapWorkerTask task = new BitmapWorkerTask(imgView) {
			@Override
			protected void onPostExecute(final Bitmap result) {
				if (result != null) {
					imgView.setImageBitmap(result);
				} else {
					imgView.setImageResource(ProductListFragment.productIcons[categoryId]);
				}
			}

		};
		task.execute(url);
	}

	public void setCategoryId(final int categoryId) {
		this.categoryId = categoryId;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setPrice(final double price) {
		this.price = price;
	}

	public void setProductId(final int productId) {
		this.productId = productId;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public void updateFromJSON(final JSONObject jObj) {
		try {
			if (jObj.has("ProductId")) {
				productId = jObj.getInt("ProductId");
			}
			if (jObj.has("ProductName")) {
				productName = jObj.getString("ProductName");
			}
			if (jObj.has("Price")) {
				price = jObj.getDouble("Price");
			}
			if (jObj.has("ProductDescription")) {
				description = jObj.getString("ProductDescription");
			}
		} catch (final JSONException JE) {
			JE.printStackTrace();
		}
	}

}
