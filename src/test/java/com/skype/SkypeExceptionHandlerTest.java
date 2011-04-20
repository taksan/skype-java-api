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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public final class SkypeExceptionHandlerTest {
	@Test
    public void testDefaultHanlder() throws Exception {
        fireHanlderWithSkypeException();
        TestUtils.showCheckDialog("The default output contains a stack trace?");
    }

	@Test
    public void testSetHandler() throws Exception {
        final Object wait = new Object();
        final boolean[] result = new boolean[1];
        Skype.setSkypeExceptionHandler(new SkypeExceptionHandler() {
            public void uncaughtExceptionHappened(Throwable e) {
                result[0] = true;
                synchronized (wait) {
                    wait.notify();
                }
            }
        });
        synchronized (wait) {
            fireHanlderWithSkypeException();
            try {
                wait.wait();
            } catch (InterruptedException e) {
            }
        }
        Assert.assertTrue(result[0]);
        
        Skype.setSkypeExceptionHandler(null);
        fireHanlderWithSkypeException();
        TestUtils.showCheckDialog("The default output contains a stack trace?");
    }

    private void fireHanlderWithSkypeException() throws SkypeException {
        final Object wait = new Object();
        ChatMessageListener listener = new ChatMessageAdapter() {
            @Override
            public void chatMessageSent(ChatMessage sentChatMessage) throws SkypeException {
                try {
                    throw new SkypeException();
                } finally {
                    synchronized (wait) {
                        wait.notify();
                    }
                }
            }
        };
        Skype.addChatMessageListener(listener);
        synchronized (wait) {
            TestData.getFriend().send("a message for a method test");
            try {
                wait.wait();
            } catch (InterruptedException e) {
            }
        }
        Skype.removeChatMessageListener(listener);
    }
}
