package com.dovoq.cubecandy;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dovoq.cubecandy.widget.SquareImageView;

public class EditFragment extends Fragment {
	private Bitmap show = null;
	private boolean initDone = false;

	public void recycleBitmap() {
		if (show != null) {
			show.recycle();
			show = null;
		}
	}

	public void setBitmap(OverlayManager om) {
		show = om.getBitmapForDraw(true);
		if (initDone) {
			SquareImageView imageView = (SquareImageView) findViewById(R.id.photo_grid_view);
			imageView.setImageBitmap(show);
			om.mRect = Utils.locateView(imageView);
		}
	}

	public View findViewById(final int resId) {
		return getView().findViewById(resId);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_edit, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		if (show != null) {
			SquareImageView imageView = (SquareImageView) findViewById(R.id.photo_grid_view);
			imageView.setImageBitmap(show);
		}
		initDone = true;
	}
}
