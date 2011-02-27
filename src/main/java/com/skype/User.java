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
package com.skype;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorListener;
import com.skype.connector.ConnectorMessageEvent;

/**
 * The <code>User</code> class contains the skype user's information.
 * <p>
 * For example, you can show the full name of the 'echo123' user by this code:
 * <pre>System.out.println(new User("echo123").getFullName());</pre>
 * </p>
 */
public class User extends SkypeObject {
    /**
     * Collection of User objects.
     */
    private static final Map<String, User> users = new HashMap<String, User>();
    
    private static Object propertyChangeListenerMutex = new Object();
    private static ConnectorListener propertyChangeListener;
    
    /** Identifies the status property. */
    public static final String STATUS_PROPERTY = "status";
    /** Identifies the mood message property. */
    public static final String MOOD_TEXT_PROPERTY = "moodText";

    /**
     * Returns the User object by the specified id.
     * @param id whose associated User object is to be returned.
     * @return User object with ID == id.
     */
    public static User getInstance(final String id) {
        synchronized(users) {
            if (!users.containsKey(id)) {
                users.put(id, new User(id));
            }
            return users.get(id);
        }
    }
    
    /**
     * Returns the Friend object by the specified id.
     * @param id whose associated Friend object is to be returned.
     * @return Friend object with ID == id.
     */
    static Friend getFriendInstance(String id) {
        synchronized(users) {
            if (!users.containsKey(id)) {
                Friend friend = new Friend(id);
                users.put(id, friend);
                return friend;
            } else {
                User user = users.get(id);
                if (user instanceof Friend) {
                    return (Friend)user;
                } else {
                    Friend friend = new Friend(id);
                    friend.copyFrom(user);
                    users.put(id, friend);
                    return friend;
                }
            }
        }
    }

    /**
     * The <code>Status</code> enum contains the online status constants of the skype user.
     * @see User#getOnlineStatus()
     */
    public enum Status {
        /**
         * The <code>UNKNOWN</code> constant indicates the skype user status is unknown.
         */
        UNKNOWN,
        /**
         * The <code>OFFLINE</code> constant indicates the skype user is offline.
         */
        OFFLINE,
        /**
         * The <code>ONLINE</code> constant indicates the skype user is online.
         */
        ONLINE,
        /**
         * The <code>AWAY</code> constant indicates the skype user is away.
         */
        AWAY,
        /**
         * The <code>NA</code> constant indicates the skype user is not available.
         */
        NA,
        /**
         * The <code>DND</code> constant indicates the skype user is in do not disturb mode.
         */
        DND,
        /**
         * The <code>SKYPEOUT</code> constant indicates the skype user is in SkypeOut mode.
         */
        SKYPEOUT,
        /**
         * The <code>SKYPEME</code> constant indicates the skype user is in SkypeMe mode.
         */
        SKYPEME,
    }

    /**
     * The <code>Sex</code> enum contains the sex constants of the skype user.
     * @see User#getSex()
     */
    public enum Sex {
        /**
         * The <code>UNKNOWN</code> constant indicates the sex of the skype user is unknown.
         */
        UNKNOWN,
        /**
         * The <code>MALE</code> constant indicates the skype user is male.
         */
        MALE,
        /**
         * The <code>FEMALE</code> constant indicates the skype user is female.
         */
        FEMALE;
    }
    
    /**
     * The <code>BuddyStatus</code> enum contains the buddy status of the skype user.
     */
    public enum BuddyStatus {
        /**
         * The <code>NEVER_BEEN</code> constant indicates the skype user has never been in contact list.
         */
        NEVER_BEEN,
        /**
         * The <code>DELETED</code> constant indicates the skype user is deleted from contact list.
         */
        DELETED,
        /**
         * The <code>PENDING</code> constant indicates the skype user is pending authorisation.
         */
        PENDING,
        /**
         * The <code>ADDED</code> constant indicates the skype user is added to contact list.
         */
        ADDED
    }

    /** ID of this User. */
    private String id;
    private PropertyChangeSupport listeners = new PropertyChangeSupport(this);

    /**
     * Constructor.
     * @param newId The USER ID.
     */
    User(String newId) {
        this.id = newId;
    }

    /**
     * Overridden to provide ID as hashcode.
     * @return ID.
     */
    public final int hashCode() {
        return getId().hashCode();
    }

    /**
     * Overridden to compare User obejct based on ID.
     * @param compared the User to compare to.
     * @return true if ID's are equal.
     */
    public final boolean equals(Object compared) {
        if (compared instanceof User) {
            User comparedUser = (User)compared;
            return getId().equals(comparedUser.getId());
        }
        return false;
    }

    /**
     * Provide ID as string representation.
     * @return ID.
     */
    public final String toString() {
        return getId();
    }

