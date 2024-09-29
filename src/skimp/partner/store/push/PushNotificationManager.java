package skimp.partner.store.push;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import m.client.push.library.common.PushConstants;
import skimp.partner.store.samples.activity.ShowPushPopup;

public class PushNotificationManager {
	private static final String TAG = PushNotificationManager.class.getSimpleName();

	private static String lastestSeqNo = "";

	private static String sChannelId = "";

	private static String createChannel(final Context context, final String channelId, final String channelName) {
		Log.i(TAG, "createChannel(final Context context, final String channelId, final String channelName)");
		Log.d(TAG, "channelId = " + channelId);
		Log.d(TAG, "channelName = " + channelName);

		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
			return null;
		}

		int importance = NotificationManager.IMPORTANCE_HIGH;
		NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
		notificationChannel.setDescription("");
		notificationChannel.enableLights(true);
		notificationChannel.setLightColor(Color.RED);
		notificationChannel.enableVibration(true);
		notificationChannel.setVibrationPattern(new long[]{0, 500, 1000});
		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		AudioAttributes audioAttributes = new AudioAttributes.Builder()
				.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
				.setUsage(AudioAttributes.USAGE_NOTIFICATION)
				.build();
		notificationChannel.setSound(defaultSoundUri, audioAttributes);

		final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.createNotificationChannel(notificationChannel);

		sChannelId = channelId;

