package com.bithackathon.charityoftheday.transactiondb;

import java.util.Date;

import com.bithackathon.charityoftheday.CoingiveErrorCode;


/**
 * Represents a single Bitcoin transaction performed using the COTD app 
 */
public class Transaction {
	private String mTransactionId;
	private int mCharityId;
	private Date mDate;
	private String mCharityName;
	private float mAmount;
	private String mErrorMessage;
	private CoingiveErrorCode mErrorCode = CoingiveErrorCode.NO_ERROR;
	
	public String getTransactionId() {
		return mTransactionId;
	}
	public void setTransactionId(String transactionId) {
		this.mTransactionId = transactionId;
	}
	public int getCharityId() {
		return mCharityId;
	}
	public void setCharityId(int charityId) {
		this.mCharityId = charityId;
	}
	public Date getDate() {
		return mDate;
	}
	public void setDate(Date date) {
		this.mDate = date;
	}
	
	public Transaction() {
		mTransactionId = "";
		mCharityId = 0;
		mDate = new Date();
		mErrorMessage = null;
	}

	public Transaction(String transactionId, int charityId, Date transactionDate) {
		mDate = transactionDate;
		mCharityId = charityId;
		mTransactionId = transactionId;
		mErrorMessage = null;
	}

	// Will be used by the ArrayAdapter in the ListView in TransactionHistoryActivity
	// Whatever this function outputs is what will show up in the transaction list
	@Override
	public String toString() {
	    return mTransactionId;
	}
	public String getCharityName() {
		return mCharityName;
	}
	public void setCharityName(String charityName) {
		this.mCharityName = charityName;
	}
	public float getAmount() {
		return mAmount;
	}
	public void setAmount(float amount) {
		this.mAmount = amount;
	}
	public String getErrorMessage() {
		return mErrorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.mErrorMessage = errorMessage;
	}
	public CoingiveErrorCode getErrorCode() {
		return mErrorCode;
	}
	public void setErrorCode(CoingiveErrorCode errorCode) {
		this.mErrorCode = errorCode;
	}
	
}
