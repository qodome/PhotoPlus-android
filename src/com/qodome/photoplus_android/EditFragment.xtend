package com.qodome.photoplus_android

import org.xtendroid.annotations.AndroidFragment
import org.xtendroid.app.OnCreate
import android.os.Bundle
import android.graphics.Bitmap
import java.util.List
import java.util.ArrayList
import org.eclipse.xtend.lib.annotations.Accessors
import org.xtendroid.adapter.BeanAdapter
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Rect
import android.util.Log

@Accessors class GridBitmaps {
  Bitmap pictureView
}

@AndroidFragment(R.layout.fragment_edit) class EditFragment {
	public var pPhotoValid = false
	public var List<Bitmap> pPhoto = new ArrayList<Bitmap>()
	public var List<Bitmap> pChar = new ArrayList<Bitmap>()
	public var Bitmap pBlank
	public var CharSequence cs = ""
	public var Context c
	
	public def applyText() {
		for (var i = 0; i < 9; i++) {
			if (i < cs.length() && cs.charAt(i) != ' ') {
		 		var canvas = new Canvas(pChar.get(i))
  				var paint = new Paint(Paint.ANTI_ALIAS_FLAG)
  				paint.setColor(Color.rgb(61, 61, 61))
  				paint.setTextSize(256)
  				paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)
 
  				var bounds = new Rect()
  				paint.getTextBounds(cs.charAt(i).toString(), 0, 1, bounds)
  				var x = (pChar.get(i).getWidth() - bounds.width())/2
  				var y = (pChar.get(i).getHeight() + bounds.height())/2

  				canvas.drawText(cs.charAt(i).toString(), x, y, paint)
			} else {
				pChar.set(i, pBlank.copy(pBlank.getConfig(), true))
			}
		}
	}
	
	def refreshGridView() {
		var List<GridBitmaps> gridList = new ArrayList<GridBitmaps>()
		for (var i = 0; i < 9; i++) {
			if (pPhotoValid == false) {
				Log.i("PhotoPlus", "1")
				var gridElement = new GridBitmaps()
				gridElement.pictureView = pChar.get(i)
				gridList.add(gridElement)
			} else {
				if (i < cs.length() && cs.charAt(i) != ' ') {
					Log.i("PhotoPlus", "2")					
					var gridElement = new GridBitmaps()
					gridElement.pictureView = pChar.get(i)
					gridList.add(gridElement)					
				} else {
					Log.i("PhotoPlus", "3")	
					var gridElement = new GridBitmaps()
					gridElement.pictureView = pPhoto.get(i)
					gridList.add(gridElement)
				}
			}
		}
		var adapter = new BeanAdapter<GridBitmaps>(c, R.layout.element_edit, gridList)
		photoGridView.adapter = adapter		
	}
	
	@OnCreate
    def init(Bundle savedInstanceState) {
		refreshGridView()
    }
    
    def updatePhoto(List<Bitmap> b) {
    	pPhoto = b
    	pPhotoValid = true
    	Log.i("PhotoPlus", "update photo")
    }
    
    override onStart () {
    	super.onStart()
    	Log.i(getString(R.string.LOGTAG), "onStart")
    	refreshGridView()
    }
}