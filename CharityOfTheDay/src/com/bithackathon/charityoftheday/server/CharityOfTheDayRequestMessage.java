package com.bithackathon.charityoftheday.server;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Asks the server for info on the charity of the day 
 */
public class CharityOfTheDayRequestMessage extends RequestMessage {

	// This is a pretty simple message, just a messageId, so no need for any user interaction.
	@Override
	public JSONObject encode() throws JSONException {
	    JSONObject requestMessage = new JSONObject();
		requestMessage.put("messageId", 1);
		return requestMessage;
	}
}
