package com.dovoq.cubecandy;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.common.io.ByteStreams;

public class HttpHelper {
	private final static String BOUNDARY = "Boundary+A789798EA789798E";
	private static StringBuffer requestBody;

	public static void addEntry(final String key, final String value) {
		HttpHelper.requestBody.append(("\r\n--" + HttpHelper.BOUNDARY));
		HttpHelper.requestBody
				.append((("\r\nContent-Disposition: form-data; name=\"" + key) + "\""));
		HttpHelper.requestBody.append("\r\n\r\n");
		HttpHelper.requestBody.append(value);
	}

	public static void upload(final String dir, final String folder,
			final String fileName) throws ClientProtocolException, IOException,
			JSONException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(
				((("http://dovoq.com/upload_params/s3/?filename=photoplus/free/" + folder) + "/") + fileName));
		get.setHeader("Authorization",
				"Token e91c2126c2a9ba77000a932fc11a85bbb29e8f54");
		HttpResponse response = client.execute(get);
		if (response.getStatusLine().getStatusCode() == 200) {
			BufferedReader r = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			StringBuilder total = new StringBuilder();
			String line = null;
			while ((line = r.readLine()) != null) {
				total.append(line);
			}
			Log.i("PhotoPlus", total.toString());
			final JSONObject jsonObj = new JSONObject(total.toString());
			HttpHelper.requestBody = new StringBuffer();

			@SuppressWarnings("unchecked")
			Iterator<String> iter = jsonObj.keys();
			while (iter.hasNext()) {
				String key = iter.next();
				try {
					HttpHelper.addEntry(key, (String) jsonObj.get(key));
				} catch (JSONException e) {
					// Something went wrong!
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
			try {
				url = new URL("http://media.dovoq.com/");
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

				File fn = new File((dir + fileName));
				if (!fn.exists()) {
					return;
				}
				byte[] bytes;
				bytes = ByteStreams.toByteArray(new BufferedInputStream(
						new FileInputStream(fn)));
				dataOS.write(bytes, 0, bytes.length);
				dataOS.writeBytes((("\r\n--" + HttpHelper.BOUNDARY) + "--\r\n"));
				dataOS.flush();
				dataOS.close();
				int responseCode = conn.getResponseCode();
				if ((responseCode != 201)) {
					Log.i("PhotoPlus", "http upload failed with code "
							+ Integer.valueOf(responseCode));
				} else {
					Log.i("PhotoPlus", "http upload success");
				}
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
