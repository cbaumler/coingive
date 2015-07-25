package com.bithackathon.charityoftheday;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bithackathon.charityoftheday.server.CharityOfTheDayRequestMessage;
import com.bithackathon.charityoftheday.server.CharityRequestMessage;
import com.bithackathon.charityoftheday.server.CharityResponseMessage;
import com.bithackathon.charityoftheday.server.CotdServer;
import com.bithackathon.charityoftheday.server.ErrorResponseMessage;
import com.bithackathon.charityoftheday.server.ResponseMessage;

/**
 * Common functionality shared between the MainActivity and the CharityDisplayActivity
 * Both of these activities display info on a single charity.
 * This base activity handles downloading charity information, graphical display, and restoring
 * the activity if it is destroyed.
 * Subclasses are mainly responsible for figuring out WHEN the charity information is first downloaded outside
 * of an activity re-creation.
 */
public abstract class BaseCharityDisplayActivity extends BaseCotdActivity {
	/**
	 * Represents the three possible states of this activity.
	 */
	protected static final int STATE_IDLE = 0;
	protected static final int STATE_LOADING = 1;
	protected static final int STATE_ERROR = 2;
	protected static final int STATE_CHARITY_DISPLAY = 3;
	
	/**
	 * This variable represents the actual state of the activity
	 */
	protected int state = STATE_IDLE;
	
	/**
	 * Charity info from the database. 
	 * charity.getId() property is valid at all times
	 * The rest of the info is only valid if state = STATE_CHARITY_DISPLAY
	 */
	protected Charity charity = new Charity();
	
	protected Bitmap charityLogoBitmap;
	protected String lastErrorMessage = ""; 

	/**
	 * onCreate handles the recreation of the activity if it was destroyed previously.
	 * Subclasses can check the state variable...if it equals STATE_IDLE, then the activity
	 * is brand-new and they can go ahead and do whatever they want (probably load charity info).
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // restarting the activity; either show previously-acquired charity information
        // or the last error message
        if (savedInstanceState != null)
        {
        	state = savedInstanceState.getInt("state");
        	charity = savedInstanceState.getParcelable("charity");
        	charityLogoBitmap = savedInstanceState.getParcelable("charityLogoBitmap");
        	lastErrorMessage = savedInstanceState.getString("lastErrorMessage");
        	
        	switch(state)
        	{
        	case STATE_ERROR:
        		showErrorInformation(lastErrorMessage);
        		break;
        	case STATE_CHARITY_DISPLAY:
        		showCharityInformation();
        		break;
        	default:
        		// We were in the process of grabbing charity of the day info, so grab it.
        		state = STATE_LOADING;
        		getCharityInformation();
        	}
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    
    /**
     * Event handler for all action bar events
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
    	return super.onOptionsItemSelected(item);
    }

	/**
     * Remember info when Android destroys our activity
     * example: if user reorients the phone
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	savedInstanceState.putInt("state", state);
    	savedInstanceState.putParcelable("charity", charity);
    	savedInstanceState.putParcelable("charityLogoBitmap", charityLogoBitmap);
    	savedInstanceState.putString("lastErrorMessage", lastErrorMessage);
    }
    
    /**
     * Called when user clicks the "Donate Now" button
     * Sends the user to the donation activity
     * @param view
     */
    public void donateNow(View view)
    {
    	Intent intent = new Intent(this, DonateActivity.class);
    	intent.putExtra("charityObject", charity);
    	startActivity(intent);
    }
    
    /**
     * Must be implemented by subclass
     * Returns the text that will be displayed while charity information is loading.
     * @return String
     */
    protected abstract String getLoadingText();
    
    /**
     * Must be implemented by subclass
     * Returns the id of the charity that should be downloaded and displayed to the user
     * If this function returns null, the charity of the day will be downloaded.
     * @return
     */
    protected abstract Integer getDesiredCharityId();

