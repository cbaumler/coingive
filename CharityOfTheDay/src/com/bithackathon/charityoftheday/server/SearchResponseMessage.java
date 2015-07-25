package com.bithackathon.charityoftheday.server;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bithackathon.charityoftheday.Charity;

public class SearchResponseMessage extends ResponseMessage {
	ArrayList<Charity> charities;
	
	public SearchResponseMessage() {
		charities = new ArrayList<Charity>();
	}
	
	@Override
	public int getResponseId() {
		return 2;
	}

	@Override
	public void populateFromJSON(JSONObject json)
			throws InvalidResponseException, JSONException {
		
		// Make sure the data\results\ structure exists
		verifyPropertyExists(json, "data");
		JSONObject data = json.getJSONObject("data");
		verifyPropertyExists(data, "results");
		JSONArray results = data.getJSONArray("results");
		
		// Parse all results into charity objects
		for(int i=0; i<results.length(); i++) {
			JSONObject rawCharity = results.getJSONObject(i);
			Charity c = new Charity();
			c.setId((int)rawCharity.getLong("id"));
			c.setName(rawCharity.getString("name"));			
			charities.add(c);
		}		
	}
	
	public ArrayList<Charity> getCharities() {
		return charities;
	}

}
