package com.dovoq.photoplus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
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

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener, SensorEventListener {
    public static class UploadFilesTask extends AsyncTask<String, Integer, Long> {
        public Long doInBackground(final String... info) {
            try {
                HttpHelper.upload(info[0], info[1], info[2]);
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return 0L;
        }

        public void onProgressUpdate(final Integer... progress) {
        }

        public void onPostExecute(final Long result) {
        }
    }

    private EditFragment editFrag;
    private OverlayManager om;
    private String folderName;
    private GestureDetector gdt;
    private List<String> welcomeNames;
    private String[] welcomeNameValues = new String[]{"welcome_1", "welcome_2", "welcome_3", "welcome_4", "welcome_5"};
    private List<Bitmap> welcomes;
    private int welcomeIdx;
    private final static int SWIPE_MIN_DISTANCE = 120;
    private final static int LOAD_PHOTO = 42;
    private final static int LOAD_CROP_VIEW = 422;
    private final static int LOAD_CAMERA = 4242;
    private final static int SECONDS_IN_ONE_DAY = 864000;
    public final static int SECONDS_OFFSET = 1425168000;
    private SharedPreferences sp;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long lastUpdate = 0;
    private float last_x;
    private float last_y;
    private float last_z;
    private boolean deleteNotified = false;

    public EditFragment newEditFrag() {
        EditFragment frag = new EditFragment();
        return frag;
    }

    public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
        Log.i("PhotoPlus", "onFling event");
        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
            Log.i("PhotoPlus", "turn left");
            this.welcomeIdx++;
            if (welcomeIdx >= welcomes.size()) {
                this.init();
            } else {
                ((ViewFlipper) this.findViewById(R.id.view_flipper)).setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
                ((ViewFlipper) this.findViewById(R.id.view_flipper)).setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
                ((ViewFlipper) this.findViewById(R.id.view_flipper)).showNext();
            }
            return true;
        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
            Log.i("PhotoPlus", "turn right");
            if ((this.welcomeIdx > 0)) {
                this.welcomeIdx--;
                ((ViewFlipper) this.findViewById(R.id.view_flipper)).setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
                ((ViewFlipper) this.findViewById(R.id.view_flipper)).setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
                ((ViewFlipper) this.findViewById(R.id.view_flipper)).showPrevious();
            }
            return true;
        }
        return false;
    }

    public void init() {
        this.setContentView(R.layout.activity_main);

        this.om = new OverlayManager(this);
        this.editFrag = this.newEditFrag();
        this.editFrag.setBitmap(this.om.getBitmapForDraw(true));
        this.getFragmentManager().beginTransaction().add(R.id.fragment_container, editFrag).commit();
        this.getInputText().addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(final Editable s) {
            }

            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                om.inputString(s);
                editFrag.setBitmap(om.getBitmapForDraw(true));
            }
        });
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.e(this.getString(R.string.LOGTAG), "External storage not mounted");
            return;
        }
        File reportFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "PhotoPlus");
        if (!reportFolder.exists()) {
            Log.i(this.getString(R.string.LOGTAG), "Creating missing directory iDoStatsMonitor");
            reportFolder.mkdirs();
        }
        this.folderName = new String(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PhotoPlus/");
        this.sp = this.getSharedPreferences("PhotoPlusPreference", Context.MODE_PRIVATE);
        if (sp.getBoolean("enable_share", false)) {
            this.getEnableShare().setChecked(true);
        } else {
            this.getEnableShare().setChecked(false);
        }
        this.getEnableShare().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                SharedPreferences.Editor ed = MainActivity.this.sp.edit();
                ed.putBoolean("enable_share", isChecked);
                ed.commit();
            }
        });
    }

    @SuppressWarnings("deprecation")
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSensorManager = (SensorManager) (this.getSystemService(Context.SENSOR_SERVICE));
        this.mAccelerometer = this.mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mSensorManager.registerListener(this, this.mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        PreferenceManager.setDefaultValues(this, "PhotoPlusPreference", Context.MODE_PRIVATE, R.xml.preferences, false);
        SharedPreferences sp = this.getSharedPreferences("PhotoPlusPreference", Context.MODE_PRIVATE);
        if (sp.getBoolean("first_time_init", false)) {
            SharedPreferences.Editor ed = sp.edit();
            ed.putBoolean("first_time_init", false);
            ed.commit();
            Log.i(this.getString(R.string.LOGTAG), "First time run, show welcome screens");
            this.setContentView(R.layout.welcome);
            this.gdt = new GestureDetector(this);
            this.welcomeIdx = 0;
            welcomeNames = new ArrayList<String>();
            for (int i = 0; i < welcomeNameValues.length; ++i) {
                welcomeNames.add(welcomeNameValues[i]);
            }
            this.welcomes = new ArrayList<Bitmap>();
            for (final String desc : this.welcomeNames) {
                int id = this.getResources().getIdentifier(desc, "drawable", this.getPackageName());
                this.welcomes.add(((BitmapDrawable) this.getResources().getDrawable(id)).getBitmap());
            }
            ((ViewFlipper) this.findViewById(R.id.view_flipper)).setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(final View view, final MotionEvent event) {
                    MainActivity.this.gdt.onTouchEvent(event);
                    return true;
                }
            });
            for (int idx = 0; idx < this.welcomes.size(); idx++) {
                {
                    ImageView img = new ImageView(this);
                    img.setImageBitmap(this.welcomes.get(idx));
                    img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ((ViewFlipper) this.findViewById(R.id.view_flipper)).addView(img);
                }
            }
            return;
        }
        this.init();
    }

    public void onDestroy() {
        this.mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    public void onResume() {
        super.onResume();
        this.mSensorManager.registerListener(this, this.mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        super.onPause();
        this.mSensorManager.unregisterListener(this);
    }

    public void font(final View v) {
        this.om.toggleTF();
        this.editFrag.setBitmap(this.om.getBitmapForDraw(true));
    }

    public void background(final View v) {
        this.om.toggleBG();
        this.editFrag.setBitmap(this.om.getBitmapForDraw(true));
    }

    public void loadPhoto(final View v) {
        Intent intent = new Intent(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Intent.ACTION_OPEN_DOCUMENT : Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, LOAD_PHOTO);
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (((requestCode == LOAD_PHOTO) && (resultCode == Activity.RESULT_OK))) {
            Intent intent = new Intent(this, CropActivity.class);
            intent.putExtra("BitmapImage", data.getData().toString());
            this.startActivityForResult(intent, LOAD_CROP_VIEW);
        } else if ((requestCode == LOAD_CROP_VIEW) && (resultCode == Activity.RESULT_OK)) {
            if (data != null && data.hasExtra("filename")) {
                String fn = data.getStringExtra("filename");
                FileInputStream is;
                try {
                    this.editFrag.recycleBitmap();
                    this.om.recyclePhoto();
                    is = this.openFileInput(fn);
                    Bitmap bmp = BitmapFactory.decodeStream(is);
                    this.om.setPhoto(bmp);
                    this.editFrag.setBitmap(this.om.getBitmapForDraw(true));
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else if (((requestCode == LOAD_CAMERA) && (resultCode == Activity.RESULT_OK))) {
            Intent intent = new Intent(this, CropActivity.class);
            intent.putExtra("BitmapImage", Uri.fromFile(new File((this.folderName + "capture.jpg"))).toString());
            this.startActivityForResult(intent, LOAD_CROP_VIEW);
        }
    }

    public String getFolderName() {
        Calendar c = Calendar.getInstance();
        long sec = (c.getTimeInMillis() + c.getTimeZone().getOffset(c.getTimeInMillis())) / 1000L;
        return String.valueOf(((sec - SECONDS_OFFSET) / SECONDS_IN_ONE_DAY));
    }

    public void share(final View v) throws IOException, WriterException {
        String uploadFn = om.dumpToFile(sp.getBoolean("enable_share", false));
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
        intent.setComponent(comp);
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");
        ArrayList<Uri> imageUris = new ArrayList<>();
        for (int i = 0; (i < 3); i++) {
            for (int j = 0; (j < 3); j++) {
                imageUris.add(Uri.fromFile(new File(((((this.folderName + "test") + Integer.valueOf(i)) + Integer.valueOf(j)) + ".png"))));
            }
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        startActivity(intent);
        if (sp.getBoolean("enable_share", false)) {
            new MainActivity.UploadFilesTask().execute(this.folderName, this.getFolderName(), uploadFn);
        }
    }

    public void search(final View v) {
        this.startActivity(new Intent(this, SearchActivity.class));
    }

    public void camera(final View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File((this.folderName + "capture.jpg"))));
        this.startActivityForResult(takePictureIntent, LOAD_CAMERA);
    }

    public boolean onDown(final MotionEvent e) {
        return false;
    }

    public void onLongPress(final MotionEvent e) {
    }

    public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
        return false;
    }

    public void onShowPress(final MotionEvent e) {
    }

    public boolean onSingleTapUp(final MotionEvent e) {
        return false;
    }

    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
    }

    public void onSensorChanged(final SensorEvent event) {
        if (event.sensor == this.mAccelerometer) {
            long curTime = System.currentTimeMillis();
            if (((this.lastUpdate != 0) && ((curTime - this.lastUpdate) > 100))) {
                long diffTime = (curTime - this.lastUpdate);
                this.lastUpdate = curTime;
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                if ((((Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000) > 1000) && (this.deleteNotified == false))) {
                    Log.i("PhotoPlus", ("shake detected"));
                    this.deleteNotified = true;
                    new AlertDialog.Builder(this)
                            .setTitle("请确认")
                            .setMessage("确认删除?")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int which) {
                                    Log.i("PhotoPlus", "Delete confirmed");
                                    MainActivity.this.getInputText().setText("");
                                    MainActivity.this.om.reset();
                                    MainActivity.this.editFrag.setBitmap(MainActivity.this.om.getBitmapForDraw(true));
                                    MainActivity.this.deleteNotified = false;
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int which) {
                                    Log.i("PhotoPlus", "Delete cancelled");
                                    MainActivity.this.deleteNotified = false;
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .show();
                }
                this.last_x = x;
                this.last_y = y;
                this.last_z = z;
            } else {
                if ((this.lastUpdate == 0)) {
                    this.lastUpdate = System.currentTimeMillis();
                    this.last_x = event.values[0];
                    this.last_y = event.values[1];
                    this.last_z = event.values[2];
                }
            }
        }
    }

    public EditText getInputText() {
        return (EditText) (findViewById(R.id.input_text));
    }

    public CheckBox getEnableShare() {
        return (CheckBox) (findViewById(R.id.enable_share));
    }
}
