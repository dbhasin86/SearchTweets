package com.tweet.search.service;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.tweet.search.SyncHandler;
import com.tweet.search.TweetConstants;
import com.tweet.search.receiver.TweetAlarmReceiver;
import com.tweet.search.util.TweetUtil;

public class TweetService extends Service {

	private SyncHandler mAutoSyncHandler = null;
	private Context mContext;

	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent == null) {
			return Service.START_NOT_STICKY;
		}

		mContext = getApplicationContext();
		mAutoSyncHandler = new SyncHandler(mContext);

		String extraSyncType = null;
		extraSyncType = intent.getStringExtra(TweetConstants.ACTION_SYNC_TYPE);

		if (extraSyncType.equals(TweetConstants.ON_BOOT)) {
			setupAlarm();
		} else if (extraSyncType.equals(TweetConstants.INTERVAL_SYNC)) {
			mAutoSyncHandler.startSync();
		}

		return Service.START_STICKY;
	}

	private void setupAlarm() {
		int PERIOD = 24 * TweetUtil.getPreference(mContext,
				TweetConstants.INTERVAL_PREFERENCE);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, PERIOD);
		Intent intent = new Intent(mContext, TweetAlarmReceiver.class);
		intent.setAction(TweetConstants.INTERVAL_ALARM_ACTION);
		PendingIntent sender = PendingIntent.getBroadcast(mContext,
				TweetConstants.INTERVAL_ALARM_REMINDER_ID, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				PERIOD * 60 * 60 * 1000, sender);
	}

	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public class MyBinder extends Binder {
	    public TweetService getService() {
	        return TweetService.this;
	    }
	}

}