    /**
     * Return ID of this User.
     * @return ID.
     */
    public final String getId() {
        return id;
    }

    /**
     * Return full name of this User.
     * @return String with fullname.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getFullName() throws SkypeException {
        return getProperty("FULLNAME");
    }

    /**
     * Return the birthdate of this User.
     * @return Date of birthday.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final Date getBirthDay() throws SkypeException {
        String value = getProperty("BIRTHDAY");
        if ("0".equals(value)) {
            return null;
        } else {
            try {
                return new SimpleDateFormat("yyyyMMdd").parse(value);
            } catch (ParseException e) {
                throw new IllegalStateException("library developer should check Skype specification.");
            }
        }
    }

    /**
     * Return the sex of this User.
     * @return Sex of this User.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final Sex getSex() throws SkypeException {
        return Sex.valueOf((getProperty("SEX")));
    }

    /**
     * Return the online status of this User.
     * @return Status of this User.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    @Deprecated
    public final Status getOnlineStatus() throws SkypeException {
        return getStatus();
    }

    /**
     * Return the online status of this User.
     * @return Status of this User.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final Status getStatus() throws SkypeException {
        return Status.valueOf(getProperty("ONLINESTATUS"));
    }


    /**
     * Return last online time (UNIX timestamp).
     * @return Time of last online.
     * @throws SkypeException when connection has gone bad.
     */
    public Date getLastOnlineTime() throws SkypeException {
        return Utils.parseUnixTime(getProperty("LASTONLINETIMESTAMP"));
    }

    /**
     * Return the native language of this User.
     * @return String with native language.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    @Deprecated
    public final String getLauguage() throws SkypeException {
        return getLanguage();
    }

    /**
     * Return the native language of this User.
     * @return String with native language.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getLanguage() throws SkypeException {
        final String value = getProperty("LANGUAGE");
        if ("".equals(value)) {
            return "";
        }
        return value.substring(value.indexOf(' ') + 1);
    }

    /**
     * Return the native language by ISO code of this User.
     * @return String with native language.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getLanguageByISOCode() throws SkypeException {
        final String value = getProperty("LANGUAGE");
        if ("".equals(value)) {
            return "";
        }
        return value.substring(0, value.indexOf(' '));
    }

    /**
     * Return the country the User is based.
     * @return String with country.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getCountry() throws SkypeException {
        final String value = getProperty("COUNTRY");
        if ("".equals(value)) {
            return "";
        }
        return value.substring(value.indexOf(' ') + 1);
    }

    /**
     * Return the country by ISO code the User is based.
     * @return String with country.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getCountryByISOCode() throws SkypeException {
        final String value = getProperty("COUNTRY");
        if ("".equals(value)) {
            return "";
        }
        return value.substring(0, value.indexOf(' '));
    }

    /**
     * Return the province the user is based.
     * @return String with the province the user is based.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getProvince() throws SkypeException {
        return getProperty("PROVINCE");
    }

    /**
     * Return the city this User is based in.
     * @return String with the city name the User is based in.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getCity() throws SkypeException {
        return getProperty("CITY");
    }

    /**
     * Return the home phone number that is in the User profile.
     * @return String with Home phone number.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    @Deprecated
    public final String getHomePhone() throws SkypeException {
        return getHomePhoneNumber();
    }

    /**
     * Return the home phone number that is in the User profile.
     * @return String with Home phone number.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getHomePhoneNumber() throws SkypeException {
        return getProperty("PHONE_HOME");
    }

    /**
     * Return the office phone number that is in the User profile.
     * @return String with office phone number.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    @Deprecated
    public final String getOfficePhone() throws SkypeException {
        return getOfficePhoneNumber();
    }

    /**
     * Return the office phone number that is in the User profile.
     * @return String with office phone number.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getOfficePhoneNumber() throws SkypeException {
        return getProperty("PHONE_OFFICE");
    }

    /**
     * Return the mobile phone number of this User.
     * @return String with mobile phone number.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    @Deprecated
    public final String getMobilePhone() throws SkypeException {
        return getMobilePhoneNumber();
    }

    /**
     * Return the mobile phone number of this User.
     * @return String with mobile phone number.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getMobilePhoneNumber() throws SkypeException {
        return getProperty("PHONE_MOBILE");
    }

    /**
     * Return the homepage URL of this User.
     * @return String with URL of homepage.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getHomePageAddress() throws SkypeException {
        return getProperty("HOMEPAGE");
    }

    /**
     * Return extra information User has provided in his/her profile.
     * @return STring with extra info.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    @Deprecated
    public final String getAbout() throws SkypeException {
        return getIntroduction();
    }

    /**
     * Returns introduction User has provided in his/her profile.
     * @return STring with extra info.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getIntroduction() throws SkypeException {
        return getProperty("ABOUT");
    }

    /**
     * Return the mood message of this user.
     * @return the mood message of this user.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public String getMoodMessage() throws SkypeException {
        return getProperty("MOOD_TEXT");
    }

    /**
     * Gets the speed dial of this user.
     * @return the speed dial of this user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setSpeedDial(String)
     */
    public String getSpeedDial() throws SkypeException {
        return getProperty("SPEEDDIAL");
    }

