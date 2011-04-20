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

import com.skype.Application;
import com.skype.ApplicationAdapter;
import com.skype.Skype;
import com.skype.SkypeException;
import com.skype.Stream;
import com.skype.StreamAdapter;

public final class Ap2ApAPIStressTestServer {
    public static void main(String[] args) throws Exception {
        Skype.setDebug(true);
        final Application application = Skype.addApplication(Ap2ApAPIStressTest.APPLICATION_NAME);
        final Object lock = new Object();
        application.addApplicationListener(new ApplicationAdapter() {
            public void connected(final Stream stream) throws SkypeException {
                stream.addStreamListener(new StreamAdapter() {
                    @Override
                    public void textReceived(String text) throws SkypeException {
                        try {
                            if ("disconnect".equals(text)) {
                                stream.disconnect();
                                return;
                            }
                            stream.write(text);
                        } catch (SkypeException e) {
                            synchronized (lock) {
                                lock.notify();
                            }
                            System.err.println("couldn't respond to " + stream.getFriend().getId() + " text");
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void disconnected(Stream stream) throws SkypeException {
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
            }
        }
        application.finish();
    }
}
