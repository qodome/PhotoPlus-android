package com.dovoq.cubecandy;

import static com.nyssance.android.util.LogUtils.logi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dovoq.cubecandy.fragments.CardDetail;
import com.dovoq.cubecandy.util.CropUtils;

public class SearchActivity extends MyActivity implements Constants {
	public Context self;
	private Bitmap sharedBitmap;
	public String folderName;
	private String subFolderName;
	private int flagShareFolder = 0;

	private CardDetail singleFragment;

	private EditText mEditText;
	private Button mShareButton;

	// unpackZip(zipFolder.getAbsolutePath(),response.getEntity().getContent());

	public class QueryJPGFilesTask extends AsyncTask<String, Integer, Bitmap> {

		public Bitmap doInBackground(final String... info) {
			String url = MEDIA_URL + "/" + info[0];
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			HttpResponse response;
			Bitmap b = null;
			try {
				response = client.execute(get);
				b = null;
				if (response.getStatusLine().getStatusCode() == 200) {
					b = BitmapFactory.decodeStream(response.getEntity()
							.getContent());
				} else {
					logi("http response: "
							+ Integer.valueOf(response.getStatusLine()
									.getStatusCode()));
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return b;
		}

		public void onProgressUpdate(final Integer... progress) {
		}

		public void onPostExecute(final Bitmap b) {
			if (b == null) {
				new AlertDialog.Builder(self)
						.setTitle("错误")
						.setMessage("图片未找到")
						.setNeutralButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(
											final DialogInterface dialog,
											final int which) {
									}
								}).setIcon(android.R.drawable.ic_dialog_alert)
						.show();
			} else {
				singleFragment.setImage(b);
				sharedBitmap = b.copy(Bitmap.Config.ARGB_8888, true);
				mShareButton.setEnabled(true);
				flagShareFolder = 0;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		singleFragment = new CardDetail();
		getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, singleFragment).commit();
		mEditText = (EditText) findViewById(R.id.input_text);
		mShareButton = (Button) findViewById(R.id.repost);
	}

	private boolean isNumeric(String string) {
		try {
			long t = Long.valueOf(string);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public void search(View view) {
		mShareButton.setEnabled(false);
		String id = mEditText.getText().toString().toLowerCase(Locale.ENGLISH);
		if (id.length() < 8 || !isNumeric(id.substring(1))) {
			new AlertDialog.Builder(this)
					.setTitle("错误")
					.setMessage("ID格式错误")
					.setNeutralButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog,
										final int which) {
								}
							}).setIcon(android.R.drawable.ic_dialog_alert)
					.show();
		} else {
			new QueryJPGFilesTask()
					.execute(CropUtils.generatePath(id) + ".jpg");
		}
	}

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
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void repost(View view) {
		Bitmap bg0 = BitmapFactory.decodeResource(getResources(),
				R.drawable.bg0);
		Bitmap bg8 = BitmapFactory.decodeResource(getResources(),
				R.drawable.bg8);
		ArrayList<Uri> uris = new ArrayList<>();
		if (flagShareFolder == 0) {
			uris = CropUtils.getImages(sharedBitmap, 3, bg0, bg8,
					getResources());
		} else {
			File zipFolder = new File(folderName + subFolderName + "/");
			if (zipFolder.exists()) {
				for (File file : zipFolder.listFiles()) {
					uris.add(Uri.fromFile(file));
				}
			}
		}
		startShareActivity(uris);
	}
}
