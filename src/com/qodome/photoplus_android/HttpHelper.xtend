package com.qodome.photoplus_android

import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import java.util.List
import org.apache.http.HttpResponse
import java.net.URI
import android.util.Log
import org.apache.http.client.methods.HttpGet
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import org.json.JSONObject
import java.io.File
import com.google.common.io.CharStreams
import java.io.FileInputStream
import com.google.common.base.Charsets
import java.net.URL
import java.io.DataOutputStream
import com.google.common.io.ByteStreams
import java.io.InputStream
import java.io.BufferedInputStream

class HttpHelper {
	val static BOUNDARY = "Boundary+A789798EA789798E"
	var static StringBuffer requestBody
	
	def static public addEntry(String key, String value) {
		requestBody.append("\r\n--" + BOUNDARY)
		requestBody.append("\r\nContent-Disposition: form-data; name=\"" + key + "\"")
		requestBody.append("\r\n\r\n")
		requestBody.append(value)
	}
	
	def static public upload(String dir, String folder, String fileName) {
		var client = new DefaultHttpClient()
		var get = new HttpGet("http://qodome.com/api/v1/get_upload_params/?app=photoplus&filename=photoplus/test/" + folder + "/" + fileName)
		get.setHeader("Authorization", "Token 707daebb6f44342e9b9c73569404fc8a971db7d3")
		var response = client.execute(get)
		
		if (response.getStatusLine().getStatusCode() == 200) {
			var r = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
			var total = new StringBuilder()
			var String line
			while ((line = r.readLine()) != null) {
    			total.append(line)
			}
			Log.i("PhotoPlus", total.toString())
			val jsonObj = new JSONObject(total.toString())
			requestBody = new StringBuffer() 
			jsonObj.keys().forEach[ key |
				addEntry(key, jsonObj.getString(key))
			]

			requestBody.append("\r\n--" + BOUNDARY)
			requestBody.append("\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + jsonObj.getString("key") + "\"")
			requestBody.append("\r\nContent-Type: image/jpg")
			requestBody.append("\r\n\r\n")
			// 文件内容可能包含\000，以byte方式补上
           		
        	var HttpURLConnection conn = null;

            // Make a connect to the server
            var url = new URL("http://media.qodome.com/");
            conn = url.openConnection() as HttpURLConnection

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

            // Send the body
            var dataOS = new DataOutputStream(conn.getOutputStream());
            dataOS.writeBytes(requestBody.toString())
            
            var fn = new File(dir + fileName)
			if (!fn.exists()) {
				return		
			}
			var bytes = ByteStreams.toByteArray(new BufferedInputStream(new FileInputStream(fn)))
			dataOS.write(bytes, 0, bytes.length())
           	dataOS.writeBytes("\r\n--" + BOUNDARY + "--\r\n")
           	
            dataOS.flush()
            dataOS.close()

            // Ensure we got the HTTP 200 response code
            var responseCode = conn.getResponseCode();
            if (responseCode != 201) {
            	Log.i("PhotoPlus", "http upload failed")  
            } else {
            	Log.i("PhotoPlus", "http upload success")
            }
		}
	}
}