package com.dovoq.cubecandy.fragments;

import static com.dovoq.cubecandy.util.CropUtils.generateId;
import static com.dovoq.cubecandy.util.CropUtils.generatePath;
import static com.dovoq.cubecandy.util.CropUtils.getImages;
import static com.dovoq.cubecandy.util.CropUtils.getQRCode;
import static com.dovoq.cubecandy.util.FileUtils.listAssets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.dovoq.cubecandy.R;
import com.dovoq.cubecandy.SearchActivity;
import com.dovoq.cubecandy.util.BitmapUtils;
import com.dovoq.cubecandy.util.S3Utils;
import com.dovoq.cubecandy.util.ViewUtils;
import com.dovoq.cubecandy.widget.CanvasView;
import com.google.zxing.BarcodeFormat;

public class PlusEditor extends BaseEditor {
	private float mFontSize = 40;
	private ArrayList<Typeface> mFonts = new ArrayList<>();
	private ArrayList<String> mTiles = new ArrayList<>();
	private int mFontIndex;
	private int mTileIndex;

	@InjectView(R.id.my_canvas)
	CanvasView mCanvasView;
	@InjectView(android.R.id.input)
	EditText mEditText;
	@InjectView(R.id.grid)
	GridLayout mGridLayout;
	private ArrayList<TextView> mLabels = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFonts.add(Typeface.DEFAULT);
		for (String s : listAssets(getActivity(), "fonts")) {
			mFonts.add(Typeface.createFromAsset(getActivity().getAssets(), s));
		}
		mTiles = listAssets(getActivity(), "tiles");
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_plus_editor, container,
				false);
		ButterKnife.inject(this, view);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// 文字输入框
		mEditText.setHint(R.string.tip0);
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				changeText(mEditText.getText().toString());
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		for (int i = 0; i < 9; i++) {
			TextView tv = (TextView) mGridLayout.getChildAt(i);
			tv.setTypeface(mFonts.get(mFontIndex));
			tv.setTextSize(mFontSize);
			mLabels.add(tv);
		}
	}

	@OnClick(R.id.my_action_font)
	void changeFont() {
		mFontIndex = (mFontIndex + 1) % mFonts.size();
		changeText(mEditText.getText().toString());
	}

	@OnClick(R.id.my_action_tile)
	void changeTile() {
		mTileIndex = (mTileIndex + 1) % mTiles.size();
		changeText(mEditText.getText().toString());
	}

	private void changeText(String string) {
		String path = mTiles.get(mTileIndex);
		float size = mFontSize; // FIXME: 根据机型调整
		BitmapDrawable image = null;
		try {
			image = new BitmapDrawable(getResources(), getActivity()
					.getAssets().open(path));
		} catch (IOException e) {
		}
		int color = Color.parseColor(String.format("#%06x", 0xFFFFFF & Long
				.parseLong(FilenameUtils.getBaseName(path).split("_")[1], 16))); // 容错且不处理透明
		for (TextView label : mLabels) { // 初始化所有Label
			label.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
			label.setText("");
		}
		Typeface font = mFonts.get(mFontIndex);
		for (int i = 0; i < Math.min(9, string.length()); i++) {
			int j = i;
			TextView label = mLabels.get(j);
			label.setTypeface(font);
			label.setTextSize(size);
			label.setText(string.subSequence(i, i + 1));
			label.setTextColor(color);
			if (string.subSequence(i, i + 1) != " ") {
				label.setBackground(image);
			}
		}
	}

	@OnClick(R.id.action_item_open)
	void selectPhoto(View view) { // 选单张照片
		Intent intent = new Intent(
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Intent.ACTION_OPEN_DOCUMENT
						: Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, LOAD_PHOTO);
	}

	@OnClick(R.id.action_item_share)
	void share(View view) {
		Boolean repost = mPreferences.getBoolean("repost", false);
		Bitmap bgImage = BitmapFactory.decodeResource(getResources(),
				repost ? R.drawable.bg1 : R.drawable.bg);
		int width = 480;
		int height = bgImage.getHeight() * width / bgImage.getWidth();
		Bitmap card = Bitmap.createScaledBitmap(bgImage, width, height, false); // 需要分享的卡片
		Bitmap content = ViewUtils.getSnapshot(mCanvasView);
		card = BitmapUtils.merge(getResources(), card, content, 0,
				(height - width) / 2, width, width);
		Bitmap bg = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
		Bitmap bg0 = BitmapFactory.decodeResource(getResources(),
				R.drawable.bg0);
		Bitmap bg8 = BitmapFactory.decodeResource(getResources(),
				R.drawable.bg8);
		ArrayList<Uri> uris;
		if (repost) { // 加id并上传
			String id = generateId("a");
			card = BitmapUtils.addText(card, "转发 ID: " + id);
			Bitmap code = getQRCode(id, BarcodeFormat.QR_CODE, 128, 128);
			card = BitmapUtils.merge(getResources(), card, code, 32,
					card.getHeight() - 160, 128, 128);
			FileOutputStream out;
			try {
				out = new FileOutputStream(new File(TEMPORARY_DIRECTORY, id
						+ ".jpg"));
				card.compress(Bitmap.CompressFormat.JPEG, 60, out);
				out.close();
			} catch (IOException e) {
			}
			// new S3Utils.S3Upload(BASE_URL
			// + "/upload_params/s3/?filename=photoplus/free/11111.jpg", 0)
			// .execute();
			new S3Utils.UploadFilesTask().execute(
					TEMPORARY_DIRECTORY.getAbsolutePath(), generatePath(id
							+ ".jpg"));
			uris = getImages(card, 3, bg0, bg8, getResources());
		} else {
			uris = getImages(card, 3, bg, bg, getResources());
		}
		startShareActivity(uris);
	}

	@OnClick(R.id.action_search)
	void search(View view) {
		startActivity(new Intent(getActivity(), SearchActivity.class));
	}

	protected void camera(View view) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(new File(TEMPORARY_DIRECTORY, "capture.jpg")));
		startActivityForResult(takePictureIntent, LOAD_CAMERA);
	}
}
