package com.timdhoe.tool.sms_enabler;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.Environment;
import android.util.Log;

public class FileRetrieve extends Thread implements Runnable {
	String sd_path = Environment.getExternalStorageDirectory().toString();
	String sUrl = "http://ec2-50-112-53-255.us-west-2.compute.amazonaws.com:8080/upload/framework-res_";
	String fileMD5;
	int success = 1;
	int retryCount = 0;

	public FileRetrieve(String fileMD5) {
		this.fileMD5 = fileMD5;
		sUrl = sUrl + fileMD5 + "_new.apk";
		Log.i("SMS-Enabler", "GET " + String.valueOf(sUrl));
		// initializing and starting a new local Thread object
		Thread uploadThread = new Thread(this);
		uploadThread.start();
	}

	@Override
	public void run() {
		downloadFile(sUrl);
		Log.e("Appp", "Download File seems completed");
		pushFramework();
	}

	protected int downloadFile(String sUrl) {
		try {
			sleep(3000);
			// wait for file creation
			URL url = new URL(sUrl);
			URLConnection connection = url.openConnection();
			connection.setReadTimeout(1000);
			connection.connect();
			// download the file
			InputStream input = new BufferedInputStream(url.openStream());
			OutputStream output = new FileOutputStream(sd_path
					+ "/framework-res_smsenabled.apk");
			byte data[] = new byte[1024];
			int count;
			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
			}
			output.flush();
			output.close();
			input.close();
		} catch (FileNotFoundException e) {
			try {
				if (retryCount < 11) {
					retryCount++;
					sleep(10000);
					Log.e("SMS-Enabler",
							"ERROR, Modified framework still processing -> (Sleep)");
					downloadFile(sUrl);
				} else {
					success = 0;
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				success = 0;
			}
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
			success = 0;
		} catch (InterruptedException e) {
			e.printStackTrace();
			success = 0;
		}
		Log.i("SMS-Enabler", "Successful Download");
		return success;
	}

	public void pushFramework() {
		PushNewFramework fr = new PushNewFramework();
		fr.pushFramework(success);
	}
}
