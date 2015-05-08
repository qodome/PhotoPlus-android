package com.dovoq.cubecandy.tmp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import com.dovoq.cubecandy.Constants;
import com.dovoq.cubecandy.R;
import com.google.common.base.Objects;

public class OverlayManager implements Constants {
	private final String[] bgDesc = { "bg0_0", "bg2_e6e6e6", "bg10_0",
			"bg20_0", "bg30_0", "bg40_0", "bg50_ffffff", "bg60_ffffff",
			"bg70_ffffff", "bg80_ffffff", "bg90_ffffff" };
	private final Integer[] bgColor = { Integer.valueOf(0xFF000000),
			Integer.valueOf(0xFFe6e6e6), Integer.valueOf(0xFF000000),
			Integer.valueOf(0xFF000000), Integer.valueOf(0xFF000000),
			Integer.valueOf(0xFF000000), Integer.valueOf(0xFFFFFFFF),
			Integer.valueOf(0xFFFFFFFF), Integer.valueOf(0xFFFFFFFF),
			Integer.valueOf(0xFFFFFFFF), Integer.valueOf(0xFFFFFFFF) };

	private int bgIdx = 0;
	private Typeface[] textTF;
	private int tfIdx = 0;
	private Bitmap gridMap;
	private Bitmap mPhotoMap;
	private Bitmap textMapDefault;
	private Bitmap textMap;
	private Bitmap appIntroMap;
	private List<Bitmap> bg;
	private Activity mActivity;
	private CharSequence textCS;
	private boolean resetState = true;

	public OverlayManager(Activity activity) {
		mActivity = activity;
		mPhotoMap = null;
		bg = new ArrayList<Bitmap>();
		for (final String desc : bgDesc) {
			int id = mActivity.getResources().getIdentifier(desc, "drawable",
					mActivity.getPackageName());
			Bitmap bitmapElement = ((BitmapDrawable) (mActivity.getResources()
					.getDrawable(id))).getBitmap();
			int[] elementArray = Utils.getIntArray(bitmapElement.getWidth()
					* bitmapElement.getHeight());
			bitmapElement.getPixels(elementArray, 0, bitmapElement.getWidth(),
					0, 0, bitmapElement.getWidth(), bitmapElement.getWidth());
			int[] finalArray = Utils.getIntArray((bitmapElement.getWidth() * 3)
					* (bitmapElement.getHeight() * 3));
			for (int row = 0; (row < bitmapElement.getHeight()); row++) {
				for (int i = 0; (i < 3); i++) {
					for (int j = 0; (j < 3); j++) {
						System.arraycopy(elementArray,
								(row * bitmapElement.getWidth()), finalArray,
								((i * (bitmapElement.getHeight()) + row)
										* (bitmapElement.getWidth() * 3) + j
										* bitmapElement.getWidth()),
								bitmapElement.getWidth());
					}
				}
			}
			Bitmap finalMap = Bitmap.createBitmap(finalArray,
					(bitmapElement.getWidth() * 3),
					(bitmapElement.getHeight() * 3), Bitmap.Config.ARGB_8888)
					.copy(Bitmap.Config.ARGB_8888, true);
			bg.add(finalMap);
		}
		int[] gridArray = Utils.getIntArray(bg.get(0).getWidth()
				* bg.get(0).getHeight());
		for (int i = 0; (i < (bg.get(0).getWidth() * bg.get(0).getHeight())); i++) {
			gridArray[i] = 0x00000000;
		}
		for (int i = 0; (i < bg.get(0).getHeight()); i++) {
			gridArray[i * bg.get(0).getWidth() + (bg.get(0).getWidth() / 3 - 1)] = 0xFFFFFFFF;
			gridArray[i * bg.get(0).getWidth() + (bg.get(0).getWidth() / 3)] = 0xFFFFFFFF;
			gridArray[i * bg.get(0).getWidth()
					+ (2 * (bg.get(0).getWidth() / 3) - 1)] = 0xFFFFFFFF;
			gridArray[i * bg.get(0).getWidth()
					+ (2 * (bg.get(0).getWidth() / 3))] = 0xFFFFFFFF;
		}
		for (int i = 0; (i < bg.get(0).getWidth()); i++) {
			gridArray[(bg.get(0).getHeight() / 3 - 1) * bg.get(0).getWidth()
					+ i] = 0xFFFFFFFF;
			gridArray[(bg.get(0).getHeight() / 3) * bg.get(0).getWidth() + i] = 0xFFFFFFFF;
			gridArray[(2 * (bg.get(0).getHeight() / 3) - 1)
					* bg.get(0).getWidth() + i] = 0xFFFFFFFF;
			gridArray[(2 * (bg.get(0).getHeight() / 3)) * bg.get(0).getWidth()
					+ i] = 0xFFFFFFFF;
		}
		gridMap = Bitmap.createBitmap(gridArray, bg.get(0).getWidth(), bg
				.get(0).getHeight(), Bitmap.Config.ARGB_8888);
		for (int i = 0; (i < (bg.get(0).getWidth() * bg.get(0).getHeight())); i++) {
			gridArray[i] = 0x00000000;
		}
		Bitmap bg0 = ((BitmapDrawable) (mActivity.getResources()
				.getDrawable(R.drawable.bg0))).getBitmap();
		Bitmap bg1 = ((BitmapDrawable) (mActivity.getResources()
				.getDrawable(R.drawable.bg1))).getBitmap();
		Bitmap bg8 = ((BitmapDrawable) (mActivity.getResources()
				.getDrawable(R.drawable.bg8))).getBitmap();
		appIntroMap = ((BitmapDrawable) (mActivity.getResources()
				.getDrawable(R.drawable.app_introduce))).getBitmap();
		textMapDefault = Bitmap.createBitmap(gridArray, bg.get(0).getWidth(),
				bg.get(0).getHeight(), Bitmap.Config.ARGB_8888).copy(
				Bitmap.Config.ARGB_8888, true);
		textTF = new Typeface[] {
				Typeface.DEFAULT,
				Typeface.createFromAsset(mActivity.getAssets(),
						"fonts/hkwwt.TTF"),
				Typeface.createFromAsset(mActivity.getAssets(), "fonts/xjl.ttf"),
				Typeface.createFromAsset(mActivity.getAssets(),
						"fonts/whxw.ttf"),
				Typeface.createFromAsset(mActivity.getAssets(),
						"fonts/fzjt.TTF"),
				Typeface.createFromAsset(mActivity.getAssets(), "fonts/ys.otf") };
		textCS = new String("");
		resetState = true;
	}

