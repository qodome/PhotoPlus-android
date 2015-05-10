package com.dovoq.cubecandy.fragments;

import java.util.ArrayList;

import butterknife.ButterKnife;

import com.dovoq.cubecandy.Constants;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

public class MyFragment extends Fragment implements Constants {

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.reset(this);
	}

	protected final void startShareActivity(
			ArrayList<? extends Parcelable> items) {
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intent.setType("image/*");
		ComponentName component = new ComponentName("com.tencent.mm",
				"com.tencent.mm.ui.tools.ShareToTimeLineUI");
		intent.setComponent(component);
		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, items);
		startActivity(intent);
	}
}
