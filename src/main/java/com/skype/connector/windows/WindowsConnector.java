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
 * Kamil Sarelo - modified getInstalledPath() to support installing by an
 *                administrator account and added javadocs
 ******************************************************************************/
package com.skype.connector.windows;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.concurrent.*;

import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.widgets.Display;

import com.skype.connector.*;

/**
 * A connector implementation for Windows based on the SWT libraries.
 * Please, use Win32Connector if SWT is not an option for you.
 */
public final class WindowsConnector extends Connector {
    /**
     * The singleton instance of the WindowsConnector class.
     */
    private static class Instance {
        static WindowsConnector instance = new WindowsConnector();
    }

    /**
     * Gets the singleton instance of the WindowsConnector class.
     * 
     * @return the singleton instance of the WindowsConnector class
     */
    public static WindowsConnector getInstance() {
        // Using 'Initialization On Demand Holder' Pattern
        return Instance.instance;
    }

    /**
     * The attached response type (value is 0).
     * <p>
     * This response is sent when the client is attached.
     * </p>
     */
    @SuppressWarnings("unused")
	private static final int ATTACH_SUCCESS = 0;

    /**
     * The pending authorization response type (value is 1).
     * <p>
     * This response is sent when Skype acknowledges the connection request and
     * is waiting for user confirmation. The client is not yet attached and must
     * wait for the {@ses #ATTACH_SUCCESS} message.
     * </p>
     */
    @SuppressWarnings("unused")
	private static final int ATTACH_PENDING_AUTHORIZATION = 1;

    /**
     * The refused response type (value is 2).
     * <p>
     * This response is sent when the user has explicitly denied the access of
     * the client.
     * </p>
     */
    @SuppressWarnings("unused")
	private static final int ATTACH_REFUSED = 2;

    /**
     * The not available response type (value is 3).
     * <p>
     * This response is sent when the API is not available at the moment because
     * no user is currently logged in. The client must wait for a
     * {@see #ATTACH_API_AVAILABLE} broadcast before attempting to connect
     * again.
     * </p>
     */
    @SuppressWarnings("unused")
	private static final int ATTACH_NOT_AVAILABLE = 3;

    /**
     * The available response type (value is 0x8001).
     * <p>
     * This response is sent when the API becomes available.
     */
    @SuppressWarnings("unused")
	private static final int ATTACH_API_AVAILABLE = 0x8001;

    /**
     * The window handle indicating all top-level windows in the system.
     * 
     * @see <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/winui/winui/windowsuserinterface/windowing/messagesandmessagequeues/messagesandmessagequeuesreference/messagesandmessagequeuesfunctions/sendmessage.asp">MSDN Library</a>
     */
    private static final int HWND_BROADCAST = 0xffff;

    /**
     * The window message type to pass data to another application.
     * 
     * @see <a href="http://search.msdn.microsoft.com/search/Redirect.aspx?title=WM_COPYDATA+Message&url=http://msdn.microsoft.com/library/en-us/winui/winui/windowsuserinterface/dataexchange/datacopy/datacopyreference/datacopymessages/wm_copydata.asp">MSDN Library</a>
     */
    private static final int WM_COPYDATA = 0x004a;

    /**
     * The window message type of the response for initiating communication from Skype.
     * 
     * @see #DISCOVER_MESSAGE_ID
     */
    @SuppressWarnings("unused")
	private static final int ATTACH_MESSAGE_ID = OS.RegisterWindowMessage(new TCHAR(0, "SkypeControlAPIAttach", true));

    /**
     * The window message type of the request to initiate communication with Skype.
     * 
     * @see #ATTACH_MESSAGE_ID
     */
    private static final int DISCOVER_MESSAGE_ID = OS.RegisterWindowMessage(new TCHAR(0, "SkypeControlAPIDiscover", true));

    /** SWT display instance. */
    private Display display;
    /** SWT window instance. */
    private TCHAR windowClass;
    /** SWT window handle. */
    private int windowHandle;
    /** Skype Client window handle. */
    private int skypeWindowHandle;

    private Thread eventLoop;
    private CountDownLatch eventLoopFinishedLatch;

