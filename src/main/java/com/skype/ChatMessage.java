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
 * Bart Lamot - good javadocs
 ******************************************************************************/
package com.skype;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements Skype CHATMESSAGE object.
 * @see https://developer.skype.com/Docs/ApiDoc/CHATMESSAGE_object
 * Protocol 3. Supersedes the MESSAGE object.
 * @author Koji Hisano
 */
public final class ChatMessage extends SkypeObject {
    /**
     * Collection of ChatMessage objects.
     */
    private static final Map<String, ChatMessage> chatMessages = new HashMap<String, ChatMessage>();
    
    /**
     * Returns the ChatMessage object by the specified id.
     * @param id whose associated ChatMessage object is to be returned.
     * @return ChatMessage object with ID == id.
     */
    static ChatMessage getInstance(final String id) {
        synchronized(chatMessages) {
            if (!chatMessages.containsKey(id)) {
                chatMessages.put(id, new ChatMessage(id));
            }
            return chatMessages.get(id);
        }
    }

    /**
     * Enumeration for type.
     */
	public enum Type {
        SETTOPIC,            //change of chat topic
        SAID,                //IM
        ADDEDMEMBERS,        //invited someone to chat
        SAWMEMBERS,          //chat participant has seen other members
        CREATEDCHATWITH,     //chat to multiple people is created
        LEFT,                //someone left chat; can also be a notification if somebody cannot be added to chat
        POSTEDCONTACTS,      //when one user sends contacts to another. Added in protocol 7.
        GAP_IN_CHAT,         //messages of this type are generated locally, during synchronization, 
                             //when a user enters a chat and it becomes apparent that it is impossible 
                             //to update user’s chat history completely. Chat history is kept only up to maximum of 400 
                             //messages or 2 weeks. When a user has been offline past that limit, GAP_IN_CHAT notification is generated.
        
        SETROLE,             //when a chat member gets promoted or demoted.
        KICKED,              //when a chat member gets kicked.
        KICKBANNED,          //when a chat member gets banned.
        SETOPTIONS,          //when chat options are changed.
        SETPICTURE,          //when a chat member has changed the public chat topic picture. Added in protocol 7.     
        SETGUIDELINES,       //when chat guidelines are changed.
        JOINEDASAPPLICANT,   //when a new user joins the chat.
        EMOTED,              // - result of /me message
        UNKNOWN              //unknown message type, possibly due to connecting to Skype with older protocol.
    }

	/**
	 * Enumeration for STATUS of CHATMESSAGE.
	 */
    public enum Status {
    	/**
    	 * SENDING - message is being sent.
    	 * SENT - message was sent.
    	 * RECEIVED - message has been received.
    	 * READ - message has been read.
    	 */
        SENDING, SENT, RECEIVED, READ;
    }

    /**
     * Enumeration for LeaveReason.
     */
    public enum LeaveReason {
    	/**
    	 * USER_NOT_FOUND - user was not found.
    	 * USER_INCAPABLE - user has an older Skype version and cannot join multichat.
    	 * ADDER_MUST_BE_FRIEND - recipient accepts messages from contacts only and sender is not in his/her contact list.
    	 * ADDED_MUST_BE_AUTHORIZED - recipient accepts messages from authorized users only and sender is not authorized.
    	 * UNSUBSCRIBE - participant left chat.
    	 */
        USER_NOT_FOUND, USER_INCAPABLE, ADDER_MUST_BE_FRIEND, ADDED_MUST_BE_AUTHORIZED, UNSUBSCRIBE;
    }

    /**
     * ID of this CHATMESSAGE.
     */
    private final String id;

    /**
     * Constructor.
     * @param newId The ID of this CHATMESSAGE.
     */
    private ChatMessage(String newId) {
        this.id = newId;
    }

    
    /**
     * Returns the hashcode for this object.
     * In this case it's ID.
     * @return ID.
     */
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * Compare two object to check equalness.
     * @param compared the object to check against.
     * @return true if object ID's are equal.
     */
    public boolean equals(Object compared) {
        if (compared instanceof ChatMessage) {
            ChatMessage comparedChatMessage = (ChatMessage)compared;
            return getId().equals(comparedChatMessage.getId());
        }
        return false;
    }

