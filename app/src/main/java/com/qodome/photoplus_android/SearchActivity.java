package com.qodome.photoplus_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.common.base.Objects;
import com.qodome.photoplus_android.OverlayManager;
import com.qodome.photoplus_android.R;
import com.qodome.photoplus_android.SquareImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;;

public class SearchActivity extends Activity {
    public static class QueryFilesTask extends AsyncTask<String, Integer, Bitmap> {
        private SearchActivity searchUI;

        public QueryFilesTask(final SearchActivity activity) {
            this.searchUI = activity;
        }

        public Bitmap doInBackground(final String... info) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(((("http://media.qodome.com/photoplus/free/" + info[0]) + "/") + (info[1] + ".jpg")));
            HttpResponse response;
            Bitmap b = null;
			try {
				response = client.execute(get);
	            b = null;
	            if (response.getStatusLine().getStatusCode() == 200) {
	                b = BitmapFactory.decodeStream(response.getEntity().getContent());
	            } else {
	                Log.i("PhotoPlus", ("http response: " + Integer.valueOf(response.getStatusLine().getStatusCode())));
	            }
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return b;
        }

        public void onProgressUpdate(final Integer... progress) {
        }

        public void onPostExecute(final Bitmap b) {
            ((SquareImageView)(this.searchUI.findViewById(R.id.photo_query_result))).setImageBitmap(b);
            if (Objects.equal(b, null)) {
                this.searchUI.getShare().setVisibility(View.GONE);
                new AlertDialog.Builder(this.searchUI)
                .setTitle("错误")
                .setMessage("图片未找到")
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
            } else {
                this.searchUI.sharedBitmap = b.copy(Bitmap.Config.ARGB_8888, true);
                this.searchUI.getShare().setVisibility(View.VISIBLE);
            }
        }
    }

    private Bitmap sharedBitmap;

    private String folderName;

    private OverlayManager om;

    public String init(final Bundle savedInstanceState) {
        return this.folderName = new String(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PhotoPlus/");
    }

    public boolean isNumeric(final String str) {
        try {
            long t = Long.parseLong(str);
        } catch(NumberFormatException nfe) {
            return false;  
        }
        return true;  
    }

    public void search(final View v) {
        String input = this.getInputText().getText().toString().substring(1);
		if (input.length() != 10 || isNumeric(input) == false) {
            new AlertDialog.Builder(this)
            .setTitle("错误")
            .setMessage("ID格式错误")
            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int which) {
                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
            return;
        }
        input = input.substring(0, (input.length() - 3));
        String folder = String.valueOf((Integer.parseInt(input) / 864000));
        new QueryFilesTask(this).execute(folder, this.getInputText().getText().toString());
    }

    public void share(final View v) {
        this.om = new OverlayManager(this);
        this.om.dumpSearchResultToFile(this.sharedBitmap);
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
        intent.setComponent(comp);
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");
        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        for (int i = 0; (i < 3); i++) {
            for (int j = 0; (j < 3); j++) {
                imageUris.add(Uri.fromFile(new File(((((this.folderName + "test") + Integer.valueOf(i)) + Integer.valueOf(j)) + ".png"))));
            }
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        this.startActivity(intent);
    }

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init(savedInstanceState);
    }

    public EditText getInputText() {
        return (EditText) findViewById(R.id.input_text);
    }

    public Button getShare() {
        return (Button) findViewById(R.id.share);
    }
}
