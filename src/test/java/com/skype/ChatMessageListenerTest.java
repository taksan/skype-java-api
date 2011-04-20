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
public class ChatMessageListenerTest {
	@Test
    public void testBasic() throws Exception {
        final boolean[] sent = new boolean[1];
        Skype.addChatMessageListener(new ChatMessageAdapter() {
            @Override
            public void chatMessageSent(ChatMessage sentChatMessage) throws SkypeException {
                sent[0] = true;
                Skype.removeChatMessageListener(this);
            }
        });
        TestUtils.showMessageDialog("Please, send a chat message to " + TestData.getFriendId() + ".");
        Assert.assertTrue(sent[0]);

        final boolean[] received = new boolean[1];
        Skype.addChatMessageListener(new ChatMessageAdapter() {
            @Override
            public void chatMessageReceived(ChatMessage receivedChatMessage) throws SkypeException {
                received[0] = true;
                Skype.removeChatMessageListener(this);
            }
        });
        TestUtils.showMessageDialog("Please, receive a chat message from " + TestData.getFriendId() + ".");
        Assert.assertTrue(received[0]);
    }
}
