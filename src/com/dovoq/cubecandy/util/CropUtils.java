package com.dovoq.cubecandy.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;

import org.apache.commons.io.FilenameUtils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;

import com.dovoq.cubecandy.Constants;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class CropUtils implements Constants {
	public static String generateId(String type) { // 东八时区 2015-03-01 00:00:00 起
													// 1425139200
		return type + (System.currentTimeMillis() - 1425168000000L);
	}

	public static String generatePath(String filename) { // 十天一个文件夹
		String id = FilenameUtils.getBaseName(filename);
		return "photoplus/free/"
				+ Integer.valueOf(id.substring(1, id.length() - 3)) / 864000
				+ "/" + filename;
	}

	/** 创建二维码 */
	public static Bitmap getQRCode(String contents, BarcodeFormat format,
			int width, int height) {
		EnumMap<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(
				EncodeHintType.class);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN, Integer.valueOf(0));
		MultiFormatWriter writer = new MultiFormatWriter();
		BitMatrix result = null;
		try {
			result = writer.encode(contents, format, width, height, hints);
		} catch (WriterException e) {
		}
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
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
					file.delete();
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
