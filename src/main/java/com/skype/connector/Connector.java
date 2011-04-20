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
 * Gabriel Takeuchi - retry commands instead of "ping-pong" to improve reliability
 ******************************************************************************/
package com.skype.connector;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for all platform specific connectors. A connector connects the
 * Skype Java API with a running Skype client.
 * 
 * @author Koji Hisano <hisano@gmail.com>
 */
public abstract class Connector {
	/**
	 * Enumeration of the connector status.
	 */
	public enum Status {
		/**
		 * PENDING_AUTHORIZATION - The connector is waiting for the user to
		 * accept this app to connect to the Skype client. ATTACHED - The
		 * connector is attached to the Skype client. REFUSED - The user denied
		 * the application to connect to the Skype client. NOT_AVAILABLE - The
		 * is no Skype client available to connect to. API_AVAILABLE - Redundant
		 * of ATTACHED. NOT_RUNNING - Connection can't be established.
		 */
		PENDING_AUTHORIZATION, ATTACHED, REFUSED, NOT_AVAILABLE, API_AVAILABLE, NOT_RUNNING;
	}

	/** Singleton instance of this class. */
	private static Connector _instance;

	/**
	 * Initializes a platform specific connection. This method will select a
	 * connector based on the os.name. Windows has two versions see
	 * useJNIConnector.
	 * 
	 * @return an initialized connection.
	 */
	public static synchronized Connector getInstance() {
		if (_instance == null) {
			String connectorClassName = null;
			String osName = System.getProperty("os.name");
			if (osName.startsWith("Windows")) {
				connectorClassName = "com.skype.connector.win32.Win32Connector";
			} else if (osName.startsWith("Linux") || osName.startsWith("LINUX")) {
				connectorClassName = "com.skype.connector.linux.LinuxConnector";
			} else if (osName.startsWith("Mac OS X")) {
				connectorClassName = "com.skype.connector.osx.OSXConnector";
			}
			if (connectorClassName == null) {
				throw new IllegalStateException(
						"This platform is not supported by Skype4Java.");
			}
			try {
				@SuppressWarnings("unchecked")
				Class<Connector> connectorClass = (Class<Connector>) Class
						.forName(connectorClassName);
				Method getInstance = connectorClass.getMethod("getInstance");
				_instance = (Connector) getInstance.invoke(null);
			} catch (Exception e) {
				throw new IllegalStateException(
						"The connector couldn't be initialized.", e);
			}
		}
		return _instance;
	}

	/**
	 * Sets the instance of the connector for test cases.
	 * 
	 * @param newInstance
	 *            The new instance.
	 * @throws ConnectorException
	 *             thrown when instance is not valid.
	 */
	protected static synchronized void setInstance(final Connector newInstance)
			throws ConnectorException {
		if (_instance != null) {
			_instance.dispose();
		}
		_instance = newInstance;
	}

	/**
	 * The mutex object for the _debugListener field.
	 */
	private final Object _debugListenerMutex = new Object();
	/**
	 * The connector listener for debug out.
	 */
	private ConnectorListener _debugListener;

	/**
	 * The debug output stream. This stream is initialized by
	 * <tt>new PrintWriter(System.out, true)</tt>.
	 */
	private volatile PrintWriter _debugOut = new PrintWriter(System.out, true);

	/**
	 * The application name used to get the access grant of Skype API.
	 */
	private volatile String _applicationName = "Skype4Java";

	/**
	 * The status of this connector.
	 */
	private volatile Status _status = Status.NOT_RUNNING;

	/**
	 * The connect timeout in milliseconds.
	 */
	private volatile int _connectTimeout = 20000;
	/**
	 * The command reply timeout in milliseconds.
	 */
	private volatile int _commandTimeout = 20000;

	/**
	 * The mutex object for the _isInitialized field.
	 */
	private final Object _isInitializedMutex = new Object();
	/**
	 * The flag to check if the connector is already initialized.
	 */
	private boolean _isInitialized;

	/** Asynchronous message sender */
	private ExecutorService _asyncSender;
	/** Synchronous message sender */
	private ExecutorService _syncSender;

