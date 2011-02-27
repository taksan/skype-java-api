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
 ******************************************************************************/
package com.skype;

import com.skype.Friend;
import com.skype.Group;
import com.skype.Skype;

import junit.framework.TestCase;

public class GroupAPITest extends TestCase {
    public void testGetGroup() throws Exception {
        TestUtils.showMessageDialog("Please create 'Test' group and add " + TestData.getFriendId() + " to it before closing this dialog.");
        Group group = Skype.getContactList().getGroup("Test");
        assertNotNull(group);
        assertTrue(group.hasFriend(TestData.getFriend()));
    }

    public void testAddAndRemoveGroup() throws Exception {
        String addedGroupName = "GroupAPITest";
        Group added = Skype.getContactList().addGroup(addedGroupName);
        assertNotNull(Skype.getContactList().getGroup(addedGroupName));
        Skype.getContactList().removeGroup(added);
        assertNull(Skype.getContactList().getGroup(addedGroupName));
    }

    public void testAddAndRemoveFriend() throws Exception {
        Group addedGroup = Skype.getContactList().addGroup("GroupAPITest");
        Friend addedFriend = TestData.getFriend();
        addedGroup.addFriend(addedFriend);
        assertTrue(addedGroup.hasFriend(addedFriend));
        addedGroup.removeFriend(addedFriend);
        assertFalse(addedGroup.hasFriend(addedFriend));
        Skype.getContactList().removeGroup(addedGroup);
    }
}
