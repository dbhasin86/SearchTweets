package com.tweet.search;

public class TweetConstants {

	public static final int INTERVAL = 1;
	public static final int THRESHOLD = 20;
	public static final int DAYS_FOR_DELETION = 1;
	public static final int DAYS = 11;
	public static final int REFRESH_DATA = 1;
	public static final int ERROR_XXX = 2;
	public static final int THRESHOLD_REACHED = 3;
	public static final int START_SYNC = 1;
	public static final int REMOVE_ENTRIES = 2;
	public static final int END_SYNC = 3;
	public static final int INTERVAL_ALARM_REMINDER_ID = 2876;
	public static final int DAYS_ALARM_REMINDER_ID = 2877;
	public static final int PREFERENCE_NOT_SET = 0;

	public static final String SERVICE_INTENT = "com.tweet.search.TweetService";
	public static final String SETTINGS_INTENT = "com.tweet.search.activity.settings";
	public static final String BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";
	public static final String INTERVAL_ALARM_ACTION = "com.tweet.search.INTERVAL_ALARM";
	public static final String DAYS_ALARM_ACTION = "com.tweet.search.DAYS_ALARM";
	public static final String ALARM_SET_PREFERENCE = "alarm_set_preference";
	public static final String INTERVAL_PREFERENCE = "interval_preference";
	public static final String THRESHOLD_PREFERENCE = "threshold_preference";
	public static final String DAYS_PREFERENCE = "days_preference";
	public static final String ACTION_SYNC_TYPE = "sync_type";
	public static final String INTERVAL_SYNC = "interval_sync";
	public static final String ON_BOOT = "on_boot";
	public static final String DAYS_SYNC = "days_sync";
}