    /**
     * Query the server for information on the selected charity
     */
    protected void getCharityInformation()
    {
    	Log.d("LoadCharity", "entering getCharityInformation()");
		// See if we have network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) 
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Start out by showing the loading screen
        	state = STATE_LOADING;
            setContentView(R.layout.activity_main_loading);
            TextView lbl = (TextView)findViewById(R.id.RetrievingMessage);
            lbl.setText(getLoadingText());
            // Data, data, data! I need data! (Get the charity information)
        	Log.d("LoadCharity", "launching new GetCharityTask");
        	new GetCharityTask().execute(getDesiredCharityId());        		
        } else {
            // no network connectivity; stop and display error
        	showErrorInformation(getString(R.string.error_no_network));
        }
    }
    
    /**
     * Called when the user clicks "Try Again" on the error page.
     * Causes the app to try to connect to the COTD server again
     * @param view
     */
    public void tryAgain(View view) {
    	getCharityInformation();
    }

    /**
     * Stolen from: https://developer.android.com/training/basics/network-ops/connecting.html
     * This task works independent of the main thread to grab info on the selected charity
     * If no charity id is specified, get the charity of the day
     * If the request succeeds, show the charity information; else, show an error message
     */
    protected class GetCharityTask extends AsyncTask<Integer, Void, ResponseMessage> {
       @Override
       protected ResponseMessage doInBackground(Integer...charityIds) {             
    	   Log.d("LoadCharity", "entering GetCharityTask.doInBackground");
           return downloadCharity(charityIds[0]);
       }
       // onPostExecute displays the results of the AsyncTask.
       @Override
       protected void onPostExecute(ResponseMessage result) {
    	   Log.d("LoadCharity", "entering GetCharityTask.onPostExecute");
    	   if (result.getResponseId() == 0) {
        	   Log.d("LoadCharity", "error occured in GetCharityTask.onPostExecute");
    		   // Error condition
    		   showErrorInformation("A server error has occurred.");
    		   System.err.println("Server response error: " + ((ErrorResponseMessage)result).getErrorMessage());    		   
    	   } else {
        	   Log.d("LoadCharity", "GetCharityTask.onPostExecute reached success branch");
    		   // We got charity information!
    		   charity = ((CharityResponseMessage)result).getCharity();

        	   Log.d("LoadCharity", "launching new DownloadImageTask");
	           	// If the charity information request was successful, download the charity logo image
	       		new DownloadImageTask()
		            .execute(CotdServer.imagePath + charity.getImageUrl());
    	   }
    		  
      }
   }

    /**
     * Queries the server for information on a given charity
     * Return the response message from the server which can
     * either be an error or the requested charity info
     * @param charityId Charity Id. If null, Charity of the Day will be requested
     * @return ResponseMessage
     */
	private ResponseMessage downloadCharity(Integer charityId) {
		CotdServer server = new CotdServer();
		if (charityId == null) {
			return server.request(new CharityOfTheDayRequestMessage());			
		} else {
			return server.request(new CharityRequestMessage(charityId));
		}
	}

	/**
	 * stole this from: http://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
	 * background task to download an image
	 */
	protected class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon11 = null;
	        HttpURLConnection urlConnection = null;
	        
	        try {
//	        	Log.d("DownloadImage", "Create new Http Client");
//	        	DefaultHttpClient httpClient = new DefaultHttpClient();
//	        	Log.d("DownloadImage", "Create new HttpGet");
//	            HttpGet request = new HttpGet(urldisplay);
//	        	Log.d("DownloadImage", "calling execute");
//	            HttpResponse response = httpClient.execute(request);
//	        	Log.d("DownloadImage", "use getEntity.getContent");
//	        	Drawable drawable = Drawable.createFromStream(response.getEntity().getContent(), 
//	        													"src");
//	            mIcon11 = drawable;
	            
	        	URL url = new URL(urldisplay);
	        	Log.d("LoadCharity", "about to launch urlConnection.getInputStream()");
	        	urlConnection = (HttpURLConnection) url.openConnection();
	        	Log.d("LoadCharity", "about to set urlConnection timeouts");
	        	urlConnection.setConnectTimeout(10000);
	        	urlConnection.setReadTimeout(5000);
	        	Log.d("LoadCharity", "about to launch urlConnection.getInputStream()");
        		InputStream in = new BufferedInputStream(urlConnection.getInputStream());

	            //InputStream in = new java.net.URL(urldisplay).openStream();
	            Log.d("LoadCharity", "about to call BitmapFactory.decodeStream()");
	            mIcon11 = BitmapFactory.decodeStream(in);
	        } catch (SocketTimeoutException e) {
	        	Log.d("LoadCharity", "DownloadImageTask.doInBackground timed out");
	        	e.printStackTrace();
	        } catch (Exception e) {
	        	Log.d("LoadCharity", "DownloadImageTask.doInBackground got an exception");
	            Log.e("DownloadImageTask Error", e.getMessage());
	            e.printStackTrace();
	        }
	        finally {
	        	if (urlConnection != null) {
	        		urlConnection.disconnect();
	        	}
	        }
	        return mIcon11;
	    }

	    // At this point, we have downloaded the charity text info and the charity logo
	    // Now display everything to the user
	    protected void onPostExecute(Bitmap result) {
	    	Log.d("LoadCharity", "reached DownloadImageTask.onPostExecute()");
	    	charityLogoBitmap = result;
	    	showCharityInformation();
	    }
	}
	
	/**
	 * Configures the Main Activity to show charity information to the user.
	 * Charity information is drawn from the cotd and charityLogoBitmap properties
	 * @param imageIsValid True if we were able to download the charity image from the server, false otherwise
	 */
	protected void showCharityInformation() {
		Log.d("LoadCharity", "entering showCharityInformation");
		state = STATE_CHARITY_DISPLAY;
        setContentView(R.layout.activity_main);
 	   Log.d("LoadCharity", "showCharityInformation, just finished setContentView");
        

        TextView charityName = (TextView) findViewById(R.id.charityName);
	    TextView charityDescription = (TextView) findViewById(R.id.charityDescription);

	    charityName.setText(charity.getName());
	    charityDescription.setText(charity.getDescription());

	    if (charityLogoBitmap != null) {
	    	ImageView logoImageView = (ImageView)findViewById(R.id.Main_ImageView_CharityLogo);
	    	if (logoImageView != null) {
	    		logoImageView.setImageBitmap(charityLogoBitmap);
	    	}
	    }
	    
	    ImageView banner = (ImageView)findViewById(R.id.Main_ImageView_TopBanner);
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

 	   	Log.d("LoadCharity", "exiting showCharityInformation");
		
	}
	
	/**
	 * Configures the Main Activity to show error information to the user
	 * Error string is stored to the lastErrorMessage property
	 */
	protected void showErrorInformation(String newErrorMessage) {
		state = STATE_ERROR;
        setContentView(R.layout.activity_main_connectionerror);
        TextView errorTextView = (TextView) findViewById(R.id.errorText);
		errorTextView.setText(newErrorMessage);
		lastErrorMessage = newErrorMessage;
	}    

	/**
	 * Fired when user clicks the "Visit Website" button
	 * Opens a browser with the selected charity's window
	 */
	public void VisitWebsite_Click(View view) {
		String url = charity.getUrl();
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}
}
