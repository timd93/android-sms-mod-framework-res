package com.timdhoe.tool.sms_enabler;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.threadshandlers.R;
import com.timdhoe.tool.sms_enabler.Shell.ShellException;

public class FrameworkAsyncTask extends Activity {
	//Global variables
	String sd_path = Environment.getExternalStorageDirectory().toString();
	private TextView txtStatus;
	public String fileMD5 = null;
	File file = new File(sd_path, "framework-res.apk");
	private File f = file;
	int counter = 1;
	

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.asynctask);
		txtStatus = (TextView) findViewById(R.id.txtStatus);
	}
	//Get root access and copy framework-res.apk to root of sd-card
	private void getRootAndCopyToSD() throws ShellException{
		String cmd = "cp /system/framework/framework-res.apk " + sd_path
				+ "/framework-res.apk";
		String remount = "mount -o remount,rw /system";
		Shell.setSuShell();
		Shell.suExec(remount);
		Shell.suExec(cmd);
		
		if (Shell.isRootUid() == true){
			txtStatus.setText(txtStatus.getText() + "Superuser OK!\n" + "Copied " + cmd + "\n");
			invokeMD5Checksum();
		}
		
	}
	//Generate MD5 of framework-res.apk
	private class MD5FrameworkTask extends AsyncTask<File, Void, String> {

		@Override
		protected String doInBackground(File... oFile) {
			File[] originalFramework = oFile;
			Log.e("FILE", originalFramework[0].toString());
			try {
				fileMD5 = IntegrityUtils.getMD5Checksum(originalFramework[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return fileMD5;
		}

		@Override
		protected void onPostExecute(String result) {
			txtStatus.setText(txtStatus.getText() + "\nMD5: " + result);
			// If MD5 generation is success, go to next step
			if (result != null) {
				try {
					invokeUpload();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}
	//Upload framework-res.apk to server for processing
	private class UploadFrameworkTask extends AsyncTask<URL, Void, String> {
		protected String doInBackground(URL... fUrl) {
			String success = "success";
			// Set incoming parameters
			String uploadScriptUrl = fUrl[0].toString();
			URL fileUrl = fUrl[1];
			Log.i("URL", fUrl[0].toString());
			Log.i("URL", fUrl[1].toString());
			Log.i("CHECK EXISTS",String.valueOf(f.exists()));
			// HTTP Variables
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(uploadScriptUrl);
			FileBody file = new FileBody(f);
			try {
				MultipartEntity entity = new MultipartEntity();
				entity.addPart("file", file);
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				// Check Status Code
				Log.i("SMS-Enabler: HTTP-Status", String.valueOf(response
						.getStatusLine().getStatusCode()));
				Log.i("SMS-Enabler: HTTP-Post",
						String.valueOf(httppost.getRequestLine()));
				// Send process request
				Log.i("SMS-Enabler", "PROCESS " + fileUrl.toString());
				URLConnection conn = fileUrl.openConnection();
				InputStream in = new BufferedInputStream(conn.getInputStream());
				try {
					readStream(in);
				} catch (Exception e) {
					e.printStackTrace();
					success = "fail";
				} finally {
					in.close();
				}
				Log.e("APPP", "Connected");
			} catch (ClientProtocolException e) {
				Log.e("SMS-ENABLER", "Client Protocol Exception");
				e.printStackTrace();
				success = "fail";
			} catch (IOException e) {
				//Will always fail, not a problem, just needs to be connected long enough to start processing
				Log.i("SMS-ENABLER", "PROCESSING STARTED");
			}
			return success;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			txtStatus.setText(txtStatus.getText() + "\n\n" + "Uploading...");
		}

		private String readStream(InputStream is) {
			try {
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				int i = is.read();
				while (i != -1) {
					bo.write(i);
					i = is.read();
				}
				return bo.toString();
			} catch (IOException e) {
				return "";
			}
		}

		@Override
		protected void onPostExecute(String AsyncTask_Success) {
			if (AsyncTask_Success == "fail") {
				txtStatus.setText(txtStatus.getText() + "\n" + "Uploading framework: Failed! \n");
			}
			else {
				try {
					txtStatus.setText(txtStatus.getText() + "\n" + "Uploading framework: Successful! \n");
					invokeDownload();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	//Download framework-res.apk from server
	private class DownloadFrameworkTask extends AsyncTask<URL, Void, String>{
		String success = "success";
		@Override
		protected String doInBackground(URL... sUrl) {
			URL url = sUrl[0];
			URLConnection connection;
			try {
				connection = url.openConnection();
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
				Log.i("DOWNLOAD",sUrl[0].toString());
			} catch (IOException e) {
				success = "fail";
				Log.i("DOWNLOAD",sUrl[0].toString());
				e.printStackTrace();
			}
			return success;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			txtStatus.setText(txtStatus.getText() + "Processing...\n");
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (success == "success"){
				txtStatus.setText(txtStatus.getText() + "Modded framework downloaded successfully \n");
				//invokeCopyNewFramework();
			}
			else {
				if (counter == 1){
					txtStatus.setText(txtStatus.getText() + "This framework is not yet previously processed\n" + "Please wait while framework is being processed\n");
					++counter;
					try {
						invokeDownload();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				else {
					txtStatus.setText(txtStatus.getText() + "Still processing\n");
				}
			}
		}
		
	}
	//Copy new framework-res.apk to system
	private void copyModdedFrameworkAndReboot(){
		String remount = "mount -o remount,rw /system";
		String copy = "cp " + sd_path + "/framework-res_smsenabled.apk "
				+ "/system/framework/framework-res.apk";
		String chmod = "chmod 644 /system/framework/framework-res.apk";
		String reboot = "reboot";
		
		try {
			Shell.setSuShell();
			Shell.suExec(remount);
			Log.i("SMS-Enabler", "Remount");
			Shell.suExec(copy);
			Log.i("SMS-Enabler", "Copy");
			Shell.suExec(chmod);
			Log.i("SMS-Enabler", "Chmod 644");
			Log.i("SMS-Enabler", "Rebooting");
			Shell.suExec(reboot);
		} catch (ShellException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//Methods for calling AsyncTasks
	public void startProcess(View view) throws ShellException{
		txtStatus.setText("Log: \n");
		getRootAndCopyToSD();
	}
	
	private void invokeMD5Checksum() {
		File originalFramework = new File(sd_path + "/framework-res.apk");
		MD5FrameworkTask task = new MD5FrameworkTask();
		task.execute(originalFramework);
	}

	private void invokeUpload() throws Exception {
		URL fileUrl = new URL("http://54.245.116.85:8080/process.php?md5=" + fileMD5);
		URL uploadScriptUrl = new URL("http://54.245.116.85:8080/upload_file.php");
		UploadFrameworkTask task = new UploadFrameworkTask();
		task.execute(uploadScriptUrl, fileUrl);

	}
	
	private void invokeDownload() throws Exception{
		URL fileUrl = new URL("http://54.245.116.85:8080/upload/framework-res_" + fileMD5 + "_new.apk");
		DownloadFrameworkTask task = new DownloadFrameworkTask();
		task.execute(fileUrl);
	}
	private void invokeCopyNewFramework(){
		copyModdedFrameworkAndReboot();
	}
}