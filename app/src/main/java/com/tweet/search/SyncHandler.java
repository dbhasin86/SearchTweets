package com.tweet.search;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tweet.search.service.TweetService;

public class SyncHandler {

	private Context mContext;
	private RetrieveTweets mRetrieveTweets = null;
	private String errorMsg = null;

	public SyncHandler(Context mContext) {
		this.mContext = mContext;
		mRetrieveTweets = RetrieveTweets.getInstance(mContext, sSyncHandler);
	}

	public void startSync() {
		Message msg = new Message();
		msg.what = TweetConstants.START_SYNC;
		sSyncHandler.sendMessage(msg);
	}

	private Handler sSyncHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case TweetConstants.START_SYNC:
				mRetrieveTweets.removeEntriesFromDatabase();
				break;
			case TweetConstants.END_SYNC:
				mContext.stopService(new Intent(mContext, TweetService.class));
				break;
			case TweetConstants.ERROR_XXX:
				errorMsg = (String) msg.obj;
				Log.d("SyncHandler", "Database Sync Failed: " + errorMsg);
			}
		}
	};

}
