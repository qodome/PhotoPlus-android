package com.dovoq.cubecandy.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;

public class ViewUtils {

	public static Bitmap getSnapshot(Activity activity, Rect rect) {
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		Bitmap screen = view.getDrawingCache();
		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		return Bitmap.createBitmap(screen, rect.left, rect.top, rect.width(),
				rect.height());
	}

	public static Rect getFrame(View view) {
		Rect rect = new Rect();
		int[] location = new int[2];
		if (view != null) {
			view.getLocationOnScreen(location); // getLocationInWindow
			rect.left = location[0];
			rect.top = location[1];
			rect.right = rect.left + view.getWidth();
			rect.bottom = rect.top + view.getHeight();
		}
		return rect;
	}
}