		return channelId;
	}

	private static void createDefaultChannel(Context context) {
		// 채널 ID 기본값 설정. 패키지명을 기본값으로 사용
		String channelId = context.getPackageName();
		String channelName = "푸시 알림";
		createChannel(context, channelId, channelName);
	}

	public static String getChannelId(Context context) {
		if(TextUtils.isEmpty(sChannelId)) {
			createDefaultChannel(context);
		}
		return sChannelId;
	}

	public static void createNotification(final Context context, final Intent intent, final String pushType) {
		// 기본 푸시 채널 생성.
		createDefaultChannel(context);

		try {
			if (pushType.equals(PushConstants.STR_UPNS_PUSH_TYPE)) {
				createUpnsNotification(context, intent);
			}
			else {
				createGcmNotification(context, intent);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isRestrictedScreen(final Context context) {
		KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		return km.inKeyguardRestrictedInputMode(); 
	}
	
	public static void showToast(final Context context, final Intent intent, String pushType) throws Exception {
		String jsonData = intent.getExtras().getString("JSON");
		String psid = intent.getExtras().getString("PSID");
		JSONObject jsonMsg = new JSONObject(jsonData);
		
		String extData = "";
		String title = "";
		String seqno = "";
		if (PushConstants.STR_UPNS_PUSH_TYPE.equals(pushType)) {
			if (jsonMsg.has("EXT")) {
				extData = jsonMsg.getString("EXT");
		    }
			title = jsonMsg.getString("MESSAGE");
		}
		else {
			JSONObject aps = jsonMsg.getJSONObject("aps");
			JSONObject mps = jsonMsg.getJSONObject("mps");
			if (aps.has("alert")) {
				title = aps.getString("alert");
		    }
		    if (mps.has("ext")) {
		    	extData = mps.getString("ext");
		    }
		    if (mps.has("seqno")) {
		    	seqno = mps.getString("seqno");
		    	if (lastestSeqNo.equals(seqno)) {
		    		return;
		    	}
		    	lastestSeqNo = seqno;
		    }
		}
		
		Toast.makeText(context, title + ": " + extData,Toast.LENGTH_SHORT).show();
	}
	
	
	public static void showNotificationPopupDialog(final Context context, final Intent intent, String pushType) throws Exception {
		String jsonData = intent.getExtras().getString("JSON");
		String psid = intent.getExtras().getString("PSID");
		JSONObject jsonMsg = new JSONObject(jsonData);
		
		String extData = "";
		String title = "";
		String seqno = "";
		if (PushConstants.STR_UPNS_PUSH_TYPE.equals(pushType)) {
			if (jsonMsg.has("EXT")) {
				extData = jsonMsg.getString("EXT");
		    }
			title = jsonMsg.getString("MESSAGE");
		}
		else {
			JSONObject aps = jsonMsg.getJSONObject("aps");
			JSONObject mps = jsonMsg.getJSONObject("mps");
			if (aps.has("alert")) {
				title = aps.getString("alert");
		    }
		    if (mps.has("ext")) {
		    	extData = mps.getString("ext");
		    }
		    if (mps.has("seqno")) {
		    	seqno = mps.getString("seqno");
		    	if (lastestSeqNo.equals(seqno)) {
		    		return;
		    	}
		    	lastestSeqNo = seqno;
		    }
		}
		
		Intent popupIntent = new Intent(); 
		popupIntent.setClass(context, ShowPushPopup.class);
		popupIntent.setAction(ShowPushPopup.class.getName());
		popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		popupIntent.setAction(ShowPushPopup.class.getName());
		popupIntent.putExtra("TITLE", title);
		popupIntent.putExtra("MESSAGE", "message");
		popupIntent.putExtra("JSON", jsonData);
		popupIntent.putExtra("PSID", psid);
		popupIntent.putExtra("EXT", extData);
		popupIntent.putExtra("PUSH_TYPE", pushType);
	    context.startActivity(popupIntent);
	}
	
	public static void createUpnsNotification(final Context context, final Intent intent) throws Exception {
        String jsonData = intent.getExtras().getString("JSON");
        String encryptData = intent.getExtras().getString(PushConstants.KEY_ORIGINAL_PAYLOAD_STRING);//intent.getExtras().getString("message_encrypt");

		// TODO : 확인해야함. EXT 데이터에 JSON 형식일 때 변환하면 안됨.
//		if (jsonData != null) {
//        	jsonData = jsonData.replaceAll("https", "http");
//        	jsonData = jsonData.replaceAll("\\\\", "/");
//        	jsonData = jsonData.replaceAll("//", "/");
//			jsonData = jsonData.replaceAll("/\"","\"");
//        }
        
        try {
        	Log.d("PushNotificationManager", "[PushNotificationManager] createUpnsNotification: " + jsonData);
	        JSONObject jsonMsg = new JSONObject(jsonData);
	        String psid = intent.getExtras().getString("PSID");
	        //PushWakeLock.acquireCpuWakeLock(context);
	        UpnsNotifyHelper.showNotification(context, jsonMsg, psid, encryptData);
	        //PushWakeLock.releaseCpuLock();
        } 
        catch(Exception e) {
        	e.printStackTrace();
        }	
	}
	
	public static void createGcmNotification(final Context context, final Intent intent) throws Exception {
		String jsonData = intent.getExtras().getString("JSON");
        String encryptData = intent.getExtras().getString(PushConstants.KEY_ORIGINAL_PAYLOAD_STRING);//intent.getExtras().getString("message_encrypt");
		// TODO : 확인해야함. EXT 데이터에 JSON 형식일 때 변환하면 안됨.
//        if (jsonData != null) {
//        	jsonData = jsonData.replaceAll("https", "http");
//        	jsonData = jsonData.replaceAll("\\\\", "/");
//        	jsonData = jsonData.replaceAll("//", "/");
//        }
        
        try {
        	Log.d("PushNotificationManager", "[PushNotificationManager] createGcmNotification: " + jsonData);
	        JSONObject jsonMsg = new JSONObject(jsonData);
	        String psid = intent.getExtras().getString("PSID");
	        //PushWakeLock.acquireCpuWakeLock(context);
	        GcmNotifyHelper.showNotification(context, jsonMsg, psid, encryptData);
	        //PushWakeLock.releaseCpuLock();
        } 
        catch(Exception e) {
        	e.printStackTrace();
        }
		
		/*String jsonData = intent.getExtras().getString("JSON");
		String psid = intent.getExtras().getString("PSID");
		JSONObject rootJsonObj = new JSONObject(jsonData);
	    JSONObject apsJsonObj = rootJsonObj.getJSONObject("aps");
	    JSONObject mpsJsonObj = rootJsonObj.getJSONObject("mps");
	
	    String alertMessage = apsJsonObj.getString("alert");
	    String seqNo = mpsJsonObj.getString("seqno");
	    if (lastestSeqNo.equals(seqNo)) {
    		return;
    	}
    	lastestSeqNo = seqNo;
	    
	    String extUrl = "";
	    if(mpsJsonObj.has("ext")){
	        extUrl = mpsJsonObj.getString("ext");
	    }
	
	    int icon = R.drawable.ic_launcher;
	    //long when = System.currentTimeMillis();
	
	    String title = context.getString(R.string.app_name);
	    Intent notificationIntent = new Intent(context, PushMessageManager.class);
	    notificationIntent.putExtra("JSON", rootJsonObj.toString());
	    notificationIntent.putExtra("PSID", psid);
	    notificationIntent.putExtra("TITLE", alertMessage);
	    notificationIntent.putExtra("EXT", extUrl);
	    notificationIntent.putExtra("PUSH_TYPE", "GCM");
	    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); 
		PendingIntent pendingIntent = PendingIntent.getActivity(context, Integer.parseInt(seqNo), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	    
	    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext())
			    .setAutoCancel(true)
				.setContentIntent(pendingIntent)
		        .setSmallIcon(icon)
		        .setContentTitle(title)
		        .setContentText(alertMessage)
		        .setTicker(title)
		        .setSound(soundUri);
		
		//mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
		notificationManager.notify("gcm", Integer.parseInt(seqNo), mBuilder.build());	*/
		
		//PushManager.getInstance().pushMessageReceiveConfirm(context, rootJsonObj.toString());
	}
	
	public static void showPopupDialog(final Context context, final Intent intent, String pushType) throws Exception {
		String jsonData = intent.getExtras().getString("message");
		String psid = intent.getExtras().getString("psid");
		JSONObject jsonMsg = new JSONObject(jsonData);
		
		String extData = "";
		if ("UPNS".equals(pushType)) {
			if (jsonMsg.has("EXT")) {
				extData = jsonMsg.getString("EXT");
		    }
		}
		else {
			JSONObject mps = jsonMsg.getJSONObject("mps");
		    if (mps.has("ext")) {
		    	extData = mps.getString("ext");
		    }
		}
		
		Intent popupIntent = new Intent(); 
		popupIntent.setClass(context, ShowPushPopup.class);
		popupIntent.setAction(ShowPushPopup.class.getName());
		popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		popupIntent.setAction(ShowPushPopup.class.getName());
		popupIntent.putExtra("TITLE", "push Notification");
		popupIntent.putExtra("MESSAGE", "message");
		popupIntent.putExtra("JSON", jsonData);
		popupIntent.putExtra("PS_ID", psid);
		popupIntent.putExtra("EXT", extData);
		popupIntent.putExtra("PUSH_TYPE", pushType);
	    context.startActivity(popupIntent);
		
	}
	
	public static void showToast(final Context context, final String message) throws Exception {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	
	public static AlertDialog createAlertDialog(final Context context, final String title, final String message, final Handler handler) {
		return new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	if (handler != null) {
					Message msg = handler.obtainMessage(Dialog.BUTTON_POSITIVE, 0, 0, 0);
					handler.sendMessage(msg);
					
					dialog.cancel();
					dialog.dismiss();
				}   
	        }
	    })
	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	if (handler != null) {
					Message msg = handler.obtainMessage(Dialog.BUTTON_NEGATIVE, 0, 0, 0);
					handler.sendMessage(msg);
					
					dialog.dismiss();
				}    
	        }
	    })
	    .create();
	}
	
	public static AlertDialog createConfirmDialog(final Context context, final String title, final String message, final Handler handler) {
		return new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	if (handler != null) {
					Message msg = handler.obtainMessage(Dialog.BUTTON_POSITIVE, 0, 0, 0);
					handler.sendMessage(msg);
					
					dialog.cancel();
					dialog.dismiss();
				}   
	        }
	    })
	    .create();
	}
}
