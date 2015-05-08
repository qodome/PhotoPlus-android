package com.dovoq.cubecandy;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;

public class MyActivity extends FragmentActivity {
	public Rect mRect;

	protected void startShareActivity(ArrayList<? extends Parcelable> items) {
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intent.setType("image/*");
		ComponentName component = new ComponentName("com.tencent.mm",
				"com.tencent.mm.ui.tools.ShareToTimeLineUI");
		intent.setComponent(component);
		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, items);
		startActivity(intent);
	}
}
