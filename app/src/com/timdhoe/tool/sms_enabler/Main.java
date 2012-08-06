package com.timdhoe.tool.sms_enabler;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.timdhoe.tool.sms_enabler.Shell.ShellException;

public class Main extends Activity {
	String sd_path = Environment.getExternalStorageDirectory().toString();
	static String fileMD5;
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
		String cmd = "cp /system/framework/framework-res.apk " + sd_path
				+ "/framework-res.apk";
		String remount = "mount -o remount,rw /system";
		try {
			Shell.setSuShell();
			Shell.nativeExec(remount);
			Shell.nativeExec(cmd);
			fileMD5 = IntegrityUtils.getMD5Checksum(originFile);
			Log.i("SMS-Enabler", fileMD5);
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

	public void getLocalFile() {
		File file = new File(sd_path, "framework-res.apk");
		String url = "http://ec2-50-112-53-255.us-west-2.compute.amazonaws.com:8080/upload_file.php";
		new FileUpload(file, url, fileMD5);
	}

}