    /**
     * Return CHATMESSAGE ID.
     * @return ID of this chatmessage.
     */
    public String getId() {
        return id;
    }

    /**
     * Return time when message was sent (UNIX timestamp).
     * @return Date of this chatmessage.
     * @throws SkypeException when connection has gone bad.
     */
    public Date getTime() throws SkypeException {
        return Utils.parseUnixTime(getProperty("TIMESTAMP"));
    }

    /**
     * Return the User who sended this CHATMESSAGE.
     * @return User object of sender.
     * @throws SkypeException when connection has gone bad.
     */
    public User getSender() throws SkypeException {
        return User.getInstance(getSenderId());
    }

    /**
     * Return the handle of the user who has sent this CHATMESSAGE.
     * @return a String with the handle.
     * @throws SkypeException when the connection has gone bad.
     */
    public String getSenderId() throws SkypeException {
        return getProperty("FROM_HANDLE");
    }

    /**
     * Return the displayname of the sender of this CHATMESSAGE.
     * @return a String with the displayname of the sender.
     * @throws SkypeException when the connection has gone bad.
     */
    public String getSenderDisplayName() throws SkypeException {
        return getProperty("FROM_DISPNAME");
    }

    /**
     * Get the type of this CHATMESSAGE.
     * @see Type
     * @return Type of this chatmessage.
     * @throws SkypeException when the connection has gone bad.
     */
    public Type getType() throws SkypeException {
        return Type.valueOf(getProperty("TYPE"));
    }

    /**
     * Get the status of this CHATMESSAGE.
     * @see Status
     * @return Status of this chatmessage.
     * @throws SkypeException when the connection has gone bad.
     */
    public Status getStatus() throws SkypeException {
        return Status.valueOf(Utils.getPropertyWithCommandId("CHATMESSAGE", getId(), "STATUS"));
    }

    /**
     * Get the leave reason.
     * @see LeaveReason
     * @return get the leave reason.
     * @throws SkypeException when the connection has gone bad.
     */
    public LeaveReason getLeaveReason() throws SkypeException {
        return LeaveReason.valueOf(getProperty("LEAVEREASON"));
    }

    /**
     * Get the content of this CHATMESSAGE.
     * @return the content of this chatmessage.
     * @throws SkypeException when the connection has gone bad.
     */
    public String getContent() throws SkypeException {
        return getProperty("BODY");
    }

    /**
     * Sets the content of the chat message to the specified string.
     * @param name  the string that is to be this chat message's content
     * @see #getContent
     * @since Protocol 7 (API version 3.0)
     * @throws SkypeException when the connection has gone bad.
     */
    public void setContent(final String content) throws SkypeException {
        setProperty("BODY", content);
    }

    /**
     * Indicates if the chat message is editable.
     * @return true if the chat message is editable
     * @throws SkypeException when the connection has gone bad.
     */
    public boolean isEditable() throws SkypeException {
        return Boolean.parseBoolean(getProperty("IS_EDITABLE"));
    }

    /**
     * Get the parent CHAT object for this CHATMESSAGE.
     * @see Chat
     * @return parent CHAT object.
     * @throws SkypeException when the connection has gone bad.
     */
    public Chat getChat() throws SkypeException {
        return Chat.getInstance(getProperty("CHATNAME"));
    }

    /**
     * Return all users added to CHAT.
     * @return Array of users.
     * @throws SkypeException when connection has gone bad.
     */
    public User[] getAllUsers() throws SkypeException {
        String value = getProperty("USERS");
        if ("".equals(value)) {
            return new User[0];
        }
        String[] ids = value.split(" ");
        User[] users = new User[ids.length];
        for (int i = 0; i < ids.length; i++) {
            users[i] = User.getInstance(ids[i]);
        }
        return users;
    }

    /**
     * Get CHATMESSAGE property.
     * @param name of the property.
     * @return value of the property.
     * @throws SkypeException when connection has gone bad or property not found.
     */
    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("CHATMESSAGE", getId(), name);
    }

    private void setProperty(String name, String value) throws SkypeException {
        Utils.setProperty("CHATMESSAGE", getId(), name, value);
    }
    // TODO void setSeen()
    // TODO boolean isSeen()
}
