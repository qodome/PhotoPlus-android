package com.dovoq.cubecandy.util;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
		Drawable drawable;
		if (width == foreground.getWidth() && height == foreground.getHeight()) {
			drawable = new BitmapDrawable(res, foreground);
		} else {
			drawable = new BitmapDrawable(res, Bitmap.createScaledBitmap(
					foreground, width, height, false));
		}
		drawable.setBounds(x, y, x + width, y + height);
		drawable.draw(canvas);
		return bitmap;
	}

	public static Bitmap addText(Bitmap bitmap, String id) {
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(0xFF000000);
		paint.setTextSize(22);
		paint.setShadowLayer(1, 0, 1, Color.TRANSPARENT);
		canvas.drawText(id, 216, 692, paint); // 217 712
		return bitmap;
	}

	public static Bitmap getBitmapFromAsset(Context context, String path) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(context.getAssets().open(path));
		} catch (IOException e) {
		}
		return bitmap;
	}
}
