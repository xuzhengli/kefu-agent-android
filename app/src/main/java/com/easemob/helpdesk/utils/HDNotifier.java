package com.easemob.helpdesk.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.easemob.badger.BadgeUtil;
import com.easemob.helpdesk.ChannelConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.mvp.ChatActivity;
import com.hyphenate.kefusdk.entity.HDSession;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.manager.session.CurrentSessionManager;
import com.hyphenate.kefusdk.utils.HDLog;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;
import static android.support.v4.app.NotificationCompat.VISIBILITY_SECRET;

public class HDNotifier {
	
	private final static String TAG = HDNotifier.class.getSimpleName();
	
	static Ringtone ringtone = null;
	
	private final static String msg_eng = "receive message!";
	
	private final static String msg_ch = "您收到了消息!";
	
	public static int notifyID = 1314;//start notification id

//	public static int alarmNotifyID = 1315;//start notification id

//	public static int hotFixNoti = 10000;

	private NotificationManager notificationManager = null;
	
	private int notificationNum = 0;
	
	private Context appContext;
	
	private String appName;
	private String packageName;
	private String msgs;
	private long lastNotifyTime;
	
	private static HDNotifier instance;
	
	private AudioManager audioManager;
	private Vibrator vibrator;
	
	private ExecutorService notifierThread = Executors.newSingleThreadExecutor();
	