	/** Collection of asynchronous event listeners for the connector. */
	private final List<ConnectorListener> _asyncListeners = new CopyOnWriteArrayList<ConnectorListener>();
	/** Collection of synchronous event listeners for the connector. */
	private final List<ConnectorListener> _syncListeners = new CopyOnWriteArrayList<ConnectorListener>();

	/** Command counter, can be used to identify message and reply pairs. */
	private final AtomicInteger _commandCount = new AtomicInteger();

	/** Command executor */
	private ExecutorService _commandExecutor;

	/** The properties of this connector **/
	private final Map<String, String> properties = new ConcurrentHashMap<String, String>();

	/**
	 * Because this object should be a singleton the constructor is protected.
	 */
	protected Connector() {
	}

	/**
	 * Try to get the absolute path to the skype client. Should be overridden
	 * for each platfrom specific connector. Not geranteed to work.
	 * 
	 * @return The absolute path to the Skype client executable.
	 */
	public String getInstalledPath() {
		return "skype";
	}

	/**
	 * Enable or disable debug printing for more information.
	 * 
	 * @param on
	 *            if true debug output will be written to System.out
	 * @throws ConnectorException
	 *             thrown when connection to Skype Client has gone bad.
	 */
	public final void setDebug(final boolean on) throws ConnectorException {
		synchronized (_debugListenerMutex) {
			if (on) {
				if (_debugListener == null) {
					_debugListener = new AbstractConnectorListener() {
						@Override
						public void messageReceived(
								final ConnectorMessageEvent event) {
							getDebugOut().println("<- " + event.getMessage());
						}

						@Override
						public void messageSent(
								final ConnectorMessageEvent event) {
							getDebugOut().println("-> " + event.getMessage());
						}
					};
					addConnectorListener(_debugListener, true, true);
				}
			} else {
				if (_debugListener != null) {
					removeConnectorListener(_debugListener);
					_debugListener = null;
				}
			}
		}
	}

	/**
	 * Sets the debug output stream.
	 * 
	 * @param newDebugOut
	 *            the new debug output stream
	 * @throws NullPointerException
	 *             if the specified new debug out is null
	 * @see #setDebugOut(PrintStream)
	 * @see #getDebugOut()
	 */
	public final void setDebugOut(final PrintWriter newDebugOut) {
		ConnectorUtils.checkNotNull("debugOut", newDebugOut);
		_debugOut = newDebugOut;
	}

	/**
	 * Sets the debug output stream.
	 * 
	 * @param newDebugOut
	 *            the new debug output stream
	 * @throws NullPointerException
	 *             if the specified new debug out is null
	 * @see #setDebugOut(PrintWriter)
	 * @see #getDebugOut()
	 */
	public final void setDebugOut(final PrintStream newDebugOut) {
		ConnectorUtils.checkNotNull("debugOut", newDebugOut);
		setDebugOut(new PrintWriter(newDebugOut, true));
	}

	/**
	 * Gets the debug output stream.
	 * 
	 * @return the current debug output stream
	 * @see #setDebugOut(PrintWriter)
	 * @see #setDebugOut(PrintStream)
	 */
	public final PrintWriter getDebugOut() {
		return _debugOut;
	}

	/**
	 * Sets the application name used to get the access grant of Skype API. The
	 * specified name is what the User will see in the Skype API Allow/Deny
	 * dialog.
	 * 
	 * @param newApplicationName
	 *            the application name
	 * @throws NullPointerException
	 *             if the specified application name is null
	 * @see #getApplicationName()
	 */
	public final void setApplicationName(final String newApplicationName) {
		ConnectorUtils.checkNotNull("applicationName", newApplicationName);
		_applicationName = newApplicationName;
	}

	/**
	 * Gets the application name used to get the access grant of Skype API.
	 * 
	 * @return the application name
	 * @see #setApplicationName(String)
	 */
	public final String getApplicationName() {
		return _applicationName;
	}

