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
 * Gabriel Takeuchi - mac osx native library improvements
 ******************************************************************************/
package com.skype.connector.osx;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.UnsupportedArchitectureException;

/**
 * Implementation of the connector for Mac OS X.
 */
public final class OSXConnector extends Connector {
    /** Singleton instance. */
    private static OSXConnector _instance = null;
    
    private static boolean _skypeEventLoopEnabled = true;

    /**
     * Get singleton instance.
     * @return instance.
     */
    public static synchronized Connector getInstance() {
    	String osArch = System.getProperty("os.arch");
		if (osArch.contains("64")) {
    		throw new UnsupportedArchitectureException(
    				"Skype Java Api doesn't support running under 64bit architectures under Mac OSX. " +
    				"You may try running with 'java -d32' if your system has java 32bit installed."
    				);
    	}
        if(_instance == null) {
            _instance = new OSXConnector();
            try {
				_instance.initialize();
			} catch (ConnectorException e) {
				throw new IllegalStateException(e);
			}
        }
        return _instance;
    }
    
    public static void disableSkypeEventLoop() {
        _skypeEventLoopEnabled = false;
    }
    
    static String fixLegacyMessage(String message) {
    	if (message.matches(".*\\bMESSAGE\\b.*")) {
    		return message.replaceFirst("\\bMESSAGE\\b", "CHATMESSAGE");
    	}
    	return message;
    }
    
    private SkypeFrameworkListener listener = new AbstractSkypeFrameworkListener() {
        @Override
        public void notificationReceived(String notificationString) {
            fireMessageReceived(fixLegacyMessage(notificationString));
        }
    
        @Override
        public void becameUnavailable() {
            setStatus(Status.NOT_AVAILABLE);
        }
    
        @Override
        public void becameAvailable() {
        }    
    };

    /**
     * Constructor.
     */
    private OSXConnector() {
    }
    
    public boolean isRunning() throws ConnectorException {
        return SkypeFramework.isRunning();
    }

    /**
     * Gets the absolute path of Skype.
     * @return the absolute path of Skype.
     */
    public String getInstalledPath() {
        File application = new File("/Applications/Skype.app/Contents/MacOS/Skype");
        if (application.exists()) {
            return application.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Initializes this connector.
     */
    protected void initializeImpl() throws ConnectorException {
        SkypeFramework.init(getApplicationName());
        SkypeFramework.addSkypeFrameworkListener(listener);
        if (_skypeEventLoopEnabled) {
            final CountDownLatch latch = new CountDownLatch(1);
            Thread eventLoop = new Thread("SkypeEventLoop") {
                @Override
                public void run() {
                    latch.countDown();
                    SkypeFramework.runApplicationEventLoop();
                };
            };
            eventLoop.setDaemon(true);
            eventLoop.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                SkypeFramework.quitApplicationEventLoop();
                throw new ConnectorException("The connector initialization was interrupted.", e);
            }
        }
    }

    /**
     * Connects to Skype client.
     * @param timeout the maximum time in milliseconds to connect.
     * @return Status the status after connecting.
     * @throws ConnectorException when connection can not be established.
     */
    protected Status connect(int timeout) throws ConnectorException {
        if (!SkypeFramework.isRunning()) {
            setStatus(Status.NOT_RUNNING);
            return getStatus();
        }
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            SkypeFrameworkListener listener = new AbstractSkypeFrameworkListener() {
                @Override
                public void attachResponse(int attachResponseCode) {
                    SkypeFramework.removeSkypeFrameworkListener(this);
                    switch(attachResponseCode) {
                        case 0:
                            setStatus(Status.REFUSED);
                            latch.countDown();
                            break;
                        case 1:
                            setStatus(Status.ATTACHED);
                            latch.countDown();
                            break;
                        default:
                            throw new IllegalStateException("not supported attachResponseCode");
                    }
                }
            };
            setStatus(Status.PENDING_AUTHORIZATION);
            SkypeFramework.addSkypeFrameworkListener(listener);
            SkypeFramework.connect();
            latch.await();
            return getStatus();
        } catch(InterruptedException e) {
            throw new ConnectorException("Trying to connect was interrupted.", e);
        }
    }

    @Override
    protected void sendProtocol() throws ConnectorException {
        // changed not to send protocol because of Skype.framework event bugs(?)
    }

    /**
     * Sends a command to the Skype client.
     * @param command The command to send.
     */
    protected void sendCommand(final String command) {
        String result = SkypeFramework.sendCommand(command);
        if (result != null) {
            fireMessageReceived(result);
        }
    }

    /**
     * Cleans up the connector and the native library.
     */
    protected void disposeImpl() {
        SkypeFramework.removeSkypeFrameworkListener(listener);
        SkypeFramework.dispose();
        if (_skypeEventLoopEnabled) {
            SkypeFramework.quitApplicationEventLoop();
        }
    }
}
