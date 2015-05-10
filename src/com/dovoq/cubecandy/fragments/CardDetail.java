package com.dovoq.cubecandy.fragments;

import static com.dovoq.cubecandy.util.CropUtils.generatePath;
import static com.dovoq.cubecandy.util.CropUtils.getImages;
import static com.nyssance.android.util.LogUtils.logi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.dovoq.cubecandy.R;

public class CardDetail extends MyFragment {
	private Bitmap sharedBitmap;
	private String subFolderName;
	private int flagShareFolder = 0;

	@InjectView(android.R.id.input)
	EditText mEditText;
	@InjectView(R.id.image)
	ImageView mPhoto;
	@InjectView(R.id.action_item_repost)
	Button mRepostButton;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_card_detail, container,
				false);
		ButterKnife.inject(this, view);
		return view;
	}

	@OnClick(R.id.action_search)
	void search(View view) {
		mRepostButton.setEnabled(false);
		String id = mEditText.getText().toString().toLowerCase(Locale.ENGLISH);
		if (id.length() < 8 || !isNumeric(id.substring(1))) {
			new AlertDialog.Builder(getActivity())
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
			new QueryJPGFilesTask().execute(generatePath(id) + ".jpg");
		}
	}

	@OnClick(R.id.action_item_repost)
	void repost(View view) {
		if (flagShareFolder == 0) {
			startShareActivity(getImages(sharedBitmap, 3,
					BitmapFactory
							.decodeResource(getResources(), R.drawable.bg0),
					BitmapFactory
							.decodeResource(getResources(), R.drawable.bg8),
					getResources()));
		} else {
			ArrayList<Uri> uris = new ArrayList<>();
			File zipFolder = new File("folderName" + subFolderName + "/");
			if (zipFolder.exists()) {
				for (File file : zipFolder.listFiles()) {
					uris.add(Uri.fromFile(file));
				}
			}
			startShareActivity(uris);
		}
	}

	private boolean isNumeric(String string) {
		try {
			Long.valueOf(string);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
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
			} catch (IOException e) {
			}
			return b;
		}

		public void onProgressUpdate(final Integer... progress) {
		}

		public void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				mPhoto.setImageBitmap(bitmap);
				sharedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
				mRepostButton.setEnabled(true);
				flagShareFolder = 0;
			} else {
				new AlertDialog.Builder(getActivity())
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
}
