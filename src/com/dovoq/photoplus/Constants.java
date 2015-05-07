package com.dovoq.photoplus;

import java.io.File;

import android.os.Environment;

public interface Constants {
	File TEMPORARY_DIRECTORY = new File(Environment.getExternalStorageDirectory(),
			"PhotoPlus");
}
