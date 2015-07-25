package com.bithackathon.charityoftheday.transactiondb;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.bithackathon.charityoftheday.Charity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * At least partially stolen from this tutorial:
 * http://www.vogella.com/tutorials/AndroidSQLite/article.html
 * 
 * This class allows us to read and write to the transactions database
 */
public class TransactionsDataSource {
	private SQLiteDatabase database;
	private TransactionDbHelper dbHelper;
	private String[] allColumns = { TransactionDbHelper.COLUMN_ID,
			TransactionDbHelper.COLUMN_TRANSACTION_ID,
			TransactionDbHelper.COLUMN_CHARITY_ID,
			TransactionDbHelper.COLUMN_DATE,
			TransactionDbHelper.COLUMN_CHARITY_NAME,
			TransactionDbHelper.COLUMN_AMOUNT};

	public TransactionsDataSource(Context context) {
		dbHelper = new TransactionDbHelper(context);
	}

	public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	}

	public void close() {
	    dbHelper.close();
	}
	
	public Transaction createTransaction(Charity charity, String transactionId, float amount) {
		ContentValues values = new ContentValues();
		values.put(TransactionDbHelper.COLUMN_CHARITY_ID, charity.getId());
		values.put(TransactionDbHelper.COLUMN_TRANSACTION_ID, transactionId);
		values.put(TransactionDbHelper.COLUMN_CHARITY_NAME, charity.getName());
		values.put(TransactionDbHelper.COLUMN_AMOUNT, amount);
		long insertId = database.insert(TransactionDbHelper.TABLE_TRANSACTIONS, null,
		        values);
	    Cursor cursor = database.query(TransactionDbHelper.TABLE_TRANSACTIONS,
	            allColumns, TransactionDbHelper.COLUMN_ID + " = " + insertId, null,
	            null, null, null);
	    cursor.moveToFirst();
	    Transaction newTransaction = cursorToTransaction(cursor);
	    cursor.close();
	    return newTransaction;
	}

	/**
	 * Get ALL of the transactions!
	 * @return A list of all the transactions, ordered by when they were entered
	 */
	public List<Transaction> getAllTransactions() {
		List<Transaction> transactions = new ArrayList<Transaction>();

		// Last parameter is "orderBy". We order by ID, which corresponds to using
		// the same order that the transactions were added to the database.
	    Cursor cursor = database.query(TransactionDbHelper.TABLE_TRANSACTIONS,
	        allColumns, null, null, null, null, TransactionDbHelper.COLUMN_ID + " DESC");

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	Transaction transaction = cursorToTransaction(cursor);
	    	transactions.add(transaction);
	    	cursor.moveToNext();
	    }
	    // make sure to close the cursor
	    cursor.close();
	    return transactions;
	  }
	
	/**
	 * Transforms a database record into a Transaction object
	 * @param cursor DB cursor pointing to a record in the Transactions database
	 * @return A Transaction object corresponding to the selected DB record
	 */
	private Transaction cursorToTransaction(Cursor cursor) {
		Transaction transaction = new Transaction();

		transaction.setTransactionId(cursor.getString(1));
		transaction.setCharityId(cursor.getInt(2));
		transaction.setDate(Timestamp.valueOf(cursor.getString(3)));
		transaction.setCharityName(cursor.getString(4));
		transaction.setAmount(cursor.getFloat(5));
		return transaction;
	}	
}
