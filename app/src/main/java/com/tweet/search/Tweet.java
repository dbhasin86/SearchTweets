package com.tweet.search;

import java.io.Serializable;

public class Tweet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String author;
	public String content;

	public Tweet(String text, String fromUser) {
		this.content = text;
		this.author = fromUser;
	}

	public String getText() {

		return this.content;

	}

	public String getFromUser() {

		return this.author;

	}
}