	/**
	 * Sets the status of this connector. After setting, an status changed event
	 * will be sent to the all listeners.
	 * 
	 * @param newValue
	 *            the new status
	 * @throws NullPointerException
	 *             if the specified status is null
	 * @see #getStatus()
	 */
	protected final void setStatus(final Status newStatus) {
		ConnectorUtils.checkNotNull("status", newStatus);
		_status = newStatus;
		fireStatusChanged(newStatus);
	}

	/**
	 * Sends a status change event to the all listeners.
	 * 
	 * @param newStatus
	 *            the new status
	 */
	private void fireStatusChanged(final Status newStatus) {
		_syncSender.execute(new Runnable() {
			public void run() {
				// use listener array instead of list because of reverse
				// iteration
				fireStatusChanged(toConnectorListenerArray(_syncListeners),
						newStatus);
			}
		});
		_asyncSender.execute(new Runnable() {
			public void run() {
				// use listener array instead of list because of reverse
				// iteration
				fireStatusChanged(toConnectorListenerArray(_asyncListeners),
						newStatus);
			}
		});
	}

	/**
	 * Converts the specified listener list to an listener array.
	 * 
	 * @param listeners
	 *            the listener list
	 * @return an listener array
	 */
	private ConnectorListener[] toConnectorListenerArray(
			final List<ConnectorListener> listeners) {
		return listeners.toArray(new ConnectorListener[0]);
	}

	/**
	 * Sends a status change event to the specified listeners.
	 * 
	 * @param listeners
	 *            the event listeners
	 * @param newStatus
	 *            the new status
	 */
	private void fireStatusChanged(final ConnectorListener[] listeners,
			final Status newStatus) {
		final ConnectorStatusEvent event = new ConnectorStatusEvent(this,
				newStatus);
		for (int i = listeners.length - 1; 0 <= i; i--) {
			listeners[i].statusChanged(event);
		}
	}

	/**
	 * Gets the status of this connector.
	 * 
	 * @return status the status of this connector
	 * @see #setStatus(com.skype.connector.Connector.Status)
	 */
	public final Status getStatus() {
		return _status;
	}

	/**
	 * Sets the connect timeout of this connector.
	 * 
	 * @param newConnectTimeout
	 *            the new connect timeout in milliseconds
	 * @throws IllegalArgumentException
	 *             if the new connect timeout is not more than 0
	 * @see #getConnectTimeout()
	 */
	public final void setConnectTimeout(final int newConnectTimeout) {
		if (newConnectTimeout < 0) {
			throw new IllegalArgumentException(
					"The connect timeout must be more than 0.");
		}
		_connectTimeout = newConnectTimeout;
	}

	/**
	 * Gets the connect timeout of this connector.
	 * 
	 * @return the connect timeout in milliseconds
	 * @see #setConnectTimeout(int)
	 */
	public final int getConnectTimeout() {
		return _connectTimeout;
	}

	/**
	 * Sets the command reply timeout of this connector.
	 * 
	 * @param newCommandTimeout
	 *            the new command reply timeout in milliseconds
	 * @throws IllegalArgumentException
	 *             if the new command reply timeout is not more than 0
	 * @see #getCommandTimeout()
	 */
	public final void setCommandTimeout(final int newCommandTimeout) {
		if (newCommandTimeout < 0) {
			throw new IllegalArgumentException(
					"The connect timeout must be more than 0.");
		}
		_commandTimeout = newCommandTimeout;
	}

	/**
	 * Gets the command reply timeout of this connector.
	 * 
	 * @return the command reply timeout in milliseconds
	 * @see #setCommandTimeout(int)
	 */
	public final int getCommandTimeout() {
		return _commandTimeout;
	}

	/**
	 * Tries to connect this connector to the Skype client.
	 * 
	 * @return the status after trying to connect.
	 * @throws ConnectorException
	 *             if trying to connect failed
	 * @throws NotAttachedException
	 *             if the Skype client is not running
	 */
	public final Status connect() throws ConnectorException {
		initialize();
		Status status = connect(getConnectTimeout());
		if (status == Status.ATTACHED) {
			sendApplicationName(getApplicationName());
			sendProtocol();
		}
		return status;
	}

