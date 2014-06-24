package uk.ac.kent.jb509.shopper.utils;

/** Copyright (C) Umano: News Read To You (https://github.com/umano/AndroidSlidingUpPanel)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except 
 * in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is 
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

public class SlidingUpPanelLayout extends ViewGroup {

	private class DragHelperCallback extends ViewDragHelper.Callback {

		@Override
		public int clampViewPositionVertical(final View child, final int top,
				final int dy) {
			final int topBound = getPaddingTop();
			final int bottomBound = topBound + mSlideRange;

			final int newLeft = Math.min(Math.max(top, topBound), bottomBound);

			return newLeft;
		}

		@Override
		public int getViewVerticalDragRange(final View child) {
			return mSlideRange;
		}

		@Override
		public void onViewCaptured(final View capturedChild,
				final int activePointerId) {
			// Make all child views visible in preparation for sliding things
			// around
			setAllChildrenVisible();
		}

		@Override
		public void onViewDragStateChanged(final int state) {
			if (mDragHelper.getViewDragState() == ViewDragHelper.STATE_IDLE) {
				if (mSlideOffset == 0) {
					updateObscuredViewVisibility();
					dispatchOnPanelExpanded(mSlideableView);
					mPreservedExpandedState = true;
				} else if (isAnchored()) {
					updateObscuredViewVisibility();
					dispatchOnPanelAnchored(mSlideableView);
					mPreservedExpandedState = true;
				} else {
					dispatchOnPanelCollapsed(mSlideableView);
					mPreservedExpandedState = false;
				}
			}
		}

		@Override
		public void onViewPositionChanged(final View changedView,
				final int left, final int top, final int dx, final int dy) {
			onPanelDragged(top);
			invalidate();
		}

		@Override
		public void onViewReleased(final View releasedChild, final float xvel,
				final float yvel) {
			int top = getPaddingTop();

			if (mAnchorPoint != 0) {
				final int anchoredTop = (int) (mAnchorPoint * mSlideRange);
				final float anchorOffset = (float) anchoredTop
						/ (float) mSlideRange;

				if (yvel > 0 || yvel == 0
						&& mSlideOffset >= (1f + anchorOffset) / 2) {
					top += mSlideRange;
				} else if (yvel == 0 && mSlideOffset < (1f + anchorOffset) / 2
						&& mSlideOffset >= anchorOffset / 2) {
					top += mSlideRange * mAnchorPoint;
				}

			} else if (yvel > 0 || yvel == 0 && mSlideOffset > 0.5f) {
				top += mSlideRange;
			}

			mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
			invalidate();
		}

		@Override
		public boolean tryCaptureView(final View child, final int pointerId) {
			if (mIsUnableToDrag) {
				return false;
			}

			return ((LayoutParams) child.getLayoutParams()).slideable;
		}
	}

	public static class LayoutParams extends ViewGroup.MarginLayoutParams {
		private static final int[] ATTRS = new int[] { android.R.attr.layout_weight };

		Paint dimPaint;

		/**
		 * True if this view should be drawn dimmed when it's been offset from
		 * its default position.
		 */
		boolean dimWhenOffset;

		/**
		 * True if this pane is the slideable pane in the layout.
		 */
		boolean slideable;

		public LayoutParams() {
			super(MATCH_PARENT, MATCH_PARENT);
		}

		public LayoutParams(final android.view.ViewGroup.LayoutParams source) {
			super(source);
		}

		public LayoutParams(final Context c, final AttributeSet attrs) {
			super(c, attrs);

			final TypedArray a = c.obtainStyledAttributes(attrs, ATTRS);
			a.recycle();
		}

		public LayoutParams(final int width, final int height) {
			super(width, height);
		}

		public LayoutParams(final LayoutParams source) {
			super(source);
		}

		public LayoutParams(final MarginLayoutParams source) {
			super(source);
		}

	}

	/**
	 * Listener for monitoring events about sliding panes.
	 */
	public interface PanelSlideListener {
		public void onPanelAnchored(View panel);

		/**
		 * Called when a sliding pane becomes slid completely collapsed. The
		 * pane may or may not be interactive at this point depending on if it's
		 * shown or hidden
		 * 
		 * @param panel
		 *            The child view that was slid to an collapsed position,
		 *            revealing other panes
		 */
		public void onPanelCollapsed(View panel);

		/**
		 * Called when a sliding pane becomes slid completely expanded. The pane
		 * is now guaranteed to be interactive. It may now obscure other views
		 * in the layout.
		 * 
		 * @param panel
		 *            The child view that was slid to a expanded position
		 */
		public void onPanelExpanded(View panel);

		/**
		 * Called when a sliding pane's position changes.
		 * 
		 * @param panel
		 *            The child view that was moved
		 * @param slideOffset
		 *            The new offset of this sliding pane within its range, from
		 *            0-1
		 */
		public void onPanelSlide(View panel, float slideOffset);
	}

	static class SavedState extends BaseSavedState {
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(final Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(final int size) {
				return new SavedState[size];
			}
		};

		boolean isExpanded;

		private SavedState(final Parcel in) {
			super(in);
			isExpanded = in.readInt() != 0;
		}

		SavedState(final Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(final Parcel out, final int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(isExpanded ? 1 : 0);
		}
	}

	/**
	 * No-op stubs for {@link PanelSlideListener}. If you only want to implement
	 * a subset of the listener methods you can extend this instead of implement
	 * the full interface.
	 */
	public static class SimplePanelSlideListener implements PanelSlideListener {
		@Override
		public void onPanelAnchored(final View panel) {
		}

		@Override
		public void onPanelCollapsed(final View panel) {
		}

		@Override
		public void onPanelExpanded(final View panel) {
		}

		@Override
		public void onPanelSlide(final View panel, final float slideOffset) {
		}
	}

	/**
	 * If no fade color is given by default it will fade to 80% gray.
	 */
	private static final int DEFAULT_FADE_COLOR = 0x99000000;

	/**
	 * Default peeking out panel height
	 */
	private static final int DEFAULT_PANEL_HEIGHT = 68; // dp;

	/**
	 * Default height of the shadow above the peeking out panel
	 */
	private static final int DEFAULT_SHADOW_HEIGHT = 4; // dp;

	/**
	 * Minimum velocity that will be detected as a fling
	 */
	private static final int MIN_FLING_VELOCITY = 400; // dips per second

	private static boolean hasOpaqueBackground(final View v) {
		final Drawable bg = v.getBackground();
		if (bg != null) {
			return bg.getOpacity() == PixelFormat.OPAQUE;
		}
		return false;
	}

	/**
	 * If panel is transparent, set default panel color
	 */
	public String DEFAULT_PANEL_COLOR_TRANSPARENT = "AA000000";

	public boolean enabled = true;

	private float mAnchorPoint = 0.f;

	/**
	 * True if a panel can slide with the current measurements
	 */
	private boolean mCanSlide;

	/**
	 * The fade color used for the panel covered by the slider. 0 = no fading.
	 */
	private int mCoveredFadeColor = DEFAULT_FADE_COLOR;

	/**
	 * The paint used to dim the main layout when sliding
	 */
	private final Paint mCoveredFadePaint = new Paint();

	private final ViewDragHelper mDragHelper;

	/**
	 * If provided, the panel can be dragged by only this view. Otherwise, the
	 * entire panel can be used for dragging.
	 */
	private View mDragView;

	private boolean mDragViewHit;

	private boolean mFirstLayout = true;

	private float mInitialMotionX;

	private float mInitialMotionY;
	/**
	 * Flag indicating that sliding feature is enabled\disabled
	 */
	private boolean mIsSlidingEnabled;
	/**
	 * Flag is Transparent
	 */
	private boolean mIsTransparent = true;
	/**
	 * A panel view is locked into internal scrolling or another condition that
	 * is preventing a drag.
	 */
	private boolean mIsUnableToDrag;

	/**
	 * Flag indicating if a drag view can have its own touch events. If set to
	 * true, a drag view can scroll horizontally and have its own click
	 * listener.
	 * 
	 * Default is set to false.
	 */
	private boolean mIsUsingDragViewTouchEvents;

	/**
	 * The size of the overhang in pixels.
	 */
	private int mPanelHeight;

	private PanelSlideListener mPanelSlideListener;
	/**
	 * Stores whether or not the pane was expanded the last time it was
	 * slideable. If expand/collapse operations are invoked this state is
	 * modified. Used by instance state save/restore.
	 */
	private boolean mPreservedExpandedState;

	/**
	 * Threshold to tell if there was a scroll touch event.
	 */
	private final int mScrollTouchSlop;

	/**
	 * Drawable used to draw the shadow between panes.
	 */
	private Drawable mShadowDrawable;

	/**
	 * The size of the shadow in pixels.
	 */
	private final int mShadowHeight;

	/**
	 * The child view that can slide, if any.
	 */
	private View mSlideableView;

	/**
	 * How far the panel is offset from its expanded position. range [0, 1]
	 * where 0 = expanded, 1 = collapsed.
	 */
	private float mSlideOffset;

	/**
	 * How far in pixels the slideable panel may move.
	 */
	private int mSlideRange;

	private final Rect mTmpRect = new Rect();

	public SlidingUpPanelLayout(final Context context) {
		this(context, null);
	}

	public SlidingUpPanelLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingUpPanelLayout(final Context context,
			final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);

		final float density = context.getResources().getDisplayMetrics().density;
		mPanelHeight = (int) (DEFAULT_PANEL_HEIGHT * density + 0.5f);
		mShadowHeight = (int) (DEFAULT_SHADOW_HEIGHT * density + 0.5f);

		setWillNotDraw(false);

		mDragHelper = ViewDragHelper.create(this, 0.5f,
				new DragHelperCallback());
		mDragHelper.setMinVelocity(MIN_FLING_VELOCITY * density);

		mCanSlide = true;
		mIsSlidingEnabled = true;

		setCoveredFadeColor(DEFAULT_FADE_COLOR);

		final ViewConfiguration vc = ViewConfiguration.get(context);
		mScrollTouchSlop = vc.getScaledTouchSlop();
	}

	/**
	 * Tests scrollability within child views of v given a delta of dx.
	 * 
	 * @param v
	 *            View to test for horizontal scrollability
	 * @param checkV
	 *            Whether the view v passed should itself be checked for
	 *            scrollability (true), or just its children (false).
	 * @param dx
	 *            Delta scrolled in pixels
	 * @param x
	 *            X coordinate of the active touch point
	 * @param y
	 *            Y coordinate of the active touch point
	 * @return true if child views of v can be scrolled by delta of dx.
	 */
	protected boolean canScroll(final View v, final boolean checkV,
			final int dx, final int x, final int y) {
		if (v instanceof ViewGroup) {
			final ViewGroup group = (ViewGroup) v;
			final int scrollX = v.getScrollX();
			final int scrollY = v.getScrollY();
			final int count = group.getChildCount();
			// Count backwards - let topmost views consume scroll distance
			// first.
			for (int i = count - 1; i >= 0; i--) {
				final View child = group.getChildAt(i);
				if (x + scrollX >= child.getLeft()
						&& x + scrollX < child.getRight()
						&& y + scrollY >= child.getTop()
						&& y + scrollY < child.getBottom()
						&& canScroll(child, true, dx,
								x + scrollX - child.getLeft(), y + scrollY
										- child.getTop())) {
					return true;
				}
			}
		}
		return checkV && ViewCompat.canScrollHorizontally(v, -dx);
	}

	@Override
	protected boolean checkLayoutParams(final ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams && super.checkLayoutParams(p);
	}

	/**
	 * Collapse the sliding pane if it is currently slideable. If first layout
	 * has already completed this will animate.
	 * 
	 * @return true if the pane was slideable and is now collapsed/in the
	 *         process of collapsing
	 */
	public boolean collapsePane() {
		return collapsePane(mSlideableView, 0);
	}

	private boolean collapsePane(final View pane, final int initialVelocity) {
		if (mFirstLayout || smoothSlideTo(1.f, initialVelocity)) {
			mPreservedExpandedState = false;
			return true;
		}
		return false;
	}

	@Override
	public void computeScroll() {
		if (mDragHelper.continueSettling(true)) {
			if (!mCanSlide) {
				mDragHelper.abort();
				return;
			}

			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	void dispatchOnPanelAnchored(final View panel) {
		if (mPanelSlideListener != null) {
			mPanelSlideListener.onPanelAnchored(panel);
		}
		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	void dispatchOnPanelCollapsed(final View panel) {
		if (mPanelSlideListener != null) {
			mPanelSlideListener.onPanelCollapsed(panel);
		}
		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	void dispatchOnPanelExpanded(final View panel) {
		if (mPanelSlideListener != null) {
			mPanelSlideListener.onPanelExpanded(panel);
		}
		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	void dispatchOnPanelSlide(final View panel) {
		if (mPanelSlideListener != null) {
			mPanelSlideListener.onPanelSlide(panel, mSlideOffset);
		}
	}

	@Override
	public void draw(final Canvas c) {
		super.draw(c);

		if (mSlideableView == null) {
			// No need to draw a shadow if we don't have one.
			return;
		}

		final int right = mSlideableView.getRight();
		final int top = mSlideableView.getTop() - mShadowHeight;
		final int bottom = mSlideableView.getTop();
		final int left = mSlideableView.getLeft();

		if (mShadowDrawable != null) {
			mShadowDrawable.setBounds(left, top, right, bottom);
			mShadowDrawable.draw(c);
		}
	}

	@Override
	protected boolean drawChild(final Canvas canvas, final View child,
			final long drawingTime) {
		final LayoutParams lp = (LayoutParams) child.getLayoutParams();
		boolean result;
		final int save = canvas.save(Canvas.CLIP_SAVE_FLAG);

		boolean drawScrim = false;

		if (mCanSlide && !lp.slideable && mSlideableView != null) {
			// Clip against the slider; no sense drawing what will immediately
			// be covered.
			canvas.getClipBounds(mTmpRect);
			mTmpRect.bottom = Math
					.min(mTmpRect.bottom, mSlideableView.getTop());

			if (!mIsTransparent) {
				canvas.clipRect(mTmpRect);
			}

			if (mSlideOffset < 1) {
				drawScrim = true;
			}
		}

		result = super.drawChild(canvas, child, drawingTime);
		canvas.restoreToCount(save);

		if (drawScrim) {
			final int baseAlpha = (mCoveredFadeColor & 0xff000000) >>> 24;
			final int imag = (int) (baseAlpha * (1 - mSlideOffset));
			final int color = imag << 24 | mCoveredFadeColor & 0xffffff;
			mCoveredFadePaint.setColor(color);
			canvas.drawRect(mTmpRect, mCoveredFadePaint);
		}

		return result;
	}

	/**
	 * Expand the sliding pane if it is currently slideable. If first layout has
	 * already completed this will animate.
	 * 
	 * @return true if the pane was slideable and is now expanded/in the process
	 *         of expading
	 */
	public boolean expandPane() {
		return expandPane(0);
	}

	/**
	 * Partially expand the sliding pane up to a specific offset
	 * 
	 * @param mSlideOffset
	 *            Value between 0 and 1, where 0 is completely expanded.
	 * @return true if the pane was slideable and is now expanded/in the process
	 *         of expading
	 */
	public boolean expandPane(final float mSlideOffset) {
		if (!isPaneVisible()) {
			showPane();
		}
		return expandPane(mSlideableView, 0, mSlideOffset);
	}

	private boolean expandPane(final View pane, final int initialVelocity,
			final float mSlideOffset) {
		if (mFirstLayout || smoothSlideTo(mSlideOffset, initialVelocity)) {
			mPreservedExpandedState = true;
			return true;
		}
		return false;
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams();
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(final AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(
			final ViewGroup.LayoutParams p) {
		return p instanceof MarginLayoutParams ? new LayoutParams(
				(MarginLayoutParams) p) : new LayoutParams(p);
	}

	/**
	 * @return The ARGB-packed color value used to fade the fixed pane
	 */
	public int getCoveredFadeColor() {
		return mCoveredFadeColor;
	}

	/**
	 * @return The current collapsed panel height
	 */
	public int getPanelHeight() {
		return mPanelHeight;
	}

	public void hidePane() {
		if (mSlideableView == null) {
			return;
		}
		mSlideableView.setVisibility(View.GONE);
		requestLayout();
	}

	/**
	 * Check if the layout is anchored in an intermediate point.
	 * 
	 * @return true if sliding panels are anchored
	 */
	public boolean isAnchored() {
		final int anchoredTop = (int) (mAnchorPoint * mSlideRange);
		return !mFirstLayout && mCanSlide
				&& mSlideOffset == (float) anchoredTop / (float) mSlideRange;
	}

	private boolean isDragViewHit(final int x, final int y) {
		final View v = mDragView != null ? mDragView : mSlideableView;
		if (v == null) {
			return false;
		}
		final int[] viewLocation = new int[2];
		v.getLocationOnScreen(viewLocation);
		final int[] parentLocation = new int[2];
		this.getLocationOnScreen(parentLocation);
		final int screenX = parentLocation[0] + x;
		final int screenY = parentLocation[1] + y;
		return screenX >= viewLocation[0]
				&& screenX < viewLocation[0] + v.getWidth()
				&& screenY >= viewLocation[1]
				&& screenY < viewLocation[1] + v.getHeight();
	}

	/**
	 * Check if the layout is completely expanded.
	 * 
	 * @return true if sliding panels are completely expanded
	 */
	public boolean isExpanded() {
		return mFirstLayout && mPreservedExpandedState || !mFirstLayout
				&& mCanSlide && mSlideOffset == 0;
	}

	public boolean isPaneVisible() {
		if (getChildCount() < 2) {
			return false;
		}
		final View slidingPane = getChildAt(1);
		return slidingPane.getVisibility() == View.VISIBLE;
	}

	/**
	 * Check if the content in this layout cannot fully fit side by side and
	 * therefore the content pane can be slid back and forth.
	 * 
	 * @return true if content in this layout can be expanded
	 */
	public boolean isSlideable() {
		return mCanSlide;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mFirstLayout = true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mFirstLayout = true;
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent ev) {
		final int action = MotionEventCompat.getActionMasked(ev);

		if (!mCanSlide || !mIsSlidingEnabled || mIsUnableToDrag
				&& action != MotionEvent.ACTION_DOWN) {
			mDragHelper.cancel();
			return super.onInterceptTouchEvent(ev);
		}

		if (action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_UP) {
			mDragHelper.cancel();
			return false;
		}

		final float x = ev.getX();
		final float y = ev.getY();
		boolean interceptTap = false;

		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			mIsUnableToDrag = false;
			mInitialMotionX = x;
			mInitialMotionY = y;
			mDragViewHit = isDragViewHit((int) x, (int) y);

			if (mDragViewHit && !mIsUsingDragViewTouchEvents) {
				interceptTap = true;
			}
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			final float adx = Math.abs(x - mInitialMotionX);
			final float ady = Math.abs(y - mInitialMotionY);
			final int dragSlop = mDragHelper.getTouchSlop();

			// Handle any horizontal scrolling on the drag view.
			if (mIsUsingDragViewTouchEvents) {
				if (adx > mScrollTouchSlop && ady < mScrollTouchSlop) {
					return super.onInterceptTouchEvent(ev);
				}
				// Intercept the touch if the drag view has any vertical scroll.
				// onTouchEvent will determine if the view should drag
				// vertically.
				else if (ady > mScrollTouchSlop) {
					interceptTap = mDragViewHit;
				}
			}

			if (ady > dragSlop && adx > ady) {
				mDragHelper.cancel();
				mIsUnableToDrag = true;
				return false;
			}
			break;
		}
		}

		final boolean interceptForDrag = mDragViewHit
				&& mDragHelper.shouldInterceptTouchEvent(ev);

		return interceptForDrag || interceptTap;
	}

	@Override
	protected void onLayout(final boolean changed, final int l, final int t,
			final int r, final int b) {
		final int paddingLeft = getPaddingLeft();
		final int paddingTop = getPaddingTop();

		final int childCount = getChildCount();
		int yStart = paddingTop;
		int nextYStart = yStart;

		if (mFirstLayout) {
			mSlideOffset = mCanSlide && mPreservedExpandedState ? 0.f : 1.f;
		}

		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);

			if (mIsTransparent && i == 1) {
				child.setBackgroundColor(Color.argb(230, 0, 0, 0));
			}

			if (child.getVisibility() == GONE) {
				continue;
			}

			final LayoutParams lp = (LayoutParams) child.getLayoutParams();

			final int childHeight = child.getMeasuredHeight();

			if (lp.slideable) {
				mSlideRange = childHeight - mPanelHeight;
				yStart += (int) (mSlideRange * mSlideOffset);
			} else {
				yStart = nextYStart;
			}

			final int childTop = yStart;
			final int childBottom = childTop + childHeight;
			final int childLeft = paddingLeft;
			final int childRight = childLeft + child.getMeasuredWidth();
			child.layout(childLeft, childTop, childRight, childBottom);

			nextYStart += child.getHeight();
		}

		if (mFirstLayout) {
			updateObscuredViewVisibility();
		}

		mFirstLayout = false;
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec,
			final int heightMeasureSpec) {
		// final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		// final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		/*
		 * if (widthMode != MeasureSpec.EXACTLY) { throw new
		 * IllegalStateException(
		 * "Width must have an exact value or MATCH_PARENT"); } else if
		 * (heightMode != MeasureSpec.EXACTLY) { throw new
		 * IllegalStateException(
		 * "Height must have an exact value or MATCH_PARENT"); }
		 */

		final int layoutHeight = heightSize - getPaddingTop()
				- getPaddingBottom();
		int panelHeight = mPanelHeight;

		final int childCount = getChildCount();

		if (childCount > 2) {
			// Log.e(TAG,
			// "onMeasure: More than two child views are not supported.");
		} else if (getChildAt(1) != null
				&& getChildAt(1).getVisibility() == GONE) {
			panelHeight = 0;
		}

		// We'll find the current one below.
		mSlideableView = null;
		mCanSlide = false;

		// First pass. Measure based on child LayoutParams width/height.
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			final LayoutParams lp = (LayoutParams) child.getLayoutParams();

			int height = layoutHeight;
			if (child.getVisibility() == GONE) {
				lp.dimWhenOffset = false;
				continue;
			}

			if (i == 1) {
				lp.slideable = true;
				lp.dimWhenOffset = true;
				mSlideableView = child;
				mCanSlide = true;
			} else {
				height -= panelHeight;
			}

			int childWidthSpec;
			int childHeightSpec;
			if (mIsTransparent && i == 0) {
				childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize,
						MeasureSpec.EXACTLY);
				childHeightSpec = MeasureSpec.makeMeasureSpec(heightSize,
						MeasureSpec.EXACTLY);
			} else {
				if (lp.width == android.view.ViewGroup.LayoutParams.WRAP_CONTENT) {
					childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize,
							MeasureSpec.AT_MOST);
				} else if (lp.width == android.view.ViewGroup.LayoutParams.MATCH_PARENT) {
					childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize,
							MeasureSpec.EXACTLY);
				} else {
					childWidthSpec = MeasureSpec.makeMeasureSpec(lp.width,
							MeasureSpec.EXACTLY);
				}

				if (lp.height == android.view.ViewGroup.LayoutParams.WRAP_CONTENT) {
					childHeightSpec = MeasureSpec.makeMeasureSpec(height,
							MeasureSpec.AT_MOST);
				} else if (lp.height == android.view.ViewGroup.LayoutParams.MATCH_PARENT) {
					childHeightSpec = MeasureSpec.makeMeasureSpec(height,
							MeasureSpec.EXACTLY);
				} else {
					childHeightSpec = MeasureSpec.makeMeasureSpec(lp.height,
							MeasureSpec.EXACTLY);
				}
			}
			child.measure(childWidthSpec, childHeightSpec);
		}

		setMeasuredDimension(widthSize, heightSize);
	}

	private void onPanelDragged(final int newTop) {
		final int topBound = getPaddingTop();
		mSlideOffset = (float) (newTop - topBound) / mSlideRange;
		dispatchOnPanelSlide(mSlideableView);
	}

	@Override
	protected void onRestoreInstanceState(final Parcelable state) {
		final SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());

		if (ss.isExpanded) {
			expandPane();
		} else {
			collapsePane();
		}
		mPreservedExpandedState = ss.isExpanded;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();

		final SavedState ss = new SavedState(superState);
		ss.isExpanded = isSlideable() ? isExpanded() : mPreservedExpandedState;

		return ss;
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw,
			final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// Recalculate sliding panes and their details
		if (h != oldh) {
			mFirstLayout = true;
		}
	}

	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		if (!enabled) {
			return false;
		}
		if (!mCanSlide || !mIsSlidingEnabled) {
			return super.onTouchEvent(ev);
		}

		mDragHelper.processTouchEvent(ev);

		final int action = ev.getAction();
		final boolean wantTouchEvents = true;

		switch (action & MotionEventCompat.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();
			mInitialMotionX = x;
			mInitialMotionY = y;
			break;
		}

		case MotionEvent.ACTION_UP: {
			final float x = ev.getX();
			final float y = ev.getY();
			final float dx = x - mInitialMotionX;
			final float dy = y - mInitialMotionY;
			final int slop = mDragHelper.getTouchSlop();
			if (dx * dx + dy * dy < slop * slop
					&& isDragViewHit((int) x, (int) y)) {
				final View v = mDragView != null ? mDragView : mSlideableView;
				v.playSoundEffect(SoundEffectConstants.CLICK);
				if (!isExpanded() && !isAnchored()) {
					expandPane(mSlideableView, 0, mAnchorPoint);
				} else {
					collapsePane();
				}
				break;
			}
			break;
		}
		}

		return wantTouchEvents;
	}

	@Override
	public void requestChildFocus(final View child, final View focused) {
		super.requestChildFocus(child, focused);
		if (!isInTouchMode() && !mCanSlide) {
			mPreservedExpandedState = child == mSlideableView;
		}
	}

	void setAllChildrenVisible() {
		for (int i = 0, childCount = getChildCount(); i < childCount; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == INVISIBLE) {
				child.setVisibility(VISIBLE);
			}
		}
	}

	/**
	 * Set an anchor point where the panel can stop during sliding
	 * 
	 * @param anchorPoint
	 *            A value between 0 and 1, determining the position of the
	 *            anchor point starting from the top of the layout.
	 */
	public void setAnchorPoint(final float anchorPoint) {
		if (anchorPoint > 0 && anchorPoint < 1) {
			mAnchorPoint = anchorPoint;
		}
	}

	/**
	 * Set the color used to fade the pane covered by the sliding pane out when
	 * the pane will become fully covered in the expanded state.
	 * 
	 * @param color
	 *            An ARGB-packed color value
	 */
	public void setCoveredFadeColor(final int color) {
		mCoveredFadeColor = color;
		invalidate();
	}

	/**
	 * Set the draggable view portion. Use to null, to allow the whole panel to
	 * be draggable
	 * 
	 * @param dragView
	 *            A view that will be used to drag the panel.
	 */
	public void setDragView(final View dragView) {
		mDragView = dragView;
	}

	/**
	 * Set if the drag view can have its own touch events. If set to true, a
	 * drag view can scroll horizontally and have its own click listener.
	 * 
	 * Default is set to false.
	 */
	public void setEnableDragViewTouchEvents(final boolean enabled) {
		mIsUsingDragViewTouchEvents = enabled;
	}

	/**
	 * Set transparent flag
	 * 
	 */
	public void setIsTransparent(final boolean mIsTransparent) {
		this.mIsTransparent = mIsTransparent;
	}

	/**
	 * Set the collapsed panel height in pixels
	 * 
	 * @param val
	 *            A height in pixels
	 */
	public void setPanelHeight(final int val) {
		mPanelHeight = val;
		requestLayout();
	}

	public void setPanelSlideListener(final PanelSlideListener listener) {
		mPanelSlideListener = listener;
	}

	/**
	 * Set the shadow for the sliding panel
	 * 
	 */
	public void setShadowDrawable(final Drawable drawable) {
		mShadowDrawable = drawable;
	}

	/**
	 * Set sliding enabled flag
	 * 
	 * @param enabled
	 *            flag value
	 */
	public void setSlidingEnabled(final boolean enabled) {
		mIsSlidingEnabled = enabled;
	}

	public void showPane() {
		if (getChildCount() < 2) {
			return;
		}
		final View slidingPane = getChildAt(1);
		slidingPane.setVisibility(View.VISIBLE);
		requestLayout();
	}

	/**
	 * Smoothly animate mDraggingPane to the target X position within its range.
	 * 
	 * @param slideOffset
	 *            position to animate to
	 * @param velocity
	 *            initial velocity in case of fling, or 0.
	 */
	boolean smoothSlideTo(final float slideOffset, final int velocity) {
		if (!mCanSlide) {
			// Nothing to do.
			return false;
		}

		final int topBound = getPaddingTop();
		final int y = (int) (topBound + slideOffset * mSlideRange);

		if (mDragHelper.smoothSlideViewTo(mSlideableView,
				mSlideableView.getLeft(), y)) {
			setAllChildrenVisible();
			ViewCompat.postInvalidateOnAnimation(this);
			return true;
		}
		return false;
	}

	void updateObscuredViewVisibility() {
		if (getChildCount() == 0) {
			return;
		}
		final int leftBound = getPaddingLeft();
		final int rightBound = getWidth() - getPaddingRight();
		final int topBound = getPaddingTop();
		final int bottomBound = getHeight() - getPaddingBottom();
		final int left;
		final int right;
		final int top;
		final int bottom;
		if (mSlideableView != null && hasOpaqueBackground(mSlideableView)) {
			left = mSlideableView.getLeft();
			right = mSlideableView.getRight();
			top = mSlideableView.getTop();
			bottom = mSlideableView.getBottom();
		} else {
			left = right = top = bottom = 0;
		}
		final View child = getChildAt(0);
		final int clampedChildLeft = Math.max(leftBound, child.getLeft());
		final int clampedChildTop = Math.max(topBound, child.getTop());
		final int clampedChildRight = Math.min(rightBound, child.getRight());
		final int clampedChildBottom = Math.min(bottomBound, child.getBottom());
		final int vis;
		if (clampedChildLeft >= left && clampedChildTop >= top
				&& clampedChildRight <= right && clampedChildBottom <= bottom) {
			vis = INVISIBLE;
		} else {
			vis = VISIBLE;
		}
		child.setVisibility(vis);
	}
}