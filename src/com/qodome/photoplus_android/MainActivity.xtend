package com.qodome.photoplus_android

import org.xtendroid.app.AndroidActivity
import android.view.View
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import android.content.ComponentName
import java.io.File
import android.net.Uri
import java.util.ArrayList
import android.util.DisplayMetrics
import android.content.Context
import android.view.WindowManager
import org.xtendroid.app.OnCreate
import android.os.Bundle
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import android.graphics.Bitmap
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.Environment
import java.io.FileOutputStream
import java.util.List
import java.util.Calendar
import java.nio.ByteBuffer
import android.graphics.drawable.BitmapDrawable
import android.text.TextWatcher
import android.text.Editable

@AndroidActivity(R.layout.activity_main) class MainActivity {
	val String sinapackage = "com.sina.weibo"
	val String sinaclassname = "com.sina.weibo.EditActivity"
	val String weixinpackage = "com.tencent.mm"
	val String weixinclassname = "com.tencent.mm.ui.tools.ShareImgUI"				//分享给好友图片
	val String pengyouquanclassname = "com.tencent.mm.ui.tools.ShareToTimeLineUI"	//分享到朋友圈的图片
	//var String folderName
	var CropFragment cropFrag
	var EditFragment editFrag
	var boolean cutFunc = false
	
	@OnCreate
    def init(Bundle savedInstanceState) {
     	var backgroundBitmap = (getResources().getDrawable(R.drawable.background1) as BitmapDrawable).getBitmap()    	
    	var blankArray = Utils.getIntArray(backgroundBitmap.getWidth() * backgroundBitmap.getHeight())
    	backgroundBitmap.getPixels(blankArray, 0, backgroundBitmap.getWidth(), 0, 248, backgroundBitmap.getWidth(), backgroundBitmap.getWidth())
    	var blankBitmap = Bitmap.createBitmap(blankArray, 0, backgroundBitmap.getWidth(), backgroundBitmap.getWidth(), backgroundBitmap.getWidth(), Bitmap.Config.ARGB_8888)

		cropFrag = new CropFragment()
		editFrag = new EditFragment()
		editFrag.c = this
		editFrag.pBlank = blankBitmap
		for (var i = 0; i < 9; i++) {
			editFrag.pChar.add(blankBitmap.copy(blankBitmap.getConfig(), true))
		}
		getFragmentManager().beginTransaction().add(R.id.fragment_container, editFrag).commit()
		
		inputText.addTextChangedListener(new TextWatcher() {
        	override afterTextChanged(Editable s) {}
        	override beforeTextChanged(CharSequence s, int start, int count, int after) {}
        	override onTextChanged(CharSequence s, int start, int before, int count) {
        		Log.i(getString(R.string.LOGTAG), "onTextChanged " + s + " " + start + " " + before + " " + count)
        		editFrag.cs = s
        		editFrag.applyText()
        		editFrag.refreshGridView()
        	}
		})
    }
	
	override loadPhoto(View v) { 
		if (cutFunc == false) {
			startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 42)			
		} else {
			var List<Bitmap> pieces = new ArrayList<Bitmap>()
			var int[] intArray
			var int width
        	var int height
		
			cropFrag.getCropImageView().queryCoordinate()
			var croppedImage = cropFrag.getCropImageView().getCroppedImage().copy(Bitmap.Config.ARGB_8888, false)
			Log.i(getString(R.string.LOGTAG), "croppedImage height: " + croppedImage.getHeight() + " width: " + croppedImage.getWidth() + " " + Calendar.getInstance().getTimeInMillis() / 1000L)
			width = croppedImage.getWidth() / 3
        	height = croppedImage.getHeight() / 3
			intArray = Utils.getIntArray(croppedImage.getWidth() * croppedImage.getHeight())

        	for (var i = 0; i < 3; i++) {
        		for (var j = 0; j < 3; j++) {
        			croppedImage.getPixels(intArray, 0, croppedImage.getWidth(), j * width, i * height, width, height)
        			var piece = Bitmap.createBitmap(intArray, 0, croppedImage.getWidth(), width, height, Bitmap.Config.ARGB_8888)
        			pieces.add(piece)
        		}
        	}
       		editFrag.updatePhoto(pieces)
			getFragmentManager().beginTransaction().replace(R.id.fragment_container, editFrag).commit()
			cutFunc = false			
		}
	}
	
	override onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == 42 && resultCode == RESULT_OK) {   		
    		// Show Crop now
    		getFragmentManager().beginTransaction().replace(R.id.fragment_container, cropFrag).commit()
    		// Change button view option
    		open.setText("CUT")
    		share.setVisibility(View.GONE)
    		cutFunc = true
    		cropFrag.bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData())		
    	}
	}	

	override share(View v) { 

	}
		
	def shareMultiplePictureToTimeLine() {
		var intent = new Intent();
		var comp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.tools.ShareToTimeLineUI");
		intent.setComponent(comp);
		intent.setAction(Intent.ACTION_SEND_MULTIPLE);
		intent.setType("image/*");
		var imageUris = new ArrayList<Uri>();
		for (var j = 0; j < 3; j++) {
			for (var i = 0; i < 3; i++) {
				//imageUris.add(Uri.fromFile(new File(folderName + "test" + i + j + ".png")))
			}
		}
		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
		startActivity(intent);
	}
}