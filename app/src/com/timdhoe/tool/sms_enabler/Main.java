package com.timdhoe.tool.sms_enabler;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.timdhoe.tool.sms_enabler.Shell.ShellException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Main extends Activity {
	String sd_path = Environment.getExternalStorageDirectory().toString();
	static String fileMD5;
	// int success = 1;
	// File retrievedFile;
	ProgressBar prgBar;
	TextView wait;
	TextView minute;
	File originFile = new File(sd_path + "/framework-res.apk");
	String originF = sd_path + "/framework-res.apk";

	public Main() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		prgBar = (ProgressBar) findViewById(R.id.prgBar);
		wait = (TextView) findViewById(R.id.txtWait);
		minute = (TextView) findViewById(R.id.txtMinutes);
	}

	public void FileSystemAccess(View v) {
		/*try {
		
			Process s = Runtime.getRuntime().exec("su");
			DataOutputStream su = new DataOutputStream(s.getOutputStream());
			su.flush();
			su.close();
			s.waitFor();
			Process p = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			// Shell commands to copy framework-res.apk to sdcard
			String cmd = "cp /system/framework/framework-res.apk " + sd_path
					+ "/framework-res.apk";
			String remount = "mount -o remount,rw /system";
			Log.i("SMS-Enabler", "EXEC " + cmd);
			os.writeBytes(remount + "\n" + cmd + "\n");
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		// makeMD5(sd_path + "framework-res.apk");
		String cmd = "cp /system/framework/framework-res.apk " + sd_path + "/framework-res.apk";
		String remount = "mount -o remount,rw /system";
		try {
			Shell.setSuShell();
			Shell.nativeExec(remount);
			Shell.nativeExec(cmd);
			fileMD5 = IntegrityUtils.getMD5Checksum(originFile);
			Log.i("SMS-Enabler",fileMD5);
		} catch (ShellException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		prgBar.setVisibility(1);
		wait.setVisibility(1);
		minute.setVisibility(1);
		getLocalFile();
	}
	/*
	public static final String makeMD5(final String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			Log.e("APP", hexString.toString());
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	*/
	public void getLocalFile() {
		File file = new File(sd_path, "framework-res.apk");
		String url = "http://ec2-50-112-53-255.us-west-2.compute.amazonaws.com:8080/upload_file.php";
		new FileUpload(file, url, fileMD5);
	}
	/*
	public static byte[] createChecksum(String filename) throws Exception {
		InputStream fis = new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();
		return complete.digest();
	}

	// see this How-to for a faster way to convert
	// a byte array to a HEX string
	public static String getMD5Checksum(String filename) throws Exception {
		byte[] b = createChecksum(filename);
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		Log.e("SMS-Enabler", result);
		fileMD5 = result;
		return result;
	}
	*/
}
