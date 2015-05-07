package com.dovoq.cubecandy.util;

import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import android.net.Uri;

public class CropUtils {
	public static String generateId(String type) { // 东八时区 2015-03-01 00:00:00 起
													// 1425168000
		return type + (System.currentTimeMillis() - 1425139200000L);
	}

	public static String generatePath(String filename) { // 十天一个文件夹
		String id = FilenameUtils.getBaseName(filename);
		return "photoplus/free/"
				+ Integer.valueOf(id.substring(1, id.length() - 3)) / 864000
				+ "/" + filename;
	}

	public static ArrayList<Uri> getImages() {
		return null;
	}
}
