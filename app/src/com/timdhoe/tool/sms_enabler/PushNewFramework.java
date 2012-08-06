package com.timdhoe.tool.sms_enabler;

import android.os.Environment;
import android.util.Log;

import com.timdhoe.tool.sms_enabler.Shell.ShellException;

public class PushNewFramework {
	String sd_path = Environment.getExternalStorageDirectory().toString();

	public PushNewFramework() {
	}

	public void pushFramework(int succes) {
		if (succes == 1) {
			String copy = "cp " + sd_path + "/framework-res_smsenabled.apk "
					+ "/system/framework/framework-res.apk";
			String reboot = "reboot";
			String remount = "mount -o remount,rw /system";
			try {
				Shell.setSuShell();
				Shell.suExec(remount);
				Log.e("SMS-Enabler", "Remount");
				Shell.suExec(copy);
				Log.e("SMS-Enabler", "Copy");
				Log.e("SMS-Enabler", "Rebooting");
				Shell.suExec(reboot);
			} catch (ShellException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
