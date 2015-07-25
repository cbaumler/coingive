package com.bithackathon.charityoftheday.server;

/**
 * Should be thrown when difficulty is encountered trying to parse a ResponseMessage from the server.
 * @author Hawkins
 *
 */
public class InvalidResponseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidResponseException(String exceptionText) {
		super(exceptionText);
	}
}
