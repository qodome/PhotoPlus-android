package com.dovoq.cubecandy.tmp;

import static com.nyssance.android.util.LogUtils.logi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.dovoq.cubecandy.Constants;
import com.google.common.io.ByteStreams;
import com.nyssance.android.util.LogUtils;

public class HttpHelper implements Constants {
	private final static String BOUNDARY = "Boundary+A789798EA789798E";
	private static StringBuffer requestBody;

	public static void addEntry(final String key, final String value) {
		HttpHelper.requestBody.append(("\r\n--" + HttpHelper.BOUNDARY));
		HttpHelper.requestBody
				.append((("\r\nContent-Disposition: form-data; name=\"" + key) + "\""));
		HttpHelper.requestBody.append("\r\n\r\n");
		HttpHelper.requestBody.append(value);
	}

	public static void upload(String dir, String path)
			throws ClientProtocolException, IOException, JSONException {
		LogUtils.loge("repost b");
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(BASE_URL + "/upload_params/s3/?filename="
				+ path);
		LogUtils.loge("repost c");
		get.setHeader("Authorization", "Token " + DEFAULT_TOKEN);
		LogUtils.loge("repost d");
		HttpResponse response = client.execute(get);
		LogUtils.loge(response.toString());
		if (response.getStatusLine().getStatusCode() == 200) {
			BufferedReader r = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			StringBuilder total = new StringBuilder();
			String line = null;
			while ((line = r.readLine()) != null) {
				total.append(line);
			}
			final JSONObject jsonObj = new JSONObject(total.toString());
			HttpHelper.requestBody = new StringBuffer();

			@SuppressWarnings("unchecked")
			Iterator<String> iter = jsonObj.keys();
			LogUtils.loge("repost0");
			while (iter.hasNext()) {
				String key = iter.next();
				try {
					HttpHelper.addEntry(key, (String) jsonObj.get(key));
				} catch (JSONException e) {
				}
			}

			HttpHelper.requestBody.append(("\r\n--" + HttpHelper.BOUNDARY));
			HttpHelper.requestBody
					.append("\r\nContent-Disposition: form-data; name=\"file\"; filename=\""
							+ jsonObj.getString("key") + "\"");
			HttpHelper.requestBody.append("\r\nContent-Type: image/jpg");
			HttpHelper.requestBody.append("\r\n\r\n");

			HttpURLConnection conn = null;
			URL url;
			DataOutputStream dataOS;
			LogUtils.loge("repost11");
			try {
				url = new URL(MEDIA_URL);
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setUseCaches(false);
				conn.setRequestMethod("POST");
				conn.setRequestProperty(
						"Content-Type",
						("multipart/form-data; boundary=" + HttpHelper.BOUNDARY));
				dataOS = new DataOutputStream(conn.getOutputStream());
				dataOS.writeBytes(HttpHelper.requestBody.toString());
				LogUtils.loge("repost2");
				File fn = new File(dir, FilenameUtils.getName(path));
				if (!fn.exists()) {
					return;
				}
				LogUtils.loge("repost3");
				byte[] bytes;
				bytes = ByteStreams.toByteArray(new BufferedInputStream(
						new FileInputStream(fn)));
				dataOS.write(bytes, 0, bytes.length);
				dataOS.writeBytes((("\r\n--" + HttpHelper.BOUNDARY) + "--\r\n"));
				dataOS.flush();
				dataOS.close();
				int responseCode = conn.getResponseCode();
				if ((responseCode != 201)) {
					logi("http upload failed with code "
							+ Integer.valueOf(responseCode));
				} else {
					logi("http upload success");
				}
			} catch (IOException e) {
			}
		}
	}
}
