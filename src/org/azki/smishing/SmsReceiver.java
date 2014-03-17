package org.azki.smishing;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.azki.smishing.pro.R;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.gson.Gson;

public class SmsReceiver extends BroadcastReceiver {
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
	public static final String TOP_LEVEL_DOMAIN_STR_FOR_WEB_URL = "(?:"
			+ "(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])"
			+ "|(?:biz|b[abdefghijmnorstvwyz])"
			+ "|(?:cat|com|coop|c[acdfghiklmnoruvxyz])"
			+ "|d[ejkmoz]"
			+ "|(?:edu|e[cegrstu])"
			+ "|f[ijkmor]"
			+ "|(?:gov|g[abdefghilmnpqrstuwy])"
			+ "|h[kmnrtu]"
			+ "|(?:info|int|i[delmnoqrst])"
			+ "|(?:jobs|j[emop])"
			+ "|k[eghimnprwyz]"
			+ "|l[abcikrstuvy]"
			+ "|(?:mil|mobi|museum|m[acdeghklmnopqrstuvwxyz])"
			+ "|(?:name|net|n[acefgilopruz])"
			+ "|(?:org|om)"
			+ "|(?:pro|p[aefghklmnrstwy])"
			+ "|qa"
			+ "|r[eosuw]"
			+ "|s[abcdeghijklmnortuvyz]"
			+ "|(?:tel|travel|t[cdfghjklmnoprtvwz])"
			+ "|u[agksyz]"
			+ "|v[aceginu]"
			+ "|w[fs]"
			+ "|(?:\u03b4\u03bf\u03ba\u03b9\u03bc\u03ae|\u0438\u0441\u043f\u044b\u0442\u0430\u043d\u0438\u0435|\u0440\u0444|\u0441\u0440\u0431|\u05d8\u05e2\u05e1\u05d8|\u0622\u0632\u0645\u0627\u06cc\u0634\u06cc|\u0625\u062e\u062a\u0628\u0627\u0631|\u0627\u0644\u0627\u0631\u062f\u0646|\u0627\u0644\u062c\u0632\u0627\u0626\u0631|\u0627\u0644\u0633\u0639\u0648\u062f\u064a\u0629|\u0627\u0644\u0645\u063a\u0631\u0628|\u0627\u0645\u0627\u0631\u0627\u062a|\u0628\u06be\u0627\u0631\u062a|\u062a\u0648\u0646\u0633|\u0633\u0648\u0631\u064a\u0629|\u0641\u0644\u0633\u0637\u064a\u0646|\u0642\u0637\u0631|\u0645\u0635\u0631|\u092a\u0930\u0940\u0915\u094d\u0937\u093e|\u092d\u093e\u0930\u0924|\u09ad\u09be\u09b0\u09a4|\u0a2d\u0a3e\u0a30\u0a24|\u0aad\u0abe\u0ab0\u0aa4|\u0b87\u0ba8\u0bcd\u0ba4\u0bbf\u0baf\u0bbe|\u0b87\u0bb2\u0b99\u0bcd\u0b95\u0bc8|\u0b9a\u0bbf\u0b99\u0bcd\u0b95\u0baa\u0bcd\u0baa\u0bc2\u0bb0\u0bcd|\u0baa\u0bb0\u0bbf\u0b9f\u0bcd\u0b9a\u0bc8|\u0c2d\u0c3e\u0c30\u0c24\u0c4d|\u0dbd\u0d82\u0d9a\u0dcf|\u0e44\u0e17\u0e22|\u30c6\u30b9\u30c8|\u4e2d\u56fd|\u4e2d\u570b|\u53f0\u6e7e|\u53f0\u7063|\u65b0\u52a0\u5761|\u6d4b\u8bd5|\u6e2c\u8a66|\u9999\u6e2f|\ud14c\uc2a4\ud2b8|\ud55c\uad6d|xn\\-\\-0zwm56d|xn\\-\\-11b5bs3a9aj6g|xn\\-\\-3e0b707e|xn\\-\\-45brj9c|xn\\-\\-80akhbyknj4f|xn\\-\\-90a3ac|xn\\-\\-9t4b11yi5a|xn\\-\\-clchc0ea0b2g2a9gcd|xn\\-\\-deba0ad|xn\\-\\-fiqs8s|xn\\-\\-fiqz9s|xn\\-\\-fpcrj9c3d|xn\\-\\-fzc2c9e2c|xn\\-\\-g6w251d|xn\\-\\-gecrj9c|xn\\-\\-h2brj9c|xn\\-\\-hgbk6aj7f53bba|xn\\-\\-hlcj6aya9esc7a|xn\\-\\-j6w193g|xn\\-\\-jxalpdlp|xn\\-\\-kgbechtv|xn\\-\\-kprw13d|xn\\-\\-kpry57d|xn\\-\\-lgbbat1ad8j|xn\\-\\-mgbaam7a8h|xn\\-\\-mgbayh7gpa|xn\\-\\-mgbbh1a71e|xn\\-\\-mgbc0a9azcg|xn\\-\\-mgberp4a5d4ar|xn\\-\\-o3cw4h|xn\\-\\-ogbpf8fl|xn\\-\\-p1ai|xn\\-\\-pgbs0dh|xn\\-\\-s9brj9c|xn\\-\\-wgbh1c|xn\\-\\-wgbl6a|xn\\-\\-xkc2al3hye2a|xn\\-\\-xkc2dl3a5ee0h|xn\\-\\-yfro4i67o|xn\\-\\-ygbi2ammx|xn\\-\\-zckzah|xxx)"
			+ "|y[et]" + "|z[amw]))";
	public static final String GOOD_IRI_CHAR = "a-zA-Z0-9\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";
	public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
	private boolean mCanNetworkInMainThread;
	private Context mContext;

