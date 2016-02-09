package com.tweet.search.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tweet.search.TweetConstants;
import com.tweet.search.service.TweetService;

public class TweetAlarmReceiver extends BroadcastReceiver {

	private static final String LOG_TAG = "TweetAlarmReceiver";

	@Override
	public void onReceive(Context mContext, Intent intent) {
		String action = intent.getAction();
		Log.d(LOG_TAG, "onReceive: " + action);

		if (TweetConstants.BOOT_COMPLETE.equals(action)) {
			mContext.startService(new Intent(mContext, TweetService.class)
					.putExtra(TweetConstants.ACTION_SYNC_TYPE,
							TweetConstants.ON_BOOT));
		} else if (TweetConstants.INTERVAL_ALARM_ACTION.equals(action)) {
			mContext.startService(new Intent(mContext, TweetService.class)
					.putExtra(TweetConstants.ACTION_SYNC_TYPE,
							TweetConstants.INTERVAL_SYNC));
		}
	}

}
