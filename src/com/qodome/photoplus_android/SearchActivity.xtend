package com.qodome.photoplus_android

import org.xtendroid.app.AndroidActivity
import android.view.View
import android.os.AsyncTask
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import android.util.Log
import com.google.common.io.ByteStreams
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.ComponentName
import java.util.ArrayList
import android.net.Uri
import java.io.File
import org.xtendroid.app.OnCreate
import android.os.Bundle
import android.os.Environment

@AndroidActivity(R.layout.activity_search) class SearchActivity {
	var Bitmap sharedBitmap
	var String folderName
	var OverlayManager om
	
	@OnCreate
    def init(Bundle savedInstanceState) {
    	folderName = new String(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PhotoPlus/")
    }
    
	def isNumeric(String str) {  
  		try {  
    		var t = Integer.parseInt(str);  
  		} catch(NumberFormatException nfe) {  
    		return false;  
  		}  
  		return true;  
	}
	
	override search(View v) {
		var input = inputText.getText().toString().substring(1)
		if (input.length() != 10 || isNumeric(input) == false) {
     		new AlertDialog.Builder(this)
               	.setTitle("错误")
                .setMessage("ID格式错误")
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    override onClick(DialogInterface dialog, int which) { 
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
            return
		}
		input = input.substring(0, (input.length() - 3))
		var folder = String.valueOf((Integer.parseInt(input) / 864000))
		new QueryFilesTask(this).execute(folder, inputText.getText().toString())
	}
	
	override share(View v) {
		om = new OverlayManager(this)
		om.dumpSearchResultToFile(sharedBitmap)
		
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
	}
	
	static class QueryFilesTask extends AsyncTask<String, Integer, Bitmap> {
		var SearchActivity searchUI
		new(SearchActivity activity) {
			searchUI = activity
		}
    	override doInBackground(String... info) {
        	// Send http get request
        	val folder = info.get(0)
        	val fileName = info.get(1) + ".jpg"
        	var client = new DefaultHttpClient()
        	var get = new HttpGet("http://media.qodome.com/photoplus/free/" + folder + "/" + fileName)
			var response = client.execute(get)
			var Bitmap b = null
		
			if (response.getStatusLine().getStatusCode() == 200) {
				b = BitmapFactory.decodeStream(response.getEntity().getContent())
			} else {
				Log.i("PhotoPlus", "http response: " + response.getStatusLine().getStatusCode())
			}
        	return b
     	}
     	override onProgressUpdate(Integer... progress) {
     	}
     	override onPostExecute(Bitmap b) {
     		(searchUI.findViewById(R.id.photo_query_result) as SquareImageView).setImageBitmap(b)    		
     		if (b == null) {
     			searchUI.getShare().setVisibility(View.GONE)
     			new AlertDialog.Builder(searchUI)
               		.setTitle("错误")
                	.setMessage("图片未找到")
                	.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                	    override onClick(DialogInterface dialog, int which) { 
                	    }
                	 })
                	.setIcon(android.R.drawable.ic_dialog_alert)
                	.show()
     		} else {
     			searchUI.sharedBitmap = b.copy(Bitmap.Config.ARGB_8888, true)
     			searchUI.getShare().setVisibility(View.VISIBLE)
     		}
     	}
 	}
}