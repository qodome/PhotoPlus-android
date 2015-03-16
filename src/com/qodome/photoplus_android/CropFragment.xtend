package com.qodome.photoplus_android

import org.xtendroid.annotations.AndroidFragment
import org.xtendroid.app.OnCreate
import android.os.Bundle
import android.graphics.Bitmap
import android.provider.MediaStore

@AndroidFragment(R.layout.fragment_crop) class CropFragment {
	public var Bitmap bitmap = null

	@OnCreate
    def init(Bundle savedInstanceState) {
        if (bitmap != null) {
        	cropImageView.setImageBitmap(bitmap)
        }
    }
}