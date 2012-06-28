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
 * Bart Lamot - good ideas for API and initial javadoc
 ******************************************************************************/
package com.skype;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorListener;

/**
 * Skype information model (not view) class of Skype4Java.
 * Use this class staticly to do model actions (send messages, SMS messages or calls, etc).
 * @see SkypeClient
 * @author Koji Hisano
 */
public final class Skype {
    /**
     * The library version.
     */
    public static final String LIBRARY_VERSION = "1.0.0.0";

    /** contactList instance. */
    private static ContactList contactList;
    
    /** Profile instance for this Skype session. */
    private static Profile profile;

    /** chatMessageListener lock. */
    private static Object chatMessageListenerMutex = new Object();
    
    /** chatMessageEditListener lock. */
    private static Object chatMessageEditListenerMutext = new Object();
    
    /** CHATMESSAGE listener. */
    private static ChatMessageConnectorListener chatMessageListener;
    /** Collection of listeners. */
    static List<ChatMessageListener> chatMessageListeners = new CopyOnWriteArrayList<ChatMessageListener>();

    /** callListener lock object. */
    private static Object callListenerMutex = new Object();
    /** CALL listener. */
    private static ConnectorListener callListener;
    /** Collection of all CALL listeners. */
    static List<CallListener> callListeners = new CopyOnWriteArrayList<CallListener>();

    /** voiceMailListener lock object. */
    private static Object voiceMailListenerMutex = new Object();
    /** VOICEMAIL listener. */
    private static ConnectorListener voiceMailListener;
    /** Collection of all VOICEMAIL listeners. */
    static List<VoiceMailListener> voiceMailListeners = new CopyOnWriteArrayList<VoiceMailListener>();

    /** User threading lock object. */
    private static Object userThreadFieldMutex = new Object();
    /** User thread. */
    private static Thread userThread;

    /** General exception handler. */
    private static SkypeExceptionHandler defaultExceptionHandler = new SkypeExceptionHandler() {
        /** Print the non caught exceptions. */
    	public void uncaughtExceptionHappened(Throwable e) {
            e.printStackTrace();
        }
    };
    /** refrence to the default exception handler. */
    private static SkypeExceptionHandler exceptionHandler = defaultExceptionHandler;

	private static ChatMessageEditConnectorListener chatMessageEditConnectorListener;

    /**
     * Sets the thread of Skype4Java to "daemon mode" or not.
     * @param on true to set the thread to "daemon mode"
     */
    @Deprecated
    public static void setDeamon(boolean on) {
        setDaemon(on);
    }

