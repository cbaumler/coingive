package com.bithackathon.charityoftheday.server;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a message from the server 
 */
public abstract class ResponseMessage {
	/**
	 * This method must return the numeric id of this message type.
	 * @return int
	 */
	public abstract int getResponseId();
	
	/**
	 * This method must examine the provided JSON response and verify that it matches the definition of
	 * this ResponseMessage. If so, populate the fields of the ResponseMessage from the JSON data. If not,
	 * pitch a fit (throw an error).
	 * This method should assume that the responseId property is already available and matches the responseId
	 * of this message.
	 * @param json
	 */
	public abstract void populateFromJSON(JSONObject json) throws InvalidResponseException, JSONException;
	
	/**
	 * Verifies that the passed JSONObject has a property of the specified name
	 * If not, throws an InvalidResponseException error
	 * @param json
	 * @throws InvalidResponseException
	 * @throws JSONException
	 */
	protected void verifyPropertyExists(JSONObject json, String name) throws InvalidResponseException, JSONException {
		if (!json.has(name)) { throw new InvalidResponseException("property missing: " + name); }
	}
}
