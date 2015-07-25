package com.bithackathon.charityoftheday.server;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchRequestMessage extends RequestMessage {
	private String searchQuery;
	
	public SearchRequestMessage() {
		searchQuery = "";
	}
	
	public SearchRequestMessage(String query) {
		searchQuery = query;
	}
	
	@Override
	public JSONObject encode() throws JSONException {
		// message id
		// data
		// |-query
	    JSONObject requestMessage = new JSONObject();
		requestMessage.put("messageId", 2);
		JSONObject data = new JSONObject();
		data.put("query", searchQuery);
		requestMessage.put("data", searchQuery);
		requestMessage.put("data", data);
		return requestMessage;
	}


	public String getSearchQuery() {
		return searchQuery;
	}


	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

}
