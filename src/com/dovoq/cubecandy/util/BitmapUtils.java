package com.dovoq.cubecandy.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BitmapUtils {

	/**
	 * foreground自带缩放功能
	 */
	public static Bitmap merge(Resources res, Bitmap background,
			Bitmap foreground, int x, int y, int width, int height) {
		Bitmap bitmap = background.copy(Config.ARGB_8888, true);
		Canvas canvas = new Canvas(bitmap);
		Drawable drawable = new BitmapDrawable(res, Bitmap.createScaledBitmap(
				foreground, width, height, false));
		drawable.setBounds(x, y, x + width, y + height);
		drawable.draw(canvas);
		return bitmap;
	}
	
	public static Bitmap addText(Bitmap bitmap, String id) {
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(0);
		paint.setTextSize(20);
		paint.setShadowLayer(1, 0, 1, Color.TRANSPARENT);
		canvas.drawText(id, 217, 712, paint);
		return bitmap;
	}
}
