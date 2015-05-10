package com.dovoq.cubecandy.util;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;

public class FileUtils {

	public static ArrayList<String> listAssets(Context context, String path) {
		ArrayList<String> items = new ArrayList<>();
		try {
			for (String s : context.getAssets().list(path)) {
				items.add(path + "/" + s);
			}
		} catch (IOException e) {
		}
		return items;
	}
}
