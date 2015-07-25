package com.bithackathon.charityoftheday.server;

import org.json.JSONException;
import org.json.JSONObject;

import com.bithackathon.charityoftheday.Charity;

public class CharityResponseMessage extends ResponseMessage {
	private Charity m_Charity;

	public CharityResponseMessage() {
		super();
		m_Charity = new Charity();
	}
	
	public Charity getCharity() {
		return m_Charity;
	}

	@Override
	public void populateFromJSON(JSONObject json) throws InvalidResponseException, JSONException {
		verifyPropertyExists(json, "data");
		JSONObject data = json.getJSONObject("data");
		verifyPropertyExists(data, "id");
		verifyPropertyExists(data, "name");
		verifyPropertyExists(data, "tagline");
		verifyPropertyExists(data, "description");
		verifyPropertyExists(data, "url");
		verifyPropertyExists(data, "bitcoinAddress");
		verifyPropertyExists(data, "imageurl");
		
		m_Charity.setId(data.getInt("id"));
		m_Charity.setName(data.getString("name"));
		m_Charity.setTagline(data.getString("tagline"));
		m_Charity.setDescription(data.getString("description"));
		m_Charity.setUrl(data.getString("url"));
		m_Charity.setBitcoinAddress(data.getString("bitcoinAddress"));
		m_Charity.setImageUrl(data.getString("imageurl"));
	}

	@Override
	public int getResponseId() {
		return 1;
	}
	
	
}
