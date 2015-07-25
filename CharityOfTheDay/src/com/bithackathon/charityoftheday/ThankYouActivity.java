package com.bithackathon.charityoftheday;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class ThankYouActivity extends BaseCotdActivity {

	private boolean showDialog = false;
	private Charity charity = null;
	private String shareDialogText = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thank_you);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            this.charity = extras.getParcelable("charity");
        }
		
		if (savedInstanceState != null) {
			
			this.shareDialogText = savedInstanceState.getString("shareDialogText");
			if (this.shareDialogText == null) {
				this.shareDialogText = "";
			}
			
			if (savedInstanceState.getBoolean("showDialog")) {
				
				// Recreate the alert dialog
				createShareAlert(shareDialogText);
			}
        }
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
		getMenuInflater().inflate(R.menu.thank_you, menu);
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
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
     * Remember info when Android destroys our activity
     * example: if user reorients the phone
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	savedInstanceState.putBoolean("showDialog", this.showDialog);
    	savedInstanceState.putString("shareDialogText", this.shareDialogText);
    	savedInstanceState.putParcelable("charity", this.charity);
    }
	
	public void SeeTransactionHistory_Click(View view) {
		super.openTransactionHistory();
	}
	
	public void SeeOtherCharities_Click(View view) {
    	Intent intent = new Intent(this, SearchActivity.class);
    	startActivity(intent);		
	}

	public void Tweet_Click(View view) {
		String shareMessage;
		
		if (this.charity == null) {
			shareMessage = "Feeling generous? Find a charity using Coingive.";
		}
		else {
			shareMessage = String.format("Feeling generous? Donate your bitcoins to %s. ", this.charity.getUrl().toString());
		}
		createShareAlert(shareMessage);	
	}
	
	private void createShareAlert(String text) {
		
		this.showDialog = true;
		
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		
		// Create the dialog
		// Done this way as an API workaround.
		// See http://stackoverflow.com/questions/16970866/android-nosuchmethod-exception-for-dialog-setondismisslistener
		AlertDialog alert = new AlertDialog.Builder(this)
			.setTitle("Speak Out")
			.setView(input)
			.setPositiveButton("Share", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = input.getText().toString();
					Intent tweetIntent = new Intent();
					tweetIntent.setAction(Intent.ACTION_SEND);
					tweetIntent.setType("text/plain");
					tweetIntent.putExtra(Intent.EXTRA_TEXT, value);
					startActivity(Intent.createChooser(tweetIntent, "Share your thoughts."));
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
				}
			})
			.create();
		
		// Set the default text
		input.setText(text);
		shareDialogText = text;
		
		// Monitor the user's input
		input.addTextChangedListener(new ShareDialogTextWatcher());

		alert.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
            	showDialog = false;
            }
        });

		

		alert.show();
	}
	
	private class ShareDialogTextWatcher implements TextWatcher {	
		
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
			shareDialogText = s.toString();
		}
	}
}
