package com.bithackathon.charityoftheday;

import java.util.Timer;
import java.util.TimerTask;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Spies on what the user is typing into the SearchBox in SearchActivity (although theoretically it
 * can be used for any EditText)
 * If the user does not type anything new within a certain time period, submit the SearchBox contents as a search 
 * request to the server.
 */
public class SearchBoxTextWatcher implements TextWatcher {
	private Timer timer;
	private String searchBoxText;
	
	/**
	 * How long (in ms) to wait before submitting a search request
	 */
	private final long typingDelay = 500;
	

	public SearchBoxTextWatcher() {
		timer = new Timer("SearchBoxTextWatcher");
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// Don't care
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// Don't care
	}

	@Override
	public void afterTextChanged(Editable s) {
		searchBoxText = s.toString();
		timer.cancel();
		timer.schedule(new SearchBoxTask(), typingDelay);
	}
	
	/**
	 * This is the code that executes after the user stops typing for typingDelay
	 */
	private class SearchBoxTask extends TimerTask {
		public void run() {
			// Don't run the search if the user ends up typing nothing
			if (searchBoxText == "") { return; }
			
			
			
		}
	}
	
	

}
