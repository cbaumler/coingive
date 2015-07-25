package com.bithackathon.charityoftheday;

import java.io.IOException;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

/**
 * Activity contains logic to handle the basic COTD action bar buttons
 * Most activities in the COTD project should inherit from this activity
 */
public class BaseCotdActivity extends ActionBarActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_transaction_history:
        	openTransactionHistory();
            return true;
        case R.id.action_account_settings:
            openAccountSettings();
            return true;
        case R.id.action_search:
        	openSearch();
        	return true;
        default:
            return super.onOptionsItemSelected(item);        	
        }
    }

    /**
     * Called when user clicks the "Transaction History" button on the action bar.
     * Sends the user to the transaction history activity
     */
    public void openTransactionHistory()
    {    	
    	Intent intent = new Intent(this, TransactionHistoryActivity.class);
    	startActivity(intent);
    }
    
    /**
     * Called when user clicks the "Account Settings" button on the action bar.
     * Sends the user to the account settings activity
     */
    public void openAccountSettings()
    {    	
    	Intent intent = new Intent(this, AccountSettingsActivity.class);
    	startActivity(intent);
    }
    
    /**
     * Fired when user clicks "search" button in the action bar
     * Open the search activity
     */
    protected void openSearch() {
    	Intent intent = new Intent(this, SearchActivity.class);
    	startActivity(intent);
	}
    
    public void authenticateWithCoinbase() {
    	try {
    		// Check whether the application is already authenticated
			if (new CoinbaseManager(getApplicationContext()).getCredential() == null) {
				Intent settingsIntent = new Intent(this, AccountSettingsActivity.class);
				startActivity(settingsIntent);
			}
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    }

}
