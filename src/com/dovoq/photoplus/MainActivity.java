package com.dovoq.photoplus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.google.zxing.WriterException;

public class MainActivity extends FragmentActivity implements
		GestureDetector.OnGestureListener, SensorEventListener, Constants {
	public static class UploadFilesTask extends
			AsyncTask<String, Integer, Long> {
		public Long doInBackground(final String... info) {
			try {
				HttpHelper.upload(info[0], info[1], info[2]);
			} catch (ClientProtocolException e) {
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
	private final static int SECONDS_IN_ONE_DAY = 864000;
	public final static int SECONDS_OFFSET = 1425168000;

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

	public void init() {
		setContentView(R.layout.activity_main);
		OM = new OverlayManager(this);
		mEditFragment = new EditFragment();
		mEditFragment.setBitmap(OM.getBitmapForDraw(true));
		getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, mEditFragment).commit();
		mEditText = (EditText) findViewById(R.id.input_text);
		mCheckBox = (CheckBox) findViewById(R.id.enable_share);
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				OM.inputString(s);
				mEditFragment.setBitmap(OM.getBitmapForDraw(true));
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
						ed.putBoolean("enable_share", isChecked);
						ed.commit();
					}
				});
		mCheckBox.setChecked(mPreferences.getBoolean("enable_share", false));
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			Log.e(getString(R.string.LOGTAG), "External storage not mounted");
			return;
		}
		File reportFolder = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/" + "PhotoPlus");
		if (!reportFolder.exists()) {
			Log.i(getString(R.string.LOGTAG),
					"Creating missing directory iDoStatsMonitor");
			reportFolder.mkdirs();
		}
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
			Log.i(getString(R.string.LOGTAG),
					"First time run, show welcome screens");
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
						mEditFragment.setBitmap(OM.getBitmapForDraw(true));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			case LOAD_CAMERA:
				intent.putExtra("BitmapImage",
						Uri.fromFile(new File(DIRECTORY_TMP + "capture.jpg"))
								.toString());
				startActivityForResult(intent, LOAD_CROP_VIEW);
				break;
			}
		}
	}

	public void font(final View v) {
		OM.toggleTF();
		mEditFragment.setBitmap(OM.getBitmapForDraw(true));
	}

	public void background(final View v) {
		OM.toggleBG();
		mEditFragment.setBitmap(OM.getBitmapForDraw(true));
	}

	public void loadPhoto(final View v) {
		Intent intent = new Intent(
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Intent.ACTION_OPEN_DOCUMENT
						: Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, LOAD_PHOTO);
	}

	private String getFolderName() {
		Calendar c = Calendar.getInstance();
		long sec = (c.getTimeInMillis() + c.getTimeZone().getOffset(
				c.getTimeInMillis())) / 1000L;
		return String.valueOf((sec - SECONDS_OFFSET) / SECONDS_IN_ONE_DAY);
	}

	public void share(final View v) throws IOException, WriterException {
		String uploadFn = OM.dumpToFile(mPreferences.getBoolean("enable_share",
				false));
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intent.setType("image/*");
		ComponentName comp = new ComponentName("com.tencent.mm",
				"com.tencent.mm.ui.tools.ShareToTimeLineUI");
		intent.setComponent(comp);
		ArrayList<Uri> uris = new ArrayList<>();
		File directory = new File(DIRECTORY_TMP);
		for (File file : directory.listFiles()) {
			if (!file.isHidden() && file.getName().endsWith(".png")) {
				uris.add(Uri.fromFile(file));
			}
		}
		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		startActivity(intent);
		if (mPreferences.getBoolean("enable_share", false)) {
			new MainActivity.UploadFilesTask().execute(DIRECTORY_TMP,
					getFolderName(), uploadFn);
		}
	}

	public void search(final View v) {
		startActivity(new Intent(this, SearchActivity.class));
	}

	public void camera(final View v) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(new File((DIRECTORY_TMP + "capture.jpg"))));
		startActivityForResult(takePictureIntent, LOAD_CAMERA);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.i("PhotoPlus", "onFling event");
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
			Log.i("PhotoPlus", "turn left");
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
			Log.i("PhotoPlus", "turn right");
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
					Log.i("PhotoPlus", ("shake detected"));
					deleteNotified = true;
					new AlertDialog.Builder(this)
							.setTitle("请确认")
							.setMessage("确认删除?")
							.setPositiveButton(android.R.string.ok,
									new DialogInterface.OnClickListener() {
										public void onClick(
												final DialogInterface dialog,
												final int which) {
											Log.i("PhotoPlus",
													"Delete confirmed");
											mEditText.setText("");
											OM.reset();
											mEditFragment.setBitmap(OM
													.getBitmapForDraw(true));
											deleteNotified = false;
										}
									})
							.setNegativeButton(android.R.string.no,
									new DialogInterface.OnClickListener() {
										public void onClick(
												final DialogInterface dialog,
												final int which) {
											Log.i("PhotoPlus",
													"Delete cancelled");
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
