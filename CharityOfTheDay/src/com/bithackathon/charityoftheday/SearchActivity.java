package com.bithackathon.charityoftheday;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bithackathon.charityoftheday.server.CotdServer;
import com.bithackathon.charityoftheday.server.ErrorResponseMessage;
import com.bithackathon.charityoftheday.server.ResponseMessage;
import com.bithackathon.charityoftheday.server.SearchRequestMessage;
import com.bithackathon.charityoftheday.server.SearchResponseMessage;

/**
 * This activity allows the user to search for charities by their names and descriptions.
 */
public class SearchActivity extends BaseCotdActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		// Show the Up button in the action bar.
		setupActionBar();
		
        // Restore search text from previous instance
		EditText searchBox = (EditText)findViewById(R.id.SearchBox);
		String searchBoxText = "";
		if (savedInstanceState != null) {
			searchBoxText = savedInstanceState.getString("SearchBoxText");
			searchBox.setText(searchBoxText);
		}

        // Setup a watcher that will fire every time the text in the SearchBox changes
		searchBox.addTextChangedListener(new SearchBoxTextWatcher());
		
		// Search on whatever is in the search box at startup
		// If this is a brand-new activity, the search box will be blank, and a search
		// will show all charities in the db. This is okay for now.
    	new SearchRequestTask().execute(searchBoxText);        		
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
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	/**
     * Remember info when Android destroys our activity
     * example: if user reorients the phone
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	
    	EditText searchBox = (EditText)findViewById(R.id.SearchBox);
    	String searchText = searchBox.getText().toString();
    	
    	savedInstanceState.putString("SearchBoxText", searchText);
    	
    	// TODO: save search results as well?
    }
    
	/**
	 * Spies on what the user is typing into the SearchBox in SearchActivity
	 * If the user does not type anything new within a certain time period, submit the SearchBox contents as a search 
	 * request to the server.
	 */
	private class SearchBoxTextWatcher implements TextWatcher {
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
			timer = new Timer("SearchBoxTextWatcher");
			timer.schedule(new SearchBoxTask(searchBoxText), typingDelay);
		}
		
		/**
		 * This is the code that executes after the user stops typing for typingDelay
		 */
		private class SearchBoxTask extends TimerTask {
			private String searchBoxText;
			public SearchBoxTask(String newSearchBoxText) {
				searchBoxText = newSearchBoxText;
			}
			public void run() {
				// Run the search request
	        	new SearchRequestTask().execute(searchBoxText);
			}
		}
		
		

	}

	/**
	 * Handles the search request and response
	 */
	private class SearchRequestTask extends AsyncTask<String, Void, ResponseMessage> {
	     protected ResponseMessage doInBackground(String... requests) {
	    	 int count = requests.length;
	    	 String request;
	         if (count == 0) {
	        	 Log.e("Search", "Cannot call SearchRequestTask with an empty string array!");
	        	 request = "";
	         } else {
	        	 request = requests[0];
	         }
	         
	         // See if we have network connectivity
	         ConnectivityManager connMgr = (ConnectivityManager) 
	        		 getSystemService(Context.CONNECTIVITY_SERVICE);
	         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	         if (networkInfo != null && networkInfo.isConnected()) {
	        	 if (request != null) {
		        	 // We're good; send request to server
			         CotdServer server = new CotdServer();
			         return server.request(new SearchRequestMessage(request));
	        	 } else {
	        		 // Search request is invalid; show an empty search list
		        	 ErrorResponseMessage msg = new ErrorResponseMessage();
		        	 msg.setErrorMessage("");
		        	 return msg;	        		 
	        	 }
	         } else {
	             // no network connectivity; create error message to this effect
	        	 ErrorResponseMessage msg = new ErrorResponseMessage();
	        	 msg.setErrorMessage(getString(R.string.search_error_no_network));
	        	 return msg;
	         }

	     }

	     protected void onPostExecute(ResponseMessage result) {
	    	 if (result.getResponseId() == 0) {
	    		   String errMsg = ((ErrorResponseMessage)result).getErrorMessage();
	    		   // Error condition
	    		   Log.e("Search", "Server response error: " + errMsg);
	    		   // Show generic error to user
	    		   if (errMsg == getString(R.string.search_error_no_network)) {
	    			   showErrorMessage(getString(R.string.search_error_no_network));
	    		   } else if (errMsg == "") {
	    			   showErrorMessage("");
	    		   } else {
	    			   showErrorMessage(getString(R.string.search_error_server_error));
	    		   }
	    	 } else if(((SearchResponseMessage)result).getCharities().size() == 0) {
	    		 showErrorMessage(getString(R.string.search_error_no_results));
	    	 } else {
	    		   // Search results arrived
	    		   ArrayList<Charity> charities = ((SearchResponseMessage)result).getCharities();
	    		   ListView l = (ListView)findViewById(R.id.SearchResults);
	    		   
	    		   // Store the name and id of each charity into the listview
	    		   ArrayAdapter<Charity> adapter = new ArrayAdapter<Charity>(getApplicationContext(), 
	    				   android.R.layout.simple_list_item_1,
	    				   charities) {
	    			    @Override
	    			    public View getView(int position, View convertView, ViewGroup parent) {
	    			        View v = convertView;
	    			        if (v == null) {
	    			            LayoutInflater vi = getLayoutInflater();
	    			            v = vi.inflate(android.R.layout.simple_list_item_1, null);
	    			        }
	    			        // This is where we store the id
	    			        int charityId = getItem(position).getId();
	    			        v.setTag(charityId);

	    			        // And display the charity name
	    			        TextView text = (TextView)v.findViewById(android.R.id.text1);
	    			        text.setText(getItem(position).getName());
	    			        return v;
	    			    }
	    			    
	    		   };
	    		   
	    		   // When user clicks on a charity, goto a new activity containing the charity info
	    		   l.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							int charityId = (Integer)view.getTag();
							
							Intent intent = new Intent(getApplicationContext(), CharityDisplayActivity.class);
							intent.putExtra(CharityDisplayActivity.EXTRA_CHARITY_ID, charityId);
							startActivity(intent);							
						}

	    		   });
	    		   l.setAdapter(adapter);
	    	   }
	     }
	}
	
	/**
	 * Replaces search results with an error message
	 * @param errMessage
	 */
	private void showErrorMessage(String errMessage) {
   	 	ListView l = (ListView)findViewById(R.id.SearchResults);
		ArrayList<String> errors = new ArrayList<String>();
		errors.add(errMessage);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), 
				android.R.layout.simple_list_item_1,
				errors){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = convertView;
				if (v == null) {
					LayoutInflater vi = getLayoutInflater();
				    v = vi.inflate(android.R.layout.simple_list_item_1, null);
				}
				((TextView)v.findViewById(android.R.id.text1)).setText(getItem(position));
				return v;
			}
				    
		};
		
		l.setAdapter(adapter);
		// This prevents users from clicking on an error message like a search result
		l.setOnItemClickListener(null);
	}
	
}
