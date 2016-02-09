/*
 * Dhruv: 01-11-2012 Added support for Content Provider for BUA+ Application
 */

package com.tweet.search.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class TweetDataProvider extends ContentProvider {

	public static final String AUTHORITY = "com.tweet.search.provider.TweetDataProvider";
	private final static String DATABASE_NAME = "TweetsDB.db";
	private final static String DATABASE_TABLE = "TweetsInfo";
	private static final int DATABASE_VERSION = 1;

	private static final int CONSTANTS = 1;
	private static final int CONSTANT_ID = 2;

	private DatabaseHelper dbHelper;
	private static HashMap<String, String> tweetProjectionMap;
	private static final UriMatcher sUriMatcher;

	private static final String DATABASE_CREATE = "create table TweetsInfo (_id integer primary key, keyword text, content BLOB, timestamp INTEGER);";

	public static final class Constants implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/info");
		public static final String KEYWORD = "keyword";
		public static final String CONTENT = "content";
		public static final String TIMESTAMP = "timestamp";
		public static final String _ID = "_id";
	}

	public static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS TweetsInfo");
			onCreate(db);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int result = db.delete(DATABASE_TABLE, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialvalues) {
		if (!isCollectionUri(uri)) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		ContentValues values;
		if (initialvalues != null) {
			values = new ContentValues(initialvalues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = db.insert(DATABASE_TABLE,
				TweetDataProvider.Constants.KEYWORD, values);
		if (rowId > 0) {
			Uri tweetUri = ContentUris.withAppendedId(
					TweetDataProvider.Constants.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(tweetUri, null);
			return tweetUri;
		}

		throw new SQLException("Unable to insert row for " + uri);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return (dbHelper == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		qb.setTables(DATABASE_TABLE);
		qb.setProjectionMap(tweetProjectionMap);
		Cursor c = qb.query(db, projection, selection, null, null, null,
				sortOrder);
		if (c != null) {
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int result = db
				.update(DATABASE_TABLE, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	private boolean isCollectionUri(Uri uri) {
		return (sUriMatcher.match(uri) == CONSTANTS);
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "info", CONSTANTS);
		sUriMatcher.addURI(AUTHORITY, "info/#", CONSTANT_ID);

		tweetProjectionMap = new HashMap<String, String>();
		tweetProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
		tweetProjectionMap.put(TweetDataProvider.Constants.KEYWORD,
				TweetDataProvider.Constants.KEYWORD);
		tweetProjectionMap.put(TweetDataProvider.Constants.CONTENT,
				TweetDataProvider.Constants.CONTENT);
		tweetProjectionMap.put(TweetDataProvider.Constants.TIMESTAMP,
				TweetDataProvider.Constants.TIMESTAMP);
	}
}
