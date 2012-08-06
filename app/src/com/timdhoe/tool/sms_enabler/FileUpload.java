package com.timdhoe.tool.sms_enabler;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class FileUpload extends Thread implements Runnable {
	private File f;
	private String url;
	public String fileMD5;
	
	public FileUpload(File f, String url, String fileMD5) {
		this.url = url;
		this.f = f;
		this.fileMD5 = fileMD5;
		// initializing and starting a new local Thread object
        Thread uploadThread = new Thread(this);
        uploadThread.start();
	}
	
	@Override
	public void run() {
		upload();
	}
	
	private void upload() {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		FileBody file = new FileBody(f);
		try {
			MultipartEntity entity = new MultipartEntity();
			entity.addPart("file", file);
			httppost.setEntity(entity);
			HttpResponse response = httpclient.execute(httppost);
			//Check Status Code
			Log.i("SMS-Enabler: HTTP-Status", String.valueOf(response.getStatusLine().getStatusCode()));
			Log.i("SMS-Enabler: HTTP-Post", String.valueOf(httppost.getRequestLine()));
			//Send process request
			URL url = new URL("http://ec2-50-112-53-255.us-west-2.compute.amazonaws.com:8080/process.php?md5="+fileMD5);
			Log.i("SMS-Enabler","PROCESS " + url.toString());
			URLConnection conn= url.openConnection();
			InputStream in = new BufferedInputStream(conn.getInputStream());
			try {
				readStream(in);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			     in.close();
			}
			Log.e("APPP","Connected");
		} catch (ClientProtocolException e) {
			Log.e("SMS-ENABLER","Client Protocol Exception");
			e.printStackTrace();
		} catch (IOException e) {
			Log.i("SMS-ENABLER","PROCESS invoked");
		} 
		new FileRetrieve(fileMD5);

	}
	
	private String readStream(InputStream is) {
	    try {
	      ByteArrayOutputStream bo = new ByteArrayOutputStream();
	      int i = is.read();
	      while(i != -1) {
	        bo.write(i);
	        i = is.read();
	      }
	      return bo.toString();
	    } catch (IOException e) {
	      return "";
	    }
	}
}
