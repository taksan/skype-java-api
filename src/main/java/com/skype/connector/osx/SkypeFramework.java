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
package com.skype.connector.osx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.skype.connector.ConnectorUtils;
import com.skype.connector.LoadLibraryException;

final class SkypeFramework {
    private static Object initializedFieldMutex = new Object();
    private static boolean initialized = false;
    
    private static final List<SkypeFrameworkListener> listeners = new CopyOnWriteArrayList<SkypeFrameworkListener>();
    
    // to protect native memory buffer in JNI code
    private static Object sendCommandMutex = new Object();
    private static Object notificationReceivedMutex = new Object();
    
    static void init(String applicationName) throws LoadLibraryException {
        ConnectorUtils.checkNotNull("applicationName", applicationName);
        synchronized(initializedFieldMutex) {
            if (!initialized) {
                ConnectorUtils.loadLibrary("libskype.jnilib");  
                setup0(applicationName);
                initialized = true;                
            }
        }
    }
    
    private static native void setup0(String applicationName);
    
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

    static boolean isAvailable() {
        return isAvailable0();
    }

    private static native boolean isAvailable0();
    
    static void fireBecameAvailable() {
        for (SkypeFrameworkListener listener: listeners) {
            listener.becameAvailable();
        }
    }
    
    static void fireBecameUnavailable() {
        for (SkypeFrameworkListener listener: listeners) {
            listener.becameUnavailable();
        }
    }
    
    static void connect() {
        connect0();
    }

    private static native void connect0();
    
    static void fireAttachResponse(int attachResponseCode) {
        for (SkypeFrameworkListener listener: listeners) {
            listener.attachResponse(attachResponseCode);
        }
    }
    
    static String sendCommand(String commandString) {
        // to protect native memory buffer
        synchronized(sendCommandMutex) {
            return sendCommand0(commandString);            
        }
    }

    private static native String sendCommand0(String commandString);
    
    static void fireNotificationReceived(String notificationString) {
        for (SkypeFrameworkListener listener: listeners) {
            listener.notificationReceived(notificationString);
        }
    }
    
    static void dispose() {
        synchronized(initializedFieldMutex) {
            if (initialized) {
                dispose0();
                listeners.clear();
                initialized = false;                
            }
        }
    }

    private static native void dispose0();
    
    static int runCurrentEventLoop(double inTimeout) {
        return runCurrentEventLoop0(inTimeout);
    }

    private static native int runCurrentEventLoop0(double inTimeout);
    
    static void runApplicationEventLoop() {
        runApplicationEventLoop0();
    }

    private static native void runApplicationEventLoop0();
    
    static void quitApplicationEventLoop() {
        quitApplicationEventLoop0();
    }

    private static native void quitApplicationEventLoop0();
}
