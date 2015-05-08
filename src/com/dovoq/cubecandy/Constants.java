package com.dovoq.cubecandy;

import java.io.File;

import android.os.Environment;

public interface Constants {
	File TEMPORARY_DIRECTORY = new File(
			Environment.getExternalStorageDirectory(), "PhotoPlus");

	String DOMAIN = "dovoq.com";
	String API_VERSION = "api/v1";

	String DEFAULT_TOKEN = "e91c2126c2a9ba77000a932fc11a85bbb29e8f54";

	String BASE_URL = "http://" + DOMAIN;
	String MEDIA_URL = "http://media." + DOMAIN;
}
