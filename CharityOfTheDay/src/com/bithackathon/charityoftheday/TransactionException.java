package com.bithackathon.charityoftheday;

/**
 * Represents something that goes wrong during the transaction process.
 * Example: Coinbase reports a transaction failure
 */
public class TransactionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    public TransactionException(String message) {
        super(message);
    }	
}
