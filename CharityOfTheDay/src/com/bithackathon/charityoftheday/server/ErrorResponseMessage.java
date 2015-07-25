package com.bithackathon.charityoftheday.server;

import org.json.JSONException;
import org.json.JSONObject;

public class ErrorResponseMessage extends ResponseMessage {
	private String m_ErrorMessage;

	public String getErrorMessage() {
		return m_ErrorMessage;
	}

	public void setErrorMessage(String m_errorMessage) {
		this.m_ErrorMessage = m_errorMessage;
	}
	
	public ErrorResponseMessage() {
		m_ErrorMessage = "";
	}

	@Override
	public void populateFromJSON(JSONObject json) throws InvalidResponseException, JSONException {
		// Dig into the payload
		verifyPropertyExists(json, "data");
		JSONObject data = json.getJSONObject("data");
		verifyPropertyExists(data, "errorMessage");
		setErrorMessage(data.getString("errorMessage"));
	}

	@Override
	public int getResponseId() {
		return 0;
	}
}
