package com.bithackathon.charityoftheday;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.bithackathon.charityoftheday.transactiondb.Transaction;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;

/**
 * (Chris, if I am mistaken, please correct this. I am trying to add comments so I figure out what's going on)
 * This class represents the interface between the application and Coinbase.
 * All Coinbase actions (authentication, payment, etc) should be done using a CoinbaseManager
 * To use, just instantiate a CoinbaseManager wherever you are and start uh, coinbasing.
 */
public class CoinbaseManager {
	
	// Global instance of the HTTP transport 
	private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
	
	// Global instance of the JSON factory
	private static final JacksonFactory JSON_FACTORY = new JacksonFactory();
	
	private final SharedPreferencesDataStoreFactory mDataStoreFactory;
	
	private final DataStore<StoredCredential> mDataStore;
	
	private AuthorizationCodeFlow mFlow;
	
	private String mRedirectUrl;
	
	
	// Constructor
	// TODO: Figure out how we should handle exceptions in constructor
	public CoinbaseManager(Context context) throws IOException {	
		// Initialize the data store
		mDataStoreFactory = new SharedPreferencesDataStoreFactory(context.getSharedPreferences("coingive_prefs", Context.MODE_PRIVATE));
		mDataStore = mDataStoreFactory.getDataStore("coingive");
		
		
		// Initialize the Authorization Code Flow
		mFlow = new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(), 
				HTTP_TRANSPORT, 
				JSON_FACTORY, 
    			new GenericUrl("https://coinbase.com/oauth/token"), 
    			new BasicAuthentication("d23206099d2ddb4e93e31bd45fd52fc5008e59707fd463529aaf6a14657818e4", "a37f540b624a04bc10bc6a8350c38aa26d40b5ce313867d04eba1e11045a8ee8"), 
    			"d23206099d2ddb4e93e31bd45fd52fc5008e59707fd463529aaf6a14657818e4", 
    			"https://coinbase.com/oauth/authorize")
				//.setCredentialDataStore(dataStore)
				.setCredentialDataStore(mDataStore)
    			.build();
		