	/**
	 * Initializes this connector.
	 * 
	 * @throws ConnectorException
	 *             if the initialization failed.
	 */
	protected final void initialize() throws ConnectorException {
		synchronized (_isInitializedMutex) {
			if (!_isInitialized) {
				_asyncSender = Executors
						.newCachedThreadPool(new ThreadFactory() {
							private final AtomicInteger threadNumber = new AtomicInteger();

							public Thread newThread(Runnable r) {
								Thread thread = new Thread(r,
										"AsyncSkypeMessageSender-"
												+ threadNumber
														.getAndIncrement());
								thread.setDaemon(true);
								return thread;
							}
						});
				_syncSender = Executors
						.newSingleThreadExecutor(new ThreadFactory() {
							public Thread newThread(Runnable r) {
								Thread thread = new Thread(r,
										"SyncSkypeMessageSender");
								thread.setDaemon(true);
								return thread;
							}
						});
				// newCachedThreadPool(
				_commandExecutor = Executors.newCachedThreadPool(
						new ThreadFactory() {
							private final AtomicInteger threadNumber = new AtomicInteger();

							public Thread newThread(Runnable r) {
								Thread thread = new Thread(r,"CommandExecutor-"+ threadNumber.getAndIncrement());
								thread.setDaemon(true);
								return thread;
							}
						});

				initializeImpl();

				_isInitialized = true;
			}
		}
	}

	/**
	 * Initializes the platform specific resources.
	 * 
	 * @throws ConnectorException
	 *             if the initialization failed.
	 */
	protected abstract void initializeImpl() throws ConnectorException;

	/**
	 * Tries to connect this connector to the Skype client on the platform
	 * mechanism.
	 * 
	 * @param timeout
	 *            the connect timeout in milliseconds to use while connecting.
	 * @return the status after trying to connect
	 * @throws ConnectorException
	 *             if the trying to connect failed.
	 */
	protected abstract Status connect(int timeout) throws ConnectorException;

	/**
	 * Sends the application name to the Skype client. The default
	 * implementation does nothing.
	 * 
	 * @param applicationName
	 *            the application name
	 * @throws ConnectorException
	 *             if sending the specified application name failed
	 */
	protected void sendApplicationName(String applicationName)
			throws ConnectorException {
	}

	/**
	 * Sends the Skype API protocol version to use. The default implementation
	 * uses the latest version of the Skype API.
	 * 
	 * @throws ConnectorException
	 *             if sending the protocol version failed
	 */
	protected void sendProtocol() throws ConnectorException {
		execute("PROTOCOL 9999", new String[] { "PROTOCOL " }, false);
	}

	/**
	 * Disconnects from the Skype client and clean up the resources.
	 * 
	 * @throws ConnectorException
	 *             if cleaning up the resources failed
	 */
	public final void dispose() throws ConnectorException {
		synchronized (_isInitializedMutex) {
			if (!_isInitialized) {
				return;
			}
			disposeImpl();
			setStatus(Status.NOT_RUNNING);
			_commandExecutor.shutdown();

			_syncSender.shutdown();
			_asyncSender.shutdown();

			_syncListeners.clear();
			_asyncListeners.clear();

			synchronized (_debugListenerMutex) {
				if (_debugListener != null) {
					addConnectorListener(_debugListener, false, true);
				}
			}

			_isInitialized = false;
		}
	}

	/**
	 * Disconnects from the Skype client and clean up the resources of the
	 * platfrom.
	 * 
	 * @throws ConnectorException
	 *             if cleaning up the resources failed
	 */
	protected abstract void disposeImpl() throws ConnectorException;

	/**
	 * Checks if the Skype client is running or not.
	 * 
	 * @return true if the Skype client is runnunig; false otherwise
	 * @throws ConnectorException
	 *             if checking the Skype client status failed
	 */
	public boolean isRunning() throws ConnectorException {
		try {
			assureAttached();
			return true;
		} catch (ConnectorException e) {
			return false;
		}
	}

