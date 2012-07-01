/*******************************************************************************
 * Copyright (c) 2006-2007 r-yu/xai
 *
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
 * r-yu/xai - initial implementation
 * Koji Hisano - changed Skype event dispatch thread to a deamon thread
 * Gabriel Takeuchi - windows 64bit support added
 ******************************************************************************/
package com.skype.connector.win32;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorUtils;
import com.skype.connector.LoadLibraryException;

/**
 * Implementation of a connector for Windows.
 * This implementation uses a small dll to connect.
 * The WindowsConnector uses SWT library.
 * Choose wisely.
 */
public class Win32Connector extends Connector {
    /** Status ATTACH_SUCCES value. */
	private static final int ATTACH_SUCCESS = 0;
	/** Status ATTACH_PENDING_AUTHORISATION value. */
    private static final int ATTACH_PENDING_AUTHORIZATION = 1;
    /** Status ATTACH_REFUSED value. */
    private static final int ATTACH_REFUSED = 2;
    /** Status ATTACH_NOT_AVAILABLE value. */
    private static final int ATTACH_NOT_AVAILABLE = 3;
    /** Status ATTACH_API_AVAILABLE value. */
    private static final int ATTACH_API_AVAILABLE = 0x8001;
    /** Filename of the DLL. */
    private static final String LIB_FILENAME_FORMAT = "skype_%s.dll";
    
    /** Singleton instance. */
    private static Win32Connector instance = null;

    /**
     * Get singleton instance.
     * @return instance.
     */
    public static synchronized Connector getInstance() {
        if(instance == null) {
            instance = new Win32Connector();
            try {
				instance.initialize();
			} catch (ConnectorException e) {
				throw new IllegalStateException(e);
			}
        }
        return (Connector) instance;
    }

    /** Thread. */
    private Thread eventDispatcher = null;

    /**
     * Constructor.
     *
     */
    protected Win32Connector() {
    }

    /**
     * Return the path of Skype.exe.
     * @return absolute path to Skype.exe.
     */
    public String getInstalledPath() {
        return jni_getInstalledPath();
    }

    /**
     * Initialize the connector.
     * @param timeout maximum time in miliseconds to initialize.
     * @throws LoadLibraryException 
     */
    protected void initializeImpl() {    	
        // Loading DLL
    	final String osArch = System.getProperty("os.arch");
    	String libfilename = String.format(LIB_FILENAME_FORMAT, osArch);
    	try {
			ConnectorUtils.loadLibrary(libfilename);
		} catch (LoadLibraryException e) {
			throw new IllegalStateException("It was impossible to load " + libfilename + " dll.", e);
		}

        // Initializing JNI
        jni_init();

        // Starting Window Thread
        eventDispatcher = new Thread(new Runnable() {
            public void run() {
                jni_windowProc();
            }
        }, "SkypeBridge WindowProc Thread");
        eventDispatcher.setDaemon(true);
        eventDispatcher.start();
    }

    /**
     * Connect to Skype client.
     * @param timeout the maximum time in milliseconds to connect.
     * @return Status the status after connecting.
     * @throws ConnectorException when connection can not be established.
     */
    protected Status connect(int timeout) throws ConnectorException {
        try {
        	int retries = 3;
            while(true) {
                jni_connect();
                long start = System.currentTimeMillis();
                if(timeout <= System.currentTimeMillis() - start) {
                    setStatus(Status.NOT_RUNNING);
                }
                Status status = getStatus();
                if(status != Status.PENDING_AUTHORIZATION && status != Status.NOT_RUNNING) {
                    return status;
                }
                if (retries <= 0)
                	return Status.NOT_RUNNING;
                	
                retries--;
                Thread.sleep(1000);
            }
        } catch(InterruptedException e) {
            throw new ConnectorException("Trying to connect was interrupted.", e);
        }
    }

    /**
     * Send applicationname to Skype client.
     * @param applicationName The new Application name.
     * @throws ConnectorException when Skype Client connection has gone bad. 
     */
    protected void sendApplicationName(final String applicationName) throws ConnectorException {
        String command = "NAME " + applicationName;
        execute(command, new String[] {command}, false);
    }

    /**
     * Set the connector status.
     * This method will be called by the native lib.
     * @param status The new status.
     */
    public void jni_onAttach(int status) {
        switch(status) {
            case ATTACH_PENDING_AUTHORIZATION:
                setStatus(Status.PENDING_AUTHORIZATION);
                break;
            case ATTACH_SUCCESS:
                setStatus(Status.ATTACHED);
                break;
            case ATTACH_REFUSED:
                setStatus(Status.REFUSED);
                break;
            case ATTACH_NOT_AVAILABLE:
                setStatus(Status.NOT_AVAILABLE);
                break;
            case ATTACH_API_AVAILABLE:
                setStatus(Status.API_AVAILABLE);
                break;
            default:
                setStatus(Status.NOT_RUNNING);
                break;
        }
    }

    /**
     * This method gets called when the native lib has a message received.
     * @param message The received message.
     */
    public void jni_onSkypeMessage(String message) {
        fireMessageReceived(message);
    }

    /**
     * Clean up the connector and the native lib.
     */
    protected void disposeImpl() {
        // TODO WindowsConnector#disposeImpl()
        throw new UnsupportedOperationException("WindowsConnector#disposeImpl() is not implemented yet.");
    }

    /**
     * Send a command to the Skype client.
     * @param command The command to send.
     */
    protected void sendCommand(final String command) {
        jni_sendMessage(command);
    }

    // for native
    /**
     * Native init method.
     */
    private native void jni_init();
    
    /**
     * native event loop method.
     *
     */
    private native void jni_windowProc();
    
    /**
     * Native send message method.
     * @param message The message to send.
     */
    private native void jni_sendMessage(String message);
    
    /***
     * The native connect method.
     *
     */
    private native void jni_connect();
    
    /**
     * The native get installed path method.
     * @return String with the absolute path to Skype.exe.
     */
    private native String jni_getInstalledPath();
}
