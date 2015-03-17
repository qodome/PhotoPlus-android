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

class OverlayManager {
	val String[] bgDesc = #["bg0_0", "bg2_e6e6e6", "bg10_0", "bg20_0", 
							"bg30_0", "bg40_0", "bg50_ffffff", "bg60_ffffff", 
							"bg70_ffffff", "bg80_ffffff", "bg90_ffffff"]
	var Bitmap gridMap
	var Bitmap photoMap
	var Bitmap bgMap
	var Bitmap bgOrigMap
	var int bgIdx = 0
	var List<Bitmap> bg
	var List<Bitmap> bgOrig
	var MainActivity parent
	
	new(MainActivity activity) {
		parent = activity
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
			gridArray.set(i * bg.get(0).getWidth() + bgOrig.get(0).getWidth(), 0xFF000000)
			gridArray.set(i * bg.get(0).getWidth() + 2 * bgOrig.get(0).getWidth() + 1, 0xFF000000)
		}
		for (var i = 0; i < bg.get(0).getWidth(); i++) {
			gridArray.set(bgOrig.get(0).getHeight() * bg.get(0).getWidth() + i, 0xFF000000)
			gridArray.set((2 * bgOrig.get(0).getHeight() + 1) * bg.get(0).getWidth() + i, 0xFF000000)
		}
		gridMap = Bitmap.createBitmap(gridArray, bg.get(0).getWidth(), bg.get(0).getHeight(), Bitmap.Config.ARGB_8888)
	}
	
	def getBG(int idx) {
		if (idx < bg.length()) {
			bgIdx = idx
			bgMap = bg.get(bgIdx).copy(Bitmap.Config.ARGB_8888, true)
			bgOrigMap = bgOrig.get(bgIdx)
			return bgMap
		}
		return null
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
		bgMap = bg.get(bgIdx).copy(Bitmap.Config.ARGB_8888, true)
		bgOrigMap = bgOrig.get(bgIdx)
		for (var i = 0; i < 9; i++) {
			if (i < input.length() && Character.isWhitespace(input.charAt(i))) {
		 		bgMap = disableGrid(bgMap, bgOrigMap, i)
			}
		}
	}
	
	def getBitmapForDraw() {
		var layers = #[new BitmapDrawable(parent.getResources(), gridMap), new BitmapDrawable(parent.getResources(), bgMap)] as Drawable[]
    	var layerDrawable = new LayerDrawable(layers)
		var b = Bitmap.createBitmap(bg.get(0).getWidth(), bg.get(0).getHeight(), Bitmap.Config.ARGB_8888)
		layerDrawable.setBounds(0, 0, bg.get(0).getWidth(), bg.get(0).getHeight())
		layerDrawable.draw(new Canvas(b))
		return b
	}
}