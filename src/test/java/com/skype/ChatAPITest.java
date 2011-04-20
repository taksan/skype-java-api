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

// non automatic test
@Ignore
public class ChatAPITest {
    public void testChat() throws Exception {
        Friend friend = TestData.getFriend();
        Chat chat = friend.chat();

        long time = chat.getTime().getTime();
        Assert.assertTrue(0 <= time && time <= System.currentTimeMillis());
        Assert.assertNull(chat.getAdder());
        Assert.assertTrue(chat.getStatus().equals(Chat.Status.DIALOG));
        Assert.assertTrue(0 < chat.getWindowTitle().length());
        Assert.assertEquals(0, chat.getAllPosters().length);
        Assert.assertEquals(2, chat.getAllMembers().length);

        chat.send("Test Message");
        TestUtils.showCheckDialog(TestData.getFriendId() + " has received \"Test Message\"");
        chat.setTopic("New Topic");
        TestUtils.showCheckDialog("Topic was changed to \"New Topic\"");
        Friend friend2 = TestData.getFriend2();
        chat.addUser(friend2);
        Assert.assertEquals(1, chat.getAllPosters().length);
        Assert.assertEquals(3, chat.getAllMembers().length);
        TestUtils.showCheckDialog(friend2.getId() + " was added to this chat");
        chat.leave();
        Assert.assertEquals(2, chat.getAllMembers().length);
        TestUtils.showCheckDialog("You have left from this chat");
        Assert.assertTrue(0 < chat.getAllChatMessages().length);
        Assert.assertTrue(0 < chat.getRecentChatMessages().length);
    }
}
