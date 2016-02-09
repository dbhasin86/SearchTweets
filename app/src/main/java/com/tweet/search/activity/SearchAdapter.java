package com.tweet.search.activity;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tweet.search.R;
import com.tweet.search.Tweet;

public class SearchAdapter extends ArrayAdapter<Tweet> {
	private ArrayList<Tweet> tweets;
	private Context mContext;

	public SearchAdapter(Context context, int textViewResourceId,
			ArrayList<Tweet> items) {
		super(context, textViewResourceId, items);
		this.tweets = items;
		this.mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.tweet_item, null);
		}
		Tweet o = tweets.get(position);
		TextView tt = (TextView) v.findViewById(R.id.author);
		TextView bt = (TextView) v.findViewById(R.id.content);
		tt.setText(o.content);
		bt.setText(o.author);
		return v;
	}
}
