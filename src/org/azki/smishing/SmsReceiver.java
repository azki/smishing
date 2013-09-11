package org.azki.smishing;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
	public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	public void onReceive(Context context, Intent intent) {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		if (intent.getAction().equals(ACTION)) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Object[] pdusObj = (Object[]) bundle.get("pdus");
				if (pdusObj != null) {
					int numOfMsg = new SmsMessage[pdusObj.length].length;
					for (int i = 0; i < numOfMsg; i++) {
						SmsMessage message = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
						String msgBody = message.getMessageBody();

						String s = "";
						Matcher m = android.util.Patterns.WEB_URL.matcher(msgBody);
						while (m.find()) {
							String urlStr = m.group();
							s += urlStr + "\n";
							try {
								URL url;
								try {
									url = new URL(urlStr);
								} catch (MalformedURLException ex) {
									url = new URL("http://" + urlStr);
								}
								HttpURLConnection connection = (HttpURLConnection) url.openConnection();
								connection.connect();
								String contentType = connection.getContentType();
								String contentDisposition = connection.getHeaderField("Content-Disposition");
								connection.disconnect();
								s += contentType + "\n";
								s += contentDisposition + "\n";
							} catch (Exception ex) {
								Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
								Log.e("tag", "error", ex);
							}
						}
						//abortBroadcast();
						Toast.makeText(context, s.trim(), Toast.LENGTH_LONG).show();
					}
				}
			}
		}
	}
}