    /**
     * Sets the thread of Skype4Java to "daemon mode" or not.
     * @param on true to set the thread to "daemon mode"
     */
    public static void setDaemon(boolean on) {
        synchronized (userThreadFieldMutex) {
            if (!on && userThread == null) {
                userThread = new Thread("SkypeUserThread") {
                    public void run() {
                        Object wait = new Object();
                        synchronized (wait) {
                            try {
                                wait.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    };
                };
                userThread.start();
            } else if (on && userThread != null) {
                userThread.interrupt();
                userThread = null;
            }
        }
    }

    /**
     * Enable debug logging.
     * @param on if true debug logging will be sent to the console.
     * @throws SkypeException when the connection has gone bad.
     */
    public static void setDebug(boolean on) throws SkypeException {
        try {
            getConnectorInstance().setDebug(on);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    /**
     * Return the version of the Skype client (not this API).
     * @return String with version.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static String getVersion() throws SkypeException {
        return Utils.getProperty("SKYPEVERSION");
    }

    /**
     * Check if Skype client is installed on this computer.
     * WARNING, does not work for all platforms yet.
     * @return true if Skype client is installed.
     */
    public static boolean isInstalled() {
        String path = getInstalledPath();
        if(path == null) {
            return false;
        }
        return new File(path).exists();
    }

    /**
     * Find the install path of the Skype client.
     * WARNING, does not work for all platforms yet.
     * @return String with the full path to Skype client.
     */
    public static String getInstalledPath() {
        return getConnectorInstance().getInstalledPath();
    }

    /**
     * Check if Skype client is running.
     * WARNING, does not work for all platforms.
     * @return true if Skype client is running.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static boolean isRunning() throws SkypeException {
        try {
            return getConnectorInstance().isRunning();
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return false;
        }
    }

    /**
     * Search users by a part of id or e-mail.
     * @param keword a part of id or e-mail
     * @return users
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static User[] searchUsers(String keyword) throws SkypeException {
        String command = "SEARCH USERS " + keyword;
        String responseHeader = "USERS ";
        String response;
        try {
            response = getConnectorInstance().executeWithId(command, responseHeader);
        } catch(ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
        Utils.checkError(response);
        String data = response.substring(responseHeader.length());
        String[] ids = Utils.convertToArray(data);
        User[] users = new User[ids.length];
        for(int i = 0; i < ids.length; ++i) {
            users[i] = User.getInstance(ids[i]);
        }
        return users;
    }

    /**
     * Get the contactlist instance of this Skype session.
     * @return contactlist singleton.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static ContactList getContactList() throws SkypeException {
        if (contactList == null) {
            contactList = new ContactList();
        }
        return contactList;
    }

    /**
     * Make a Skype CALL to multiple users.
     * Without using the Skype client dialogs.
     * @param skypeIds The users to call.
     * @return The started Call.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Call call(String... skypeIds) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        return call(Utils.convertToCommaSeparatedString(skypeIds));
    }

    /**
     * Make a Skype CALL to one single Skype user.
     * Without using the Skype client dialogs.
     * @param skypeId The user to call.
     * @return The new call object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Call call(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeId);
        try {
            String responseHeader = "CALL ";
            String response = getConnectorInstance().executeWithId("CALL " + skypeId, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return Call.getInstance(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Start a chat with multiple Skype users.
     * Without using the Skype client dialogs.
     * @param skypeIds The users to start a chat with.
     * @return The new chat object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Chat chat(String[] skypeIds) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        return chat(Utils.convertToCommaSeparatedString(skypeIds));
    }

    /**
     * Start a chat with a single Skype user.
     * Without using the Skype client dialogs.
     * @param skypeId The user to start the with.
     * @return The new chat.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Chat chat(String skypeId) throws SkypeException {
        try {
            String responseHeader = "CHAT ";
            String response = getConnectorInstance().executeWithId("CHAT CREATE " + skypeId, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return Chat.getInstance(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Send a SMS confirmation code.
     * An outgoing SMS message from Skype lists the reply-to number as the user's 
     * Skype ID. It is possible to change the reply-to number to a mobile phone number by
     * registering the number in the Skype client. Skype validates the number and this
     * number then becomes the reply-to number for outgoing SMS messages.
     * @param numbers the cell phone numbers to validate.
     * @return A new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS submitConfirmationCode(String[] numbers) throws SkypeException {
        Utils.checkNotNull("numbers", numbers);
        return submitConfirmationCode(Utils.convertToCommaSeparatedString(numbers));
    }

    /**
     * Send a SMS confirmation code.
     * An outgoing SMS message from Skype lists the reply-to number as the user's 
     * Skype ID. It is possible to change the reply-to number to a mobile phone number by
     * registering the number in the Skype client. Skype validates the number and this
     * number then becomes the reply-to number for outgoing SMS messages.
     * @param number the cell phone numbers to validate.
     * @return A new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS submitConfirmationCode(String number) throws SkypeException {
        SMS message = createSMS(number, SMS.Type.CONFIRMATION_CODE_REQUEST);
        message.send();
        return message;
    }

    /**
     * Send a SMS confirmation code.
     * An outgoing SMS message from Skype lists the reply-to number as the user's 
     * Skype ID. It is possible to change the reply-to number to a mobile phone number by
     * registering the number in the Skype client. Skype validates the number and this
     * number then becomes the reply-to number for outgoing SMS messages.
     * @param numbers the cell phone numbers to validate.
     * @param code the validation code to send.
     * @return A new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS submitConfirmationCode(String[] numbers, String code) throws SkypeException {
        Utils.checkNotNull("numbers", numbers);
        Utils.checkNotNull("code", code);
        return submitConfirmationCode(Utils.convertToCommaSeparatedString(numbers), code);
    }
    
    /**
     * Send a SMS confirmation code.
     * An outgoing SMS message from Skype lists the reply-to number as the user's 
     * Skype ID. It is possible to change the reply-to number to a mobile phone number by
     * registering the number in the Skype client. Skype validates the number and this
     * number then becomes the reply-to number for outgoing SMS messages.
     * @param number the cell phone numbers to validate.
     * @param code the validation code to send.
     * @return A new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS submitConfirmationCode(String number, String code) throws SkypeException {
        Utils.checkNotNull("number", number);
        Utils.checkNotNull("code", code);
        SMS message = createSMS(number, SMS.Type.CONFIRMATION_CODE_REQUEST);
        message.setContent(code);
        message.send();
        return message;
    }

    /**
     * Send an SMS to one or more cell phone numbers.
     * @param numbers the cell phone numbers to send to.
     * @param content the message to send.
     * @return The new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS sendSMS(String[] numbers, String content) throws SkypeException {
        Utils.checkNotNull("numbers", numbers);
        return sendSMS(Utils.convertToCommaSeparatedString(numbers), content);
    }

    /**
     * Send an SMS to one cell phone number.
     * @param number the cell phone numbers to send to.
     * @param content the message to send.
     * @return The new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */    
    public static SMS sendSMS(String number, String content) throws SkypeException {
        Utils.checkNotNull("number", number);
        Utils.checkNotNull("content", content);
        SMS message = createSMS(number, SMS.Type.OUTGOING);
        message.setContent(content);
        message.send();
        return message;
    }

    /**
     * Create a new SMS object to send later using SMS.send .
     * @param number Cell phone number to send it to.
     * @param type The type of SMS message.
     * @return The new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    private static SMS createSMS(String number, SMS.Type type) throws SkypeException {
        try {
            String responseHeader = "SMS ";
            String response = getConnectorInstance().executeWithId("CREATE SMS " + type + " " + number, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return SMS.getInstance(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Find all SMS messages.
     * @return Array of SMS messages.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public SMS[] getAllSMSs() throws SkypeException {
        return getAllSMSs("SMSS");
    }

    /**
     * Find all missed SMS messages.
     * @return Array of SMS messages.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public SMS[] getAllMissedSMSs() throws SkypeException {
        return getAllSMSs("MISSEDSMSS");
    }

    /**
     * Find all SMS message of a certain type.
     * @param type The type to search for.
     * @return Array of found SMS messages.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    private SMS[] getAllSMSs(String type) throws SkypeException {
        try {
            String command = "SEARCH " + type;
            String responseHeader = "SMSS ";
            String response = getConnectorInstance().execute(command, responseHeader);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            SMS[] smss = new SMS[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                smss[i] = SMS.getInstance(ids[i]);
            }
            return smss;
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Leave a voicemail in a other Skype users voicemailbox.
     * @param skypeId The Skype user to leave a voicemail.
     * @return The new Voicemail object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static VoiceMail voiceMail(String skypeId) throws SkypeException {
        try {
            String responseHeader = "VOICEMAIL ";
            String response = getConnectorInstance().executeWithId("VOICEMAIL " + skypeId, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(' ', responseHeader.length()));
            return VoiceMail.getInstance(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Gets the all voice mails.
     * @return The all voice mails
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static VoiceMail[] getAllVoiceMails() throws SkypeException {
        try {
            String command = "SEARCH VOICEMAILS";
            String responseHeader = "VOICEMAILS ";
            String response = getConnectorInstance().execute(command, responseHeader);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            VoiceMail[] voiceMails = new VoiceMail[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                voiceMails[i] = VoiceMail.getInstance(ids[i]);
            }
            return voiceMails;
        } catch (ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
    }

    /**
     * Add an AP2AP capable application.
     * @param name The name of the AP2AP application.
     * @return Application object reference.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Application addApplication(String name) throws SkypeException {
        Utils.checkNotNull("name", name);
        return Application.getInstance(name);
    }

    /**
     * Gets the current audio input device of this Skype.
     * 
     * @return the audio input device name of this Skype, or <code>null</code>
     *         if the device is the default.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #setAudioInputDevice(String)
     */
    public static String getAudioInputDevice() throws SkypeException {
        return convertDefaultDeviceToNull(Utils.getProperty("AUDIO_IN"));
    }

    /**
     * Gets the current audio output device of this Skype.
     * 
     * @return the audio output device name of this Skype, or <code>null</code>
     *         if the device is the default.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #setAudioOutputDevice(String)
     */
    public static String getAudioOutputDevice() throws SkypeException {
        return convertDefaultDeviceToNull(Utils.getProperty("AUDIO_OUT"));
    }

    /**
     * Get the current video input device used by the Skype Client.
     * @return String with the device name or null if there isn't one.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static String getVideoDevice() throws SkypeException {
        return convertDefaultDeviceToNull(Utils.getProperty("VIDEO_IN"));
    }

    /**
     * Return null if the default device is used.
     * @param deviceName Name of the device to check.
     * @return <code>null</code> if device is default else devicename.
     */
    private static String convertDefaultDeviceToNull(String deviceName) {
        if (isDefaultDevice(deviceName)) {
            return null;
        } else {
            return deviceName;
        }
    }

    /**
     * Compare the devicename to the default value.
     * @param deviceName the string to compare.
     * @return true if devicename is equal to defaultname.
     */
    private static boolean isDefaultDevice(String deviceName) {
        return "".equals(deviceName);
    }

    /**
     * Sets the current audio input device of this Skype.
     * 
     * @param deviceName
     *            the audio input device name. A <code>null</code> value means
     *            the default.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #getAudioInputDevice()
     */
    public static void setAudioInputDevice(String deviceName) throws SkypeException {
        Utils.setProperty("AUDIO_IN", convertNullToDefaultDevice(deviceName));
    }

    /**
     * Sets the current audio output device of this Skype.
     * 
     * @param deviceName
     *            the audio output device name. A <code>null</code> value
     *            means the default.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #getAudioOutputDevice()
     */
    public static void setAudioOutputDevice(String deviceName) throws SkypeException {
        Utils.setProperty("AUDIO_OUT", convertNullToDefaultDevice(deviceName));
    }

    /**
     * Set the video device used by the Skype client.
     * @param deviceName name of the device to set.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void setVideoDevice(String deviceName) throws SkypeException {
        Utils.setProperty("VIDEO_IN", convertNullToDefaultDevice(deviceName));
    }

    /**
     * Convert a <code>null</code> to the default device.
     * @param deviceName String to convert.
     * @return Default device string.
     */
    private static String convertNullToDefaultDevice(String deviceName) {
        if (deviceName == null) {
            return "";
        } else {
            return deviceName;
        }
    }

    /**
     * Get the singleton instance of the users profile.
     * @return Profile.
     */
    public static synchronized Profile getProfile() {
        if (profile == null) {
            profile = new Profile();
        }
        return profile;
    }

    /**
     * Gets all the active calls visible on calltabs.
     *
     * @return all the active calls visible on calltabs
     * @throws SkypeException thrown when Skype API is unavailable or getting an Skype API error.
     */
    public static Call[] getAllActiveCalls() throws SkypeException {
        try {
            String command = "SEARCH ACTIVECALLS";
            String responseHeader = "CALLS ";
            String response = getConnectorInstance().execute(command, responseHeader);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            Call[] calls = new Call[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                calls[i] = Call.getInstance(ids[i]);
            }
            return calls;
        } catch (ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
    }

    /**
     * Gets the all chats.
     *
     * @return The all chats
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static Chat[] getAllChats() throws SkypeException {
        return getAllChats("CHATS");
    }

    /**
     * Gets the all chats which are open in the windows.
     *
     * @return The all chats which are open in the windows
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static Chat[] getAllActiveChats() throws SkypeException {
        return getAllChats("ACTIVECHATS");
    }

    /**
     * Gets the all chats which include unread messages
     *
     * @return The all chats which include unread messages
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static Chat[] getAllMissedChats() throws SkypeException {
        return getAllChats("MISSEDCHATS");
    }

    /**
     * Gets the all recent chats in the locally-cached history.
     *
     * @return The all recent chats in the locally-cached history
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static Chat[] getAllRecentChats() throws SkypeException {
        return getAllChats("RECENTCHATS");
    }

    /**
     * Gets the all bookmarked chats.
     *
     * @return The all bookmarked chats
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static Chat[] getAllBookmarkedChats() throws SkypeException {
        return getAllChats("BOOKMARKEDCHATS");
    }

    /**
     * Gets the all chats by the type.
     *
     * @return The all chats by the type
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    private static Chat[] getAllChats(String type) throws SkypeException {
        try {
            String command = "SEARCH " + type;
            String responseHeader = "CHATS ";
            String response = getConnectorInstance().execute(command, responseHeader);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            Chat[] chats = new Chat[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                chats[i] = Chat.getInstance(ids[i]);
            }
            return chats;
        } catch (ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
    }

    /**
     * Clears all call history.
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static void clearCallHistory() throws SkypeException {
        Utils.executeWithErrorCheck("CLEAR CALLHISTORY ALL"); // in doc, there is without "ALL", but only this works (protocol 5)
    }

    /**
     * Clears all chat history.
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static void clearChatHistory() throws SkypeException {
        Utils.executeWithErrorCheck("CLEAR CHATHISTORY");
    }    

    /**
     * Clears all voice mail history.
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static void clearVoiceMailHistory() throws SkypeException {
        Utils.executeWithErrorCheck("CLEAR VOICEMAILHISTORY");
    }    

    /**
     * Return User based on ID.
     * @param id ID of the User.
     * @return The user found.
     */
    public static User getUser(String id) {
        return User.getInstance(id);
    }

    /**
     * Add a listener for CHATMESSAGE events received from the Skype API.
     * @param listener the Listener to add.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #removeChatMessageListener(ChatMessageListener)
     */
    public static void addChatMessageListener(ChatMessageListener listener) throws SkypeException {
        Utils.checkNotNull("listener", listener);
        synchronized (chatMessageListenerMutex) {
            chatMessageListeners.add(listener);
            if (chatMessageListener == null) {
                chatMessageListener = new ChatMessageConnectorListener();
                try {
                    getConnectorInstance().addConnectorListener(chatMessageListener);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
    }
    
    /**
     * Remove a listener for CHATMESSAGE events.
     * If the listener is already removed nothing happens.
     * @param listener The listener to remove.
     * @see #addChatMessageListener(ChatMessageListener)
     */
    public static void removeChatMessageListener(ChatMessageListener listener) {
        Utils.checkNotNull("listener", listener);
        synchronized (chatMessageListenerMutex) {
            chatMessageListeners.remove(listener);
            if (chatMessageListeners.isEmpty()) {
                getConnectorInstance().removeConnectorListener(chatMessageListener);
                chatMessageListener = null;
            }
        }
    }
    
    /**
     * Add a listener for CHATMESSAGE events with EDITED_BY status received from the Skype API.
     * @param listener the Listener to add.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #removeChatMessageEditListener(ChatMessageEditListener)
     */
    public static void addChatMessageEditListener(ChatMessageEditListener listener) throws SkypeException {
    	Utils.checkNotNull("listener", listener);
        synchronized (chatMessageEditListenerMutext) {
            if (chatMessageEditConnectorListener == null) {
            	chatMessageEditConnectorListener = new ChatMessageEditConnectorListener();
                try {
                    getConnectorInstance().addConnectorListener(chatMessageEditConnectorListener);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
            chatMessageEditConnectorListener.addListener(listener);
        }
    }
    
    /**
     * Remove a listener for CHATMESSAGE with status EDITED_BY events.
     * If the listener is already removed nothing happens.
     * @param listener The listener to remove.
     * @see #addChatMessageEditListener(ChatMessageEditListener)
     */
    public static void removeChatMessageEditListener(ChatMessageEditListener listener) throws SkypeException {
    	Utils.checkNotNull("listener", listener);
        synchronized (chatMessageEditListenerMutext) {
            if (chatMessageEditConnectorListener != null) {
            	chatMessageEditConnectorListener.removeListener(listener);
            }
        }
    }    

    /**
     * Add a listener for CALL events received from the Skype API.
     * @see CallListener
     * @param listener the listener to add.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void addCallListener(CallListener listener) throws SkypeException {
        Utils.checkNotNull("listener", listener);
        synchronized (callListenerMutex) {
        	boolean success = false;
        	try {
	            callListeners.add(listener);
	            if (callListener == null) {
	                callListener = new ConnectorListenerImpl();
	                try {
	                    getConnectorInstance().addConnectorListener(callListener);
	                } catch (ConnectorException e) {
	                    Utils.convertToSkypeException(e);
	                }
	            }
	            success = true;
        	}
        	finally {
        		if (!success) {
        			callListeners.remove(listener);
        		}
        	}
        	
        }
    }
    
    public static boolean isCallListenerRegistered(CallListener listener) {
    	return callListeners.contains(listener);
    }

    /**
     * Remove a listener for CALL events.
     * If listener is already removed nothing happens.
     * @param listener The listener to add.
     */
    public static void removeCallListener(CallListener listener) {
        Utils.checkNotNull("listener", listener);
        synchronized (callListenerMutex) {
            callListeners.remove(listener);
            if (callListeners.isEmpty()) {
            	if (callListener != null)
            		getConnectorInstance().removeConnectorListener(callListener);
                callListener = null;
            }
        }
    }

    /**
     * Adds a listener for voice mail events received from the Skype API.
     * @param listener the added listener
     * @throws SkypeException if connection is bad or error is returned
     * @see VoicemaListener
     */
    public static void addVoiceMailListener(VoiceMailListener listener) throws SkypeException {
        Utils.checkNotNull("listener", listener);
        synchronized (voiceMailListenerMutex) {
            voiceMailListeners.add(listener);
            if (voiceMailListener == null) {
                voiceMailListener = new VoiceMailConnectorListener();
                try {
                    getConnectorInstance().addConnectorListener(voiceMailListener);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
    }

    /**
     * Remove a listener for VOICEMAIL events.
     * If listener is already removed nothing happens.
     * @param listener The listener to add.
     */
    public static void removeVoiceMailListener(VoiceMailListener listener) {
        Utils.checkNotNull("listener", listener);
        synchronized (voiceMailListenerMutex) {
            voiceMailListeners.remove(listener);
            if (voiceMailListeners.isEmpty()) {
                getConnectorInstance().removeConnectorListener(voiceMailListener);
                voiceMailListener = null;
            }
        }
    }
    
    static Object chatListenerManagerMutex = new Object();
	static ChatListenerMananager chatListenerManager = null;
	public static void addGlobalChatListener(GlobalChatListener listener) throws SkypeException {
		synchronized (chatListenerManagerMutex ) {
			if (chatListenerManager == null) {
				chatListenerManager = new ChatListenerMananager();
				addChatMessageListener(chatListenerManager);
			}
			chatListenerManager.addGlobalChatListener(listener);
		}
	}
	
	public static void removeGlobalChatListener(GlobalChatListener listener) {
		synchronized (chatListenerManager) {
			if (chatListenerManager == null) return;
			chatListenerManager.addGlobalChatListener(listener);
		}
	}
	
	static void addGlobalChatListener(GlobalChatListener listener, Chat chat) throws SkypeException {
		addGlobalChatListener(listener);
	}

    /**
     * Use another exceptionhandler then the default one.
     * @see SkypeExceptionHandler
     * @param handler the handler to use.
     */
    public static void setSkypeExceptionHandler(SkypeExceptionHandler handler) {
        if (handler == null) {
            handler = defaultExceptionHandler;
        }
        exceptionHandler = handler;
    }

    /**
     * Handle uncaught exceptions in a default way.
     * @param e the uncaught exception.
     */
    static void handleUncaughtException(Throwable e) {
        exceptionHandler.uncaughtExceptionHappened(e);
    }
    
    static void setReplacementConnectorInstance(Connector connector) {
    	replacementConnectorInstance = connector;
    }

    static Connector replacementConnectorInstance = null;

	public static boolean isDebuggingNativeLib;
	private static Connector getConnectorInstance() {
		if (replacementConnectorInstance == null)
			return Connector.getInstance();
		return replacementConnectorInstance;
	}    

    /** 
     * Private constructor.
     * Please use this object staticly.
     */
    private Skype() {
    }

	public static void setDebugNative(boolean b) {
		isDebuggingNativeLib = b;
	}
}
