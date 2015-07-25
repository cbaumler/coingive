package com.bithackathon.charityoftheday.server;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Asks the server for info on a given charity, specified by charity id
 */
public class CharityRequestMessage extends RequestMessage {

	private int m_CharityId;
	
	public CharityRequestMessage(int charityId) {
		m_CharityId = charityId;
	}
	
	@Override
	public JSONObject encode() throws JSONException {
		JSONObject requestMessage = new JSONObject();
		requestMessage.put("messageId", 3);

		JSONObject data = new JSONObject();
		data.put("charityId", m_CharityId);
		requestMessage.put("data", data);
		
		return requestMessage;
	}

}
