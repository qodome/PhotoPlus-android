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
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.util.Map
import java.util.EnumMap

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
	var List<Bitmap> cardFrame
	var MainActivity parent
	var CharSequence textCS
	var String folderName
	
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
		
		// Generate card frame
		var template_left = (parent.getResources().getDrawable(R.drawable.template_left) as BitmapDrawable).getBitmap()
		var template_right = (parent.getResources().getDrawable(R.drawable.template_right) as BitmapDrawable).getBitmap()
		var template_share = (parent.getResources().getDrawable(R.drawable.template_share) as BitmapDrawable).getBitmap()
		cardFrame = #[template_left, template_share, template_left,
					  template_share, template_left, template_share,
					  template_left, template_share, template_right]		
		
		textMapDefault = Bitmap.createBitmap(gridArray, bg.get(0).getWidth(), bg.get(0).getHeight(), Bitmap.Config.ARGB_8888).copy(Bitmap.Config.ARGB_8888, true)
		textTF = #[Typeface.DEFAULT, Typeface.DEFAULT_BOLD, Typeface.MONOSPACE, Typeface.SANS_SERIF, Typeface.SERIF, Typeface.createFromAsset(parent.getAssets(), "fonts/ys.otf")]
		bgMap = bg.get(bgIdx).copy(Bitmap.Config.ARGB_8888, true)
		bgOrigMap = bgOrig.get(bgIdx)
		textCS = new String("")
		folderName = new String(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PhotoPlus/")
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
	
	def getBitmapForDraw(boolean withGrid) {
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
			if (withGrid == true) {
				layers = #[new BitmapDrawable(parent.getResources(), photoMap), new BitmapDrawable(parent.getResources(), bgMap), new BitmapDrawable(parent.getResources(), textMap), new BitmapDrawable(parent.getResources(), gridMap)]				
			} else {
				layers = #[new BitmapDrawable(parent.getResources(), photoMap), new BitmapDrawable(parent.getResources(), bgMap), new BitmapDrawable(parent.getResources(), textMap)]
			}
		} else {
			if (withGrid == true) {
				layers = #[new BitmapDrawable(parent.getResources(), bgMap), new BitmapDrawable(parent.getResources(), textMap), new BitmapDrawable(parent.getResources(), gridMap)]
			} else {
				layers = #[new BitmapDrawable(parent.getResources(), bgMap), new BitmapDrawable(parent.getResources(), textMap)]
			}
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
	
	def dumpToFile() {
		var b = Bitmap.createScaledBitmap(getBitmapForDraw(false), (640 * 3), (640 * 3), false)
		var intArray = Utils.getIntArray(b.getWidth() * b.getHeight())
		var barArray = Utils.getIntArray(192 * 192)
		b.getPixels(intArray, 0, b.getWidth(), 0, 0, (640 * 3), (640 * 3))
		for (var i = 0; i < 3; i++) {
        	for (var j = 0; j < 3; j++) {
        		var output = cardFrame.get((i * 3) + j).copy(Bitmap.Config.ARGB_8888, true)
				output.setPixels(intArray, (i * 640 * 640 * 3 + j * 640), (640 * 3), 0, 248, 640, 640)
				if (((i * 3) + j) % 2 == 1) {
					var barcode = encodeAsBitmap("a10000000", BarcodeFormat.QR_CODE, 192, 192)
					barcode.getPixels(barArray, 0, barcode.getWidth(), 0, 0, 192, 192)
					output.setPixels(barArray, 0, 192, 30, 914, 192, 192)					
				}				
				var fn = new File(folderName + "test" + i + j + ".png")
				if (!fn.exists()) {
					fn.createNewFile()
				}
				var out = new FileOutputStream(folderName + "test" + i + j + ".png")
    			output.compress(Bitmap.CompressFormat.PNG, 100, out)
    			out.close()
        	}
        }
	}
	
	/////////////////////// Bar code /////////////////////////
    def encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) {
    	var encoding = "UTF-8"
        var hints = new EnumMap<EncodeHintType, Object>(typeof(EncodeHintType))
        hints.put(EncodeHintType.CHARACTER_SET, encoding)
        hints.put(EncodeHintType.MARGIN, 0)
    	var writer = new MultiFormatWriter()
    	var BitMatrix result = writer.encode(contents, format, img_width, img_height, hints)
    	var width = result.getWidth()
    	var height = result.getHeight()
    	var pixels = Utils.getIntArray(width * height)
    	var int value
    	for (var y = 0; y < height; y++) {
        	var offset = y * width
        	for (var x = 0; x < width; x++) {
        		if (result.get(x, y)) {
        			value = 0xFF000000
        		} else {
        			value = 0xFFFFFFFF
        		}
        		pixels.set((offset + x), value)
        	}
    	}

    	var ret = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    	ret.setPixels(pixels, 0, width, 0, 0, width, height)
    	return ret
    }
}