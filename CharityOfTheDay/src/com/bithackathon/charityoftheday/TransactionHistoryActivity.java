package com.bithackathon.charityoftheday;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TwoLineListItem;

import com.bithackathon.charityoftheday.transactiondb.Transaction;
import com.bithackathon.charityoftheday.transactiondb.TransactionsDataSource;

public class TransactionHistoryActivity extends BaseCotdActivity {
	
	private TransactionsDataSource datasource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transaction_history);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Populates the list of transactions
		// stolen from: http://www.vogella.com/tutorials/AndroidSQLite/article.html
		
		datasource = new TransactionsDataSource(this);		
		datasource.open();		
	    List<Transaction> values = datasource.getAllTransactions();
		
	    // This list adapter converts the transaction info from Transaction objects
	    // into a two-line list item. Line 1 = charity name, Line 2 = date and amount
	    ListView lv = (ListView)findViewById(R.id.TransactionList);
	    ArrayAdapter<Transaction> adapter = new ArrayAdapter<Transaction>(getApplicationContext(),
	        android.R.layout.simple_list_item_2, values) {
	        @Override
	        public View getView(int position, View convertView, ViewGroup parent){
	        	// And so, the final adventure of all the deprecated functions began...
	            TwoLineListItem row;            
	            if(convertView == null){
	            	LayoutInflater inflater = getLayoutInflater();
	                row = (TwoLineListItem)inflater.inflate(android.R.layout.simple_list_item_2, null);                    
	            }else{
	                row = (TwoLineListItem)convertView;
	            }
	            Transaction t = getItem(position);
		        row.getText1().setText(t.getCharityName());	            	

	            String tDate = new SimpleDateFormat("MMMMM d, yyyy").format(t.getDate());
	            row.getText2().setText(Float.toString(Math.abs(t.getAmount())) + " BTC          " + tDate);

	            return row;
	        }	    	
	    };
	    lv.setAdapter(adapter);
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
		getMenuInflater().inflate(R.menu.transaction_history, menu);
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
	protected void onResume() {
		datasource.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
	    datasource.close();
	    super.onPause();
	}    
}
