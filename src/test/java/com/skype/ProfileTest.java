/*******************************************************************************
 * Copyright (c) 2006-2007 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006-2007 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006-2007 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * Skype4Java is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 *
 * If it is possible to cooperate with the publicity of Skype4Java, please add
 * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
 * in your web site or documents.
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 * Gabriel Takeuchi - Ignored non working tests, fixed some, removed warnings
 ******************************************************************************/
package com.skype;

import java.text.SimpleDateFormat;

import com.skype.connector.test.TestCaseByCSVFile;

public final class ProfileTest extends TestCaseByCSVFile {
    @Override
    protected void setUp() throws Exception {
//        setRecordingMode(true);
    }
    
    public void testBasic() throws Exception {
        Profile profile = Skype.getProfile();
        if (isRecordingMode()) {
            System.out.println(profile.getId());
            System.out.println(profile.getStatus());
            System.out.println(profile.canDoSkypeOut());
            System.out.println(profile.canDoSkypeIn());
            System.out.println(profile.canDoVoiceMail());

            System.out.println(profile.getCredit());
            System.out.println(profile.getCreditCurrencyUnit());
            System.out.println(profile.getFullName());
            System.out.println(new SimpleDateFormat("yyyy/MM/dd").format(profile.getBirthDay()));
            System.out.println(profile.getSex());
            System.out.println(profile.getLanguageByISOCode());
            System.out.println(profile.getCountry());
            System.out.println(profile.getCountryByISOCode());
            System.out.println(profile.getIPCountryByISOCode());
            System.out.println(profile.getProvince());
            System.out.println(profile.getCity());
            System.out.println(profile.getHomePhoneNumber());
            System.out.println(profile.getOfficePhoneNumber());
            System.out.println(profile.getMobilePhoneNumber());
            System.out.println(profile.getWebSiteAddress());
            System.out.println(profile.getIntroduction());
            System.out.println(profile.getMoodMessage());
            System.out.println(profile.getTimeZone());
        } else {
            assertEquals("skype_api_for_java", profile.getId());
            assertEquals(Profile.Status.ONLINE, profile.getStatus());
            assertTrue(profile.canDoSkypeOut());
            assertTrue(profile.canDoSkypeIn());
            assertTrue(profile.canDoVoiceMail());

            assertEquals(1000000, profile.getCredit());
            assertEquals("JPY", profile.getCreditCurrencyUnit());
            assertEquals("Skype API for Java", profile.getFullName());
            assertEquals("1980/12/05", new SimpleDateFormat("yyyy/MM/dd").format(profile.getBirthDay()));
            assertEquals(Profile.Sex.MALE, profile.getSex());
            assertEquals("ja", profile.getLanguageByISOCode());
            assertEquals("Japan", profile.getCountry());
            assertEquals("jp", profile.getCountryByISOCode());
            assertEquals("jp", profile.getIPCountryByISOCode());
            assertEquals("Tokyo", profile.getProvince());
            assertEquals("Chofu", profile.getCity());
            assertEquals("+8130-0000-0000", profile.getHomePhoneNumber());
            assertEquals("+8130-0000-0000", profile.getOfficePhoneNumber());
            assertEquals("+8190-0000-0000", profile.getMobilePhoneNumber());
            assertEquals("http://skype.sourceforge.jp/", profile.getWebSiteAddress());
            assertEquals("Skype API for Java", profile.getIntroduction());
            assertEquals("Please, enjoy 'Skype API for Java'.", profile.getMoodMessage());
            assertEquals(118800, profile.getTimeZone());
        }
    }
}
