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

import java.util.Date;

import com.skype.Call;
import com.skype.ChatMessage;
import com.skype.Friend;

import junit.framework.TestCase;

public class HistoryAPITest extends TestCase {
    public void testGetAllMessages() throws Exception {
        TestUtils.showMessageDialog("Please, send a chat message 'Hello, World!' to " + TestData.getFriendId() + ".");
        Friend friend = TestData.getFriend();
        ChatMessage[] messages = friend.getAllChatMessages();
        assertTrue(0 < messages.length);
    }

    public void testGetAllCalls() throws Exception {
        TestUtils.showMessageDialog("Please, start a call to " + TestData.getFriendId() + "and finsh it in 10 seconds.");
        Friend friend = TestData.getFriend();
        Call[] calls = friend.getAllCalls();
        assertTrue(0 < calls.length);
        Call latest = calls[0];
        assertEquals(TestData.getFriendId(), latest.getPartnerId());
        assertEquals(TestData.getFriendDisplayName(), latest.getPartnerDisplayName());
        assertTrue(new Date().getTime() - 10000 <= latest.getStartTime().getTime());
        assertEquals(Call.Type.OUTGOING_P2P, latest.getType());
    }
}
