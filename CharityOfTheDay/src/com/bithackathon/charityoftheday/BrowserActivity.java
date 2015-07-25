package com.bithackathon.charityoftheday;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.google.api.client.auth.oauth2.TokenResponseException;

public class BrowserActivity extends Activity {
	protected FrameLayout webViewPlaceholder;
	protected WebView webView;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browser);

		Intent intent = getIntent();
		if (intent.getAction() == Intent.ACTION_VIEW) {
			Uri url = intent.getData();
			if (url != null) {
				// Initialize the UI
				initUI(url);
			} else {
				finish();
			}
		} else {
			finish();
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	protected void initUI(Uri initialUri)
	{
		// Retrieve UI elements
		webViewPlaceholder = ((FrameLayout)findViewById(R.id.webViewPlaceholder));

		// Initialize the WebView if necessary
		if (webView == null)
		{
			// Create the webview
			webView = new WebView(this);
			webView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			webView.getSettings().setSupportZoom(true);
			webView.getSettings().setBuiltInZoomControls(true);
			webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
			webView.setScrollbarFadingEnabled(true);
			webView.getSettings().setLoadsImagesAutomatically(true);

			// Enable js; apparently, this is necessary for the Coinbase site to operate
			webView.getSettings().setJavaScriptEnabled(true);

			webView.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onProgressChanged(WebView view, int progress) {
					setProgress(progress * 100);
					if (progress == 100) {
						setProgressBarIndeterminateVisibility(false);
						setProgressBarVisibility(false);
					}
				}
			});

			// Load the URLs inside the WebView, not in the external web browser
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					if (url.startsWith(getString(R.string.redirect_url))) {
						if (url.indexOf("code=")!=-1) {
							view.setVisibility(View.INVISIBLE);
							new RequestTokenTask().execute(Uri.parse(url).getQueryParameter("code"));
						}
						else if (url.indexOf("error=")!=-1 && url.indexOf("error_description")!=-1){
							// Check the error that occurred.
							String error = Uri.parse(url).getQueryParameter("error");
							String errorDescription = Uri.parse(url).getQueryParameter("error_description");
							if (!error.contains("access_denied")) {
								// Send this error back to the settings page.
								Intent data = new Intent();
								data.putExtra("error_description", errorDescription);
								setResult(Activity.RESULT_OK, data);
								finish();
							}
							else {
								// The user denied the authorization request.
								finish();
							}
						}
						else {
							finish();
						}
					}
				}
			});
			
			// Load a page
			if (initialUri != null) {
				webView.loadUrl(initialUri.toString());
			}
		}

		// Attach the WebView to its placeholder
		webViewPlaceholder.addView(webView);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		if (webView != null)
		{
			// Remove the WebView from the old placeholder
			webViewPlaceholder.removeView(webView);
		}

		super.onConfigurationChanged(newConfig);

		// Load the layout resource for the new configuration
		setContentView(R.layout.activity_browser);

		// Reinitialize the UI
		initUI(null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		// Save the state of the WebView
		webView.saveState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);

		// Restore the state of the WebView
		webView.restoreState(savedInstanceState);
	}
	  
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.browser, menu);
		return true;
	}
	
	private class RequestTokenTask extends AsyncTask<String, Void, String> {
	
		@Override
		protected String doInBackground(String... args) {
			
			try {
            	new CoinbaseManager(getApplicationContext()).requestAccessToken(args[0]);
			} catch (TokenResponseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		 @Override
	       protected void onPostExecute(String arg) {
	    	   
			 BrowserActivity.this.finish();  
	      }
	}

}
