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
public final class Ap2ApAPIStressTest {
    static final String APPLICATION_NAME = Ap2ApAPIStressTest.class.getName();

    @Test
    public void testWriteOnMultiThread() throws Exception {
        Skype.setDebug(true);
        Application application = Skype.addApplication(APPLICATION_NAME);
        Friend friend = TestData.getFriend();
        try {
            Stream[] streams = application.connect(friend);
            Assert.assertEquals(1, streams.length);
            Stream stream = streams[0];
            checkConnectedFriends(application, friend);
            checkWriteOnMultiThread(stream);
            checkDisconnect(application, stream);
        } finally {
            application.finish();
        }
    }

    private void checkConnectedFriends(Application application, Friend friend) throws SkypeException {
        Friend[] connectableFriends = application.getAllConnectedFriends();
        Assert.assertEquals(1, connectableFriends.length);
        Assert.assertEquals(friend, connectableFriends[0]);
    }

    private void checkWriteOnMultiThread(final Stream stream) throws SkypeException {
        Thread[] threads = new Thread[20];
        final String[] results = new String[threads.length];
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            Thread thread = new Thread("write-" + index) {
                @Override
                public void run() {
                    try {
                        final Object lock = new Object();
                        stream.addStreamListener(new StreamAdapter() {
                            @Override
                            public void textReceived(String text) throws SkypeException {
                                results[index] = text;
                                synchronized (lock) {
                                    lock.notify();
                                }
                            }
                        });
                        synchronized (lock) {
                            stream.write("Hello, World!");
                            try {
                                lock.wait(10000);
                            } catch (InterruptedException e) {
                            }
                        }
                    } catch (SkypeException e) {
                    }
                };
            };
            thread.start();
            threads[i] = thread;
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }
        for (String result : results) {
            Assert.assertEquals("Hello, World!", result);
        }
    }

    private void checkDisconnect(Application application, Stream stream) throws Exception {
        final Object lock = new Object();
        final boolean[] result = new boolean[1];
        application.addApplicationListener(new ApplicationAdapter() {
            @Override
            public void disconnected(Stream stream) throws SkypeException {
                result[0] = true;
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            stream.write("disconnect");
            try {
                lock.wait(10000);
            } catch (InterruptedException e) {
                Assert.fail();
            }
        }
        Assert.assertTrue(result[0]);
    }
}
