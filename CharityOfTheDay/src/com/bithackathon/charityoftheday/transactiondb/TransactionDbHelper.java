package com.bithackathon.charityoftheday.transactiondb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Stolen from this tutorial:
 * http://www.vogella.com/tutorials/AndroidSQLite/article.html
 * This class is responsible for creating and/or upgrading the database
 * The member functions of this class will be called automatically if the app tries to access
 * the db but it does not exist.
 */
public class TransactionDbHelper extends SQLiteOpenHelper {

	public static final String TABLE_TRANSACTIONS = "transactions";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TRANSACTION_ID = "transactionId";
	public static final String COLUMN_CHARITY_ID = "charityId";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_CHARITY_NAME = "charityName";
	public static final String COLUMN_AMOUNT = "amount";
	private static final String DATABASE_NAME = "transactions.db";
	private static final int DATABASE_VERSION = 2;	// Important, see comments for onUpgrade below
	
	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_TRANSACTIONS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TRANSACTION_ID
			+ " varchar(50) not null, " + COLUMN_CHARITY_ID
			+ " integer not null, " + COLUMN_DATE
			+ " timestamp default current_timestamp, " + COLUMN_CHARITY_NAME
			+ " varchar(30) not null, " + COLUMN_AMOUNT
			+ " decimal(7,2) not null);";
	  
	public TransactionDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	  
	/**
	 * This function is called if:
	 * (a) the app tries to access the database
	 * and 
	 * (b) it's not there.
	 */
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	/**
	 * This function is called if:
	 * A new version of the app is installed that has a new database version.
	 * Database version is set by the DATABASE_VERSION property
	 * If we change the database in a later app version, we increment this property.
	 * That way, the app will know to upgrade the database.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (1 == oldVersion && 2 == newVersion) {
			// Upgrade from version 1->2
			// This is where we added the charityName and amount fields
	        db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS
	        		+ " ADD " + COLUMN_CHARITY_NAME + " VARCHAR( 30 ) NOT NULL DEFAULT ''");
   	        db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS
	        		+ " ADD " + COLUMN_AMOUNT + " DECIMAL( 7, 2 ) NOT NULL DEFAULT 0");
		} else {
			// Unknown version increment; just delete and reform the database
		    Log.w(TransactionDbHelper.class.getName(),
		            "Upgrading database from version " + oldVersion + " to "
		                + newVersion + ", which will destroy all old data");
		        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
		        onCreate(db);
		}
	}

}
