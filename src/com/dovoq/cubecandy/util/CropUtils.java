package com.dovoq.cubecandy.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;

import com.dovoq.cubecandy.Constants;

public class CropUtils implements Constants {
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

	public static ArrayList<Uri> getImages(Bitmap image, int count, Bitmap bg0,
			Bitmap bg8, Resources res) {
		int width = image.getWidth();
		int side = width / count;
		int top = (image.getHeight() - width) / 2; // 只考虑竖版
		ArrayList<Uri> uris = new ArrayList<>();
		int i = 0;
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				Bitmap bg;
				if (i == count * count - 1) { // 最后一张
					bg = bg8;
				} else { // 中间的循环出现
					bg = i % 2 == 0 ? bg0 : image;
				}
				Bitmap content = Bitmap.createBitmap(image, x * side, top + y
						* side, side, side);
				Bitmap card = BitmapUtils.merge(res, bg, content, 0, top,
						width, width);
				try {
					File file = new File(TEMPORARY_DIRECTORY, "plus/" + i
							+ ".jpg");
					file.getParentFile().mkdirs();
					FileOutputStream out = new FileOutputStream(file);
					card.compress(Bitmap.CompressFormat.JPEG, 100, out); // 让微信压缩
					out.close();
					uris.add(Uri.fromFile(file));
				} catch (IOException e) {
				}
				i++;
			}
		}
		return uris;
	}
}