    /**
     * Sets the speed dial of this user.
     * @param newValue the speed dial of this user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getSpeedDial()
     */
    public void getSpeedDial(final String newValue) throws SkypeException {
        setProperty("SPEEDDIAL", newValue);
    }

    /**
     * Gets the time zone of the current user.
     * @return the time zone of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setTimeZone(int)
     */
    public int getTimeZone() throws SkypeException {
        return Integer.parseInt(getProperty("TIMEZONE"));
    }

    /**
     * Return the displayname of this User.
     * @return String with displayname.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getDisplayName() throws SkypeException {
        return getProperty("DISPLAYNAME");
    }

    /**
     * Check if this User has a Skype client that can do video chats.
     * @return true if User can do videochats.
     * @throws SkypeException when connection to Skype client has gone bad. 
     */
    public final boolean isVideoCapable() throws SkypeException {
        return Boolean.parseBoolean(getProperty("IS_VIDEO_CAPABLE"));
    }
    
    /**
     * Returns the buddy status of this user.
     * @return Buddy status of this user
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final BuddyStatus getBuddyStatus() throws SkypeException {
        return BuddyStatus.values()[Integer.parseInt(getProperty("BUDDYSTATUS"))];
    }

    final void askForAuthorization(String messageForAuthorization) throws SkypeException {
        try {
            String command = "SET " + "USER" + " " + getId() + " " + "BUDDYSTATUS" + " " + (BuddyStatus.PENDING.ordinal() + " " + messageForAuthorization);
            String responseHeader = "USER" + " " + getId() + " " + "BUDDYSTATUS";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }
    
    final void removeFromContactList() throws SkypeException {
        setProperty("BUDDYSTATUS", "" + BuddyStatus.DELETED.ordinal());
    }
    
    /**
     * Check if this User is authorized in your contactlist.
     * @return true if User is authorized.
     * @throws SkypeException when connection to Skype client has gone bad. 
     */
    public final boolean isAuthorized() throws SkypeException {
        return Boolean.parseBoolean(getProperty("ISAUTHORIZED"));
    }

    /**
     * Set this user being authorized, or not in your contactlist.
     * @param on true if user will be authorized.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final void setAuthorized(boolean on) throws SkypeException {
        setProperty("ISAUTHORIZED", on);
    }

    /**
     * Check if this User is blocked in your contactlist.
     * @return true if User is blocked.
     * @throws SkypeException when connection to Skype client has gone bad. 
     */
    public final boolean isBlocked() throws SkypeException {
        return Boolean.parseBoolean(getProperty("ISBLOCKED"));
    }
    
    /**
     * Indicates whether the current user can leave voice mails to this user.
     * @return <code>true</code> if the current user can leave voice mails; <code>false</code> otherwise.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public final boolean canLeaveVoiceMail() throws SkypeException {
        return Boolean.parseBoolean(getProperty("CAN_LEAVE_VM"));
    }

    /**
     * Indicates whether the current user is forwarding calls.
     * @return <code>true</code> if the current user is forwarding calls; <code>false</code> otherwise.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public final boolean isForwardingCalls() throws SkypeException {
        return Boolean.parseBoolean(getProperty("IS_CF_ACTIVE"));
    }

    /**
     * Set this user being blocked, or not in your contactlist.
     * @param on true if user will be blocked.
     * @throws SkypeException when connection to Skype client has gone bad. 
     */
    public final void setBlocked(boolean on) throws SkypeException {
        setProperty("ISBLOCKED", on);
    }

    /**
     * Gets the avatar of this user.
     * @return the avatar image of this user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @since Protocol 7
     */
    public BufferedImage getAvatar() throws SkypeException {
        try {
            final File file = Utils.createTempraryFile("get_avator_", "jpg");
            final String command = "GET USER " + getId() + " AVATAR 1 " + file.getAbsolutePath();
            final String responseHeader = "USER " + getId() + " AVATAR 1 ";
            final String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
            final BufferedImage image = ImageIO.read(file);
            file.delete();
            return image;
        } catch(ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        } catch(IOException e) {
            return null;
        }
    }
    
