package skimp.partner.store.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import skimp.partner.store.R;


public class UpnsNotifyHelper {
	private static final String TAG = UpnsNotifyHelper.class.getSimpleName();

	public static void showNotification(final Context context, final JSONObject jsonMsg, final String psid, final String encryptData) throws Exception {
	    String extData = "";
	    //final String messageuniquekey = jsonMsg.getString("MESSAGEID");
	    if (jsonMsg.has("EXT")) {
	        extData = jsonMsg.getString("EXT");
	        //extData = URLDecoder.decode(extData, "UTF-8");
	        System.out.println("extData: " + extData);
	    }
	 
	    //extData = "http:/211.241.199.158:8180/msg/totalInfo/0218115649_msp.html";
	    //extData = "3|145,text|null";
	    if (extData.startsWith("http")) {
			Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
	        		if(msg.what == 0) {
	        			String message = (String) msg.obj;
	        			if (message != null) {
	        				message = message.replaceAll("https", "http");
	        				message = message.replaceAll("\\\\", "/");

	        				try { 
	        					// "TYPE":"R","VAR":"hoseok|2015-06-31|2015-07-30|104320|11"
	        					if (jsonMsg.has("TYPE") && jsonMsg.getString("TYPE").equals("R")) {
	        						String var = jsonMsg.getString("VAR");
	        						HashMap<String, String> varMap = new HashMap<String, String>();
	        						if (var != null) {
	        							String[] varArray = var.split("\\|");
	        							for (int i=0; i<varArray.length; i++) {
	        								int idx = i+1;
	        								varMap.put("%VAR" + idx + "%", varArray[i]);
	        								//Log.d("test", "%VAR" + idx + "%" + " " +  varArray[i]);
	        							}
	        							
	        							Iterator<?> keys = varMap.keySet().iterator();
	        							while (keys.hasNext()) {
	        								String key = (String) keys.next();
	        								message = message.replaceAll(key, varMap.get(key));
	        								//Log.d("test", "message: " + message);
	        							}
	        						}
	        					}
	        				} catch (JSONException e) {
	        					e.printStackTrace();
	        				}
	        			}
	        			
	        			//DBUtils.getDbOpenHelper(context).insertPushMSG(messageuniquekey, jsonMsg.toString(), message, encryptData);
	        			createNotification(context, jsonMsg, psid, encryptData, message);
	        		}
				}
			};
			new HttpGetStringThread(handler, extData).start();
		}
		else {
			//DBUtils.getDbOpenHelper(context).insertPushMSG(messageuniquekey, jsonMsg.toString(), extData, encryptData);
			createNotification(context, jsonMsg, psid, encryptData, extData);
		}
	}	
	
	private static void createNotification(final Context context, final JSONObject jsonMsg, final String psid, final String encryptData, String message) {
		ArrayList<String> params = null;
		if (message != null) {
			String[] paramArray = message.split("\\|");
			params = new ArrayList<String>(Arrays.asList(paramArray));
		}

		Log.d("NotificationManager", "[NotificationManager] params size:: " + params.size());
		if (params != null && params.size() > 0) {
			try {
				jsonMsg.remove("EXT");
//				jsonMsg.put("EXT", message);
				try {
					// 파이프(|)없으면 마지막의 json 데이터만 ext에 넣는다.
					JSONObject extJson = new JSONObject(message);
					jsonMsg.put("EXT", extJson);
				} catch (Exception e1) {
					try {
						// 파이프(|)있으면 마지막의 json 데이터만 ext에 넣는다.
						JSONObject extJson = new JSONObject(params.get(params.size() - 1));
						jsonMsg.put("EXT", extJson);
					} catch (Exception e2) {
						jsonMsg.put("EXT", message);
					}
				}

				if (params.get(0).equals("0") || params.get(0).equals("4")) {
					Log.d("NotificationManager", "[NotificationManager] defaultNotification(0 or 4): " + message);
					defaultNotification(context, jsonMsg, params.get(1), psid, message, encryptData, null);
				}
				else if (params.get(0).equals("11")) { //시큐어 푸시를 위한 구분 (Icon 이미지를 포함한 ext)
					Log.d("NotificationManager", "[NotificationManager] defaultNotification(11): " + message);
					showIconNotification(context, jsonMsg, params.get(1), psid, message, encryptData, params.get(2));
				}
				else if (params.size() > 2) {
					Log.d("NotificationManager", "[NotificationManager] showImageNotification(2 or 3): " + message);
					String url = params.get(2);
					if (TextUtils.isEmpty(url) || "null".equals(url) || params.get(0).equals("6")) {
						defaultNotification(context, jsonMsg, params.get(1), psid, message, encryptData, null);
					}
					else {
						showImageNotification(context, jsonMsg, params.get(1), params.get(2), psid, message, encryptData);
					}
				}
				else {
					Log.d("NotificationManager", "[NotificationManager] UNKNOUWN TYPE:: " + params.get(0));
					defaultNotification(context, jsonMsg, message, psid, message, encryptData, null);
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void showIconNotification(final Context context, final JSONObject jsonMsg, 
			final String strMessage, final String psid, final String ext, final String encryptData, String iconPath) {
		if (iconPath.contains("https")) {
			iconPath = iconPath.replaceAll("https", "http");
			iconPath = iconPath.replaceAll("\\\\", "/");
		}

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();
		ImageLoader.getInstance().init(config);
		ImageLoader.getInstance().loadImage(iconPath, new SimpleImageLoadingListener() {
			
		    @Override
		    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		    	System.out.println("loadedImage.getWidth(): " + loadedImage.getWidth());
		    	System.out.println("loadedImage.getHeight(): " + loadedImage.getHeight());
		    	Bitmap scaledImage = Bitmap.createScaledBitmap(loadedImage, 192, 192, false);
		    	if (loadedImage != null) {
		    		loadedImage.recycle();
		    		loadedImage = null;
		    	}
		    	defaultNotification(context, jsonMsg, strMessage, psid, ext, encryptData, scaledImage);
		    }
		});		
	}

	private static void defaultNotification(Context context, JSONObject jsonMsg, String strMessage, String psid, String ext, String encryptData, Bitmap loadedIcon) {
		String title = null;
		String body = null;
		String alertMessage = strMessage;
		try {
			alertMessage = jsonMsg.getString("MESSAGE");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			JSONObject msgJson = new JSONObject(alertMessage);
			Log.w("UpnsNotifyHelper", "title = " + msgJson.getString("title"));
			Log.w("UpnsNotifyHelper", "body = " + msgJson.getString("body"));
			title = msgJson.getString("title");
			body = msgJson.getString("body");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		alertMessage = (TextUtils.isEmpty(alertMessage))? "" : alertMessage;
		
	    int icon = R.drawable.ic_launcher;
		Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
	    if (loadedIcon != null) {
	    	largeIcon = loadedIcon;
	    }
	    
//	    String title = context.getString(R.string.app_name);
	    //int seqno = Integer.parseInt(jsonMsg.getString("SEQNO"));
	    //boolean isRunningApp = PushUtils.isRunningPushApps(context);

		int seqno = 0;
		try {
			seqno = Integer.parseInt(jsonMsg.getString("SEQNO"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
        Log.w("UpnsNotifyHelper", "seqno = " + seqno);

	    Intent intent = new Intent(context, PushStatusCheckReceiver.class);
	    intent.setAction(context.getPackageName() + ".ACTION_NOTIFICATION_HANDLE");
		intent.putExtra("JSON", jsonMsg.toString());
    	intent.putExtra("PS_ID", psid);
    	intent.putExtra("TITLE", alertMessage);
    	intent.putExtra("EXT", ext);
    	intent.putExtra("ENCRYPT", encryptData);
    	intent.putExtra("PUSH_TYPE", "UPNS");
    	//intent.putExtra("PUSH_STATUS", (isRunningApp)? PushConstants.APP_STATUS_ACTIVE : PushConstants.APP_STATUS_BACKGROUND);
    	//intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); 
	    
		PendingIntent pIntent = PendingIntent.getBroadcast(context, seqno, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationManager mManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), PushNotificationManager.getChannelId(context))
				.setAutoCancel(true)
				.setContentIntent(pIntent)
				.setSmallIcon(icon)
				.setSound(soundUri)
				.setPriority(Notification.PRIORITY_MAX);
		if(TextUtils.isEmpty(body)) {
			mBuilder.setContentText(alertMessage)
					.setStyle(new NotificationCompat.BigTextStyle().bigText(alertMessage));
		} else {
			mBuilder.setContentTitle(title)
					.setContentText(body)
					.setStyle(new NotificationCompat.BigTextStyle().bigText(body));
		}
		
		assert mManager != null;

		mManager.notify("UPNS", seqno, mBuilder.build());
	}


	private static void showImageNotification(Context context, final JSONObject jsonMsg, final String strMessage, String img, final String psid, final String ext, final String encryptData) {
		final int icon = R.drawable.ic_launcher;
		final String title = context.getString(R.string.app_name);
		final Context ctx = context;
		
		if (img.contains("https")) {
			img = img.replaceAll("https", "http");
			img = img.replaceAll("\\\\", "/");
		}
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();
		ImageLoader.getInstance().init(config);
		ImageLoader.getInstance().loadImage(img, new SimpleImageLoadingListener() {
			
		    @Override
		    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				notificationWithBigPicture(ctx, jsonMsg, strMessage, icon, loadedImage, psid, ext, encryptData);
		    }
		});		
	}
	
	private static void notificationWithBigPicture(Context context, JSONObject jsonMsg, String message, int icon, Bitmap banner, String psid, String ext, String encryptData) {
		String title = null;
		String body = null;
		String alertMessage = message;
		try {
			alertMessage = jsonMsg.getString("MESSAGE");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			JSONObject msgJson = new JSONObject(alertMessage);
			Log.w("UpnsNotifyHelper", "title = " + msgJson.getString("title"));
			Log.w("UpnsNotifyHelper", "body = " + msgJson.getString("body"));
			title = msgJson.getString("title");
			body = msgJson.getString("body");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		alertMessage = (TextUtils.isEmpty(alertMessage))? "" : alertMessage;

		//boolean isRunningApp = PushUtils.isRunningPushApps(context);
		Intent intent = new Intent(context, PushStatusCheckReceiver.class);
		intent.setAction(context.getPackageName() + ".ACTION_NOTIFICATION_HANDLE");
		intent.putExtra("JSON", jsonMsg.toString());
		intent.putExtra("PS_ID", psid);
		intent.putExtra("TITLE", alertMessage);
		intent.putExtra("EXT", ext);
		intent.putExtra("ENCRYPT", encryptData);
		intent.putExtra("PUSH_TYPE", "UPNS");
		//intent.putExtra("PUSH_STATUS", (isRunningApp)? PushConstants.APP_STATUS_ACTIVE : PushConstants.APP_STATUS_BACKGROUND);
		//intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); 
    	int seqno = 0;
		try {
			seqno = Integer.parseInt(jsonMsg.getString("SEQNO"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PushNotificationManager.getChannelId(context))
			.setSmallIcon(icon)
			.setLargeIcon(banner)
			.setTicker(title)
			.setContentTitle(title)
			.setContentText(alertMessage)
			.setAutoCancel(true)
			.setPriority(Notification.PRIORITY_MAX);
		if(TextUtils.isEmpty(body)) {
			builder.setContentText(alertMessage);
		} else {
			builder.setContentTitle(title)
					.setContentText(body);
		}

		NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
		style.setBigContentTitle(title);
		style.setSummaryText(message);
		style.bigPicture(banner);
		if(TextUtils.isEmpty(body)) {
			style.setSummaryText(alertMessage);
		} else {
			style.setBigContentTitle(title)
					.setSummaryText(body);
		}
		builder.setStyle(style);
		builder.setContentIntent(pendingIntent);
//		builder.setFullScreenIntent(pendingIntent, true);
		
		builder.setDefaults(Notification.DEFAULT_VIBRATE);
		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		assert notificationManager != null;

		notificationManager.notify("upns", seqno, builder.build());
	}
}