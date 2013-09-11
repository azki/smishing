package org.azki.smishing;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;

public class SmsReceiver extends BroadcastReceiver {
	public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION)) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Object[] pdusObj = (Object[]) bundle.get("pdus");
				if (pdusObj != null) {
					int numOfMsg = new SmsMessage[pdusObj.length].length;
					for (int i = 0; i < numOfMsg; i++) {
						SmsMessage message = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
						String msgOriginating = message.getOriginatingAddress();
						String msgBody = message.getMessageBody();

						Matcher m = android.util.Patterns.WEB_URL.matcher(msgBody);
						boolean canNetworkInMainThread = false;
						while (m.find()) {
							String urlStr = m.group();
							if (urlStr.contains(".apk")) {
								blockMsg(context, msgBody, msgOriginating);
								try {
									EasyTracker.getInstance().setContext(context);
									EasyTracker.getTracker().sendView("block by url");
								} catch (Exception ex) {
									Log.e("tag", "ga error", ex);
								}
							} else {
								if (canNetworkInMainThread == false) {
									StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll()
											.build();
									StrictMode.setThreadPolicy(policy);
									canNetworkInMainThread = true;
								}
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

									if (contentType != null && contentType.contains("vnd.android.package-archive")) {
										blockMsg(context, msgBody, msgOriginating);
										try {
											EasyTracker.getInstance().setContext(context);
											EasyTracker.getTracker().sendView("block by contentType");
										} catch (Exception ex) {
											Log.e("tag", "ga error", ex);
										}
									} else if (contentDisposition != null && contentDisposition.contains(".apk")) {
										blockMsg(context, msgBody, msgOriginating);
										try {
											EasyTracker.getInstance().setContext(context);
											EasyTracker.getTracker().sendView("block by contentDisposition");
										} catch (Exception ex) {
											Log.e("tag", "ga error", ex);
										}
									}
								} catch (Exception ex) {
									Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
									Log.e("tag", "error", ex);
								}
							}
						}
					}
				}
			}
		}
	}

	private void blockMsg(Context context, String msgBody, String msgOriginating) {
		abortBroadcast();

		RowData rowData = new RowData();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd E HH:mm:ss", Locale.getDefault());
		rowData.when = sdf.format(cal.getTime());
		rowData.body = msgBody;
		rowData.sender = msgOriginating;
		Gson gson = new Gson();
		String rawJson = gson.toJson(rowData);

		SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_MULTI_PROCESS);
		int blockedCount = pref.getInt("blockedCount", 0);
		Editor editor = pref.edit();
		editor.putString("blocked" + blockedCount, rawJson);
		editor.putInt("blockedCount", blockedCount + 1);
		editor.commit();

		String title = context.getResources().getString(R.string.blocked_msg);
		String text = context.getResources().getString(R.string.blocked_msg_detail, msgOriginating);

		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, new Random().nextInt(), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher).setTicker(title).setContentTitle(title).setContentText(text)
				.setAutoCancel(true).setContentIntent(resultPendingIntent);
		Notification notification = mBuilder.build();
		notification.defaults = Notification.DEFAULT_ALL;
		notification.flags = Notification.FLAG_SHOW_LIGHTS;
		nm.notify(new Random().nextInt(), notification);
	}
}
