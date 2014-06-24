package uk.ac.kent.jb509.shopper;

import java.util.ArrayList;

import uk.ac.kent.jb509.shopper.adapter.NavDrawerListAdapter;
import uk.ac.kent.jb509.shopper.fragments.CategoryFragment;
import uk.ac.kent.jb509.shopper.fragments.ProductListFragment;
import uk.ac.kent.jb509.shopper.fragments.ShoppingCartFragment;
import uk.ac.kent.jb509.shopper.fragments.StartFragment;
import uk.ac.kent.jb509.shopper.utils.AppConfig;
import uk.ac.kent.jb509.shopper.utils.NavDrawerItem;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

public class MainActivity extends Activity {
	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view,
				final int position, final long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	public static MainActivity instance;
	private NavDrawerListAdapter adapter;
	public Context context;

	public Button mCloseButton;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;

	// nav drawer title
	private CharSequence mDrawerTitle;
	private ActionBarDrawerToggle mDrawerToggle;

	public Menu menu;
	public Button mOpenButton;

	public SearchView mSearchView;

	// used to store app title
	private CharSequence mTitle;

	private ArrayList<NavDrawerItem> navDrawerItems;

	private TypedArray navMenuIcons;

	// slide menu items
	private String[] navMenuTitles;

	ShopperApp shopperApp = (ShopperApp) getApplication();

	public TextView shoppingCartTextView;

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(final int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case 0:
			fragment = new CategoryFragment();
			break;
		case 1:
			AppConfig.SELECTED_CATEGORY = Integer.toString(position);
			fragment = new ProductListFragment();
			break;
		case 2:
			AppConfig.SELECTED_CATEGORY = Integer.toString(position);
			fragment = new ProductListFragment();
			break;
		case 3:
			AppConfig.SELECTED_CATEGORY = Integer.toString(position);
			fragment = new ProductListFragment();
			break;
		case 4:
			AppConfig.SELECTED_CATEGORY = Integer.toString(position);
			fragment = new ProductListFragment();
			break;
		case 5:
			fragment = new ShoppingCartFragment();
			break;

		default:
			fragment = new StartFragment();
		}

		if (fragment != null) {
			AppConfig.transitionFragment(getFragmentManager(), fragment);

			// update selected item and title, then close the drawer
			if (position != 99) {
				mDrawerList.setItemChecked(position, true);
				mDrawerList.setSelection(position);
			}
			setTitle(position == 99 ? "Start" : navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		}
	}

	@Override
	public void onBackPressed() {
		invalidateOptionsMenu();
		// Otherwise defer to system default behavior.
		super.onBackPressed();
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getActionBar().setBackgroundDrawable(
				new ColorDrawable(Color.parseColor("#111111")));

		// getWindow()
		// .setBackgroundDrawableResource(R.drawable.black_pattern_tile);
		setContentView(R.layout.adapter_drawer_layout);
		mTitle = mDrawerTitle = "Shopper.net";

		// load slide menu items
		final String[] titles = new String[] { "Home",
				getResources().getStringArray(R.array.categories)[0],
				getResources().getStringArray(R.array.categories)[1],
				getResources().getStringArray(R.array.categories)[2],
				getResources().getStringArray(R.array.categories)[3],
				"Checkout" };
		navMenuTitles = titles;

		// nav drawer icons from resources
		navMenuIcons = getResources().obtainTypedArray(R.array.categoryicons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Home
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons
				.getResourceId(0, -1)));
		// Find People
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons
				.getResourceId(1, -1)));
		// Photos
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons
				.getResourceId(2, -1)));
		// Communities, Will add a counter here
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons
				.getResourceId(3, -1), true, "22"));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons
				.getResourceId(4, -1)));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons
				.getResourceId(5, -1)));

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			@Override
			public void onDrawerClosed(final View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(final View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerLayout.setBackgroundResource(R.drawable.background_gradient);

		ImageView view = (ImageView) findViewById(android.R.id.home);
		view.setPadding(
				getResources().getDimensionPixelSize(
						R.dimen.activity_horizontal_margin), 8, 0, 8);

		if (savedInstanceState == null) {
			// on first time display view for first nav item
			displayView(99);// startfragment
		}
		instance = this;
		context = this;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;

		final RelativeLayout badgeLayout = (RelativeLayout) menu.findItem(
				R.id.show_cart).getActionView();
		if (badgeLayout != null) {
			badgeLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					final Fragment fragment = new ShoppingCartFragment();
					if (fragment != null) {
						AppConfig.transitionFragment(getFragmentManager(),
								fragment);
					}
				}
			});
			final ImageView iv = (ImageView) badgeLayout
					.findViewById(R.id.cart_img);
			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					final Fragment fragment = new ShoppingCartFragment();
					if (fragment != null) {
						AppConfig.transitionFragment(getFragmentManager(),
								fragment);
					}
				}
			});
			final ShopperApp shopperApp = (ShopperApp) getApplication();
			shoppingCartTextView = (TextView) badgeLayout
					.findViewById(R.id.actionbar_notifcation_textview);
			shoppingCartTextView
					.setBackgroundResource(R.drawable.list_item_divider);

			if (shoppingCartTextView != null) {
				String s = "";
				if (shopperApp.getCartSize() > 0) {
					s += shopperApp.getCartSize();
				}
				shoppingCartTextView.setText(s);
				shoppingCartTextView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(final View v) {
						final Fragment fragment = new ShoppingCartFragment();
						if (fragment != null) {
							AppConfig.transitionFragment(getFragmentManager(),
									fragment);
						}
					}
				});
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void setTitle(final CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(title);
	}
}