		mRedirectUrl = context.getString(R.string.redirect_url);
		
	}
	
	/**
	 * Returns true if the user has already authenticated their Coinbase account. False otherwise.
	 * @return Boolean
	 * @throws IOException 
	 */
	public boolean isAuthenticated() throws IOException {
		return (!(getCredential() == null));
	}
	
	// Attempt to load the user's credential from the credential store
	public Credential getCredential() throws IOException {
		return mFlow.loadCredential("user1");
	}
	
	/**
	 * Clear the stored credentials.
	 * @throws IOException 
	 */
	public void clearCredentials() throws IOException {
		mFlow.getCredentialDataStore().clear();
	}
	
	// Direct the user to authenticate the application
	public String getAuthenticationUri(String debitLimit) {
		Collection<String> permissions = Arrays.asList("balance", "send");
		
		String url = mFlow.newAuthorizationUrl().setState("coingive")
				.setScopes(permissions)
				.setRedirectUri(mRedirectUrl)
				.set("meta[send_limit_amount]", debitLimit)
				.set("meta[send_limit_currency]", "USD")
				.set("meta[send_limit_period]", "daily")
				.build();
		
		return url;
	}
	
	/** 
	 * Request an access token from Coinbase using the code returned after authentication.
	 * 
	 * @param code
	 * @return
	 * @throws IOException
	 * @throws TokenResponseException
	 */
	public Credential requestAccessToken(String code) throws IOException, TokenResponseException {
		TokenResponse response = mFlow.newTokenRequest(code).setRedirectUri(mRedirectUrl).execute();
		return mFlow.createAndStoreCredential(response,"user1");
	}
	
	/** 
	 * Perform an HTTP GET for the provided URL.
	 * 
	 * @param apiUrl
	 * @return
	 * @throws IOException
	 */
	public String executeApiGet(String apiUrl) throws IOException {
		String response = null;	
		try {
			response = HTTP_TRANSPORT.createRequestFactory(getCredential()).buildGetRequest(new GenericUrl(apiUrl)).execute().parseAsString();
		}
		catch (IOException e) {
			if (e.getMessage() != null && e.getMessage().contains("401 Unauthorized")) {
				// Stored credentials are invalid
				clearCredentials();
			}
			throw new IOException(e.getMessage());
		}
		return response;
	}
	
	/**
	 * Perform an HTTP POST for the provided URL and parameters.
	 * 
	 * @param apiUrl
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public String executeApiPost(String apiUrl, JSONObject params) throws IOException {
		String response = null;
		try {
			String jsonString = params.toString();
			HttpRequest request = HTTP_TRANSPORT.createRequestFactory(getCredential()).buildPostRequest(new GenericUrl(apiUrl), ByteArrayContent.fromString("application/json", jsonString));
			request.getHeaders().setContentType("application/json");
			response = request.execute().parseAsString();
		}
		catch (IOException e) {
			if (e.getMessage() != null && e.getMessage().contains("401 Unauthorized")) {
				// Stored credentials are invalid
				clearCredentials();
			}
			throw new IOException(e.getMessage());
		}
		return response;
	}
	
	/**
	 * Query Coinbase for details associated with a specific transaction ID. A JSON formatted string containing
	 * the data is returned.
	 * 
	 * @param transactionId
	 * @return
	 * @throws IOException
	 */
	public String getTransactionDetails(String transactionId) throws IOException {
		return executeApiGet("https://coinbase.com/api/v1/transactions/" + transactionId);
	}
	
	/**
	 * Query Coinbase for the daily debit limit. Returns -1 if the limit can't be retrieved.
	 * 
	 * Sample JSON data
	 * {"auth_type":"oauth","meta":{"send_limit_amount":"0.01","send_limit_currency":"USD","send_limit_period":"daily"}}
	 * 
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public double getDailyDebitLimit() throws IOException, JSONException {
		double limit = -1;
		String rawResponse = executeApiGet("https://coinbase.com/api/v1/authorization");
		JSONObject response = new JSONObject(rawResponse);
		if (response.has("meta") && response.getJSONObject("meta").has("send_limit_amount")) {
			limit = response.getJSONObject("meta").getDouble("send_limit_amount");
		}
		
		return limit;
	}
	
	/**
	 * Query Coinbase for the user's account balance in Bitcoin. Returns the balance.
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public double getAccountBalanceInBtc() throws IOException, JSONException {
		double balance = 0;
		
		String rawResponse = executeApiGet("https://coinbase.com/api/v1/account/balance");
		JSONObject response = new JSONObject(rawResponse);
		if (response.has("amount")) {
			balance = response.getDouble("amount");
		}
		
		return balance;
	}
	
	/**
	 * Query Coinbase for the user's account balance in US dollars. Returns the balance.
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public double getAccountBalanceInUsd() throws IOException, JSONException {		
		double balanceBtc = getAccountBalanceInBtc();	
		double exchangeRate = getExchangeRate();
		double balanceUsd = balanceBtc * exchangeRate;
		return balanceUsd;
	}
	
	public double getExchangeRate() throws IOException, JSONException {
		double exchangeRate = 0;
		String rawResponse = executeApiGet("https://coinbase.com/api/v1/currencies/exchange_rates");
		JSONObject response = new JSONObject(rawResponse);
		if (response.has("btc_to_usd")) {
			exchangeRate = response.getDouble("btc_to_usd");
		}
		
		return exchangeRate;
	}
	
	/**
	 * SEND THAT MONEY
	 * If it works, returns a Transaction object that summarizes the bitcoin transaction.
	 * If it doesn't, better get ready to catch some exceptions. And by exceptions, I mean all of them.
	 * 
	 * @param apiUrl
	 * @param amount
	 * @param address
	 * @return Transaction
	 * @throws IOException
	 * @throws JSONException
	 * @throws TransactionException
	 */
	public Transaction sendMoney(String apiUrl, String amount, Charity charity) throws IOException, JSONException, TransactionException {
		JSONObject params = new JSONObject();
		params.put("to", charity.getBitcoinAddress());
		params.put("amount", amount); 
		params.put("notes", "Donation to " + charity.getName() + ".");
	
		JSONObject requestMoneyObject = new JSONObject();
		requestMoneyObject.put("transaction", params);
		
		// Execute the HTTP Post to send money
		String rawResponse = executeApiPost(apiUrl, requestMoneyObject);
		
		JSONObject response = new JSONObject(rawResponse);
		System.out.println(rawResponse);

		// Check the response for a success notification
		String errorMessage = null;
		if (response.has("success")) {
			boolean success = response.getBoolean("success");
			if (!success) {
				// Check for an error from Coinbase
				if (response.has("errors")) {
					errorMessage = response.getJSONArray("errors").getString(0);
				}
				else {
					throw new TransactionException("Malformed JSON response?");
				}					
			}
		} 
		else {
			throw new TransactionException("Malformed JSON response?");
		}
		
		// If we got here, a valid response (Pass/fail) was received from Coinbase.
		Transaction t = new Transaction();
		t.setErrorMessage(errorMessage);
		if (t.getErrorMessage() == null) {
			// If we got here, the send succeeded.
			String transactionId = response.getJSONObject("transaction").getString("id");
			t.setCharityId(charity.getId());
			t.setCharityName(charity.getName());
			t.setTransactionId(transactionId);
			String rawActualAmount = response.getJSONObject("transaction").getJSONObject("amount").getString("amount");
			float actualAmount = Float.valueOf(rawActualAmount);
			t.setAmount(actualAmount);
		}
		else {
			t.setErrorCode(CoingiveErrorCode.COINBASE_API_FAIL);
		}
				
		return t;
	}
}
