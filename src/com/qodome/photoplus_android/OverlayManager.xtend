package com.qodome.photoplus_android

import android.graphics.Bitmap
import java.util.List
import java.util.ArrayList
import java.lang.reflect.Field
import android.util.Log
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Color

class OverlayManager {
	val String[] bgDesc = #["bg0_0", "bg2_e6e6e6", "bg10_0", "bg20_0", 
							"bg30_0", "bg40_0", "bg50_ffffff", "bg60_ffffff", 
							"bg70_ffffff", "bg80_ffffff", "bg90_ffffff"]
	val bgColor = #[0xFF000000, 0xFFe6e6e6, 0xFF000000, 0xFF000000,
							0xFF000000, 0xFF000000, 0xFFFFFFFF, 0xFFFFFFFF,
							0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF]
	var int bgIdx = 0
	var Typeface[] textTF
	var int tfIdx = 0
	var Bitmap gridMap			// FIXED
	var Bitmap photoMap
	var Bitmap textMapDefault	// FIXED
	var Bitmap textMap	
	var Bitmap bgMap
	var Bitmap bgOrigMap
	var List<Bitmap> bg
	var List<Bitmap> bgOrig
	var MainActivity parent
	var CharSequence textCS
	
	new(MainActivity activity) {
		parent = activity
		photoMap = null
		bg = new ArrayList<Bitmap>()
		bgOrig = new ArrayList<Bitmap>()
		for (String desc : bgDesc) {
			var id = parent.getResources().getIdentifier(desc, "drawable", parent.getPackageName())
			var bitmapElement = (parent.getResources().getDrawable(id) as BitmapDrawable).getBitmap()
			var elementArray = Utils.getIntArray(bitmapElement.getWidth() * bitmapElement.getHeight())
			bitmapElement.getPixels(elementArray, 0, bitmapElement.getWidth(), 0, 0, bitmapElement.getWidth(), bitmapElement.getWidth())
			var finalArray = Utils.getIntArray((bitmapElement.getWidth() * 3 + 2) * (bitmapElement.getHeight() * 3 + 2))		
			for (var row = 0; row < bitmapElement.getHeight(); row++) {
				for (var i = 0; i < 3; i++) {
					for (var j = 0; j < 3; j++) {
						System.arraycopy(elementArray, (row * bitmapElement.getWidth()), finalArray, ((i * (bitmapElement.getHeight() + 1) + row) * (bitmapElement.getWidth() * 3 + 2) + j * (bitmapElement.getWidth() + 1)), bitmapElement.getWidth())
					}
				}				
			}
			var finalMap = Bitmap.createBitmap(finalArray, (bitmapElement.getWidth() * 3 + 2), (bitmapElement.getHeight() * 3 + 2), Bitmap.Config.ARGB_8888).copy(Bitmap.Config.ARGB_8888, true)
			bg.add(finalMap)
			bgOrig.add(bitmapElement)
		}
		// Generate gridMap
		var gridArray = Utils.getIntArray(bg.get(0).getWidth() * bg.get(0).getHeight())
		for (var i = 0; i < bg.get(0).getWidth() * bg.get(0).getHeight(); i++) {
			gridArray.set(i, 0x00000000)
		}
		for (var i = 0; i < bg.get(0).getHeight(); i++) {
			gridArray.set(i * bg.get(0).getWidth() + bgOrig.get(0).getWidth(), 0xFFFFFFFF)
			gridArray.set(i * bg.get(0).getWidth() + 2 * bgOrig.get(0).getWidth() + 1, 0xFFFFFFFF)
		}
		for (var i = 0; i < bg.get(0).getWidth(); i++) {
			gridArray.set(bgOrig.get(0).getHeight() * bg.get(0).getWidth() + i, 0xFFFFFFFF)
			gridArray.set((2 * bgOrig.get(0).getHeight() + 1) * bg.get(0).getWidth() + i, 0xFFFFFFFF)
		}
		gridMap = Bitmap.createBitmap(gridArray, bg.get(0).getWidth(), bg.get(0).getHeight(), Bitmap.Config.ARGB_8888)
		
		// Generate textMapDefault
		for (var i = 0; i < bg.get(0).getWidth() * bg.get(0).getHeight(); i++) {
			gridArray.set(i, 0x00000000)
		}
		textMapDefault = Bitmap.createBitmap(gridArray, bg.get(0).getWidth(), bg.get(0).getHeight(), Bitmap.Config.ARGB_8888).copy(Bitmap.Config.ARGB_8888, true)
		textTF = #[Typeface.DEFAULT, Typeface.DEFAULT_BOLD, Typeface.MONOSPACE, Typeface.SANS_SERIF, Typeface.SERIF, Typeface.createFromAsset(parent.getAssets(), "fonts/ys.otf")]
		bgMap = bg.get(bgIdx).copy(Bitmap.Config.ARGB_8888, true)
		bgOrigMap = bgOrig.get(bgIdx)
		textCS = new String("")
	}
	
	def toggleTF() {
		tfIdx++
		if (tfIdx >= textTF.length()) {
			tfIdx = 0
		}
	}
	
	def toggleBG() {
		bgIdx++
		if (bgIdx >= bg.length()) {
			bgIdx = 0
		}
		bgMap = bg.get(bgIdx).copy(Bitmap.Config.ARGB_8888, true)
		bgOrigMap = bgOrig.get(bgIdx)
	}
	
	def disableGrid(Bitmap bp, Bitmap bpOrig, int i) {
		val row = i / 3
		val col = i % 3
		var tmpArray = Utils.getIntArray(bp.getWidth() * bp.getHeight())
    	bp.getPixels(tmpArray, 0, bp.getWidth(), col * (bpOrig.getWidth() + 1), row * (bpOrig.getHeight() + 1), bpOrig.getWidth(), bpOrig.getHeight())
    	for (var intVal = 0; intVal < tmpArray.length(); intVal++) {
    		tmpArray.set(intVal, Utils.getMaskedValue(tmpArray.get(intVal), 0x00FFFFFF))
    	}
    	bp.setPixels(tmpArray, 0, bp.getWidth(), col * (bpOrig.getWidth() + 1), row * (bpOrig.getHeight() + 1), bpOrig.getWidth(), bpOrig.getHeight())
    	return bp
	}
	
	def inputString(CharSequence input) {
		textCS = new String(input.toString())
	}
	
	def getBitmapForDraw() {
		var Drawable[] layers
		
		bgMap = bg.get(bgIdx).copy(Bitmap.Config.ARGB_8888, true)
		bgOrigMap = bgOrig.get(bgIdx)
		textMap = textMapDefault.copy(Bitmap.Config.ARGB_8888, true)		
		for (var i = 0; i < 9; i++) {
			if (i < textCS.length() && !Character.isWhitespace(textCS.charAt(i))) {
				var canvas = new Canvas(textMap)
  				var paint = new Paint(Paint.ANTI_ALIAS_FLAG)
  				paint.setColor(bgColor.get(bgIdx))
  				paint.setTextSize(64)
  				paint.setShadowLayer(1f, 0f, 1f, Color.TRANSPARENT)
  				paint.setTypeface(textTF.get(tfIdx))
 
  				var bounds = new Rect()
  				paint.getTextBounds(textCS.charAt(i).toString(), 0, 1, bounds)
  				val row = i / 3
				val col = i % 3
  				var x = col * (bgOrig.get(0).getWidth() + 1) + (bgOrig.get(0).getWidth() - bounds.width())/2
  				var y = row * (bgOrig.get(0).getHeight() + 1) + (bgOrig.get(0).getHeight() + bounds.height())/2

  				canvas.drawText(textCS.charAt(i).toString(), x, y, paint)
			} else if (i < textCS.length() && Character.isWhitespace(textCS.charAt(i))) {
		 		bgMap = disableGrid(bgMap, bgOrigMap, i)
			}
		}
		if (photoMap != null) {
			layers = #[new BitmapDrawable(parent.getResources(), photoMap), new BitmapDrawable(parent.getResources(), bgMap), new BitmapDrawable(parent.getResources(), textMap), new BitmapDrawable(parent.getResources(), gridMap)]
		} else {
			layers = #[new BitmapDrawable(parent.getResources(), bgMap), new BitmapDrawable(parent.getResources(), textMap), new BitmapDrawable(parent.getResources(), gridMap)]
		}
    	var layerDrawable = new LayerDrawable(layers)
		var b = Bitmap.createBitmap(bg.get(0).getWidth(), bg.get(0).getHeight(), Bitmap.Config.ARGB_8888)
		layerDrawable.setBounds(0, 0, bg.get(0).getWidth(), bg.get(0).getHeight())
		layerDrawable.draw(new Canvas(b))
		return b
	}
	
	def setPhoto(Bitmap photo) {
		photoMap = photo
	}
}