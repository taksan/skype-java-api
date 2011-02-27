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
 ******************************************************************************/
package com.skype.connector.linux;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import com.skype.connector.ConnectorUtils;
import com.skype.connector.LoadLibraryException;

final class SkypeFramework {
    private static Object initializedFieldMutex = new Object();
    private static boolean initialized = false;

    private static CountDownLatch eventLoopFinishedLatch;
    private static Thread eventLoop;
    private static final List<SkypeFrameworkListener> listeners = new CopyOnWriteArrayList<SkypeFrameworkListener>();
    
    static void init() throws LoadLibraryException {
        synchronized(initializedFieldMutex) {
            if (!initialized) {
                ConnectorUtils.loadLibrary("skype");  
                setup0();
                
                eventLoopFinishedLatch = new CountDownLatch(1);
                eventLoop = new Thread(new Runnable() {
                    public void run() {
                        runEventLoop0();
                        eventLoopFinishedLatch.countDown();
                    }
                }, "Skype4Java Event Loop");
                eventLoop.setDaemon(true);
                eventLoop.start();
                initialized = true;                
            }
        }
    }
    
    private static native void setup0();
    private static native void runEventLoop0();
    
    static void addSkypeFrameworkListener(SkypeFrameworkListener listener) {
        listeners.add(listener);
    }
    
    static void removeSkypeFrameworkListener(SkypeFrameworkListener listener) {
        listeners.remove(listener);
    }
    
    static boolean isRunning() {
        return isRunning0();
    }

    private static native boolean isRunning0();
    
    static void sendCommand(String commandString) {
        sendCommand0(commandString);            
    }

    private static native void sendCommand0(String commandString);
    
    static void fireNotificationReceived(String notificationString) {
        for (SkypeFrameworkListener listener: listeners) {
            listener.notificationReceived(notificationString);
        }
    }
    
    static void dispose() {
        synchronized(initializedFieldMutex) {
            if (initialized) {
                listeners.clear();
                stopEventLoop0();
                try {
                    eventLoopFinishedLatch.await();
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                closeDisplay0();
                initialized = false;                
            }
        }
    }

    private static native void stopEventLoop0();
    private static native void closeDisplay0();
}
