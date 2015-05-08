package com.dovoq.cubecandy.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dovoq.cubecandy.R;
import com.dovoq.cubecandy.widget.SquareImageView;

public class SearchSingleFragment extends Fragment {
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
			((SquareImageView) findViewById(R.id.search_single_result))
					.setImageBitmap(show);
		}
	}

	public View findViewById(final int resId) {
		return getView().findViewById(resId);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if (show != null) {
			((SquareImageView) findViewById(R.id.search_single_result))
					.setImageBitmap(show);
		}
		initDone = true;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search_single, container,
				false);
	}
}
