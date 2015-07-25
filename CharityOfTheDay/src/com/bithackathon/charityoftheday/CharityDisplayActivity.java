package com.bithackathon.charityoftheday;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Displays information on a single charity
 * Gives the user the option to donate to this charity
 * The charity id must be specified under the key name "Charity ID" in the intent extras 
 */
public class CharityDisplayActivity extends BaseCharityDisplayActivity {
	
	public static final String EXTRA_CHARITY_ID = "Charity ID";
	
	public CharityDisplayActivity() {
		super();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (state == STATE_IDLE) {
            // Activity may be starting for the first time
        	// If so, intent should specify which charity to load
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.containsKey(EXTRA_CHARITY_ID)) {
                    charity.setId(extras.getInt(EXTRA_CHARITY_ID));
                    getCharityInformation();
                }
            }
        	
        }        
    }

	@Override
	protected String getLoadingText() {
		return getString(R.string.retrieving_charity);
	}

	@Override
	protected Integer getDesiredCharityId() {
		return charity.getId();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// NavUtils.navigateUpFromSameTask(this);
			// TODO: not sure if this is the right way to do this. goes "back" instead of up to home
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void showCharityInformation() {
		super.showCharityInformation();
        // Change the text of the "charity of the day" label
        TextView label = (TextView)findViewById(R.id.Main_TextView_CotdLabel);
        label.setText("Charity");
	}
}
