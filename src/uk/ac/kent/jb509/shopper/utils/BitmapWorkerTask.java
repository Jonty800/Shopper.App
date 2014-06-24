/** Shopper application
 * Copyright (C) <2013> Jonathan Baker, University of Kent
 * All rights reserved
 */
package uk.ac.kent.jb509.shopper.utils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

/**
 * Extension of AsyncTask for image caching based on documentation:
 * http://developer.android.com/training/displaying-bitmaps/process-bitmap.html
 * 
 * @author jb509
 * 
 */
public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

	/**
	 * Disposable implementation of BipmapDrawable
	 * 
	 * @author jb509 Reference:
	 *         http://developer.android.com/training/displaying-
	 *         bitmaps/process-bitmap.html
	 */
	public static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(final Resources res, final Bitmap bitmap,
				final BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(
					bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	private static LruCache<String, Bitmap> cache = null;

	public static boolean cancelPotentialWork(final String url,
			final ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final String bitmapData = bitmapWorkerTask.url;
			if (!bitmapData.equals(url)) {
				// cancel the previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// return false if same work is already in progress
				return false;
			}
		}
		return true;
	}

	private static BitmapWorkerTask getBitmapWorkerTask(
			final ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	/**
	 * loads the bitmap into the view
	 */
	public static void loadBitmap(final Context context, final String url,
			final ImageView imageView) {
		if (cancelPotentialWork(url, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(
					context.getResources(), null, task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(url);
		}
	}

	private String url = "";

	public BitmapWorkerTask(final ImageView imageView) {
		if (cache == null) { // init if needed
			final int memClass = ((ActivityManager) imageView.getContext()
					.getSystemService(Context.ACTIVITY_SERVICE))
					.getMemoryClass();
			cache = new LruCache<String, Bitmap>(1024 * 1024 * memClass / 3) {
				@Override
				protected int sizeOf(final String key, final Bitmap value) {
					return value.getRowBytes() * value.getHeight();
				}
			};
		}
	}

	/**
	 * 
	 * @param key
	 *            to identify the image to add
	 * @param bitmap
	 *            image to add
	 */
	public void addBitmapToCache(final String key, final Bitmap bitmap) {
		if (getBitmapFromCache(key) == null) {
			cache.put(key, bitmap);
		}
	}

	@Override
	protected Bitmap doInBackground(final String... strings) {
		try {
			url = strings[0];

			Bitmap bitmap = getBitmapFromCache(url);

			if (bitmap != null) {
				System.out.println("Key matched using image cache");
				return bitmap; // use from cache if available
			}

			bitmap = BitmapFactory.decodeStream(new URL(url).openStream());
			addBitmapToCache(url, bitmap); // add to cache
			return bitmap;
		} catch (final IOException e) {
			return null;
		}
	}

	/**
	 * 
	 * @param key
	 * @return bitmap stored from key ID
	 */
	public Bitmap getBitmapFromCache(final String key) {
		return cache.get(key);
	}
}