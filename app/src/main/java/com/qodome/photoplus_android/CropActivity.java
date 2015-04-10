package com.qodome.photoplus_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CropActivity extends Activity {

    public void init(final Bundle savedInstanceState) {
    	try {
			Bitmap b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(getIntent().getStringExtra("BitmapImage")));
			int width = 0, height = 0;
			int sizeLimit = Utils.getMaximumTextureSize() / 2;
	    	if (b.getWidth() > sizeLimit || b.getHeight() > sizeLimit) {
	    		if (b.getWidth() > b.getHeight()) {
	    			width = sizeLimit;
	            	double scaleFactor = (double)b.getWidth() / (double)sizeLimit;
	            	height = (int)(b.getHeight() / scaleFactor);
	    		} else {
	    			height = sizeLimit;
	            	double scaleFactor = (double)b.getHeight() / (double)sizeLimit;
	            	width = (int)(b.getWidth() / scaleFactor);
	    		}
	            b = Bitmap.createScaledBitmap(b, width, height, false);
	    	}
			this.getCropImageView().setFixedAspectRatio(true);
			this.getCropImageView().setImageBitmap(b);
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void crop(final View v) {
        Bitmap croppedImage = getCropImageView().getCroppedImage().copy(Bitmap.Config.ARGB_8888, false);
        String fn = "croppedImage.png";
        FileOutputStream stream;
		try {
			stream = this.openFileOutput(fn, Context.MODE_PRIVATE);
	        croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			stream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        croppedImage.recycle();
        Intent result = new Intent(this, MainActivity.class);
        result.putExtra("filename", fn);
        this.setResult(Activity.RESULT_OK, result);
        this.finish();
    }

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        init(savedInstanceState);
    }

    public CropImageView getCropImageView() {
        return (CropImageView) findViewById(R.id.crop_image_view);
    }
}
