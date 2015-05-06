package com.dovoq.photoplus;

import android.graphics.Rect;
import android.view.View;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class Utils {

	public Rect locateView(View view) {
		Rect rect = new Rect();
		int[] location = new int[2];
		if (view != null) {
			view.getLocationOnScreen(location);
			rect.left = location[0];
			rect.top = location[1];
			rect.right = rect.left + view.getWidth();
			rect.bottom = rect.top + view.getHeight();
		}
		return rect;
	}

	public static int[] getIntArray(int size) {
		return new int[size];
	}

	public static int getMaskedValue(int i, int mask) {
		return i & mask;
	}
	
	public static int getMaximumTextureSize() {
	    EGL10 egl = (EGL10)EGLContext.getEGL();
	    EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
	 
	    // Initialise
	    int[] version = new int[2];
	    egl.eglInitialize(display, version);
	 
	    // Query total number of configurations
	    int[] totalConfigurations = new int[1];
	    egl.eglGetConfigs(display, null, 0, totalConfigurations);
	 
	    // Query actual list configurations
	    EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
	    egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);
	 
	    int[] textureSize = new int[1];
	    int maximumTextureSize = 0;
	 
	    // Iterate through all the configurations to located the maximum texture size
	    for (int i = 0; i < totalConfigurations[0]; i++)
	    {
	        // Only need to check for width since opengl textures are always squared
	        egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);
	 
	        // Keep track of the maximum texture size
	        if (maximumTextureSize < textureSize[0])
	        {
	            maximumTextureSize = textureSize[0];
	        }
	 
	        //Log.i("GLHelper", Integer.toString(textureSize[0]));
	    }
	 
	    // Release
	    egl.eglTerminate(display);
	    //Log.i("GLHelper", "Maximum GL texture size: " + Integer.toString(maximumTextureSize));
	 
	    return maximumTextureSize;
	}
}
