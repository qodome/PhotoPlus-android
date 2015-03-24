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
import android.os.AsyncTask
import java.net.URL
import android.preference.PreferenceManager
import android.content.SharedPreferences
import android.view.Window
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.GestureDetector
import android.widget.ImageView
import android.view.View.OnTouchListener
import android.widget.ViewFlipper
import android.view.animation.AnimationUtils
import android.graphics.BitmapFactory
import java.io.FileInputStream
import android.widget.CompoundButton

@AndroidActivity(R.layout.activity_main) class MainActivity implements 
        GestureDetector.OnGestureListener {
	val String sinapackage = "com.sina.weibo"
	val String sinaclassname = "com.sina.weibo.EditActivity"
	val String weixinpackage = "com.tencent.mm"
	val String weixinclassname = "com.tencent.mm.ui.tools.ShareImgUI"				//分享给好友图片
	val String pengyouquanclassname = "com.tencent.mm.ui.tools.ShareToTimeLineUI"	//分享到朋友圈的图片
	//var String folderName
	var EditFragment editFrag
	var String inputString
	var boolean cutFunc = false
	var OverlayManager om
	var String folderName
	var GestureDetector gdt
	var welcomeNames = #["welcome_1", "welcome_2", "welcome_3", "welcome_4", "welcome_5"]
	var List<Bitmap> welcomes
	var int welcomeIdx
	val static SWIPE_MIN_DISTANCE = 120
	val static SWIPE_THRESHOLD_VELOCITY = 200
	var SharedPreferences sp
			
	def EditFragment newEditFrag() {
		var frag = new EditFragment()
		return frag
	}
		
    override onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    	Log.i("PhotoPlus","onFling event");
        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
            Log.i("PhotoPlus", "turn left")
            welcomeIdx++
            if (welcomeIdx >= welcomes.length()) {
            	init()
            } else {
            	(findViewById(R.id.view_flipper) as ViewFlipper).setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_in)) 
            	(findViewById(R.id.view_flipper) as ViewFlipper).setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_out))
            	(findViewById(R.id.view_flipper) as ViewFlipper).showNext()            	
            }            
            return true;
        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
            Log.i("PhotoPlus", "turn right")
            if (welcomeIdx > 0) {
            	welcomeIdx--
            	(findViewById(R.id.view_flipper) as ViewFlipper).setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_in));  
            	(findViewById(R.id.view_flipper) as ViewFlipper).setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_out));  
            	(findViewById(R.id.view_flipper) as ViewFlipper).showPrevious()            	
            }
            return true;
        }
        return false;
    }
	
	def init() {
		setContentView(R.layout.activity_main)
    	
    	om = new OverlayManager(this)
		inputString = new String("")
		editFrag = newEditFrag()
		editFrag.setBitmap(om.getBitmapForDraw(true))
		getFragmentManager().beginTransaction().add(R.id.fragment_container, editFrag).commit()
		
		inputText.addTextChangedListener(new TextWatcher() {
        	override afterTextChanged(Editable s) {}
        	override beforeTextChanged(CharSequence s, int start, int count, int after) {}
        	override onTextChanged(CharSequence s, int start, int before, int count) {
        		Log.i(getString(R.string.LOGTAG), "onTextChanged " + s + " " + start + " " + before + " " + count)
        		om.inputString(s)
        		editFrag.setBitmap(om.getBitmapForDraw(true))
        	}
		})
		
		// Storage
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Log.e(getString(R.string.LOGTAG), "External storage not mounted")
	        return
	    }
		
		var reportFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "PhotoPlus")
		if (!reportFolder.exists()) {
			Log.i(getString(R.string.LOGTAG), "Creating missing directory iDoStatsMonitor")
			reportFolder.mkdirs()
		}
		folderName = new String(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PhotoPlus/")
		
		// 上传并分享选项
		sp = getSharedPreferences("PhotoPlusPreference", MODE_PRIVATE)
		if (sp.getBoolean("enable_share", false)) {
			enableShare.setChecked(true)
		} else {
			enableShare.setChecked(false)
		}
		
		enableShare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			override onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				var ed = sp.edit()
				ed.putBoolean("enable_share", isChecked)
				ed.commit()
			}
		})
	}
	
	override onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	PreferenceManager.setDefaultValues(this, "PhotoPlusPreference", MODE_PRIVATE, R.xml.preferences, false)
    	var sp = getSharedPreferences("PhotoPlusPreference", MODE_PRIVATE)
		if (sp?.getBoolean("first_time_init", false)) {
			var ed = sp.edit()
			ed.putBoolean("first_time_init", false)
			ed.commit()
			Log.i(getString(R.string.LOGTAG), "First time run, show welcome screens")
			     
        	setContentView(R.layout.welcome)
        	
        	gdt = new GestureDetector(this)

        	welcomeIdx = 0
        	welcomes = new ArrayList<Bitmap>()
        	for (String desc : welcomeNames) {
				var id = getResources().getIdentifier(desc, "drawable", getPackageName())
				welcomes.add((getResources().getDrawable(id) as BitmapDrawable).getBitmap())
        	}
        	
			(findViewById(R.id.view_flipper) as ViewFlipper).setOnTouchListener(new OnTouchListener() {
        		override onTouch(View view, MotionEvent event) {
            		gdt.onTouchEvent(event);
            		return true
        		}})
        	for (var idx = 0; idx < welcomes.length(); idx++) {
        		var img = new ImageView(this)
        		img.setImageBitmap(welcomes.get(idx))
        		img.setScaleType(ImageView.ScaleType.CENTER_CROP)
        		(findViewById(R.id.view_flipper) as ViewFlipper).addView(img)	
        	}        	
        	
        	return
		}
		
		init()    	
  	}
	
	override font(View v) {
		om.toggleTF()
		editFrag.setBitmap(om.getBitmapForDraw(true))
	}
	
	override background(View v) {
		om.toggleBG()
		editFrag.setBitmap(om.getBitmapForDraw(true))
	}
	
	override loadPhoto(View v) { 
		startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 42)
	}
	
	override onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == 42 && resultCode == RESULT_OK) {
    		Log.i("PhotoPlus", "onActivityResult 42")
    		var intent = new Intent(this, typeof(CropActivity))
			intent.putExtra("BitmapImage", data.getData().toString())   		
    		startActivityForResult(intent, 422)	
    	} else if (requestCode == 422) {
    		Log.i("PhotoPlus", "onActivityResult 422")
    		
    		var fn = data.getStringExtra("filename")
    		var is = this.openFileInput(fn);
    		var bmp = BitmapFactory.decodeStream(is)
    		om.setPhoto(bmp)
        	//editFrag = newEditFrag()
        	editFrag.setBitmap(om.getBitmapForDraw(true))
    	} else if (requestCode == 4242 && resultCode == RESULT_OK) {
        	var intent = new Intent(this, typeof(CropActivity));
			intent.putExtra("BitmapImage", Uri.fromFile(new File(folderName + "capture.jpg")).toString())   		
    		startActivityForResult(intent, 422)
    	}
	}	

	def getFolderName() {
		var c = Calendar.getInstance()
    	var sec = (c.getTimeInMillis() + c.getTimeZone().getOffset(c.getTimeInMillis())) / 1000L
    	return String.valueOf(((sec - 1425168000) / 864000)) 
	}

	override share(View v) {
		var uploadFn = om.dumpToFile(sp.getBoolean("enable_share", false))
		
		var intent = new Intent();
		var comp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.tools.ShareToTimeLineUI");
		intent.setComponent(comp);
		intent.setAction(Intent.ACTION_SEND_MULTIPLE);
		intent.setType("image/*");
		var imageUris = new ArrayList<Uri>();
		for (var i = 0; i < 3; i++) {
			for (var j = 0; j < 3; j++) {
				imageUris.add(Uri.fromFile(new File(folderName + "test" + i + j + ".png")))
			}
		}
		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
		startActivity(intent);
		if (sp.getBoolean("enable_share", false)) {
			new UploadFilesTask().execute(folderName, getFolderName, uploadFn)	
		}
	}
	
	override search(View v) {
		startActivity(new Intent(this, typeof(SearchActivity)))
	}
	
	override camera(View v) {
    	var takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(folderName + "capture.jpg")))
        startActivityForResult(takePictureIntent, 4242);
	}
	
	static class UploadFilesTask extends AsyncTask<String, Integer, Long> {
    	override doInBackground(String... info) {
        	HttpHelper.upload(info.get(0), info.get(1), info.get(2))
        	return 0L
     	}
     	override onProgressUpdate(Integer... progress) {
     	}
     	override onPostExecute(Long result) {
     	}
 	}
		
	override onDown(MotionEvent e) {return false}
	override onLongPress(MotionEvent e) {}
	override onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {return false}
	override onShowPress(MotionEvent e) {}
	override onSingleTapUp(MotionEvent e) {return false}
		
}