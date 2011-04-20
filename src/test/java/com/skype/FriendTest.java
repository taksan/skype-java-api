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
 * Contributors: Koji Hisano - initial API and implementation
 * Gabriel Takeuchi - Ignored non working tests, fixed some, removed warnings
 ******************************************************************************/
package com.skype;

import java.text.SimpleDateFormat;

import com.skype.connector.test.TestCaseByCSVFile;

public final class FriendTest extends TestCaseByCSVFile {
    @Override
    protected void setUp() throws Exception {
//        setRecordingMode(true);
    }
    
    public void testGetAllFriends() throws Exception {
        if (isRecordingMode()) {
            System.out.println(Skype.getContactList().getAllFriends().length);
        } else {
            assertTrue(0 < Skype.getContactList().getAllFriends().length);
        }
    }
    
    public void testFriendProperties() throws Exception {
        if (isRecordingMode()) {
            Friend friend = Skype.getContactList().getFriend("skype_api_for_java");
            System.out.println(friend.getId());
            System.out.println(friend.getFullName());
            System.out.println(new SimpleDateFormat("yyyy/MM/dd").format(friend.getBirthDay()));
            System.out.println(friend.getSex());
            System.out.println(friend.getLanguage());
            System.out.println(friend.getLanguageByISOCode());
            System.out.println(friend.getCountry());
            System.out.println(friend.getCountryByISOCode());
            System.out.println(friend.getProvince());
            System.out.println(friend.getCity());
            System.out.println(friend.getHomePhoneNumber());
            System.out.println(friend.getOfficePhoneNumber());
            System.out.println(friend.getMobilePhoneNumber());
            System.out.println(friend.getHomePageAddress());
            System.out.println(friend.getIntroduction());
            System.out.println(friend.getMoodMessage());
            System.out.println(friend.isVideoCapable());
            System.out.println(friend.getBuddyStatus());
            System.out.println(friend.isAuthorized());
            System.out.println(friend.isBlocked());
            System.out.println(friend.getLastOnlineTime());
            System.out.println(friend.canLeaveVoiceMail());
            System.out.println(friend.getSpeedDial());
            System.out.println(friend.getTimeZone());
        } else {
            Friend friend = Skype.getContactList().getFriend("skype_api_for_java");
            assertEquals("skype_api_for_java", friend.getId());
            assertEquals("Skype API for Java", friend.getFullName());
            assertEquals("1980/12/05", new SimpleDateFormat("yyyy/MM/dd").format(friend.getBirthDay()));
            assertEquals(User.Sex.MALE, friend.getSex());
            assertEquals("Japanese", friend.getLanguage());
            assertEquals("ja", friend.getLanguageByISOCode());
            assertEquals("Japan", friend.getCountry());
            assertEquals("jp", friend.getCountryByISOCode());
            assertEquals("Tokyo", friend.getProvince());
            assertEquals("Chofu", friend.getCity());
            assertEquals("+813-0000-0000", friend.getHomePhoneNumber());
            assertEquals("+813-0000-0000", friend.getOfficePhoneNumber());
            assertEquals("+890-0000-0000", friend.getMobilePhoneNumber());
            assertEquals("http://skype.sourceforge.jp/", friend.getHomePageAddress());
            assertEquals("Please, enjoy 'Skype API for Java'.", friend.getIntroduction());
            assertEquals("", friend.getMoodMessage());
            assertTrue(friend.isVideoCapable());
            assertEquals(User.BuddyStatus.ADDED, friend.getBuddyStatus());
            assertTrue(friend.isAuthorized());
            assertFalse(friend.isBlocked());
            assertEquals("2007/03/22", new SimpleDateFormat("yyyy/MM/dd").format(friend.getLastOnlineTime()));
            assertTrue(friend.canLeaveVoiceMail());
            assertEquals("", friend.getSpeedDial());
            assertEquals(86400, friend.getTimeZone());
        }
    }
}
