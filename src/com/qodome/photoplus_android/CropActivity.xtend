package com.qodome.photoplus_android

import org.xtendroid.app.AndroidActivity
import android.view.View
import org.xtendroid.app.OnCreate
import android.os.Bundle
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.util.List
import java.util.ArrayList
import android.util.Log
import java.io.ByteArrayOutputStream
import android.content.Intent
import java.io.FileOutputStream
import android.content.Context

@AndroidActivity(R.layout.activity_crop) class CropActivity {
	
	@OnCreate
    def init(Bundle savedInstanceState) {
		cropImageView.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(getIntent().getStringExtra("BitmapImage"))))
    }	
	
	override crop(View v) {
		var List<Bitmap> pieces = new ArrayList<Bitmap>()
		var int[] intArray
		var int width
        var int height
		
		getCropImageView().queryCoordinate()
		var croppedImage = getCropImageView().getCroppedImage().copy(Bitmap.Config.ARGB_8888, false)
		Log.i(getString(R.string.LOGTAG), "croppedImage height: " + croppedImage.getHeight() + " width: " + croppedImage.getWidth())
		
		var fn = "croppedImage.png";
    	var stream = this.openFileOutput(fn, Context.MODE_PRIVATE);
    	croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream)
		//Cleanup
    	stream.close();
    	croppedImage.recycle()
		
		var result = new Intent(this, typeof(MainActivity))		
		result.putExtra("filename", fn)
		
		setResult(RESULT_OK, result)
		finish()
	}
}