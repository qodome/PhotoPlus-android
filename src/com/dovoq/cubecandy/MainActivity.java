package com.dovoq.cubecandy;

import static com.nyssance.android.util.LogUtils.loge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.dovoq.cubecandy.util.BitmapUtils;
import com.dovoq.cubecandy.util.CropUtils;
import com.dovoq.cubecandy.util.ViewUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

public class MainActivity extends FragmentActivity implements
		GestureDetector.OnGestureListener, SensorEventListener, Constants {
	public static class UploadFilesTask extends
			AsyncTask<String, Integer, Long> {
		public Long doInBackground(final String... info) {
			try {
				HttpHelper.upload(info[0], info[1]);
			} catch (IOException e) {
			} catch (JSONException e) {
			}
			return 0L;
		}

		public void onProgressUpdate(final Integer... progress) {
		}

		public void onPostExecute(final Long result) {
		}
	}

	private final static int SWIPE_MIN_DISTANCE = 120;
	private final static int LOAD_PHOTO = 42;
	private final static int LOAD_CROP_VIEW = 422;
	private final static int LOAD_CAMERA = 4242;

	private OverlayManager OM;
	private EditFragment mEditFragment;
	private EditText mEditText;
	private CheckBox mCheckBox;

	private SharedPreferences mPreferences;

	private GestureDetector gdt;
	private List<String> welcomeNames;
	private String[] welcomeNameValues = new String[] { "welcome_1",
			"welcome_2", "welcome_3", "welcome_4", "welcome_5" };
	private List<Bitmap> welcomes;
	private int welcomeIdx;

	private SensorManager mSensorManager;
	private Sensor mSensor;
	private long mLastUpdate = 0;
	private float mLastX;
	private float mLastY;
	private float mLastZ;
	private boolean deleteNotified = false;

	public Rect mRect;

	public void init() {
		setContentView(R.layout.activity_main);
		OM = new OverlayManager(this);
		mEditFragment = new EditFragment();
		mEditFragment.setBitmap(OM);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, mEditFragment).commit();
		mEditText = (EditText) findViewById(R.id.input_text);
		mCheckBox = (CheckBox) findViewById(R.id.repost);
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				OM.inputString(s);
				mEditFragment.setBitmap(OM);
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mPreferences = getSharedPreferences("PhotoPlusPreference", MODE_PRIVATE);
		mCheckBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						SharedPreferences.Editor ed = mPreferences.edit();
						ed.putBoolean("repost", isChecked);
						ed.commit();
					}
				});
		mCheckBox.setChecked(mPreferences.getBoolean("repost", false));
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			loge("External storage not mounted");
			return;
		}
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
			SharedPreferences.Editor ed = sp.edit();
			ed.putBoolean("first_time_init", false);
			ed.commit();
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
			((ViewFlipper) findViewById(R.id.view_flipper))
					.setOnTouchListener(new View.OnTouchListener() {
						public boolean onTouch(final View view,
								final MotionEvent event) {
							gdt.onTouchEvent(event);
							return true;
						}
					});
			for (int idx = 0; idx < welcomes.size(); idx++) {
				ImageView img = new ImageView(this);
				img.setImageBitmap(welcomes.get(idx));
				img.setScaleType(ImageView.ScaleType.CENTER_CROP);
				((ViewFlipper) findViewById(R.id.view_flipper)).addView(img);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Intent intent = new Intent(this, CropActivity.class);
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
						mEditFragment.recycleBitmap();
						OM.recyclePhoto();
						is = openFileInput(fn);
						Bitmap bmp = BitmapFactory.decodeStream(is);
						OM.setPhoto(bmp);
						mEditFragment.setBitmap(OM);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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

	public void font(final View v) {
		OM.toggleTF();
		mEditFragment.setBitmap(OM);
	}

	public void background(final View v) {
		OM.toggleBG();
		mEditFragment.setBitmap(OM);
	}

	public void loadPhoto(final View v) {
		Intent intent = new Intent(
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Intent.ACTION_OPEN_DOCUMENT
						: Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, LOAD_PHOTO);
	}

	public void share(View v) throws IOException, WriterException {
		Boolean repost = mPreferences.getBoolean("repost", false);
		Bitmap bgImage = BitmapFactory.decodeResource(getResources(),
				repost ? R.drawable.bg1 : R.drawable.bg);
		int width = 480;
		int height = bgImage.getHeight() * width / bgImage.getWidth();
		Bitmap card = Bitmap.createScaledBitmap(bgImage, width, height, false); // 需要分享的卡片
		Bitmap content = ViewUtils.getSnapshot(this, mRect);
		card = BitmapUtils.merge(getResources(), card, content, 0,
				(height - width) / 2, width, width);
		Bitmap bg = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
		Bitmap bg0 = BitmapFactory.decodeResource(getResources(),
				R.drawable.bg0);
		Bitmap bg8 = BitmapFactory.decodeResource(getResources(),
				R.drawable.bg8);
		ArrayList<Uri> uris;
		if (repost) { // 加id并上传
			String id = CropUtils.generateId("a");
			card = BitmapUtils.addText(card, "转发ID: " + id);
			FileOutputStream out = new FileOutputStream(new File(
					TEMPORARY_DIRECTORY, id + ".jpg"));
			card.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.close();
			new MainActivity.UploadFilesTask().execute(
					TEMPORARY_DIRECTORY.getAbsolutePath(),
					CropUtils.generatePath(id + ".jpg"));
			uris = CropUtils.getImages(card, 3, bg0, bg8, getResources());
		} else {
			uris = CropUtils.getImages(card, 3, bg, bg, getResources());
		}
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intent.setType("image/*");
		ComponentName comp = new ComponentName("com.tencent.mm",
				"com.tencent.mm.ui.tools.ShareToTimeLineUI");
		intent.setComponent(comp);
		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		startActivity(intent);
		if (mPreferences.getBoolean("repost", false)) {

		}
	}

	public void search(final View v) {
		startActivity(new Intent(this, SearchActivity.class));
	}

	public void camera(final View v) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(new File(TEMPORARY_DIRECTORY, "capture.jpg")));
		startActivityForResult(takePictureIntent, LOAD_CAMERA);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
			welcomeIdx++;
			if (welcomeIdx >= welcomes.size()) {
				init();
			} else {
				((ViewFlipper) findViewById(R.id.view_flipper))
						.setInAnimation(AnimationUtils.loadAnimation(this,
								R.anim.push_left_in));
				((ViewFlipper) findViewById(R.id.view_flipper))
						.setOutAnimation(AnimationUtils.loadAnimation(this,
								R.anim.push_left_out));
				((ViewFlipper) findViewById(R.id.view_flipper)).showNext();
			}
			return true;
		} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
			if ((welcomeIdx > 0)) {
				welcomeIdx--;
				((ViewFlipper) findViewById(R.id.view_flipper))
						.setInAnimation(AnimationUtils.loadAnimation(this,
								R.anim.push_right_in));
				((ViewFlipper) findViewById(R.id.view_flipper))
						.setOutAnimation(AnimationUtils.loadAnimation(this,
								R.anim.push_right_out));
				((ViewFlipper) findViewById(R.id.view_flipper)).showPrevious();
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
		if (event.sensor == mSensor) {
			long curTime = System.currentTimeMillis();
			if (mLastUpdate != 0 && (curTime - mLastUpdate) > 100) {
				long diffTime = (curTime - mLastUpdate);
				mLastUpdate = curTime;
				float x = event.values[0];
				float y = event.values[1];
				float z = event.values[2];
				if ((((Math.abs(x + y + z - mLastX - mLastY - mLastZ)
						/ diffTime * 10000) > 1000) && (deleteNotified == false))) {
					deleteNotified = true;
					new AlertDialog.Builder(this)
							.setTitle("请确认")
							.setMessage("确认删除?")
							.setPositiveButton(android.R.string.ok,
									new DialogInterface.OnClickListener() {
										public void onClick(
												final DialogInterface dialog,
												final int which) {
											mEditText.setText("");
											OM.reset();
											mEditFragment.setBitmap(OM);
											deleteNotified = false;
										}
									})
							.setNegativeButton(android.R.string.no,
									new DialogInterface.OnClickListener() {
										public void onClick(
												final DialogInterface dialog,
												final int which) {
											deleteNotified = false;
										}
									})
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setCancelable(false).show();
				}
				mLastX = x;
				mLastY = y;
				mLastZ = z;
			} else {
				if ((mLastUpdate == 0)) {
					mLastUpdate = System.currentTimeMillis();
					mLastX = event.values[0];
					mLastY = event.values[1];
					mLastZ = event.values[2];
				}
			}
		}
	}
}
