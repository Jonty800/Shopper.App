package uk.ac.kent.jb509.shopper.utils;

import uk.ac.kent.jb509.shopper.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast extends Toast {

	public CustomToast(final Activity activity, final String message,
			final boolean flag) {
		super(activity);

		final LayoutInflater inflater = activity.getLayoutInflater();
		final View view = inflater.inflate(R.layout.adapter_toast_layout, null);
		final ImageView img = (ImageView) view
				.findViewById(R.id.imgCustomToast);
		final TextView txt = (TextView) view.findViewById(R.id.txtCustomToast);

		if (flag) {
			img.setImageResource(R.drawable.success);
		} else {
			img.setImageResource(R.drawable.failure);
		}
		txt.setText(message);
		// setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
		setDuration(Toast.LENGTH_SHORT);
		setView(view);
	}

	public CustomToast(final Context context) {
		super(context);
	}
}
