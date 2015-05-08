package com.dovoq.cubecandy.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dovoq.cubecandy.MyActivity;
import com.dovoq.cubecandy.R;
import com.dovoq.cubecandy.util.ViewUtils;
import com.dovoq.cubecandy.widget.SquareImageView;

public class PlusEditor extends Fragment {
	private SquareImageView mPhoto;

	public void setImage(Bitmap bitmap) {
		mPhoto.setImageBitmap(bitmap);
		((MyActivity) getActivity()).mRect = ViewUtils.locateView(mPhoto);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_edit, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mPhoto = (SquareImageView) view.findViewById(R.id.photo_grid_view);
	}
}
