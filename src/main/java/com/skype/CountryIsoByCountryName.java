package com.skype;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class CountryIsoByCountryName {
	private static Map<String,String> countryIsoByCountryName = new LinkedHashMap<String, String>();
	static {
		String[] isoCountries = Locale.getISOCountries();
    	for (String countryIso : isoCountries) {
			String displayCountry = new Locale("", countryIso).getDisplayCountry(new Locale("en_US"));
			countryIsoByCountryName.put(displayCountry.toLowerCase(), countryIso.toLowerCase());
		}
	}
	
	public static String getIsoForCountry(String country) {
		return countryIsoByCountryName.get(country.toLowerCase());
	}
	
}
