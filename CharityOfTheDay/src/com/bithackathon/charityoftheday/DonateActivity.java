package com.bithackathon.charityoftheday;

import java.io.IOException;

import org.json.JSONException;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.BadTokenException;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bithackathon.charityoftheday.transactiondb.Transaction;
import com.bithackathon.charityoftheday.transactiondb.TransactionsDataSource;

/**
 * This activity allows the user to donate to their charity of choice.
 * This activity expects one parameter in its starting intent: "charityObject", which contains a Charity
 * object (whoa, crazy right?) that describes the user's charity selection.
 */
public class DonateActivity extends BaseCotdActivity {
	
	private Charity charity;
	
	/**
	 * These constants are used by changeDonateButton to determine what text
	 * is displayed by the donate button and whether the user can click it or not
	 */
	private final int DONATE_BUTTON_NORMAL = 0;
	private final int DONATE_BUTTON_WORKING = 1;
	private final int DONATE_BUTTON_DONE = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_donate);
		// Show the Up button in the action bar.
		setupActionBar();
		// Load up charity information
		Bundle extras = getIntent().getExtras();
		charity = (Charity)extras.getParcelable("charityObject");
		TextView charityName = (TextView) findViewById(R.id.charityName_donate_activity);
		charityName.setText(charity.getName());
		
		TextView bitcoinAddress = (TextView)findViewById(R.id.Donate_TextView_BitcoinAddress);
		bitcoinAddress.setText(charity.getBitcoinAddress());

		// If in portrait mode, show this charity's personal image
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
		    ImageView banner = (ImageView)findViewById(R.id.Donate_ImageView_TopBannerPortrait);
		    // TODO: get this image from server, not from app. heh.
		    Log.d("LoadCharity", "charity id = " + Integer.valueOf(charity.getId()));
		    switch(charity.getId())
		    {
		    case 2:
		    	banner.setImageDrawable(getResources().getDrawable(R.drawable.main_charity_eip));
		    	break;
		    case 3:
		    	banner.setImageDrawable(getResources().getDrawable(R.drawable.main_charity_pgeeks));
		    	break;
		    case 4:
		    	banner.setImageDrawable(getResources().getDrawable(R.drawable.main_charity_recycles));
		    	break;
		    case 5:
		    	banner.setImageDrawable(getResources().getDrawable(R.drawable.main_charity_sol));
		    	break;
		    case 6:
		    	banner.setImageDrawable(getResources().getDrawable(R.drawable.main_charity_seans));
		    	break;
		    case 7:
		    	banner.setImageDrawable(getResources().getDrawable(R.drawable.main_charity_gbsi));
		    	break;
		    case 8:
		    	banner.setImageDrawable(getResources().getDrawable(R.drawable.main_charity_fr33));
		    	break;
		    case 10:
		    	banner.setImageDrawable(getResources().getDrawable(R.drawable.main_charity_twp));
		    	break;
		    default:
		    }
		}

		authenticateWithCoinbase();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Restore state of btc balance controls
		// RESTORE BALANCE TO THE FORCE JEDI MASTER
		TextView userBalanceBtc = (TextView)findViewById(R.id.Donate_TextView_Balance);
		userBalanceBtc.setVisibility(View.VISIBLE);
		userBalanceBtc.setText("Loading....");
		((TextView)findViewById(R.id.Donate_TextView_Balancelabel)).setText(getString(R.string.donate_balance));
		
		new GetBalanceTask().execute();
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
		getMenuInflater().inflate(R.menu.donate, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // NavUtils.navigateUpFromSameTask(this);
        	finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when user clicks the "Donate" button
     * Process donation and send user to Thank You page
     * @param view
     */
    public void userClicksDonateButton(View view)
    {
    	// Don't let the user accidentally donate twice
    	changeDonateButton(DONATE_BUTTON_WORKING);
    	
    	EditText amountEditText = (EditText)findViewById(R.id.donationAmount);
    	String amount = amountEditText.getText().toString();
    	
    	// TODO Connect this to activity elements
    	System.out.println("About to call sendMoney");
    	System.out.println("amount = " + amount);
    	System.out.println("address = " + charity.getBitcoinAddress());
    	sendMoney(amount, charity.getBitcoinAddress());
    }
    
    /**
     * Change the text and enabled status of the Donate button
     * See top of file for valid displayState values
     * @param displayState int
     */
    private void changeDonateButton(int displayState) {
    	Button donateButton = (Button)findViewById(R.id.Donate_Button_Donate);

    	switch (displayState) {
    	case DONATE_BUTTON_WORKING:
        	donateButton.setText(getString(R.string.donate_donation_button_working));
        	donateButton.setEnabled(false);    	
    		break;
    	case DONATE_BUTTON_DONE:
        	donateButton.setText(getString(R.string.donate_donation_button_done));
        	donateButton.setEnabled(false);    	
    		break;
    	default:	// NORMAL or other
        	donateButton.setText(getString(R.string.donate_donation_button_donate));
        	donateButton.setEnabled(true);    	
    	}
    }
        
    /**
     * You know you've come far in life when you have a function called sendMoney in your app.
     * @param amount In BTC
     * @param address Bitcoin address of intended recipient
     */
    private void sendMoney(String amount, String address) {
//		AlertDialog ad = new AlertDialog.Builder(DonateActivity.this).create();  
//		ad.setMessage("Sending money is disabled for testing purposes. See DonateActivity.sendMoney()");  
//		ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {  
//		    @Override  
//		    public void onClick(DialogInterface dialog, int which) {  
//		        dialog.dismiss();                      
//		    }  
//		});  
//		ad.show();
		
		// TODO: uncomment this when you want to send money
    	String url = "https://coinbase.com/api/v1/transactions/send_money/";
		new SendMoneyTask().execute(url, amount, address);
    	//Intent intent = new Intent(getApplicationContext(), ThankYouActivity.class);
    	//intent.putExtra("charity",  charity);
    	//startActivity(intent);
    }
    
    /**
     * Background task that communicates with the Coinbase server to send donation money
     * to the given charity address.
     * If successful, log the transaction in the COTD ledger and redirect to the Thank You activity
     * If unsuccessful, show a bleak error message.
     * Two String arguments:
     * (1) API URL
     * (2) Amount (yeah, it's a string)
     */
    private class SendMoneyTask extends AsyncTask<String, Void, Transaction> {

		@Override
		protected Transaction doInBackground(String... args) {
			ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			Transaction t = null;
			
			if (networkInfo != null && networkInfo.isConnected()) {
				try {
					// Check whether the user entered a donation amount.
					// TODO remove this if statement
					if ((args[1]) == null || args[0] == null || charity == null) {
						t = new Transaction();
						t.setErrorMessage("C'mon don't be cheap.");
						t.setErrorCode(CoingiveErrorCode.INVALID_DONATION_AMOUNT);
					}
					else {
						t = new CoinbaseManager(getApplicationContext()).sendMoney(args[0], args[1], charity);
					}
				} catch (IOException e) {
					if (e.getMessage() != null && e.getMessage().contains("401 Unauthorized")) {
						// Coingive is no longer authenticated
						t = new Transaction();
						t.setErrorMessage("To donate link your Coinbase account");
						t.setErrorCode(CoingiveErrorCode.UNAUTHORIZED);
					}
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransactionException e) {
					// TODO Dave-generated catch block
					e.printStackTrace();				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				t = new Transaction();
				t.setErrorMessage(getString(R.string.donate_error_nonetwork));
				t.setErrorCode(CoingiveErrorCode.NO_NETWORK_CONNECTION);
			}
			
			return t;
		}
		
		@Override
		protected void onPostExecute(Transaction transaction) {
			AlertDialog ad;
			
			if (transaction == null) {
				transaction = new Transaction();
				transaction.setErrorCode(CoingiveErrorCode.UNKNOWN_ERROR);
				transaction.setErrorMessage(getString(R.string.donate_donation_failed));
			}
			
			switch (transaction.getErrorCode()) {
				case NO_ERROR:
					changeDonateButton(DONATE_BUTTON_DONE);

			    	// Log the transaction in the cotd transaction history
					TransactionsDataSource datasource = new TransactionsDataSource(getApplicationContext());		
					datasource.open();
					datasource.createTransaction(charity, transaction.getTransactionId(), transaction.getAmount());				
					datasource.close();				

					// Redirect to Thank You page
			    	Intent intent = new Intent(getApplicationContext(), ThankYouActivity.class);
			    	// Personalize the Thank You page with this information
			    	intent.putExtra("charity",  charity);
					// String donationAmount = response.getJSONObject("transaction").getJSONObject("amount").getString("amount");
			    	// intent.putExtra("amount", donationAmount);
			    	startActivity(intent);
					break;
				case UNAUTHORIZED:
			    	changeDonateButton(DONATE_BUTTON_NORMAL);
			    	// Show a "special" error message
			    	ad = new AlertDialog.Builder(DonateActivity.this).create();  
					ad.setMessage(transaction.getErrorMessage());  
					ad.setButton(DialogInterface.BUTTON_POSITIVE, "Link", new DialogInterface.OnClickListener() {  
					    @Override  
					    public void onClick(DialogInterface dialog, int which) {  
					        // Send the user to the account settings page
					    	Intent settingsIntent = new Intent(DonateActivity.this, AccountSettingsActivity.class);
							startActivity(settingsIntent);
					    }  
					});
					ad.setButton(DialogInterface.BUTTON_NEGATIVE, "Later", new DialogInterface.OnClickListener() {  
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
					break;
				case COINBASE_API_FAIL:
				case UNKNOWN_ERROR:
				case INVALID_DONATION_AMOUNT:
				default:
			    	changeDonateButton(DONATE_BUTTON_NORMAL);
			    	// Show an error message
					ad = new AlertDialog.Builder(DonateActivity.this).create();  
					ad.setMessage(transaction.getErrorMessage());  
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
					break;
			}
		}
	}
    
    /**
     * Get the user's Coinbase account balance
     */
    private class GetBalanceTask extends AsyncTask<String, Void, CoinbaseBalance> {
    	
		@Override
		protected CoinbaseBalance doInBackground(String... args) {
			
			CoinbaseBalance balance = new CoinbaseBalance();
			balance.setValid(false);
			try {
				
				balance.setBalanceBtc(new CoinbaseManager(getApplicationContext()).getAccountBalanceInBtc());
				balance.setBalanceUsd(new CoinbaseManager(getApplicationContext()).getAccountBalanceInUsd());
				balance.setValid(true);
			} 
			catch (IOException e) {
				e.printStackTrace();	
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return balance;
		}
		
		 @Override
		 protected void onPostExecute(CoinbaseBalance balance) {
			 
			 TextView userBalanceBtc = (TextView)findViewById(R.id.Donate_TextView_Balance);
	
			 if (balance.isValid()) {
				 userBalanceBtc.setText(String.format(getApplicationContext().getString(R.string.balance), 
						 balance.getBalanceBtc(), balance.getBalanceUsd())); 
			 } else {
				 userBalanceBtc.setVisibility(View.INVISIBLE);
				 ((TextView)findViewById(R.id.Donate_TextView_Balancelabel)).setText(getString(R.string.donate_error_accountbalance));
			 }
	      }
	}
}
