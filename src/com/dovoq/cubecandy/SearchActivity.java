package com.dovoq.cubecandy;

import static com.nyssance.android.util.LogUtils.logi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dovoq.cubecandy.util.CropUtils;
import com.google.common.base.Objects;

public class SearchActivity extends FragmentActivity implements Constants {
	public Context self;
	private Bitmap sharedBitmap;
	public String folderName;
	private String subFolderName;
	private OverlayManager om;
	private int flagShareFolder = 0;
	private Fragment prevFragment = null;
	private SearchSingleFragment singleFragment = null;
	private SearchMultipleFragment multipleFragment = null;
	private String prevSearchString = null;

	private EditText mEditText;
	private Button mShareButton;

	public class QueryZIPFilesTask extends AsyncTask<String, Integer, String> {
		public String doInBackground(String... info) {
			String url = MEDIA_URL + "/" + info[0];
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			HttpResponse response;
			String ret = null;
			try {
				response = client.execute(get);
				if (response.getStatusLine().getStatusCode() == 200) {
					File zipFolder = new File(folderName,
							FilenameUtils.getBaseName(url));
					zipFolder.mkdirs();
					unpackZip(zipFolder.getAbsolutePath(), response.getEntity()
							.getContent());
					logi("QueryZIPFilesTask got zip file");
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
			return ret;
		}

		public void onProgressUpdate(final Integer... progress) {
		}

		public void onPostExecute(final String fn) {
			if (fn != null) {
				File zipFolder = new File(folderName + fn + "/");
				if (zipFolder.exists()) {
					if (singleFragment != null) {
						singleFragment.recycleBitmap();
						singleFragment = null;
					}
					if (multipleFragment != null) {
						multipleFragment.recycleBitmap();
						multipleFragment = null;
					}

					if (prevFragment != null) {
						getSupportFragmentManager().beginTransaction()
								.remove(prevFragment).commit();
					}
					multipleFragment = new SearchMultipleFragment();
					multipleFragment.mContext = self;
					multipleFragment.setBitmap(folderName + fn + "/");
					getSupportFragmentManager().beginTransaction()
							.add(R.id.fragment_container, multipleFragment)
							.commit();
					prevFragment = multipleFragment;
					mShareButton.setEnabled(true);
					flagShareFolder = 1;
					subFolderName = fn;
				} else {
					mShareButton.setEnabled(false);
					new AlertDialog.Builder(self)
							.setTitle("错误")
							.setMessage("图片未找到")
							.setNeutralButton(android.R.string.ok,
									new DialogInterface.OnClickListener() {
										public void onClick(
												final DialogInterface dialog,
												final int which) {
										}
									})
							.setIcon(android.R.drawable.ic_dialog_alert).show();
				}
			} else {
				if (singleFragment != null) {
					singleFragment.recycleBitmap();
					singleFragment = null;
				}
				if (multipleFragment != null) {
					multipleFragment.recycleBitmap();
					multipleFragment = null;
				}
				if (prevFragment != null) {
					getSupportFragmentManager().beginTransaction()
							.remove(prevFragment).commit();
					prevFragment = null;
				}
				mShareButton.setEnabled(false);
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
			}
		}
	}

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
			if (Objects.equal(b, null)) {
				if (singleFragment != null) {
					singleFragment.recycleBitmap();
					singleFragment = null;
				}
				if (multipleFragment != null) {
					multipleFragment.recycleBitmap();
					multipleFragment = null;
				}
				if (prevFragment != null) {
					getSupportFragmentManager().beginTransaction()
							.remove(prevFragment).commit();
					prevFragment = null;
				}
				mShareButton.setEnabled(false);
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
				if (singleFragment != null) {
					singleFragment.recycleBitmap();
					singleFragment = null;
				}
				if (multipleFragment != null) {
					multipleFragment.recycleBitmap();
					multipleFragment = null;
				}

				if (prevFragment != null) {
					getSupportFragmentManager().beginTransaction()
							.remove(prevFragment).commit();
				}
				singleFragment = new SearchSingleFragment();
				singleFragment.setBitmap(b);
				getSupportFragmentManager().beginTransaction()
						.add(R.id.fragment_container, singleFragment).commit();
				prevFragment = singleFragment;
				sharedBitmap = b.copy(Bitmap.Config.ARGB_8888, true);
				mShareButton.setEnabled(true);
				flagShareFolder = 0;
			}
		}
	}

	public boolean isNumeric(final String str) {
		try {
			long t = Long.parseLong(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public void search(final View v) {
		String prefix = mEditText.getText().toString().substring(0, 1);
		String input = mEditText.getText().toString().substring(1);
		if (input.length() != 10 || isNumeric(input) == false) {
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
			return;
		}
		if (prevSearchString == null
				|| !prevSearchString.equals(mEditText.getText().toString())) {
			if (singleFragment != null) {
				singleFragment.recycleBitmap();
				singleFragment = null;
			}
			if (multipleFragment != null) {
				multipleFragment.recycleBitmap();
				multipleFragment = null;
			}
			if (prevFragment != null) {
				getSupportFragmentManager().beginTransaction()
						.remove(prevFragment).commit();
				prevFragment = null;
			}
			prevSearchString = new String(mEditText.getText().toString());
			input = input.substring(0, (input.length() - 3));
			String folder = String.valueOf((Integer.parseInt(input) / 864000));
			String id = mEditText.getText().toString();
			if (prefix.equals("a")) {
				new QueryJPGFilesTask().execute(CropUtils.generatePath(id)
						+ ".jpg");
			} else if (prefix.equals("z")) {
				new QueryZIPFilesTask().execute(CropUtils.generatePath(id)
						+ ".zip");
			}
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

	public void share(final View v) {
		if (flagShareFolder == 0) {
			om = new OverlayManager(this);
			om.dumpSearchResultToFile(sharedBitmap);
		}
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intent.setType("image/*");
		ComponentName comp = new ComponentName("com.tencent.mm",
				"com.tencent.mm.ui.tools.ShareToTimeLineUI");
		intent.setComponent(comp);
		ArrayList<Uri> uris = new ArrayList<>();
		if (flagShareFolder == 0) {
			for (File file : TEMPORARY_DIRECTORY.listFiles()) {
				if (!file.isHidden() && file.getName().endsWith(".png")) {
					uris.add(Uri.fromFile(file));
				}
			}
		} else {
			File zipFolder = new File(folderName + subFolderName + "/");
			if (zipFolder.exists()) {
				for (File file : zipFolder.listFiles()) {
					uris.add(Uri.fromFile(file));
				}
			}
		}
		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		singleFragment = new SearchSingleFragment();
		multipleFragment = new SearchMultipleFragment();
		multipleFragment.mContext = this;
		self = this;
		prevFragment = null;
		mEditText = (EditText) findViewById(R.id.input_text);
		mShareButton = (Button) findViewById(R.id.share);
		mShareButton.setVisibility(View.VISIBLE);
	}
}