    /**
     * Constructor.
     */
    private WindowsConnector() {
    }

    /**
     * Returns the location of Skype.exe file from the MS Windows registry
     * (implicit it check if Skype is installed or not). Checks in the registry
     * if the key: {HKCU\Software\Skype\Phone, SkypePath} exists; if not, it
     * checks again but now for {HKLM\Software\Skype\Phone, SkypePath}; if HKCU
     * key does not exist but the HKLM key is present, Skype has been installed
     * from an administrator account has but not been used from the current
     * account; otherwise there is no Skype installed.
     * 
     * @return the path to the <code>Skype.exe</code> file if Skype is
     *         installed or <code>null</code>.
     */
    @Override
    public String getInstalledPath() {
        String result = getRegistryValue(OS.HKEY_CURRENT_USER, "Software\\Skype\\Phone", "SkypePath");
        if (result == null) {
            result = getRegistryValue(OS.HKEY_LOCAL_MACHINE, "Software\\Skype\\Phone", "SkypePath");
        }
        return result;
    }

    /**
     * Returns the value to which the specified key and data is mapped in the
     * Windows registry, or null if the registry contains no mapping for this
     * key and/or data.
     * 
     * @param hKey registry hKey.
     * @param keyName registry key name.
     * @param dataName registry data name.
     * @return the value to which the specified key and data is mapped or
     *         <code>null</code>.
     */
    private String getRegistryValue(final int hKey, final String keyName, final String dataName) {
        final int[] phkResult = new int[1];
        if (OS.RegOpenKeyEx(hKey, new TCHAR(0, keyName, true), 0, OS.KEY_READ, phkResult) != 0) {
            return null;
        }
        String result = null;
        final int[] lpcbData = new int[1];
        if (OS.RegQueryValueEx(phkResult[0], new TCHAR(0, dataName, true), 0, null, (TCHAR) null, lpcbData) == 0) {
            result = "";
            int length = lpcbData[0] / TCHAR.sizeof;
            if (length != 0) {
                TCHAR lpData = new TCHAR(0, length);
                if (OS.RegQueryValueEx(phkResult[0], new TCHAR(0, dataName, true), 0, null, lpData, lpcbData) == 0) {
                    length = Math.max(0, lpData.length() - 1);
                    result = lpData.toString(0, length);
                }
            }
        }
        if (phkResult[0] != 0) {
            OS.RegCloseKey(phkResult[0]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean isRunning() throws ConnectorException {
        try {
            final Class clazz = Class.forName("com.skype.connector.windows.SkypeFramework");
            final Method method = clazz.getDeclaredMethod("isRunning");
            return ((Boolean)method.invoke(null)).booleanValue();
        } catch(Exception e) {
            throw new UnsupportedOperationException("The winp-1.5.jar <https://winp.dev.java.net/> is not contained in the classpath.");
        }
    }

    /**
     * Initialize the connector.
     * @param timeout Maximum amout of time in millieseconds to initialize.
     * @throws ConnectorException when initialization cannot be completed.
     */
    @Override
    protected void initializeImpl() throws ConnectorException {
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] errorMessage = new String[1];
        eventLoopFinishedLatch = new CountDownLatch(1);
        eventLoop = new Thread("SkypeEventLoop") {
            @Override
            public void run() {
                try {
                    display = new Display();
                    windowClass = new TCHAR(0, "" + System.currentTimeMillis() + (int) (Math.random() * 1000), true);
                    int messageReceived = new Callback(WindowsConnector.this, "messageReceived", 4).getAddress();
                    if (messageReceived == 0) {
                        setErrorMessage("The Windows connector couldn't get a callback resource.");
                        return;
                    }
                    int hHeap = OS.GetProcessHeap();
                    if (hHeap == 0) {
                        setErrorMessage("The Windows connector couldn't get the heap handle.");
                        return;
                    }
                    int hInstance = OS.GetModuleHandle(null);
                    if (hInstance == 0) {
                        setErrorMessage("The Windows connector couldn't get the module handle.");
                        return;
                    }
                    WNDCLASS lpWndClass = new WNDCLASS();
                    lpWndClass.hInstance = hInstance;
                    lpWndClass.lpfnWndProc = messageReceived;
                    lpWndClass.style = OS.CS_BYTEALIGNWINDOW | OS.CS_DBLCLKS;
                    lpWndClass.hCursor = OS.LoadCursor(0, OS.IDC_ARROW);
                    if (lpWndClass.hCursor == 0) {
                        setErrorMessage("The Windows connector couldn't get a cursor handle.");
                        return;
                    }
                    int byteCount = windowClass.length() * TCHAR.sizeof;
                    lpWndClass.lpszClassName = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount);
                    if (lpWndClass.lpszClassName == 0) {
                        setErrorMessage("The Windows connector couldn't get a resource.");
                        return;
                    }
                    OS.MoveMemory(lpWndClass.lpszClassName, windowClass, byteCount);
                    if (OS.RegisterClass(lpWndClass) == 0) {
                        setErrorMessage("The Windows connector couldn't register a window class.");
                        return;
                    }
                    windowHandle = OS.CreateWindowEx(0, windowClass, null, OS.WS_OVERLAPPED, 0, 0, 0, 0, 0, 0, hInstance, null);
                    if (windowHandle == 0) {
                        setErrorMessage("The Windows connector couldn't create a window.");
                        return;
                    }
                } finally {
                    latch.countDown();
                }
                while (true) {
                    if (!display.readAndDispatch()) {
                        display.sleep();

                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                    }
                }
                eventLoopFinishedLatch.countDown();
            };

            private void setErrorMessage(String message) {
                errorMessage[0] = message;
            }
        };
        eventLoop.setDaemon(true);
        eventLoop.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new ConnectorException("The Windows connector initialization was interrupted.", e);
        }
        if (errorMessage[0] != null) {
            throw new ConnectorException(errorMessage[0]);
        }
    }

