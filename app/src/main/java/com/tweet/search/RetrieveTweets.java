package com.tweet.search;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.tweet.search.provider.TweetDataProvider;
import com.tweet.search.util.TweetUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class RetrieveTweets {

	private static RetrieveTweets mSyncManager = null;
	private Context mContext = null;
	private Handler mHandler = null;
	String Key = null;
	String Secret = null;

	private RetrieveTweets(Context context, Handler mHandler) {
		this.mContext = context;
		this.mHandler = mHandler;
		Key = getStringFromManifest("CONSUMER_KEY");
		Secret = getStringFromManifest("CONSUMER_SECRET");
	}

	private String getStringFromManifest(String key) {
		String results = null;

		try {
			if(mContext != null) {
				ApplicationInfo ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
				results = (String)ai.metaData.get(key);
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return results;
	}

	public static RetrieveTweets getInstance(Context context, Handler mHandler) {

		if (mSyncManager == null) {
			mSyncManager = new RetrieveTweets(context, mHandler);
		}
		return mSyncManager;
	}

	public class RetrieveTweetsTask extends AsyncTask<String, Void, Void> {

		private ProgressDialog progressDialog;
		private byte[] keywordContent = null;
		private ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		final static String TwitterTokenURL = "https://api.twitter.com/oauth2/token";
		final static String TwitterSearchURL = "https://api.twitter.com/1.1/search/tweets.json?q=";
		String results = null;

		private String content = null;
		private String author = null;

		protected void onPreExecute() {
			keywordContent = null;
			if (mContext != null) {
				progressDialog = ProgressDialog.show(mContext, "",
						"Loading Feed...", true);
			}
		}

		@Override
		protected Void doInBackground(String... keyword) {
			Cursor c = null, cursor = null;
			int count = 0;
			try {

				String[] PROJECTION = new String[] {
						TweetDataProvider.Constants.KEYWORD,
						TweetDataProvider.Constants.CONTENT };
				cursor = mContext.getContentResolver().query(
						TweetDataProvider.Constants.CONTENT_URI, PROJECTION,
						null, null, null);
				if (cursor != null) {
					count = cursor.getCount();
				}

				String whereClause = TweetDataProvider.Constants.KEYWORD
						+ " = \"" + keyword[0] + "\"";
				c = mContext.getContentResolver().query(
						TweetDataProvider.Constants.CONTENT_URI, PROJECTION,
						whereClause, null, null);
				if (null != c && c.moveToNext()) {
					int index = c
							.getColumnIndex(TweetDataProvider.Constants.CONTENT);
					keywordContent = c.getBlob(index);
				}
				if (keywordContent == null) {
					String query = keyword[0];
					String encodedSearch = URLEncoder.encode(query, "UTF-8");
					String searchUrl = TwitterSearchURL + encodedSearch;
					// URL encode the consumer key and secret
					String urlApiKey = URLEncoder.encode(Key, "UTF-8");
					String urlApiSecret = URLEncoder.encode(Secret, "UTF-8");

					// Concatenate the encoded consumer key, a colon character, and the encoded consumer secret
					String combined = urlApiKey + ":" + urlApiSecret;

					// Base64 encode the string
					String base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);

					// Step 2: Obtain a bearer token
					URL url= new URL(TwitterTokenURL);
					HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					connection.setRequestMethod("POST");
					connection.setDoInput(true);
					connection.setRequestProperty("Authorization", "Basic " + base64Encoded);
					connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
					String postParameters = "grant_type=client_credentials";
					PrintWriter out = null;
					out = new PrintWriter(connection.getOutputStream());
					out.print(postParameters);
					out.close();

					StringBuilder sb = new StringBuilder();
					// handle issues
					int statusCode = connection.getResponseCode();
					if (statusCode == HttpURLConnection.HTTP_OK) {
						InputStream inputStream =
								new BufferedInputStream(connection.getInputStream());
						BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
						String line = null;
						while ((line = bReader.readLine()) != null) {
							sb.append(line);
						}
					}
					String rawAuthorization = sb.toString();
					Authenticated auth = jsonToAuthenticated(rawAuthorization);
					if (auth != null && auth.token_type.equals("bearer")) {

						url= new URL(searchUrl);
						HttpURLConnection getConnection = (HttpURLConnection)url.openConnection();
						getConnection.setRequestMethod("GET");
							// construct a normal HTTPS request and include an Authorization
							// header with the value of Bearer <>
						getConnection.setRequestProperty("Authorization", "Bearer " + auth.access_token);
						getConnection.setRequestProperty("Content-Type", "application/json");
						// update the results with the body of the response
						statusCode = getConnection.getResponseCode();
						if(statusCode == HttpURLConnection.HTTP_OK) {
							BufferedReader in = new BufferedReader(
									new InputStreamReader(getConnection.getInputStream()));
							String inputLine;
							StringBuffer response = new StringBuffer();

							while ((inputLine = in.readLine()) != null) {
								response.append(inputLine);
							}
							in.close();
							results = response.toString();
						}
					}
					if (results != null || !results.isEmpty()) {
						JSONObject root = new JSONObject(results);
						JSONArray sessions = root.getJSONArray("statuses");
						tweets = new ArrayList<Tweet>();

						for (int i = 0; i < sessions.length(); i++) {
							JSONObject session = sessions.getJSONObject(i);
							content = session.getString("text");
							String authorObjectString = session.getString("user");
							JSONObject authorObject = new JSONObject(authorObjectString);
							author = authorObject.getString("name");
							Tweet tweet = new Tweet(author, content);
							tweets.add(tweet);
						}

						keywordContent = TweetUtil.encodeByteArray(tweets);

					} else {
						if(!query.equals("")) {
							Message msg = new Message();
							msg.what = TweetConstants.ERROR_XXX;
							msg.obj = mContext.getString(R.string.retrieve_error);
							mHandler.sendMessage(msg);
						}
					}
					int thresholdPreference = TweetUtil.getPreference(mContext,
							TweetConstants.THRESHOLD_PREFERENCE);
					if (count < thresholdPreference) {
						ContentValues cv = new ContentValues();
						cv.put(TweetDataProvider.Constants.KEYWORD, keyword[0]);
						cv.put(TweetDataProvider.Constants.CONTENT,
								keywordContent);
						cv.put(TweetDataProvider.Constants.TIMESTAMP,
								System.currentTimeMillis());
						mContext.getContentResolver().insert(
								TweetDataProvider.Constants.CONTENT_URI, cv);
					} else {
						
						Message msg = new Message();
						msg.what = TweetConstants.THRESHOLD_REACHED;
						mHandler.sendMessage(msg);
					}
				} else {

					tweets = TweetUtil.decodeByteArray(keywordContent);

				}
			} catch (Exception e) {
				Log.e("SearchTweets", "Tweets feed unavailable", e);
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				Message msg = new Message();
				msg.what = TweetConstants.ERROR_XXX;
				msg.obj = sw.toString();
				mHandler.sendMessage(msg);
			} finally {
				if (c != null)
					c.close();
			}
			return null;
		}

			@Override
		protected void onPostExecute(Void result) {

			Message msg = new Message();
			msg.what = TweetConstants.REFRESH_DATA;
			msg.obj = tweets;
			mHandler.sendMessage(msg);

				if (progressDialog != null) {
					progressDialog.dismiss();
				}


		}
	}

	private Authenticated jsonToAuthenticated(String rawAuthorization) {
		Authenticated auth = null;
		if (rawAuthorization != null && rawAuthorization.length() > 0) {
			try {
				Gson gson = new Gson();
				auth = gson.fromJson(rawAuthorization, Authenticated.class);
			} catch (IllegalStateException ex) {
				// just eat the exception for now, but you'll need to add some handling here
			}
		}
		return auth;
	}

	public class RemoveEntriesFromDatabase extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg) {
			try {
				Integer days = TweetUtil.getPreference(mContext,
						TweetConstants.DAYS_PREFERENCE);
				Long daysInMillis = days.longValue() * 24 * 60 * 60 * 1000;
				String whereClause = TweetDataProvider.Constants.TIMESTAMP
						+ " < " + daysInMillis;
				mContext.getContentResolver().delete(
						TweetDataProvider.Constants.CONTENT_URI, whereClause,
						null);
			} catch (Exception e) {
				Log.e("SearchTweets", "Tweets auto sync unavailable", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// Nothing for now TODO
		}
	}

	public void startKeywordSearch(String keyword) {
		new RetrieveTweetsTask().execute(keyword);
	}


	public void removeEntriesFromDatabase() {
		new RemoveEntriesFromDatabase().execute();
	}

	public Cursor fetchKeywords(String partialKey) {
		Cursor cursor = null;
		String whereClause = TweetDataProvider.Constants.KEYWORD + " LIKE '%"
				+ partialKey + "%'";
		String[] PROJECTION = new String[] { TweetDataProvider.Constants._ID,
				TweetDataProvider.Constants.KEYWORD };
		cursor = mContext.getContentResolver().query(
				TweetDataProvider.Constants.CONTENT_URI, PROJECTION,
				whereClause, null, null);
		return cursor;
	}

		public class Authenticated {
			String token_type;
			String access_token;
		}
}