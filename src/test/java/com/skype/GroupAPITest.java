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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore 
public class GroupAPITest {

	@Test
    public void testGetGroup() throws Exception {
        TestUtils.showMessageDialog("Please create 'Test' group and add " + TestData.getFriendId() + " to it before closing this dialog.");
        Group group = Skype.getContactList().getGroup("Test");
        Assert.assertNotNull(group);
        Assert.assertTrue(group.hasFriend(TestData.getFriend()));
    }

	@Test
    public void testAddAndRemoveGroup() throws Exception {
        String addedGroupName = "GroupAPITest";
        Group added = Skype.getContactList().addGroup(addedGroupName);
        Assert.assertNotNull(Skype.getContactList().getGroup(addedGroupName));
        Skype.getContactList().removeGroup(added);
        Assert.assertNull(Skype.getContactList().getGroup(addedGroupName));
    }

	@Test
    public void testAddAndRemoveFriend() throws Exception {
        Group addedGroup = Skype.getContactList().addGroup("GroupAPITest");
        Friend addedFriend = TestData.getFriend();
        addedGroup.addFriend(addedFriend);
        Assert.assertTrue(addedGroup.hasFriend(addedFriend));
        addedGroup.removeFriend(addedFriend);
        Assert.assertFalse(addedGroup.hasFriend(addedFriend));
        Skype.getContactList().removeGroup(addedGroup);
    }
}