	/**
	 * Executes the specified command and handles the response by the specified
	 * message processor.
	 * 
	 * @param command
	 *            the command to execute
	 * @param processor
	 *            the message processor
	 * @throws NullPointerException
	 *             if the specified command or processor is null
	 * @throws ConnectorException
	 *             if executing the command failed
	 */
	@Deprecated
	public final void execute(final String command,
			final MessageProcessor processor) throws ConnectorException {
		ConnectorUtils.checkNotNull("command", command);
		ConnectorUtils.checkNotNull("processor", processor);
		assureAttached();
		final Object wait = new Object();
		ConnectorListener listener = new AbstractConnectorListener() {
			public void messageReceived(ConnectorMessageEvent event) {
				processor.messageReceived(event.getMessage());
			}
		};
		processor.init(wait, listener);
		addConnectorListener(listener, false);
		synchronized (wait) {
			try {
				fireMessageSent(command);
				sendCommand(command);
				long start = System.currentTimeMillis();
				long commandResponseTime = getCommandTimeout();
				wait.wait(commandResponseTime);
				if (commandResponseTime <= System.currentTimeMillis() - start) {
					setStatus(Status.NOT_RUNNING);
					throw new NotAttachedException(Status.NOT_RUNNING);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new ConnectorException("The '" + command
						+ "' command was interrupted.", e);
			} finally {
				removeConnectorListener(listener);
			}
		}
	}

	/**
	 * Executes the specified command and gets the response. It is better to use
	 * {@link #executeWithId(String, String)} because it returns the accurate
	 * response.
	 * 
	 * @param command
	 *            the command to execute
	 * @return the response after execution
	 * @throws NullPointerException
	 *             if the specified command is null
	 * @throws ConnectorException
	 *             if executing the command failed
	 * @see #executeWithId(String, String)
	 */
	public final String execute(final String command) throws ConnectorException {
		ConnectorUtils.checkNotNull("command", command);
		return execute(command, command);
	}

	/**
	 * Executes the specified command and gets the response using a command ID.
	 * 
	 * @param command
	 *            the command to execute
	 * @param responseHeader
	 *            the response header to get the accurate response
	 * @return the response after execution
	 * @throws NullPointerException
	 *             if the specified command or responseHeader is null
	 * @throws ConnectorException
	 *             if executing the command failed
	 */
	public final String executeWithId(final String command,
			final String responseHeader) throws ConnectorException {
		ConnectorUtils.checkNotNull("command", command);
		ConnectorUtils.checkNotNull("responseHeader", responseHeader);
		final String header = "#" + _commandCount.getAndIncrement() + " ";
		final String response = execute(header + command, new String[] {
				header + responseHeader, header + "ERROR " }, true);
		return response.substring(header.length());
	}

	/**
	 * Executes the specified command and gets the future using a command ID.
	 * 
	 * @param command
	 *            the command to execute
	 * @param responseHeader
	 *            the response header to get the accurate first response
	 * @param checker
	 *            the notification checker to detect the end
	 * @return the future to wait for the end of the execution
	 * @throws NullPointerException
	 *             if the specified command, responseHeader or checker is null
	 * @throws ConnectorException
	 *             if executing the command failed
	 */
	@SuppressWarnings("rawtypes")
	public final Future waitForEndWithId(final String command,
			final String responseHeader, final NotificationChecker checker)
			throws ConnectorException {
		ConnectorUtils.checkNotNull("command", command);
		ConnectorUtils.checkNotNull("responseHeader", responseHeader);
		ConnectorUtils.checkNotNull("responseHeader", checker);
		final String header = "#" + _commandCount.getAndIncrement() + " ";
		final NotificationChecker wrappedChecker = new NotificationChecker() {
			public boolean isTarget(String message) {
				if (checker.isTarget(message)) {
					return true;
				}
				return message.startsWith(header + "ERROR ");
			}
		};
		final Future<String> future = execute(header + command, wrappedChecker,
				true, false);
		return new Future<String>() {
			public boolean isDone() {
				return future.isDone();
			}

			public boolean isCancelled() {
				return future.isCancelled();
			}

			public String get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException,
					TimeoutException {
				return removeId(future.get(timeout, unit));
			}

			public String get() throws InterruptedException, ExecutionException {
				return removeId(future.get());
			}

			private String removeId(String message) {
				if (message.startsWith(header)) {
					return message.substring(header.length());
				}
				return message;
			}

			public boolean cancel(boolean mayInterruptIfRunning) {
				return future.cancel(mayInterruptIfRunning);
			}
		};
	}

	/**
	 * Executes the specified command and waits for the response without
	 * timeout.
	 * 
	 * @param command
	 *            the command to execute
	 * @param responseHeader
	 *            the response header to get the accurate response
	 * @return the response after execution
	 * @throws NullPointerException
	 *             if the specified command or responseHeader is null
	 * @throws ConnectorException
	 *             if executing the command failed
	 */
	public final String executeWithoutTimeout(final String command,
			final String responseHeader) throws ConnectorException {
		ConnectorUtils.checkNotNull("command", command);
		ConnectorUtils.checkNotNull("responseHeader", responseHeader);
		return execute(command, new String[] { responseHeader, "ERROR " },
				true, true);
	}

	/**
	 * Executes the specified command and gets the response.
	 * 
	 * @param command
	 *            the command to execute
	 * @param responseHeader
	 *            the response header to get the accurate response
	 * @return the response after execution
	 * @throws NullPointerException
	 *             if the specified command or responseHeader is null
	 * @throws ConnectorException
	 *             if executing the command failed
	 */
	public final String execute(final String command,
			final String responseHeader) throws ConnectorException {
		ConnectorUtils.checkNotNull("command", command);
		ConnectorUtils.checkNotNull("responseHeader", responseHeader);
		return execute(command, new String[] { responseHeader, "ERROR " }, true);
	}

	/**
	 * Executes the specified command and gets the response.
	 * 
	 * @param command
	 *            the command to execute
	 * @param responseHeaders
	 *            the response headers to get the accurate response
	 * @return the response after execution
	 * @throws NullPointerException
	 *             if the specified command or responseHeader is null
	 * @throws ConnectorException
	 *             if executing the command failed
	 */
	public final String execute(final String command,
			final String[] responseHeaders) throws ConnectorException {
		ConnectorUtils.checkNotNull("command", command);
		ConnectorUtils.checkNotNull("responseHeaders", responseHeaders);
		return execute(command, responseHeaders, true);
	}

	/**
	 * Executes the specified command and gets the response.
	 * 
	 * @param command
	 *            the command to execute
	 * @param responseHeaders
	 *            the response headers to get the accurate response
	 * @param checkAttached
	 *            if true check if this connector is attached
	 * @return the response after execution
	 * @throws NullPointerException
	 *             if the specified command or responseHeader is null
	 * @throws ConnectorException
	 *             if executing the command failed
	 */
	protected final String execute(final String command,
			final String[] responseHeaders, final boolean checkAttached)
			throws ConnectorException {
		return execute(command, responseHeaders, checkAttached, false);
	}

	/**
	 * Executes the specified command and gets the response.
	 * 
	 * @param command
	 *            the command to execute
	 * @param responseHeaders
	 *            the response headers to get the accurate response
	 * @param checkAttached
	 *            if true check if this connector is attached
	 * @param withoutTimeout
	 *            if true it will not be time out
	 * @return the response after execution
	 * @throws NullPointerException
	 *             if the specified command or responseHeader is null
	 * @throws ConnectorException
	 *             if executing the command failed
	 */
	private String execute(final String command,
			final String[] responseHeaders, final boolean checkAttached,
			boolean withoutTimeout) throws ConnectorException {
		final NotificationChecker checker = new NotificationChecker() {
			public boolean isTarget(String message) {
				for (String responseHeader : responseHeaders) {
					if (message.startsWith(responseHeader)) {
						return true;
					}
				}
				return false;
			}
		};
		try {
			return execute(command, checker, checkAttached, withoutTimeout)
					.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ConnectorException("The '" + command
					+ "' command was interrupted.", e);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof NotAttachedException) {
				NotAttachedException cause = (NotAttachedException) e
						.getCause();
				throw new NotAttachedException(cause.getStatus(), cause);
			} else if (e.getCause() instanceof ConnectorException) {
				ConnectorException cause = (ConnectorException) e.getCause();
				throw new ConnectorException(cause.getMessage(), cause);
			}
			throw new ConnectorException("The '" + command
					+ "' command execution failed.", e);
		}
	}

