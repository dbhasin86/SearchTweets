package com.tweet.search.activity;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.tweet.search.R;
import com.tweet.search.TweetConstants;
import com.tweet.search.receiver.TweetAlarmReceiver;
import com.tweet.search.util.TweetUtil;

public class Settings extends PreferenceActivity {
	private PreferenceScreen mPreferenceScreen;
	private Preference mIntervalPreference;
	private Preference mThresholdPreference;
	private Preference mDaysPreference;

	private Context mContext;
	private static final int INTERVAL_DIALOG = 0;
	private static final int THRESHOLD_DIALOG = 1;
	private static final int DAYS_DIALOG = 2;

	public static final String KEY_PREFERENCE_INTERVAL = "preference_interval";
	public static final String KEY_PREFERENCE_THRESHOLD = "preference_threshold";
	public static final String KEY_PREFERENCE_DAYS = "preference_days";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_setting);

		addPreferencesFromResource(R.xml.setting);

		mContext = getApplicationContext();
		mPreferenceScreen = getPreferenceScreen();
		if (mPreferenceScreen != null) {
			mIntervalPreference = mPreferenceScreen
					.findPreference(KEY_PREFERENCE_INTERVAL);
			mThresholdPreference = mPreferenceScreen
					.findPreference(KEY_PREFERENCE_THRESHOLD);
			mDaysPreference = mPreferenceScreen
					.findPreference(KEY_PREFERENCE_DAYS);
		}

		if (mIntervalPreference != null) {
			IntervalClickListener mIntervalListener = new IntervalClickListener();
			mIntervalPreference.setOnPreferenceClickListener(mIntervalListener);
		}

		if (mThresholdPreference != null) {
			ThresholdClickListener mThresholdClickListener = new ThresholdClickListener();
			mThresholdPreference
					.setOnPreferenceClickListener(mThresholdClickListener);
		}

		if (mDaysPreference != null) {
			DaysClickListener mDaysClickListener = new DaysClickListener();
			mDaysPreference.setOnPreferenceClickListener(mDaysClickListener);
		}

	}

	class IntervalClickListener implements OnPreferenceClickListener {
		public boolean onPreferenceClick(Preference pref) {
			showDialog(INTERVAL_DIALOG);
			return true;
		}
	}

	class ThresholdClickListener implements OnPreferenceClickListener {

		public boolean onPreferenceClick(Preference pref) {
			showDialog(THRESHOLD_DIALOG);
			return true;
		}
	}

	class DaysClickListener implements OnPreferenceClickListener {

		public boolean onPreferenceClick(Preference pref) {
			showDialog(DAYS_DIALOG);
			return true;
		}
	}

	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		switch (id) {
		case INTERVAL_DIALOG:
			ab.setTitle(getString(R.string.UpdateInterval));
			ab.setSingleChoiceItems(R.array.interval_items, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							TypedArray ar = getResources().obtainTypedArray(R.array.threshold_items);
							int value = ar.getInt(item, 4);
							SharedPreferences.Editor mEditor = null;
							SharedPreferences interval_preferences = mContext
									.getSharedPreferences(
											TweetConstants.INTERVAL_PREFERENCE,
											0);
							if (interval_preferences != null) {
								mEditor = interval_preferences.edit();
								if (mEditor != null) {
									mEditor.putInt(
											TweetConstants.INTERVAL_PREFERENCE,
											value);
								}
							}
							mEditor.commit();
							setupAlarm();
							dialog.cancel();
						}
					});
			return ab.create();
		case THRESHOLD_DIALOG:
			ab.setTitle(getString(R.string.Threshold));
			ab.setSingleChoiceItems(R.array.threshold_items, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							TypedArray ar = getResources().obtainTypedArray(R.array.threshold_items);
							int value = ar.getInt(item, 100);
							SharedPreferences.Editor mEditor = null;
							SharedPreferences interval_preferences = mContext
									.getSharedPreferences(
											TweetConstants.THRESHOLD_PREFERENCE,
											0);
							if (interval_preferences != null) {
								mEditor = interval_preferences.edit();
								if (mEditor != null) {
									mEditor.putInt(
											TweetConstants.THRESHOLD_PREFERENCE,
											value);
								}
							}
							mEditor.commit();
							dialog.cancel();
						}
					});
			return ab.create();
		case DAYS_DIALOG:
			ab.setTitle(getString(R.string.DeletionInterval));
			ab.setSingleChoiceItems(R.array.day_items, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							TypedArray ar = getResources().obtainTypedArray(R.array.threshold_items);
							int value = ar.getInt(item, 4);
							SharedPreferences.Editor mEditor = null;
							SharedPreferences interval_preferences = mContext
									.getSharedPreferences(
											TweetConstants.DAYS_PREFERENCE, 0);
							if (interval_preferences != null) {
								mEditor = interval_preferences.edit();
								if (mEditor != null) {
									mEditor.putInt(
											TweetConstants.DAYS_PREFERENCE,
											value);
								}
							}
							mEditor.commit();
							dialog.cancel();
						}
					});
			return ab.create();
		}
		return null;
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

}
