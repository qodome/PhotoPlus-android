package com.dovoq.cubecandy.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public class ViewUtils {

	public static Bitmap getSnapshot(View view) {
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
				Config.ARGB_8888);
		view.draw(new Canvas(bitmap));
		return bitmap;
	}

	// http://magiclen.org/android-drawingcache/ DrawingCache() 性能较差
	public static Bitmap getScreenshot(Activity activity) {
		View view = activity.getWindow().getDecorView();
		view.destroyDrawingCache(); // 不销毁的话每次都是第一次的cache
		view.setDrawingCacheEnabled(true);
		return view.getDrawingCache();
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
