package com.skype;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CountryIsoByCountryNameTest {
	@Test
	public void onGetIsoForCountry_ShouldReturnCountryIso()
	{
		String actual = CountryIsoByCountryName.getIsoForCountry("Russia");
		assertEquals("ru", actual);
	}
}