package com.bithackathon.charityoftheday.server;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Request Message. Request messages are sent to the server.
 */
public abstract class RequestMessage {

	/**
	 * This function must encode the message as a JSONObject
	 * ready to be sent to the server
	 * @return
	 */
	public abstract JSONObject encode() throws JSONException;
}
