package com.bithackathon.charityoftheday;

import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.BadTokenException;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This activity allows the user to authenticate with Coinbase.
 * If the user has not authenticated, we tell them to authenticate.
 * If the user has already authenticated, we tell them that they're good to go.
 */
public class AccountSettingsActivity extends BaseCotdActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Show the Up button in the action bar.
		setupActionBar();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Get the updated daily debit limit. This will also cause the authentication tokens
		// to get cleared if Coingive is no longer authenticated.
		new GetDailyDebitLimitTask().execute();
		
		// Check if Coingive is authenticated and display the appropriate view.
		checkAuthenticated();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.account_settings, menu);
		return true;
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
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1 && resultCode == Activity.RESULT_OK && data.getExtras().containsKey("error_description")) {
			String error_description = data.getStringExtra("error_description");
			// Show an error message
			AlertDialog ad = new AlertDialog.Builder(this).create();  
			ad.setMessage(error_description);  
			ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {  
			    @Override  
			    public void onClick(DialogInterface dialog, int which) {  
			        dialog.dismiss();                      
			    }  
			}); 
			try {
				ad.show();
			}
			catch (BadTokenException e) {
				// This will happen if the user hits the back button before the dialog is displayed
			}
		}
	}
	
	/**
	 * Fired when user clicks the "Link My Coinbase" button
	 * @param view
	 */
	public void linkMyCoinbase(View view)
	{
		ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
		try {
			// Check for a network connection
			if (networkInfo != null && networkInfo.isConnected()) {
				try {
					// Get the debit limit entered by the user
					EditText debitLimitText = (EditText)findViewById(R.id.settings_editText_withdrawal_limit);
					String debitLimit = debitLimitText.getText().toString();
					if (debitLimit == null) {
						debitLimit = "25";
					}
					
					// Direct the user to the Coinbase authentication page
					Intent browserIntent;
					browserIntent = new Intent(Intent.ACTION_VIEW, 
							Uri.parse(new CoinbaseManager(getApplicationContext()).getAuthenticationUri(debitLimit)), 
							getApplicationContext(), 
							BrowserActivity.class);
					startActivityForResult(browserIntent, 1);
				} catch (IOException e) {
					e.printStackTrace();
					// Rethrow exception with friendlier message
					throw new Exception("Couldn't connect to Coinbase.");
				}					
			} else {
				// Inform user that they need to move out of ND and get an internet connection
				throw new Exception("To link your account, connect to the Internet.");
			}	
			
		} catch (Exception ex) { 
			showErrorDialog(ex.getMessage());   
		}
	}
	
	/**
	 * Fired when user clicks "Edit Coingive settings" button
	 */
	public void goToCoinbaseAccountSettings(View view) {
		ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
		if (networkInfo != null && networkInfo.isConnected()) {
			// Direct the user to the Coinbase app account settings page
			String url = "https://coinbase.com/account/apps";
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(browserIntent);
		}
		else {
			showErrorDialog("To change your settings, connect to the Internet.");
		}
	}
	
	private void showErrorDialog(String message) {
		AlertDialog ad = new AlertDialog.Builder(this).create();  
		ad.setMessage(message);  
		ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {  
		    @Override  
		    public void onClick(DialogInterface dialog, int which) {  
		        dialog.dismiss();                      
		    }  
		});  
		try {
			ad.show();
		}
		catch (BadTokenException e) {
			// This will happen if the user hits the back button before the dialog is displayed
		} 
	}
	
	/**
	 * Fired when user clicks the "What is Coinbase?" button
	 * Opens a browser to coinbase.com
	 */
	public void WhatIsCoinbase_Click(View view) {
		String url = "http://www.coinbase.com";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}
	    
    private class GetDailyDebitLimitTask extends AsyncTask<String, Void, Double> {
    	
		@Override
		protected Double doInBackground(String... args) {
			double debitLimit = -1;
			ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			
			if (networkInfo != null && networkInfo.isConnected()) {
				try {
					debitLimit = new CoinbaseManager(getApplicationContext()).getDailyDebitLimit();
				} catch (Exception e) {
					// Just ignore an exception and return -1 for the debit limit.
					e.printStackTrace();
				}
			}
			
			return debitLimit;
		}
		
		 @Override
	       protected void onPostExecute(Double debitLimit) {
			 
			 // Check if Coingive is authenticated and display the appropriate view.
			 boolean authenticated = checkAuthenticated();
			 
			 if (authenticated) {
				 ProgressBar progressBar = (ProgressBar)findViewById(R.id.AccountSettings_ProgressBar_Refreshing);
				 TextView askingCoinbaseTextView = (TextView)findViewById(R.id.AccountSettings_TextView_AskingCoinbase);
				 TextView authenticatedTextView = (TextView)findViewById(R.id.lblAuthenticatedText);
				 
				 // Hide the progress bar and associated text view.
				 progressBar.setVisibility(ProgressBar.INVISIBLE);
				 askingCoinbaseTextView.setVisibility(TextView.INVISIBLE);
				 
				 // Display the authenticated text 
				 if (debitLimit == -1) {
					 authenticatedTextView.setText(getString(R.string.account_settings_authenticated_text1));
				 }
				 else {
					 String authenticatedText = String.format(getString(R.string.account_settings_authenticated_text2), debitLimit);
					 authenticatedTextView.setText(authenticatedText);
				 }
			 }
	      }
	}
    
    /**
     * Check for stored authentication tokens and display the appropriate view.
     */
    private boolean checkAuthenticated() {
    	boolean authenticated = false;
    	
		try {
			CoinbaseManager c = new CoinbaseManager(getApplicationContext());
			authenticated = c.isAuthenticated();
			Log.d("Authentication", "AccountSettingsActivity.onResume(): authenticated = " + Boolean.valueOf(authenticated));
		} catch(Exception ex) {
			// TODO: Not sure what to do here yet.
			ex.printStackTrace();
		}
		
		// If user is authenticated, show a confirmation
		// Otherwise, show a huge wall of text saying OMG AUTHENTICATE NAO
		if (authenticated) {
			setContentView(R.layout.activity_account_settings_authenticated);
		} else {
			setContentView(R.layout.activity_account_settings);
		}
		
		return authenticated;
    }

}