	public void toggleTF() {
		tfIdx++;
		if (tfIdx >= textTF.length) {
			tfIdx = 0;
		}
	}

	public void toggleBG() {
		bgIdx++;
		if (bgIdx >= bg.size()) {
			bgIdx = 0;
		}
	}

	public void reset() {
		if (mPhotoMap != null) {
			mPhotoMap.recycle();
			mPhotoMap = null;
		}
		resetState = true;
	}

	public Bitmap disableGrid(final Bitmap bp, final int i) {
		int row = i / 3;
		int col = i % 3;
		int[] tmpArray = Utils.getIntArray(bp.getWidth() * bp.getHeight());
		bp.getPixels(tmpArray, 0, bp.getWidth(), col * (bp.getWidth() / 3), row
				* (bp.getHeight() / 3), (bp.getWidth() / 3),
				(bp.getHeight() / 3));
		for (int intVal = 0; (intVal < tmpArray.length); intVal++) {
			tmpArray[intVal] = Utils.getMaskedValue(tmpArray[intVal],
					0x00FFFFFF);
		}
		bp.setPixels(tmpArray, 0, bp.getWidth(), col * (bp.getWidth() / 3), row
				* (bp.getHeight() / 3), (bp.getWidth() / 3),
				(bp.getHeight() / 3));
		return bp;
	}

	public void inputString(final CharSequence input) {
		resetState = false;
		textCS = new String(input.toString());
	}

