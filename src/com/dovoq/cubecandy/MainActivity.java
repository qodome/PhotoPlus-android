package com.dovoq.cubecandy;

import static com.nyssance.android.util.LogUtils.loge;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.dovoq.cubecandy.fragments.PlusEditor;
import com.nyssance.android.app.BaseActivity;

public class MainActivity extends BaseActivity implements Constants,
		GestureDetector.OnGestureListener, SensorEventListener {
	private GestureDetector gdt;
	private List<String> welcomeNames;
	private String[] welcomeNameValues = new String[] { "welcome_1",
			"welcome_2", "welcome_3", "welcome_4" };
	private List<Bitmap> welcomes;
	private int welcomeIdx;

	private SensorManager mSensorManager;
	private Sensor mSensor;
	private long mLastUpdate = 0;
	private float mLastX;
	private float mLastY;
	private float mLastZ;
	private boolean deleteNotified = false;

	private ViewFlipper mViewFlipper;

	public void init() {
		setContentView(R.layout.activity_main);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, new PlusEditor()).commit();
		TEMPORARY_DIRECTORY.mkdirs();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSensorManager = (SensorManager) (getSystemService(SENSOR_SERVICE));
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_GAME);
		PreferenceManager.setDefaultValues(this, "PhotoPlusPreference",
				MODE_PRIVATE, R.xml.preferences, false);
		SharedPreferences sp = getSharedPreferences("PhotoPlusPreference",
				MODE_PRIVATE);
		if (sp.getBoolean("first_time_init", false)) {
			sp.edit().putBoolean("first_time_init", false).apply();
			loge("First time run, show welcome screens");
			setContentView(R.layout.welcome);
			gdt = new GestureDetector(this);
			welcomeIdx = 0;
			welcomeNames = new ArrayList<String>();
			for (int i = 0; i < welcomeNameValues.length; ++i) {
				welcomeNames.add(welcomeNameValues[i]);
			}
			welcomes = new ArrayList<Bitmap>();
			for (final String desc : welcomeNames) {
				int id = getResources().getIdentifier(desc, "drawable",
						getPackageName());
				welcomes.add(((BitmapDrawable) getResources().getDrawable(id))
						.getBitmap());
			}
			mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
			mViewFlipper.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(final View view, final MotionEvent event) {
					gdt.onTouchEvent(event);
					return true;
				}
			});
			for (int idx = 0; idx < welcomes.size(); idx++) {
				ImageView img = new ImageView(this);
				img.setImageBitmap(welcomes.get(idx));
				img.setScaleType(ImageView.ScaleType.CENTER_CROP);
				mViewFlipper.addView(img);
			}
			return;
		}
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onDestroy() {
		mSensorManager.unregisterListener(this);
		super.onDestroy();
	}

	public void font(final View v) {
		// OM.toggleTF();
		// mEditFragment.setBitmap(OM);
	}

	public void background(final View v) {
		// OM.toggleBG();
		// mEditFragment.setBitmap(OM);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
			welcomeIdx++;
			if (welcomeIdx >= welcomes.size()) {
				init();
			} else {
				mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_left_in));
				mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_left_out));
				mViewFlipper.showNext();
			}
			return true;
		} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
			if ((welcomeIdx > 0)) {
				welcomeIdx--;
				mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_right_in));
				mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_right_out));
				mViewFlipper.showPrevious();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// 临时去掉摇一摇
		// if (event.sensor == mSensor) {
		// long curTime = System.currentTimeMillis();
		// if (mLastUpdate != 0 && (curTime - mLastUpdate) > 100) {
		// long diffTime = (curTime - mLastUpdate);
		// mLastUpdate = curTime;
		// float x = event.values[0];
		// float y = event.values[1];
		// float z = event.values[2];
		// if ((((Math.abs(x + y + z - mLastX - mLastY - mLastZ)
		// / diffTime * 10000) > 1000) && (deleteNotified == false))) {
		// deleteNotified = true;
		// new AlertDialog.Builder(this)
		// .setTitle("请确认")
		// .setMessage("确认删除?")
		// .setPositiveButton(android.R.string.ok,
		// new DialogInterface.OnClickListener() {
		// public void onClick(
		// final DialogInterface dialog,
		// final int which) {
		// // mEditText.setText("");
		// // OM.reset();
		// // mEditFragment.setImage(null);
		// deleteNotified = false;
		// }
		// })
		// .setNegativeButton(android.R.string.no,
		// new DialogInterface.OnClickListener() {
		// public void onClick(
		// final DialogInterface dialog,
		// final int which) {
		// deleteNotified = false;
		// }
		// })
		// .setIcon(android.R.drawable.ic_dialog_alert)
		// .setCancelable(false).show();
		// }
		// mLastX = x;
		// mLastY = y;
		// mLastZ = z;
		// } else {
		// if ((mLastUpdate == 0)) {
		// mLastUpdate = System.currentTimeMillis();
		// mLastX = event.values[0];
		// mLastY = event.values[1];
		// mLastZ = event.values[2];
		// }
		// }
		// }
	}
}
