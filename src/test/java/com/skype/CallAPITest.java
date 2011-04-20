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

import java.util.Date;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

// non automatic tests
@Ignore
public class CallAPITest {
	@Test
    public void testCallAndFinish() throws Exception {
        TestUtils.showMessageDialog("Please, check " + TestData.getFriendId() + " will receive a call and it will be finished after two seconds?");
        Friend friend = Skype.getContactList().getFriend(TestData.getFriendId());
        Call call = friend.call();
        Thread.sleep(2000);
        call.finish();
        TestUtils.showCheckDialog(TestData.getFriendId() + " has received a call and it was finished after two seconds?");
    }

	@Test
    public void testFinishEndedCall() throws Exception {
        Friend friend = Skype.getContactList().getFriend(TestData.getFriendId());
        Call call = friend.call();
        call.finish();
        Thread.sleep(1000);
        try {
            call.finish();
        } catch (CommandFailedException e) {
            Assert.assertEquals(24, e.getCode());
            Assert.assertEquals("Cannot hangup inactive call", e.getMessage());
        }
    }

	@Test
    public void testHoldAndResume() throws Exception {
        TestUtils.showMessageDialog("Please, start a talking with " + TestData.getFriendId() + " in ten seconds after starting a call.");
        Friend friend = Skype.getContactList().getFriend(TestData.getFriendId());
        Call call = friend.call();
        Thread.sleep(10000);
        TestUtils.showMessageDialog("Please, check 2 seconds suspending, 2 seconds talking and finishing.");
        call.hold();
        Thread.sleep(2000);
        call.resume();
        Thread.sleep(2000);
        call.finish();
        TestUtils.showCheckDialog("You have gotton a 2 seconds suspending, and 2 seconds talking?");
    }

	@Test
    public void testCallProperty() throws Exception {
        Date startTime = new Date();
        TestUtils.showMessageDialog("Please, start a talking with " + TestData.getFriendId() + " and finish in ten seconds after starting a call.");
        Friend friend = Skype.getContactList().getFriend(TestData.getFriendId());
        Call call = friend.call();
        Thread.sleep(10000);
        Date endTime = new Date();
        Assert.assertTrue(call.getStartTime().getTime() - startTime.getTime() <= endTime.getTime() - startTime.getTime());
        Assert.assertTrue(call.getDuration() <= endTime.getTime() - startTime.getTime());
        Assert.assertEquals(TestData.getFriendId(), call.getPartnerId());
        Assert.assertEquals(TestData.getFriendDisplayName(), call.getPartnerDisplayName());
    }
	
	@Test
    public void testCallReceived() throws Exception {
        final Object wait = new Object();
        final Call[] result = new Call[1];
        Skype.addCallListener(new CallAdapter() {
            @Override
            public void callReceived(Call call) throws SkypeException {
                synchronized(wait) {
                    wait.notify();
                }
                result[0] = call;
            }
        });
        TestUtils.showMessageDialog("Please, request " + TestData.getFriendId() + " to call me, receive the call, and finish.");
        synchronized(wait) {
            wait.wait();
        }
        Assert.assertEquals(TestData.getFriendId(), result[0].getPartnerId());
    }
}
