package uk.ac.kent.jb509.shopper.utils;

/**
 * AutoResizeTextView.java Copyright (C) adneal (http://stackoverflow.com/users/420015/adneal) 
 * Source: http://stackoverflow.com/questions/9713669/auto-scale-text-size
 */

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class AutoResizeTextView extends TextView {

	private float maxTextSize;
	private float minTextSize;

	public AutoResizeTextView(final Context context) {
		super(context);
		init();
	}

	public AutoResizeTextView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public float getMaxTextSize() {
		return maxTextSize;
	}

	public float getMinTextSize() {
		return minTextSize;
	}

	private void init() {
		maxTextSize = this.getTextSize();
		if (maxTextSize < 35) {
			maxTextSize = 30;
		}
		minTextSize = 20;
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec,
			final int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		refitText(this.getText().toString(), parentWidth);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw,
			final int oldh) {
		if (w != oldw) {
			refitText(this.getText().toString(), w);
		}
	}

	@Override
	protected void onTextChanged(final CharSequence text, final int start,
			final int before, final int after) {
		refitText(text.toString(), this.getWidth());
	}

	private void refitText(final String text, final int textWidth) {
		if (textWidth > 0) {
			final int availableWidth = textWidth - this.getPaddingLeft()
					- this.getPaddingRight();
			float trySize = maxTextSize;

			this.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
			while (trySize > minTextSize
					&& this.getPaint().measureText(text) > availableWidth) {
				trySize -= 1;
				if (trySize <= minTextSize) {
					trySize = minTextSize;
					break;
				}
				this.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
			}
			this.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
		}
	}

	public void setMaxTextSize(final int minTextSize) {
		this.maxTextSize = minTextSize;
	}

	public void setMinTextSize(final int minTextSize) {
		this.minTextSize = minTextSize;
	}
}