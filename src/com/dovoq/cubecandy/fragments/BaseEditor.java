package com.dovoq.cubecandy.fragments;

import static com.nyssance.android.util.LogUtils.loge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.dovoq.cubecandy.CropActivity;
import com.dovoq.cubecandy.R;

public abstract class BaseEditor extends MyFragment {
	protected ImageView mPhoto;
	protected CheckBox mCheckBox;

	protected SharedPreferences mPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = getActivity().getSharedPreferences("pref",
				Context.MODE_PRIVATE);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mPhoto = (ImageView) view.findViewById(R.id.image);
		mCheckBox = (CheckBox) view.findViewById(android.R.id.checkbox);
		mCheckBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						mPreferences.edit().putBoolean("repost", isChecked)
								.apply();
					}
				});
		mCheckBox.setChecked(mPreferences.getBoolean("repost", false));
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			loge("External storage not mounted");
			return;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Intent intent = new Intent(getActivity(), CropActivity.class);
			switch (requestCode) {
			case LOAD_PHOTO:
				intent.putExtra("BitmapImage", data.getData().toString());
				startActivityForResult(intent, LOAD_CROP_VIEW);
				break;
			case LOAD_CROP_VIEW:
				if (data != null && data.hasExtra("filename")) {
					String fn = data.getStringExtra("filename");
					FileInputStream is;
					try {
						is = getActivity().openFileInput(fn);
						Bitmap bitmap = BitmapFactory.decodeStream(is);
						mPhoto.setImageBitmap(bitmap);
					} catch (FileNotFoundException e) {
					}
				}
				break;
			case LOAD_CAMERA:
				intent.putExtra("BitmapImage", Uri.fromFile(new File(
						TEMPORARY_DIRECTORY, "capture.jpg")));
				startActivityForResult(intent, LOAD_CROP_VIEW);
				break;
			}
		}
	}
}