	/**
	 * Executes the specified command and gets the future using a command ID.
	 * 
	 * @param command
	 *            the command to execute
	 * @param responseChecker
	 *            the notification checker to detect the end
	 * @param checkAttached
	 *            if true check if this connector is attached
	 * @return the future to wait for the end of the execution
	 * @throws NullPointerException
	 *             if the specified command or responseChecker is null
	 * @throws ConnectorException
	 *             if executing the command failed
	 */
	private Future<String> execute(final String command,
			final NotificationChecker responseChecker,
			final boolean checkAttached, boolean withoutTimeout)
			throws ConnectorException {
		ConnectorUtils.checkNotNull("command", command);
		ConnectorUtils.checkNotNull("responseChecker", responseChecker);

		if (checkAttached) {
			assureAttached();
		}

		return _commandExecutor.submit(new Callable<String>() {
			public String call() throws Exception {
				final BlockingQueue<String> responses = new LinkedBlockingQueue<String>();

				ConnectorListener listener = new AbstractConnectorListener() {
					public void messageReceived(ConnectorMessageEvent event) {
						String message = event.getMessage();
						
						if (responseChecker.isTarget(message)
								|| message.startsWith("PONG")) {
							responses.add(message);
						}
					}
				};
				addConnectorListener(listener, false);

				fireMessageSent(command);
				sendCommand(command);
				try {
					boolean pinged = false;
					while (true) {
						// to cancel getting responses, you must call
						// Future#cancel(true)
						String response = responses.poll(getCommandTimeout(),
								TimeUnit.MILLISECONDS);
						if (response == null) {
							if (pinged) {
								setStatus(Status.NOT_RUNNING);
								throw new NotAttachedException(
										Status.NOT_RUNNING);
							} else {
								// retry the message again
								fireMessageSent(command);
								sendCommand(command);
								
								pinged = true;
								continue;
							}
						}
						
						return response;
					}
				} finally {
					removeConnectorListener(listener);
				}
			}
		});
	}

