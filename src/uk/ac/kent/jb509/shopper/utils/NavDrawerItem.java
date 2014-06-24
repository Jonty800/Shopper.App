package uk.ac.kent.jb509.shopper.utils;

public class NavDrawerItem {

	private String count = "0";
	private int icon;
	// boolean to set visiblity of the counter
	private boolean isCounterVisible = false;
	private String title;

	public NavDrawerItem() {
	}

	public NavDrawerItem(final String title, final int icon) {
		this.title = title;
		this.icon = icon;
	}

	public NavDrawerItem(final String title, final int icon,
			final boolean isCounterVisible, final String count) {
		this.title = title;
		this.icon = icon;
		this.isCounterVisible = isCounterVisible;
		this.count = count;
	}

	public String getCount() {
		return this.count;
	}

	public boolean getCounterVisibility() {
		return this.isCounterVisible;
	}

	public int getIcon() {
		return this.icon;
	}

	public String getTitle() {
		return this.title;
	}

	public void setCount(final String count) {
		this.count = count;
	}

	public void setCounterVisibility(final boolean isCounterVisible) {
		this.isCounterVisible = isCounterVisible;
	}

	public void setIcon(final int icon) {
		this.icon = icon;
	}

	public void setTitle(final String title) {
		this.title = title;
	}
}
