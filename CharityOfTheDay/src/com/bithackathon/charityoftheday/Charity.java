package com.bithackathon.charityoftheday;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a single Charity record from the server charity database
 */
public class Charity implements Parcelable {
	private String m_Name;
	private String m_Tagline;
	private String m_Description;
	private String m_Url;
	private String m_BitcoinAddress;
	private String m_ImageUrl;
	private int m_Id;
	
	public Charity() {
		m_Id = 0;
		m_Name = "";
		m_Description = "";
		m_BitcoinAddress = "";
		m_ImageUrl = "";
		m_Tagline = "";
		m_Url = "";
	}
	public Charity(int id, String name, String tagline, String description, String url, String bitcoinAddress, String imageUrl) {
		m_Id = id;
		m_Name = name;
		m_Tagline = tagline;
		m_Description = description;
		m_Url = url;
		m_BitcoinAddress = bitcoinAddress;
		m_ImageUrl = imageUrl;
	}
	public Charity(Parcel in) {
		this.m_Name = in.readString();
		this.m_Description = in.readString();
		this.m_BitcoinAddress = in.readString();
		this.m_ImageUrl = in.readString();
		this.m_Tagline = in.readString();
		this.m_Url = in.readString();
		this.m_Id = in.readInt();
	}
	
	public String getName() {
		return m_Name;
	}
	public void setName(String m_Name) {
		this.m_Name = m_Name;
	}
	public String getDescription() {
		return m_Description;
	}
	public void setDescription(String m_Description) {
		this.m_Description = m_Description;
	}
	public String getBitcoinAddress() {
		return m_BitcoinAddress;
	}
	public void setBitcoinAddress(String m_BitcoinAddress) {
		this.m_BitcoinAddress = m_BitcoinAddress;
	}
	public String getImageUrl() {
		return m_ImageUrl;
	}
	public void setImageUrl(String m_ImageUrl) {
		this.m_ImageUrl = m_ImageUrl;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		arg0.writeString(m_Name);
		arg0.writeString(m_Description);
		arg0.writeString(m_BitcoinAddress);
		arg0.writeString(m_ImageUrl);
		arg0.writeString(m_Tagline);
		arg0.writeString(m_Url);
		arg0.writeInt(m_Id);
	}
	
    public String getTagline() {
		return m_Tagline;
	}
	public void setTagline(String m_Tagline) {
		this.m_Tagline = m_Tagline;
	}

	public String getUrl() {
		return m_Url;
	}
	public void setUrl(String m_Url) {
		this.m_Url = m_Url;
	}

	public int getId() {
		return m_Id;
	}
	public void setId(int m_Id) {
		this.m_Id = m_Id;
	}

	public static final Parcelable.Creator<Charity> CREATOR = new Parcelable.Creator<Charity>() {
        public Charity createFromParcel(Parcel in)
        {
            return new Charity(in);
        }
 
        public Charity[] newArray(int size)
        {
            return new Charity[size];
        }
    };
    
    /**
     * Charities will be displayed in this format on the search results page.
     */
    public String toString() {
    	return m_Name;
    }
}
