package com.bithackathon.charityoftheday;

import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;

public class MainActivity extends BaseCharityDisplayActivity {
	
	private Date lastInfoRequest = new Date(0);

	public MainActivity() {
		super();
	}
	    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Remember the last time we requested charity of the day information
        if (savedInstanceState != null) {
        	lastInfoRequest.setTime(savedInstanceState.getLong("lastInfoRequest"));
        }

        if (state == STATE_IDLE) {
        	getCharityInformation();
        }
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	// Get charity info if we have not already done so today
    	// The reason this is in onResume vs onCreate is that it's theoretically possible
    	// the app would be sleeping across the day boundary, and onCreate wouldn't get called when the
    	// user pulled it up again
    	// Thanks to: http://stackoverflow.com/questions/2517709/comparing-two-dates-to-see-if-they-are-in-the-same-day
    	Calendar cal1 = Calendar.getInstance();
    	Calendar cal2 = Calendar.getInstance();
    	
    	Date today = new Date();
    	
    	cal1.setTime(today);
    	cal2.setTime(lastInfoRequest);
    	
    	int cal1yr = cal1.get(Calendar.YEAR);
    	int cal2yr = cal2.get(Calendar.YEAR);
    	int cal1day = cal1.get(Calendar.DAY_OF_YEAR);
    	int cal2day = cal2.get(Calendar.DAY_OF_YEAR);
    	
    	boolean sameDay = cal1yr == cal2yr &&
    			cal1day == cal2day;
    	if (!sameDay && state != STATE_LOADING)
    	{
    		getCharityInformation();
    	}	
    }

	/**
     * Remember info when Android destroys our activity
     * example: if user reorients the phone
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	savedInstanceState.putLong("lastInfoRequest", lastInfoRequest.getTime());
    }
    
	@Override
	protected String getLoadingText() {
		return getString(R.string.retrieving_cotd);
	}

	@Override
	protected Integer getDesiredCharityId() {
		return null;	// Null means use charity of the day instead of a specific charity
	}
	
	@Override
	protected void getCharityInformation() {
		lastInfoRequest.setTime(new Date().getTime());
		super.getCharityInformation();
	}
	
}
