package com.dovoq.cubecandy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Bundle;

import com.dovoq.cubecandy.fragments.CardDetail;
import com.nyssance.android.app.BaseActivity;

public class SearchActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, new CardDetail()).commit();
	}

	// unpackZip(zipFolder.getAbsolutePath(),response.getEntity().getContent());
	private boolean unpackZip(String path, InputStream is) {
		ZipInputStream zis;
		try {
			String filename;
			zis = new ZipInputStream(is);
			ZipEntry ze;
			byte[] buffer = new byte[1024];
			int count;
			while ((ze = zis.getNextEntry()) != null) {
				filename = ze.getName();
				// Need to create directories if not exists, or
				// it will generate an Exception...
				if (ze.isDirectory()) {
					File fmd = new File(path + filename);
					fmd.mkdirs();
					continue;
				}
				FileOutputStream fout = new FileOutputStream(path + filename);

				while ((count = zis.read(buffer)) != -1) {
					fout.write(buffer, 0, count);
				}
				fout.close();
				zis.closeEntry();
			}
			zis.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
