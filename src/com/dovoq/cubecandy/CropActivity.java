package com.dovoq.cubecandy;

import static com.nyssance.android.util.LogUtils.logi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.edmodo.cropper.CropImageView;

public class CropActivity extends FragmentActivity {
	private static Bitmap previousBitmap = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crop);
		try {
			if (previousBitmap != null) {
				logi("recycle previous bitmap");
				previousBitmap.recycle();
				previousBitmap = null;
			} else {
				logi("previous null");
			}
			Bitmap b = MediaStore.Images.Media.getBitmap(getContentResolver(),
					Uri.parse(getIntent().getStringExtra("BitmapImage")));
			Bitmap scaledBitmap = null;
			int width = 0, height = 0;
			int sizeLimit = getMaximumTextureSize() / 2;
			if (b.getWidth() > sizeLimit || b.getHeight() > sizeLimit) {
				if (b.getWidth() > b.getHeight()) {
					width = sizeLimit;
					double scaleFactor = (double) b.getWidth()
							/ (double) sizeLimit;
					height = (int) (b.getHeight() / scaleFactor);
				} else {
					height = sizeLimit;
					double scaleFactor = (double) b.getHeight()
							/ (double) sizeLimit;
					width = (int) (b.getWidth() / scaleFactor);
				}
				scaledBitmap = Bitmap.createScaledBitmap(b, width, height,
						false);
				b.recycle();
			} else {
				scaledBitmap = b;
			}
			getCropImageView().setFixedAspectRatio(true);
			getCropImageView().setImageBitmap(scaledBitmap);
			previousBitmap = scaledBitmap;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	public void crop(final View v) {
		Bitmap croppedImage = getCropImageView().getCroppedImage().copy(
				Bitmap.Config.ARGB_8888, false);
		String fn = "croppedImage.png";
		FileOutputStream stream = null;
		try {
			stream = openFileOutput(fn, MODE_PRIVATE);
			croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		} catch (FileNotFoundException e) {
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
		croppedImage.recycle();
		Intent result = new Intent(this, MainActivity.class);
		result.putExtra("filename", fn);
		setResult(RESULT_OK, result);
		finish();
	}

	public CropImageView getCropImageView() {
		return (CropImageView) findViewById(R.id.crop_image_view);
	}

	private int getMaximumTextureSize() {
		EGL10 egl = (EGL10) EGLContext.getEGL();
		EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

		// Initialise
		int[] version = new int[2];
		egl.eglInitialize(display, version);

		// Query total number of configurations
		int[] totalConfigurations = new int[1];
		egl.eglGetConfigs(display, null, 0, totalConfigurations);

		// Query actual list configurations
		EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
		egl.eglGetConfigs(display, configurationsList, totalConfigurations[0],
				totalConfigurations);

		int[] textureSize = new int[1];
		int maximumTextureSize = 0;

		// Iterate through all the configurations to located the maximum texture
		// size
		for (int i = 0; i < totalConfigurations[0]; i++) {
			// Only need to check for width since opengl textures are always
			// squared
			egl.eglGetConfigAttrib(display, configurationsList[i],
					EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

			// Keep track of the maximum texture size
			if (maximumTextureSize < textureSize[0]) {
				maximumTextureSize = textureSize[0];
			}

			// Log.i("GLHelper", Integer.toString(textureSize[0]));
		}

		// Release
		egl.eglTerminate(display);
		// Log.i("GLHelper", "Maximum GL texture size: " +
		// Integer.toString(maximumTextureSize));

		return maximumTextureSize;
	}
}
