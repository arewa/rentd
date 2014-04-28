package com.rentd.datamodel;

import java.util.Arrays;

public class Offer {
	public String uuid;
	public String title;
	public String description;
	public String price;
	public String city;
	public String url;
	public String type;
	public String[] phones;
	
	@Override
	public String toString() {
		return "Offer [uuid=" + uuid 
				+ ", title=" + title 
				+ ", price=" + price  
				+ ", city=" + city 
				+ ", url=" + url 
				+ ", type=" + type 
				+ ", phones=" + Arrays.toString(phones) + "]";
	}
}
