package com.dovoq.cubecandy.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.json.JSONException;

import android.net.Uri;
import android.os.AsyncTask;

import com.dovoq.cubecandy.Constants;
import com.dovoq.cubecandy.tmp.HttpHelper;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.nyssance.android.content.HttpTask;
import com.nyssance.android.util.LogUtils;
import com.nyssance.models.UploadParams;
import com.nyssance.util.JsonUtils;

public class S3Utils implements Constants {
	/** 上传到S3 */
	public static void uploadToS3(String filename, File file) {
	}

	public static class GetS3UploadParams extends HttpTask<UploadParams> {

		public GetS3UploadParams(String url, int cachePolicy) {
			super(url, cachePolicy);
		}

		@Override
		protected UploadParams onParse(InputStream in) throws IOException {
			return null;
		}
	}

	public static class S3Upload extends HttpTask<UploadParams> {

		public S3Upload(String url, int cachePolicy) {
			super(url, cachePolicy);
			LogUtils.loge(url);
		}

		@Override
		protected UploadParams onParse(InputStream in) throws IOException {
			return new JsonObjectParser(JsonUtils.JSON_FACTORY).parseAndClose(
					in, Charset.defaultCharset(), UploadParams.class);
		}

		@Override
		protected void onPostExecute(UploadParams result) {
			super.onPostExecute(result);
			if (result != null) {
				Uri uri = Uri.parse(mUrl);
				LogUtils.loge(result.toString());
				LogUtils.loge(mUrl.toString());
				LogUtils.loge(uri.getQueryParameter("filename"));
				try {
					File file = new File(TEMPORARY_DIRECTORY.getAbsolutePath(),
							"a6196673932" + ".jpg");
					InputStreamContent content = new InputStreamContent(
							"image/jpeg", new BufferedInputStream(
									new FileInputStream(file)));
					content.setLength(file.length());
					HttpRequest request = (new NetHttpTransport())
							.createRequestFactory()
							.buildPostRequest(
									new GenericUrl(MEDIA_URL + "/"
											+ uri.getQueryParameter("filename")),
									content);
					request.execute();
				} catch (IOException e) {
				}
			}
		}
	}

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