	public void onReceive(Context context, Intent intent) {
		mCanNetworkInMainThread = false;
		mContext = context;
		if (intent.getAction().equals(ACTION)) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Object[] pdusObj = (Object[]) bundle.get("pdus");
				if (pdusObj != null) {
					int numOfMsg = new SmsMessage[pdusObj.length].length;
					for (int i = 0; i < numOfMsg; i++) {
						try {
							SmsMessage message = SmsMessage
									.createFromPdu((byte[]) pdusObj[i]);
							String msgOriginating = message
									.getOriginatingAddress();
							String msgBody = message.getMessageBody();

							Matcher m = getUrlPatternsMatcher(msgBody);
							while (m.find()) {
								String urlStr = m.group();
								checkUrl(urlStr, msgOriginating, msgBody, 1);
							}
						} catch (Exception ex) {
							Log.e("tag", "error", ex);
							gaLog("errorSms", ex, ex.toString());
						}
					}
				}
			}
		}

		if (mContext.getString(R.string.app_name).contains("Pro")) {
			EasyTracker.getInstance(mContext).send(
					MapBuilder.createEvent("sms", "init", "pro_version", null)
							.build());
		} else {
			EasyTracker.getInstance(mContext).send(
					MapBuilder.createEvent("sms", "init", "free_version", null)
							.build());
		}
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private Matcher getUrlPatternsMatcher(String msgBody) {
		Matcher m;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			m = android.util.Patterns.WEB_URL.matcher(msgBody);
		} else {
			m = Pattern
					.compile(
							"((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
									+ "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
									+ "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
									+ "((?:(?:["
									+ GOOD_IRI_CHAR
									+ "]["
									+ GOOD_IRI_CHAR
									+ "\\-]{0,64}\\.)+" // named host
									+ TOP_LEVEL_DOMAIN_STR_FOR_WEB_URL
									+ "|(?:(?:25[0-5]|2[0-4]" // or ip address
									+ "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]"
									+ "|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1]"
									+ "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
									+ "|[1-9][0-9]|[0-9])))"
									+ "(?:\\:\\d{1,5})?)" // plus option port
															// number
									+ "(\\/(?:(?:["
									+ GOOD_IRI_CHAR
									+ "\\;\\/\\?\\:\\@\\&\\=\\#\\~" // plus
																	// option
																	// query
																	// params
									+ "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?"
									+ "(?:\\b|$)").matcher(msgBody);
		}
		return m;
	}

	void checkUrl(String urlStr, String msgOriginating, String msgBody,
			int depth) {
		if (depth > 5) {
			return;
		}
		if (urlStr.contains(".apk")) {
			blockMsgAndLog(msgOriginating, msgBody, "block by url", urlStr);
		} else {
			turnOnCanNetworkInMainThread();
			HttpURLConnection connection = null;
			URL url = null;
			try {
				url = getUrlFromUrlStr(urlStr);
				if (url.getProtocol().equals("https")) {
					trustAllHosts();
					HttpsURLConnection httpsConnection = (HttpsURLConnection) url
							.openConnection();
					httpsConnection.setHostnameVerifier(DO_NOT_VERIFY);
					connection = httpsConnection;
				} else {
					connection = (HttpURLConnection) url.openConnection();
				}
				connection.connect();

				String contentType = connection.getContentType();
				String contentDisposition = connection
						.getHeaderField("Content-Disposition");
				String redirectLocation = connection.getHeaderField("Location");
				String fileName = null;
				try {
					fileName = connection.getURL().getFile();
				} catch (Exception ex) {
					Log.e("tag", "error", ex);
					gaLog("errorFileName", ex, ex.toString());
				}
				Log.d("tag", "contentType: " + contentType);
				Log.d("tag", "contentDisposition: " + contentDisposition);
				Log.d("tag", "redirectLocation: " + redirectLocation);
				Log.d("tag", "fileName: " + fileName);
				if (redirectLocation != null) {
					checkRedirectLocation(redirectLocation, msgOriginating,
							msgBody, depth);
				} else if (fileName != null && fileName.contains(".apk")) {
					blockMsgAndLog(msgOriginating, msgBody,
							"block by fileName", urlStr);
				} else if (contentType != null
						&& contentType.contains("vnd.android.package-archive")) {
					blockMsgAndLog(msgOriginating, msgBody,
							"block by contentType", urlStr);
				} else if (contentDisposition != null
						&& contentDisposition.contains(".apk")) {
					blockMsgAndLog(msgOriginating, msgBody,
							"block by contentDisposition", urlStr);
				} else if (contentType != null && contentType.contains("text")) {
					String contentText = readStream(connection);
					Log.d("tag", "contentText: " + contentText);

					checkMetaPattern(contentText, msgOriginating, msgBody,
							depth);
					checkScriptPattern(contentText, msgOriginating, msgBody,
							depth);
				}
			} catch (Exception ex) {
				Log.e("tag", "error", ex);
				gaLog("connectionError", ex, url.toString());
			} finally {
				closeConnection(connection);
			}
		}
	}

	private static void trustAllHosts() {
		X509TrustManager easyTrustManager = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
				// Oh, I am easy!
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
				// Oh, I am easy!
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { easyTrustManager };
		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void closeConnection(HttpURLConnection connection) {
		try {
			if (connection != null) {
				connection.disconnect();
			}
		} catch (Exception ex) {
		}
	}

	void checkRedirectLocation(String redirectLocation, String msgOriginating,
			String msgBody, int depth) {
		if (redirectLocation.contains("://")) {
			checkUrl(redirectLocation, msgOriginating, msgBody, depth + 1);
			gaLog("redirectLocation", "absolute", redirectLocation);
		} else {
			gaLog("redirectLocation", "relative", redirectLocation);
		}
	}

	void checkMetaPattern(String contentText, String msgOriginating,
			String msgBody, int depth) {
		Pattern refreshMetaPattern = Pattern.compile("<meta[^>]+refresh[^>]*>",
				Pattern.CASE_INSENSITIVE);
		Matcher m = refreshMetaPattern.matcher(contentText);
		while (m.find()) {
			String metaTagStr = m.group();
			Pattern urlPatternInMetaTag = Pattern.compile(
					"url=['\"]?([^'\";\\s>]+)", Pattern.CASE_INSENSITIVE);
			Matcher m2 = urlPatternInMetaTag.matcher(metaTagStr);
			if (m2.find()) {
				String metaTagUrlStr = m2.group(1);
				checkRedirectLocation(metaTagUrlStr, msgOriginating, msgBody,
						depth + 1);
				gaLog("findPattern", "refreshMetaPattern", metaTagUrlStr);
			}
		}
	}

	void checkScriptPattern(String contentText, String msgOriginating,
			String msgBody, int depth) {
		Pattern redirectScriptPattern = Pattern.compile(
				"location.href\\s*=\\s*['\"]([^'\"]+)['\"]",
				Pattern.CASE_INSENSITIVE);
		Matcher m = redirectScriptPattern.matcher(contentText);
		if (m.find()) {
			String scriptTagUrlStr = m.group(1);
			checkRedirectLocation(scriptTagUrlStr, msgOriginating, msgBody,
					depth + 1);
			gaLog("findPattern", "redirectScriptPattern", scriptTagUrlStr);
		}
	}

	String readStream(HttpURLConnection connection) throws Exception {
		BufferedInputStream inputStream = new BufferedInputStream(
				connection.getInputStream());
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(
				new InputStreamReader(inputStream), 1024);
		for (String line = r.readLine(); line != null; line = r.readLine()) {
			sb.append(line);
			if (sb.length() > 2048) {
				break;
			}
		}
		return sb.toString();
	}

	URL getUrlFromUrlStr(String urlStr) throws MalformedURLException {
		URL url;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException ex) {
			url = new URL("http://" + urlStr);
		}
		return url;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	void turnOnCanNetworkInMainThread() {
		if (mCanNetworkInMainThread == false) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}
			mCanNetworkInMainThread = true;
		}
	}

	void blockMsgAndLog(String msgOriginating, String msgBody, String logMsg,
			String url) {
		blockMsg(mContext, msgBody, msgOriginating);
		gaLog("blockMsg", logMsg, url);
	}

	void gaLog(String category, Exception ex, String label) {
		String action = "unknown";
		try {
			action = ex.getClass().getCanonicalName();
		} catch (Throwable ignore) {
		}
		gaLog(category, action, label);
	}

	void gaLog(String category, String action, String label) {
		try {
			EasyTracker.getInstance(mContext).send(
					MapBuilder.createEvent(category, action, label, null)
							.build());
		} catch (Throwable ignore) {
		}
	}

	void blockMsg(Context context, String msgBody, String msgOriginating) {
		abortBroadcast();

		String rawJson = getRawJsonFromMsg(msgBody, msgOriginating);
		saveBlockedToPref(context, rawJson);

		String title = context.getResources().getString(R.string.blocked_msg);
		String text = context.getResources().getString(
				R.string.blocked_msg_detail, msgOriginating);
		showNotification(context, title, text);
	}

	String getRawJsonFromMsg(String msgBody, String msgOriginating) {
		RowData rowData = new RowData();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd E HH:mm:ss",
				Locale.getDefault());
		rowData.when = sdf.format(cal.getTime());
		rowData.body = msgBody;
		rowData.sender = msgOriginating;
		Gson gson = new Gson();
		String rawJson = gson.toJson(rowData);
		return rawJson;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	void saveBlockedToPref(Context context, String rawJson) {
		SharedPreferences pref = context.getSharedPreferences("pref",
				Context.MODE_MULTI_PROCESS);
		int blockedCount = pref.getInt("blockedCount", 0);
		Editor editor = pref.edit();
		editor.putString("blocked" + blockedCount, rawJson);
		editor.putInt("blockedCount", blockedCount + 1);
		editor.commit();
	}

	void showNotification(Context context, String title, String text) {
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
				new Random().nextInt(), intent, 0);
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_launcher).setTicker(title)
				.setContentTitle(title).setContentText(text)
				.setAutoCancel(true).setContentIntent(resultPendingIntent);
		Notification notification = mBuilder.build();
		notification.defaults = Notification.DEFAULT_ALL;
		notification.flags = Notification.FLAG_SHOW_LIGHTS
				| Notification.FLAG_AUTO_CANCEL;
		nm.notify(new Random().nextInt(), notification);
	}
}