	/**
	 * Fires a message sent event.
	 * 
	 * @param message
	 *            the message that triggered the event
	 */
	private void fireMessageSent(final String message) {
		fireMessageEvent(message, false);
	}

	/**
	 * Sends the specified command to the Skype client on the platform dependent
	 * communication layer.
	 * 
	 * @param command
	 *            the command to be executed
	 */
	protected abstract void sendCommand(String command);

	/**
	 * Assures the attached status.
	 * 
	 * @throws ConnectorException
	 *             if this connector is not attached or trying to connect
	 *             failed.
	 */
	private void assureAttached() throws ConnectorException {
		Status attachedStatus = getStatus();
		if (attachedStatus != Status.ATTACHED) {
			attachedStatus = connect();
			if (attachedStatus != Status.ATTACHED) {
				throw new NotAttachedException(attachedStatus);
			}
		}
	}

	/**
	 * Adds the specified listener to this connector.
	 * 
	 * @param listener
	 *            the listener to be added
	 * @throws NullPointerException
	 *             if the specified listener is null
	 * @throws ConnectorException
	 *             if trying to connect failed
	 * @see #removeConnectorListener(ConnectorListener)
	 */
	public final void addConnectorListener(final ConnectorListener listener)
			throws ConnectorException {
		addConnectorListener(listener, true);
	}