    /**
     * Method used by other methods to retrieve a property value from Skype client.
     * @param name name of the property.
     * @return value of the property.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("USER", getId(), name);
    }

    private void setProperty(String name, boolean newValue) throws SkypeException {
        setProperty(name, ("" + newValue).toUpperCase());
    }
    
    private void setProperty(String name, String newValue) throws SkypeException {
        Utils.setProperty("USER", getId(), name, newValue);
    }

    /**
     * Start a call to this User.
     * @return new Call object.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final Call call() throws SkypeException {
        return Skype.call(getId());
    }

    /**
     * Start a chat to this User.
     * @return new Chat object.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final Chat chat() throws SkypeException {
        return Skype.chat(getId());
    }

    /**
     * Send this User a chatMessage.
     * @param message The message to send.
     * @return the new chatMessage object.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final ChatMessage send(String message) throws SkypeException {
        return Skype.chat(getId()).send(message);
    }

    /**
     * Leave a voicemail for this User.
     * @return new VoiceMail object.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final VoiceMail voiceMail() throws SkypeException {
        return Skype.voiceMail(getId());
    }

    /**
     * Set a displayname for this User.
     * @param newValue the new name to set.
     * @throws SkypeException  when connection to Skype client has gone bad.
     */
    public final void setDisplayName(String newValue) throws SkypeException {
        Utils.setProperty("USER", getId(), "DISPLAYNAME", newValue);
    }

    /**
     * Search for all chatMessages to and from this User.
     * @return array of Chatmessages found.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final ChatMessage[] getAllChatMessages() throws SkypeException {
        String[] ids = getHistory("CHATMESSAGES");
        ChatMessage[] messages = new ChatMessage[ids.length];
        for (int i = 0; i < ids.length; i++) {
            messages[i] = ChatMessage.getInstance(ids[i]);
        }
        List<ChatMessage> messageList = Arrays.asList(messages);
        Collections.reverse(messageList);
        return messageList.toArray(new ChatMessage[0]);
    }

    /**
     * Search all calls to and from this User.
     * @return an array of found calls.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final Call[] getAllCalls() throws SkypeException {
        String[] ids = getHistory("CALLS");
        Call[] calls = new Call[ids.length];
        for (int i = 0; i < ids.length; i++) {
            calls[i] = Call.getInstance(ids[i]);
        }
        return calls;
    }

    /**
     * Search the history with this user.
     * @param type Specify which history to search for.
     * @return an String array with found events.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    private String[] getHistory(String type) throws SkypeException {
        try {
            String responseHeader = type + " ";
            String response = Connector.getInstance().execute("SEARCH " + type + " " + getId(), responseHeader);
            Utils.checkError(response);
            String data = response.substring(responseHeader.length());
            return Utils.convertToArray(data);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Remove this User from the list of watchable Users.
     */
    final void dispose() {
        users.remove(getId());
    }
    
    private void firePropertyChanged(String propertyName, Object oldValue, Object newValue) {
        listeners.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    /**
     * Adds a PropertyChangeListener to this user.
     * <p>
     * The listener is registered for all bound properties of this user, including the following:
     * <ul>
     *    <li>this user's status ("status")</li>
     *    <li>this user's mood message ("moodMessage")</li>
     * </ul>
     * </p><p>
     * If listener is null, no exception is thrown and no action is performed.
     * </p>
     * @param listener the PropertyChangeListener to be added
     * @see #removePropertyChangeListener(PropertyChangeListener)
     */
    public final void addPropertyChangeListener(PropertyChangeListener listener) throws SkypeException {
        synchronized (propertyChangeListenerMutex) {
            if (propertyChangeListener == null) {
                ConnectorListener connectorListener = new AbstractConnectorListener() {
                    @Override
                    public void messageReceived(ConnectorMessageEvent event) {
                        String message = event.getMessage();
                        if (message.startsWith("USER ")) {
                            String data = message.substring("USER ".length());
                            String skypeId = data.substring(0, data.indexOf(' '));
                            data = data.substring(data.indexOf(' ') + 1);
                            String propertyName = data.substring(0, data.indexOf(' '));
                            String propertyValue = data.substring(data.indexOf(' ') + 1);
                            if (propertyName.equals("ONLINESTATUS")) {
                                User.getInstance(skypeId).firePropertyChanged(STATUS_PROPERTY, null, Status.valueOf(propertyValue));
                            } else if (propertyName.equals("MOOD_TEXT")) {
                                User.getInstance(skypeId).firePropertyChanged(MOOD_TEXT_PROPERTY, null, propertyValue);
                            }
                        }
                    }
                };
                try {
                    Connector.getInstance().addConnectorListener(connectorListener);
                    propertyChangeListener = connectorListener;
                } catch(ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
        listeners.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes the PropertyChangeListener from this user.
     * <p>
     * If listener is null, no exception is thrown and no action is performed.
     * </p>
     * @param listener the PropertyChangeListener to be removed
     * @see #addPropertyChangeListener(PropertyChangeListener)
     */
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }
}
