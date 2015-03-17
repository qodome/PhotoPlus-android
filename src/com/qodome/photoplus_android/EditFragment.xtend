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

@AndroidFragment(R.layout.fragment_edit) class EditFragment {
	var Bitmap show = null
	var Boolean initDone = false
	
	def setBitmap(Bitmap b) {
		show = b
		if (initDone == true) {
			getPhotoGridView().setImageBitmap(show)
		}
	}
	
	@OnCreate
    def init(Bundle savedInstanceState) {
		if (show != null) {
			getPhotoGridView().setImageBitmap(show)
		}
		initDone = true
    }
}