	/**
	 * Adds the specified listener to this connector.
	 * 
	 * @param listener
	 *            the listener to be added
	 * @param checkAttached
	 *            if true checks if this connector is attached
	 * @throws NullPointerException
	 *             if the specified listener is null
	 * @throws ConnectorException
	 *             if trying to connect failed
	 * @see #removeConnectorListener(ConnectorListener)
	 */
	public final void addConnectorListener(final ConnectorListener listener,
			final boolean checkAttached) throws ConnectorException {
		addConnectorListener(listener, checkAttached, false);
	}

	/**
	 * Adds the specified listener to this connector.
	 * 
	 * @param listener
	 *            the listener to be added
	 * @param checkAttached
	 *            if true checks if this connector is attached
	 * @param isSynchronous
	 *            if true the listener will be handled synchronously
	 * @throws NullPointerException
	 *             if the specified listener is null
	 * @throws ConnectorException
	 *             if trying to connect failed
	 * @see #removeConnectorListener(ConnectorListener)
	 */
	public final void addConnectorListener(final ConnectorListener listener,
			final boolean checkAttached, final boolean isSynchronous)
			throws ConnectorException {
		ConnectorUtils.checkNotNull("listener", listener);
		if (isSynchronous) {
			_syncListeners.add(listener);
		} else {
			_asyncListeners.add(listener);
		}
		if (checkAttached) {
			assureAttached();
		}
	}

	/**
	 * Removes the specified listener from this connector.
	 * 
	 * @param listener
	 *            the listener to be removed
	 * @throws NullPointerException
	 *             if the specified listener is null
	 * @see #addConnectorListener(ConnectorListener)
	 */
	public final void removeConnectorListener(final ConnectorListener listener) {
		ConnectorUtils.checkNotNull("listener", listener);
		_syncListeners.remove(listener);
		_asyncListeners.remove(listener);
	}

	/**
	 * Fires a message received event.
	 * 
	 * @param message
	 *            the message that triggered the event
	 */
	protected final void fireMessageReceived(final String message) {
		fireMessageEvent(message, true);
	}

	/**
	 * Fires a message event.
	 * 
	 * @param message
	 *            the message that triggered the event
	 * @param isReceived
	 *            the message is a received type or not
	 */
	private void fireMessageEvent(final String message, final boolean isReceived) {
		ConnectorUtils.checkNotNull("message", message);
		_syncSender.execute(new Runnable() {
			public void run() {
				fireMessageEvent(toConnectorListenerArray(_syncListeners),
						message, isReceived);
			}
		});
		_asyncSender.execute(new Runnable() {
			public void run() {
				fireMessageEvent(toConnectorListenerArray(_asyncListeners),
						message, isReceived);
			}
		});
	}

	/**
	 * Fires a message event.
	 * 
	 * @param listenerList
	 *            the event listener list
	 * @param message
	 *            the message that triggered the event
	 * @param isReceived
	 *            the message is a received type or not
	 */
	private void fireMessageEvent(final ConnectorListener[] listeners,
			final String message, final boolean isReceived) {
		ConnectorMessageEvent event = new ConnectorMessageEvent(this, message);
		for (int i = listeners.length - 1; 0 <= i; i--) {
			if (isReceived) {
				listeners[i].messageReceived(event);
			} else {
				listeners[i].messageSent(event);
			}
		}
	}

	/**
	 * Sets the specified property. If the specified value is null, the property
	 * is removed.
	 * 
	 * @param name
	 *            the property name
	 * @param value
	 *            the property value
	 * @throws NullPointerException
	 *             if the specified name is null
	 * @see #getStringProperty(String)
	 */
	public final void setStringProperty(final String name, final String value) {
		ConnectorUtils.checkNotNull("name", name);
		if (value != null) {
			properties.put(name, value);
		} else {
			properties.remove(name);
		}
	}

	/**
	 * Gets the specified property value.
	 * 
	 * @param name
	 *            the property name
	 * @return the property value
	 * @throws NullPointerException
	 *             if the specified name is null
	 * @see #setStringProperty(String, String)
	 */
	public final String getStringProperty(final String name) {
		ConnectorUtils.checkNotNull("name", name);
		return properties.get(name);
	}
}