    /**
     * Implementation of the connect method for this connector.
     * @param timeout maximum amout of time to connect.
     * @return Status after connecting.
     * @throws ConnectorException when connection could not be established.
     */
    protected Status connect(final int timeout) throws ConnectorException {
        final BlockingQueue<Status> queue = new LinkedBlockingQueue<Status>();
        final ConnectorListener listener = new AbstractConnectorListener() {
            @Override
            public void statusChanged(ConnectorStatusEvent event) {
                queue.add(event.getStatus());
            }
        };
        addConnectorListener(listener, false);
        try {
            while (true) {
                OS.PostMessage(HWND_BROADCAST, DISCOVER_MESSAGE_ID, windowHandle, 0);
                final Status status = queue.poll(timeout, TimeUnit.MILLISECONDS);
                if (status == null) {
                    setStatus(Status.NOT_RUNNING);
                }
                if (status != Status.PENDING_AUTHORIZATION) {
                    return status;
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectorException("Trying to connect was interrupted.", e);
        } finally {
            removeConnectorListener(listener);
        }
    }

    /**
     * Send the application name to the Skype Client.
     * @param applicationName the new applicationname.
     * @throws ConnectorException when connection to Skype client has gone bad.
     */
    protected void sendApplicationName(final String applicationName) throws ConnectorException {
        final String command = "NAME " + applicationName;
        execute(command, new String[] {command}, false);
    }
    
    /**
     * Clean up and disconnect.
     */
    protected void disposeImpl() {
        eventLoop.interrupt();
        display.wake();
        try {
            eventLoopFinishedLatch.await();
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Send a command to the Skype client.
     * @param command The command to send.
     */
    protected void sendCommand(final String command) {
        display.asyncExec(new Runnable() {
            public void run() {
                try {
                    final byte[] data = (command + "\u0000").getBytes("UTF-8");
                    final int hHeap = OS.GetProcessHeap();
                    final int pMessage = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, data.length);
                    OS.MoveMemory(pMessage, data, data.length);
                    OS.SendMessage(skypeWindowHandle, WM_COPYDATA, windowHandle, new int[] { 0, data.length, pMessage });
                    OS.HeapFree(hHeap, 0, pMessage);
                } catch (UnsupportedEncodingException e) {
                }
            }
        });
    }
}
