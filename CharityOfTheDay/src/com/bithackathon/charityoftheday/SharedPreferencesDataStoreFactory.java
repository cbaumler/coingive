package com.bithackathon.charityoftheday;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.DataStore;

public class SharedPreferencesDataStoreFactory extends AbstractDataStoreFactory {

	private static final String ACCESS_TOKEN = "_access_token";
	private static final String EXPIRES_IN = "_expires_in";
	private static final String REFRESH_TOKEN = "_refresh_token";
	private static final String SCOPE = "_scope";
	
	private SharedPreferences mPrefs;
	
	public SharedPreferencesDataStoreFactory(SharedPreferences sharedPreferences) {
		mPrefs = sharedPreferences;
	}

	@Override
	protected <V extends Serializable> DataStore<V> createDataStore(String id) throws IOException {
		return new SharedPreferencesDataStore<V>(this, id);
	}
	
	public SharedPreferences GetSharedPreferences() {
		return mPrefs;
	}
	
	
	static class SharedPreferencesDataStore<V extends Serializable> extends AbstractDataStore<V> {
		
		private SharedPreferences mPrefs;
		
		SharedPreferencesDataStore(SharedPreferencesDataStoreFactory dataStore, String id) {
			super(dataStore, id);
			mPrefs = dataStore.GetSharedPreferences();
		}

		@Override
		public DataStore<V> clear() throws IOException {
			Editor editor = mPrefs.edit();
			editor.clear();
			editor.commit();
			return this;
		}

		@Override
		public DataStore<V> delete(String userId) throws IOException {
			Editor editor = mPrefs.edit();
			editor.remove(userId + ACCESS_TOKEN);
			editor.remove(userId + EXPIRES_IN);
			editor.remove(userId + REFRESH_TOKEN);
			editor.remove(userId + SCOPE);
			editor.commit();
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public V get(String userId) throws IOException {
			StoredCredential storedCredential = new StoredCredential();
			
			if (mPrefs.contains(userId + ACCESS_TOKEN)) {
				storedCredential.setAccessToken(mPrefs.getString(userId + ACCESS_TOKEN, null));
				
				if (mPrefs.contains(userId + EXPIRES_IN)) {
					storedCredential.setExpirationTimeMilliseconds(mPrefs.getLong(userId + EXPIRES_IN, 0));
				}
				
				storedCredential.setRefreshToken(mPrefs.getString(userId + REFRESH_TOKEN, null));
				
				return (V)storedCredential;
			}
			else {
				return null;
			}
		}

		@Override
		public Set<String> keySet() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataStore<V> set(String userId, V value) throws IOException {
			StoredCredential storedCredential = (StoredCredential)value;
			Editor editor = mPrefs.edit();
			
			editor.putString(userId + ACCESS_TOKEN, storedCredential.getAccessToken());
			
			if (storedCredential.getExpirationTimeMilliseconds() != null) {
				editor.putLong(userId + EXPIRES_IN, storedCredential.getExpirationTimeMilliseconds());	
			}
			
			if (storedCredential.getRefreshToken() != null) {
				editor.putString(userId + REFRESH_TOKEN, storedCredential.getRefreshToken());
			}
			editor.commit();
			return this;
		}

		@Override
		public Collection<V> values() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
	    public SharedPreferencesDataStoreFactory getDataStoreFactory() {
	      return (SharedPreferencesDataStoreFactory) super.getDataStoreFactory();
	    }
	}

}