	public Bitmap getBitmapForDraw(final boolean withGrid) {
		Drawable[] layers = null;
		Bitmap bgMap = null;
		int outputWidth = bg.get(0).getWidth();
		int outputHeight = bg.get(0).getHeight();
		if (resetState) {
			layers = new Drawable[] {
					new BitmapDrawable(mActivity.getResources(), appIntroMap),
					new BitmapDrawable(mActivity.getResources(), gridMap) };
			LayerDrawable layerDrawable = new LayerDrawable(layers);
			Bitmap b = Bitmap.createBitmap(appIntroMap.getWidth(),
					appIntroMap.getHeight(), Bitmap.Config.ARGB_8888);
			layerDrawable.setBounds(0, 0, appIntroMap.getWidth(),
					appIntroMap.getHeight());
			layerDrawable.draw(new Canvas(b));
			return b;
		}
		bgMap = bg.get(bgIdx).copy(Bitmap.Config.ARGB_8888, true);
		textMap = textMapDefault.copy(Bitmap.Config.ARGB_8888, true);
		for (int i = 0; (i < 9); i++) {
			if (i < textCS.length()
					&& !Character.isWhitespace(textCS.charAt(i))) {
				Canvas canvas = new Canvas(textMap);
				Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
				paint.setColor(bgColor[bgIdx]);
				paint.setTextSize(64);
				paint.setShadowLayer(1f, 0f, 1f, Color.TRANSPARENT);
				paint.setTypeface(textTF[tfIdx]);
				Rect bounds = new Rect();
				paint.getTextBounds(Character.valueOf(textCS.charAt(i))
						.toString(), 0, 1, bounds);
				final int row = (i / 3);
				final int col = (i % 3);
				int x = col * (bg.get(0).getWidth() / 3)
						+ ((bg.get(0).getWidth() / 3) - bounds.width()) / 2;
				int y = row * (bg.get(0).getHeight() / 3)
						+ ((bg.get(0).getHeight() / 3) + bounds.height()) / 2;
				canvas.drawText(Character.valueOf(textCS.charAt(i)).toString(),
						x, y, paint);
			} else if (i < textCS.length()
					&& Character.isWhitespace(textCS.charAt(i))) {
				bgMap = disableGrid(bgMap, i);
			} else {
				bgMap = disableGrid(bgMap, i);
			}
		}

		if (!Objects.equal(mPhotoMap, null)) {
			if (withGrid) {
				layers = new Drawable[] {
						new BitmapDrawable(mActivity.getResources(), mPhotoMap),
						new BitmapDrawable(mActivity.getResources(), bgMap),
						new BitmapDrawable(mActivity.getResources(), textMap),
						new BitmapDrawable(mActivity.getResources(), gridMap) };
			} else {
				layers = new Drawable[] {
						new BitmapDrawable(mActivity.getResources(), mPhotoMap),
						new BitmapDrawable(mActivity.getResources(), bgMap),
						new BitmapDrawable(mActivity.getResources(), textMap) };
			}
			if (mPhotoMap.getWidth() > outputWidth) {
				outputWidth = mPhotoMap.getWidth();
			}
			if (mPhotoMap.getHeight() > outputHeight) {
				outputHeight = mPhotoMap.getHeight();
			}
		} else {
			if (withGrid) {
				layers = new Drawable[] {
						new BitmapDrawable(mActivity.getResources(), bgMap),
						new BitmapDrawable(mActivity.getResources(), textMap),
						new BitmapDrawable(mActivity.getResources(), gridMap) };
			} else {
				layers = new Drawable[] {
						new BitmapDrawable(mActivity.getResources(), bgMap),
						new BitmapDrawable(mActivity.getResources(), textMap) };
			}
		}
		LayerDrawable layerDrawable = new LayerDrawable(layers);
		Bitmap b = Bitmap.createBitmap(outputWidth, outputHeight,
				Bitmap.Config.ARGB_8888);
		layerDrawable.setBounds(0, 0, outputWidth, outputHeight);
		layerDrawable.draw(new Canvas(b));
		return b;
	}

	public void recyclePhoto() {
		if (mPhotoMap != null) {
			mPhotoMap.recycle();
			mPhotoMap = null;
		}
	}

	public void setPhoto(final Bitmap photo) {
		resetState = false;
		mPhotoMap = photo;
	}
}