	private HDNotifier(Context context){
		appContext = context;
		if(notificationManager == null){
			notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		
		if(appContext.getApplicationInfo().labelRes != 0){
			appName = appContext.getString(appContext.getApplicationInfo().labelRes);
		}else{
			appName = "";
		}
		
		packageName = appContext.getApplicationInfo().packageName;
//		if(Locale.getDefault().getLanguage().equals("zh")){
//			
//		}else{
//			
//		}
		msgs = msg_ch;
		
		audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
		vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
		
	}
	
	public static  HDNotifier getInstance(){
		if(instance == null){
			synchronized (HDNotifier.class){
				if(instance == null){
					instance = new HDNotifier(HDApplication.getInstance());
				}
			}
		}
		return instance;
	}
	
	
	public void stop(){
		if(ringtone != null){
			ringtone.stop();
			ringtone = null;
		}
	}

//	public void sendHotFixNotiAction(String info) {
//		PackageManager packageManager = appContext.getPackageManager();
//
//		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(appContext);
//		mBuilder.setSmallIcon(ChannelConfig.getInstance().getNotificationSmallIcon());
//		mBuilder.setWhen(System.currentTimeMillis());
//		mBuilder.setAutoCancel(true);
//
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//			mBuilder.setColor(Color.GRAY);
//		} else {
//			mBuilder.setColor(Color.TRANSPARENT);
//		}
//
//		mBuilder.setContentTitle("紧急修复");
//		mBuilder.setTicker(info);
//		mBuilder.setContentText(info);
//		Notification notification = mBuilder.build();
//
//		try {
//			notificationManager.cancel(hotFixNoti);
//		} catch (Exception ignored) {
//		}
//
//		notificationManager.notify(hotFixNoti, notification);
//	}

//
//	private void sendAlarmNotifaction(){
//		PackageManager packageManager = appContext.getPackageManager();
//
//		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(appContext);
////		mBuilder.setSmallIcon(appContext.getApplicationInfo().icon);
//		mBuilder.setSmallIcon(ChannelConfig.getInstance().getNotificationSmallIcon());
////		mBuilder.setSmallIcon(R.drawable.icon_launcher2);
////		mBuilder.setLargeIcon(BitmapFactory.decodeResource(appContext.getResources(), R.drawable.icon_launcher2));
//		mBuilder.setWhen(System.currentTimeMillis());
//		mBuilder.setAutoCancel(true);
//
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//			mBuilder.setColor(Color.GRAY);
//		} else {
//			mBuilder.setColor(Color.TRANSPARENT);
//		}
//
//
//
//		Intent msgIntent = appContext.getPackageManager().getLaunchIntentForPackage(packageName);
//
//		PendingIntent pendingIntent = PendingIntent.getActivity(appContext, alarmNotifyID, msgIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//		mBuilder.setContentTitle(packageManager.getApplicationLabel(appContext.getApplicationInfo()));
//		mBuilder.setTicker("有新的告警记录");
//		mBuilder.setContentText("有新的告警记录");
//		mBuilder.setContentIntent(pendingIntent);
//		Notification notification = mBuilder.build();
//
////		try{
////			Field field = notification.getClass().getDeclaredField("extraNotification");
////			Object extraNotification = field.get(notification);
////			Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
////			int unreadcount = HDApplication.getInstance().getUnReadMsgCount();
////			method.invoke(extraNotification, unreadcount);
////		}catch (Exception e){
////		}
//
//		try {
//			notificationManager.cancel(alarmNotifyID);
//		} catch (Exception ignored) {
//		}
//		int count = HDApplication.getInstance().getUnReadMsgCount();
//		try{
//			BadgeUtil.sendBadgeNotification(notification, alarmNotifyID, appContext, count, count);
//		}catch (Exception e){
//			HDLog.e(TAG, "send notification error" + e.getMessage());
//		}
////		notificationManager.notify(notifyID, notification);
//	}

	private NotificationManager mManager;

	private NotificationManager getManager() {
		if (mManager == null) {
			mManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return mManager;
	}

	private void sendNotifaction(HDMessage message){
		PackageManager packageManager = appContext.getPackageManager();

		//notification title

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel("com.hyphenate.helpdesk", "channel_name",
					NotificationManager.IMPORTANCE_DEFAULT);
			//是否绕过请勿打扰模式
			channel.canBypassDnd();
			//闪光灯
			channel.enableLights(true);
			//锁屏显示通知
			channel.setLockscreenVisibility(VISIBILITY_SECRET);
			//闪光灯的灯光颜色
			channel.setLightColor(Color.RED);
			//桌面launcher的消息角标
			channel.canShowBadge();
			//是否允许震动
			channel.enableVibration(true);
			//获取系统通知响铃声音的配置
			channel.getAudioAttributes();
			//获取通知取到组
			channel.getGroup();
			//设置可绕过 请勿打扰模式
			channel.setBypassDnd(true);
			//设置震动模式
			channel.setVibrationPattern(new long[]{100, 100, 200});
			//是否会有灯光
			channel.shouldShowLights();
			getManager().createNotificationChannel(channel);
		}

		//create and send notification
		NotificationCompat.Builder mBuilder = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			mBuilder = new NotificationCompat.Builder(appContext, "com.hyphenate.helpdesk");
		} else {
			mBuilder = new NotificationCompat.Builder(appContext);
			mBuilder.setPriority(PRIORITY_DEFAULT);
		}
//		mBuilder.setSmallIcon(appContext.getApplicationInfo().icon);
		mBuilder.setSmallIcon(ChannelConfig.getInstance().getNotificationSmallIcon());
//		mBuilder.setSmallIcon(R.drawable.icon_launcher2);
//		mBuilder.setLargeIcon(BitmapFactory.decodeResource(appContext.getResources(), R.drawable.icon_launcher2));
		mBuilder.setWhen(System.currentTimeMillis());
		mBuilder.setAutoCancel(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			mBuilder.setColor(Color.GRAY);
		} else {
			mBuilder.setColor(Color.TRANSPARENT);
		}
		Intent msgIntent = appContext.getPackageManager().getLaunchIntentForPackage(packageName);
		
		if(message!=null){
			msgIntent.putExtra("hasUnReadMessage", true);
			msgIntent.putExtra("visitorid", message.getSessionServiceId());
			HDSession session = CurrentSessionManager.getInstance()
					.getSessionEntity(message.getSessionServiceId());
			if (session != null && !HDApplication.getInstance().isNoActivity()) {
				msgIntent.setClass(appContext, ChatActivity.class);
//				System.out.println("HDSession is " + session.getUser().getNicename());
				msgIntent.putExtra("user", session.getUser());
				msgIntent.putExtra("originType", session.getOriginType());
				msgIntent.putExtra("techChannelName", session.getTechChannelName());
				msgIntent.putExtra("chatGroupId", session.getChatGroupId());
			} else {
//				System.out.println("HDSession is null");
				msgIntent.putExtra("user", message.getFromUser());
			}
		}
		PendingIntent pendingIntent = PendingIntent.getActivity(appContext, notifyID, msgIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentTitle(packageManager.getApplicationLabel(appContext.getApplicationInfo()));
		mBuilder.setTicker(msgs);
		mBuilder.setContentText(msgs);
		mBuilder.setContentIntent(pendingIntent);
		Notification notification = mBuilder.build();

		try {
			notificationManager.cancel(notifyID);
		} catch (Exception ignored) {
		}
		int count = HDApplication.getInstance().getUnReadMsgCount();
		try{
			getManager().notify(notifyID, mBuilder.build());
			BadgeUtil.sendBadgeNotification(notification, notifyID, appContext, count, count);
		}catch (Exception e){
			HDLog.e(TAG, "send notification error" + e.getMessage());
		}
//		notificationManager.notify(notifyID, notification);
	}
	
	public synchronized void notifiChatMsg(HDMessage message){
		if (!HDApplication.getInstance().isNewMsgNotiStatus()) {
			return;
		}

		if(!CommonUtils.isAppRunningForeground(appContext)){
			sendNotifaction(message);
		}
		notifyOnNewMsg();
	}

//	public synchronized void notifyAlarmMsg() {
//		if (!HDApplication.getInstance().isNewMsgNotiStatus()) {
//			return;
//		}
//
//		if(!CommonUtils.isAppRunningForeground(appContext)){
//			sendAlarmNotifaction();
//		}
//
//		notifyOnNewMsg();
//
//	}
	
	public void notifyOnNewMsg() {
		try {
			lastNotifyTime = System.currentTimeMillis();
			// 判断是否处于静音模式
			if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
				HDLog.e(TAG, "in slient mode now");
				return;
			}
			if (HDApplication.getInstance().isNotiAlertVibrateStatus()) {
				long[] pattern = new long[] { 0, 180, 80, 120 };
				vibrator.vibrate(pattern, -1);
			}

			if (!HDApplication.getInstance().isNotiAlertSoundStatus()) {
				return;
			}

			String vendor = Build.MANUFACTURER;
			if (ringtone == null) {
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				ringtone = RingtoneManager.getRingtone(appContext, notification);
				if (ringtone == null) {
					HDLog.e(TAG, "cant find ringtone at:" + notification.getPath());
					return;
				}
			}
			if (!ringtone.isPlaying()) {
				ringtone.play();
				if (vendor != null && vendor.toLowerCase(Locale.US).contains("samsung")) {
					Thread ctlThread = new Thread() {

						@Override
						public void run() {
							super.run();
							try {
								Thread.sleep(3000);
								if (ringtone.isPlaying()) {
									ringtone.stop();
								}
							} catch (Exception ignored) {
							}

						}

					};
					ctlThread.start();

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * cancel notification
	 */
	public void cancelNotification(){
		if(notificationManager!=null){
			try {
				notificationManager.cancel(notifyID);
//				notificationManager.cancel(alarmNotifyID);
			} catch (Exception ignored) {
			}
		}
	}
	
	
	
	
	
	
	
}
