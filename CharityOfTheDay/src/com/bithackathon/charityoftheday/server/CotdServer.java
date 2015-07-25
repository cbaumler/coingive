package com.bithackathon.charityoftheday.server;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents the charity information server
 * Send server requests through this object. Use the request() function 
 */
public class CotdServer {
	/**
	 * Location of the server entry point
	 */
	static final String url = "http://larcada.com/cotd/cotd.php";
	
	/**
	 * Path to charity logo images
	 * @example String fullImageUrl = CotdServer::imagePath + "charity_image.jpg";
	 */
	public static final String imagePath = "http://larcada.com/cotd/images/";
	
	private HttpClient httpclient;
	
	public CotdServer() {
		HttpParams myParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(myParams, 10000);
	    HttpConnectionParams.setSoTimeout(myParams, 10000);
	    httpclient = new DefaultHttpClient(myParams);
	}
	
	/**
	 * Sends a RequestMessage to the server
	 * Returns the server response as a ResponseMessage
	 * If an error occurs, the error will be trapped and returned as an ErrorResponseMessage
	 * @return A ResponseMessage representing the server response or error condition
	 */
	public ResponseMessage request(RequestMessage r) {
		ResponseMessage responseMessage;
		
	    try
	    {
			HttpPost httppost = new HttpPost(url.toString());
            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-type", "application/json");
	
	        StringEntity se = new StringEntity(r.encode().toString()); 
	        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	        httppost.setEntity(se); 
	
	        HttpResponse response = httpclient.execute(httppost);
	        String temp = EntityUtils.toString(response.getEntity());
	        
	        System.out.println("CotdServer: Response received: " + temp);
			
			JSONObject rawResponse = new JSONObject(temp);
			
			responseMessage = getResponseMessageFromJSON(rawResponse);

	    } catch (Exception ex) {
			responseMessage = new ErrorResponseMessage();
			((ErrorResponseMessage)responseMessage).setErrorMessage(ex.getLocalizedMessage());	    	
	    }
	    
		return responseMessage;
	}
	
	/**
	 * Takes a JSON object and attempts to turn it into a ResponseMessage.
	 * If the JSON doesn't match any known ResponseMessage, throw an error. 
	 * @return The ResponseMessage representing the server response.
	 */
	private ResponseMessage getResponseMessageFromJSON(JSONObject json) throws InvalidResponseException, JSONException {
		ResponseMessage responseMessage;
		if (!json.has("responseId")) {
			throw new InvalidResponseException("property missing: responseId");
		}
		
		switch(json.getInt("responseId")) {
		case 0:	// Error
			responseMessage = new ErrorResponseMessage();
			break;
		case 1:	// Charity of the Day
			responseMessage = new CharityResponseMessage();
			break;
		case 2:	// Search response
			responseMessage = new SearchResponseMessage();
			break;
		default:
			Integer i = Integer.valueOf(json.getInt("responseId"));
			throw new InvalidResponseException("Unknown message id: " + i.toString());
		}
		responseMessage.populateFromJSON(json);

		return responseMessage;
		
	}
	

}
