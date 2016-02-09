package com.tweet.search.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;

import com.tweet.search.Tweet;
import com.tweet.search.TweetConstants;

public class TweetUtil {

	public static byte[] encodeByteArray(ArrayList<Tweet> tweets) {
		byte[] keywordContent = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = null;
			out = new ObjectOutputStream(bos);
			out.writeObject(tweets);
			keywordContent = bos.toByteArray();
			out.flush();
			out.close();
			bos.close();
		} catch (Exception e) {
		}

		return keywordContent;
	}

	public static ArrayList<Tweet> decodeByteArray(byte[] keywordContent) {
		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		try {
			ByteArrayInputStream byteIn = new ByteArrayInputStream(
					keywordContent);
			ObjectInputStream in = new ObjectInputStream(byteIn);
			tweets.clear();
			tweets = (ArrayList<Tweet>) in.readObject();
		} catch (Exception e) {
		}

		return tweets;
	}

	public static void setAlarmExpirationState(Context mContext, Boolean mState) {
		SharedPreferences app_preferences = mContext.getSharedPreferences(
				TweetConstants.ALARM_SET_PREFERENCE, 0);
		SharedPreferences.Editor mEditor = null;
		if (app_preferences != null) {
			mEditor = app_preferences.edit();
			if (mEditor != null) {
				mEditor.putBoolean(TweetConstants.ALARM_SET_PREFERENCE, mState);
			}
		}
		mEditor.commit();
	}

	public static boolean getAlarmExpirationState(Context mContext) {
		boolean nReturnValue = false;
		if (mContext != null) {
			SharedPreferences app_preferences = mContext.getSharedPreferences(
					TweetConstants.ALARM_SET_PREFERENCE, 0);
			if (app_preferences != null) {
				nReturnValue = app_preferences.getBoolean(
						TweetConstants.ALARM_SET_PREFERENCE, false);
			}
		}
		return nReturnValue;
	}

	public static int getPreference(Context mContext, String preference) {
		int nReturnValue = TweetConstants.PREFERENCE_NOT_SET;
		if (preference.equals(TweetConstants.INTERVAL_PREFERENCE))
			nReturnValue = TweetConstants.INTERVAL;
		if (preference.equals(TweetConstants.THRESHOLD_PREFERENCE))
			nReturnValue = TweetConstants.THRESHOLD;
		if (preference.equals(TweetConstants.DAYS_PREFERENCE))
			nReturnValue = TweetConstants.DAYS_FOR_DELETION;
		if (mContext != null) {
			SharedPreferences app_preferences = mContext.getSharedPreferences(
					preference, 0);
			if (app_preferences != null) {
				nReturnValue = app_preferences.getInt(preference, nReturnValue);
			}
		}
		return nReturnValue;
	}
}
