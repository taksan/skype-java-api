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

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DialogAPITest {
	@Test
    public void testShowMainWindow() throws Exception {
        SkypeClient.showSkypeWindow();
        TestUtils.showCheckDialog("Skype main window is showed on the top?");
        SkypeClient.hideSkypeWindow();
        TestUtils.showCheckDialog("Skype main window is minimized?");
    }

	@Test
    public void testShowAddFriendWindow() throws Exception {
        SkypeClient.showAddFriendWindow();
        TestUtils.showCheckDialog("'Add a Contact' window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
        SkypeClient.showAddFriendWindow(TestData.getFriendId());
        TestUtils.showCheckDialog("'Add a Contact' window' is showed with " + TestData.getFriendId() + "?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

	@Test
    public void testShowChatWindow() throws Exception {
        SkypeClient.showChatWindow(TestData.getFriendId());
        TestUtils.showCheckDialog("Chat window with " + TestData.getFriendId() + " is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
        SkypeClient.showChatWindow(TestData.getFriendId(), "Hello, World!");
        TestUtils.showCheckDialog("Chat window with " + TestData.getFriendId() + " which have a message 'Hello World!' is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

	@Test
    public void testShowFileTransferWindow() throws Exception {
        SkypeClient.showFileTransferWindow(TestData.getFriendId());
        TestUtils.showCheckDialog("'Send file to " + TestData.getFriendId() + "' window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
        SkypeClient.showFileTransferWindow(TestData.getFriendId(), new File("C:\\"));
        TestUtils.showCheckDialog("'Send file to " + TestData.getFriendId() + "' window with selecting 'C:\\' is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

	@Test
    public void testShowProfileWindow() throws Exception {
        SkypeClient.showProfileWindow();
        TestUtils.showCheckDialog("Profile window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

	@Test
    public void testShowUserInformationWindow() throws Exception {
        SkypeClient.showUserInformationWindow(TestData.getFriendId());
        TestUtils.showCheckDialog(TestData.getFriendId() + "'s profile window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

	@Test
    public void testShowConferenceWindow() throws Exception {
        SkypeClient.showConferenceWindow();
        TestUtils.showCheckDialog("'Start a Skype Conference Call' window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

	@Test
    public void testShowSearchWindow() throws Exception {
        SkypeClient.showSearchWindow();
        TestUtils.showCheckDialog("'Search for Skype Users' window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

	@Test
    public void testShowOptionsWindow() throws Exception {
        SkypeClient.showOptionsWindow(SkypeClient.OptionsPage.ADVANCED);
        TestUtils.showCheckDialog("Options window with selecting 'Advanced' page is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }
}
