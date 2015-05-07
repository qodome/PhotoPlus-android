package com.dovoq.photoplus;

import static com.nyssance.android.util.LogUtils.logd;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EditFragment extends Fragment {
	private Bitmap show = null;
	private boolean initDone = false;

	public void recycleBitmap() {
		if (show != null) {
			show.recycle();
			show = null;
		}
	}

	public void setBitmap(final Bitmap b) {
		show = b;
		if (initDone) {
			SquareImageView imageView = (SquareImageView) findViewById(R.id.photo_grid_view);
			imageView.setImageBitmap(show);
			Rect rect = Utils.locateView(imageView);
			logd("a " + rect.width());
			logd("a " + rect.height());
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
