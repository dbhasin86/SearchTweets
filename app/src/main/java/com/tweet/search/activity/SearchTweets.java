package com.tweet.search.activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.tweet.search.R;
import com.tweet.search.RetrieveTweets;
import com.tweet.search.Tweet;
import com.tweet.search.TweetConstants;
import com.tweet.search.provider.TweetDataProvider;
import com.tweet.search.receiver.TweetAlarmReceiver;
import com.tweet.search.service.TweetService;
import com.tweet.search.util.TweetUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class SearchTweets extends ListActivity implements OnFocusChangeListener {

	private AutoCompleteTextView mRecepientEditor = null;
	private ImageView mRecipientBoxImageView = null;
	private ImageView mCancelInput = null;
	private ImageButton mSearchButton = null;
	
	private Spannable recepientString = null;
	private String keyword = null;
	private Context mContext = null;
	private SearchAdapter mSearchAdapter = null;
	private ArrayList<Tweet> tweets = new ArrayList<Tweet>();
	private RetrieveTweets mRetrieveTweets = null;
	private SimpleCursorAdapter dropdownAdapter = null;

	final int[] to = new int[] { android.R.id.text1 };
	final String[] from = new String[] { TweetDataProvider.Constants.KEYWORD };
	private TweetService myServiceBinder;
	private String errorMsg = null;
	private static boolean mIsBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_tweets);
		mContext = getApplicationContext();

		if (!TweetUtil.getAlarmExpirationState(mContext)) {
			setupAlarm();
		}

		initResourceRefs();

		mRetrieveTweets = RetrieveTweets.getInstance(SearchTweets.this,
				mHandler);

		if (mRecepientEditor != null
				&& mRecepientEditor.getText().length() == 0)
			mRecipientBoxImageView.setVisibility(View.VISIBLE);
		else
			mRecipientBoxImageView.setVisibility(View.GONE);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, getString(R.string.settings));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Intent intent = new Intent(TweetConstants.SETTINGS_INTENT);
			startActivity(intent);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			myServiceBinder = ((TweetService.MyBinder) binder).getService();
			mIsBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			myServiceBinder = null;
			mIsBound = false;
		}
	};

	private void doBindService() {
		Intent i = new Intent(this, TweetService.class);
		bindService(i, mServiceConn, 0);
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
		// am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() +
		// 10000,
		// 1000000, sender);

		TweetUtil.setAlarmExpirationState(mContext, true);
	}

	private void initResourceRefs() {
		mRecepientEditor = (AutoCompleteTextView) findViewById(R.id.RecipientEditor);
		mRecepientEditor.setOnFocusChangeListener(this);
		mRecepientEditor.addTextChangedListener(mRecipientsWatcher);
		mRecepientEditor.setOnEditorActionListener(new OnEditorActionListener() {
	        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
	            		keyword = mRecepientEditor.getText().toString();
	        			mRetrieveTweets.startKeywordSearch(keyword);
	            }    
	            return false;
	        }
	    });
		dropdownAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_dropdown_item_1line, null, from, to, 0);

		dropdownAdapter
				.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
					@Override
					public CharSequence convertToString(Cursor cursor) {
						final int colIndex = cursor
								.getColumnIndexOrThrow(TweetDataProvider.Constants.KEYWORD);
						return cursor.getString(colIndex);
					}
				});

		dropdownAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence constraint) {
				String partialKey = null;
				if (constraint != null) {
					partialKey = constraint.toString();
				}
				return mRetrieveTweets.fetchKeywords(partialKey);
			}
		});

		mRecepientEditor.setAdapter(dropdownAdapter);

		mRecipientBoxImageView = (ImageView) findViewById(R.id.RecipientBoxImageView);
		mCancelInput = (ImageView) findViewById(R.id.CancelInput);
		mCancelInput.setOnClickListener(mClearContactListener);
		mSearchButton = (ImageButton) findViewById(R.id.Searchbtn);
		mSearchButton.setOnClickListener(mSearchListener);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mRecepientEditor != null) {
			recepientString = mRecepientEditor.getText();
		}
		releaseViews();
		if (mIsBound) {
			unbindService(mServiceConn);
			mIsBound = false;
		}
	}

	@Override
	protected void onResume() {
		if (!mIsBound) {
	        doBindService();
	    }
		super.onResume();
		initResourceRefs();
		if (mRecepientEditor != null && recepientString != null) {
			mRecepientEditor.setText(recepientString);
			mCancelInput.setVisibility(View.VISIBLE);
			if (mRecepientEditor.getText().length() == 0) {
				mCancelInput.setVisibility(View.GONE);
			}

		} else {
			mCancelInput.setVisibility(View.GONE);
		}
	}
	
	

	@Override
	protected void onPause() {
		super.onPause();
		if (mIsBound) {
			unbindService(mServiceConn);
			mIsBound = false;
		}
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (mRecepientEditor != null) {
			recepientString = mRecepientEditor.getText();
		}

		setContentView(R.layout.search_tweets);
		releaseViews();
		initResourceRefs();
		if (mRecepientEditor != null && recepientString != null) {
			mRecepientEditor.setText(recepientString);
			mCancelInput.setVisibility(View.VISIBLE);
			if (mRecepientEditor.getText().length() == 0) {
				mCancelInput.setVisibility(View.GONE);
			}

		} else {
			mCancelInput.setVisibility(View.GONE);
		}
	}

	@Override
	public void onFocusChange(View view, boolean hasFocus) {

		if (view == mRecepientEditor) {
			if (!hasFocus) {
				if (mRecepientEditor.getText().length() == 0) {
					mRecipientBoxImageView.setVisibility(View.VISIBLE);
				}
			} else {
				mRecipientBoxImageView.setVisibility(View.GONE);
				mRecepientEditor.setHint("");
			}
		}
	}

	private final TextWatcher mRecipientsWatcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int count,
				int after) {
			if (s.length() > 0) {
				mCancelInput.setVisibility(View.VISIBLE);
			} else {
				mCancelInput.setVisibility(View.GONE);
			}
		}

	};

	private View.OnClickListener mSearchListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			keyword = mRecepientEditor.getText().toString();
			mRetrieveTweets.startKeywordSearch(keyword);
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case TweetConstants.REFRESH_DATA:
				tweets = (ArrayList<Tweet>) msg.obj;
				mSearchAdapter = new SearchAdapter(mContext,
						R.layout.tweet_item, tweets);
				setListAdapter(mSearchAdapter);
				break;
			case TweetConstants.ERROR_XXX:
				errorMsg = (String) msg.obj;
				showDialog(0);
			case TweetConstants.THRESHOLD_REACHED:
				Toast.makeText(mContext,
						mContext.getString(R.string.toast_threshold),
						Toast.LENGTH_LONG).show();
			}
		}
	};

	private View.OnClickListener mClearContactListener = new View.OnClickListener() {
		public void onClick(View arg0) {
			mRecepientEditor.setText("");
			mCancelInput.setVisibility(View.GONE);
		}
	};

	private void releaseViews() {

		if (null != mRecepientEditor)
			mRecepientEditor.destroyDrawingCache();
		mRecepientEditor = null;

		if (null != mRecipientBoxImageView)
			mRecipientBoxImageView.destroyDrawingCache();
		mRecipientBoxImageView = null;

		if (null != mCancelInput)
			mCancelInput.destroyDrawingCache();
		mCancelInput = null;
	}
	
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		switch (id) {
		case 0:
			ab.setTitle(getString(R.string.Error));
			ab.setMessage(errorMsg);
			ab.setPositiveButton(getString(R.string.ok), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
		}
		return ab.create();
	}

}
