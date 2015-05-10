package com.dovoq.cubecandy.util;

import java.io.IOException;

import org.json.JSONException;

import android.os.AsyncTask;

import com.dovoq.cubecandy.tmp.HttpHelper;

public class HttpUtils {
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
}
