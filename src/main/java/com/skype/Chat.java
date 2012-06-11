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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

/**
 * object representing Skype CHAT object.
 * @see https://developer.skype.com/Docs/ApiDoc/CHAT_object 
 * @author Koji Hisano.
 */
public final class Chat extends SkypeObject {
    /**
     * Collection of Chat objects.
     */
    private static final Map<String, Chat> chats = new HashMap<String, Chat>();
    
    /**
     * Returns the Chat object by the specified id.
     * @param id whose associated Chat object is to be returned.
     * @return Chat object with ID == id.
     */
    static Chat getInstance(final String id) {
        synchronized(chats) {
            if (!chats.containsKey(id)) {
                Chat chat = new Chat(id);
				chats.put(id, chat);
            }
            return chats.get(id);
        }
    }

	private final class ChatInstanceListener implements GlobalChatListener {
		@Override
		public void userLeft(Chat chat, User user) {
			if (chat.equals(Chat.this)) {
				for (ChatListener listener : instanceChatListeners) {
					listener.userLeft(user);
				}
			}
		}

		@Override
		public void userAdded(Chat chat, User user) {
			if (chat.equals(Chat.this)) {
				for (ChatListener listener : instanceChatListeners) {
					listener.userAdded(user);
				}
			}
		}

		@Override
		public void newChatStarted(Chat chat, User[] users) {
		}
	}


	/**
	 * Enumeration of the status of CHAT object.
	 */
    public enum Status {
        // TODO examine when LEGACY_DIALOG is used
    	/**
    	 * LEGACY_DIALOG - old style IM
    	 * DIALOG - 1:1 chat.
    	 * MULTI_SUBSCRIBED - participant in chat
    	 * UNSUBSCRIBED - left chat 
    	 */
        DIALOG, LEGACY_DIALOG, MULTI_SUBSCRIBED, UNSUBSCRIBED;
    }

    /**
     * ID of this CHAT object.
     */
    private final String id;

    /**
     * Constructor, please use getChat() instead.
     * @param newId ID of this CHAT.
     */
    private Chat(String newId) {
        assert newId != null;
        this.id = newId;
    }

    
    /**
     * Return the hashcode of this CHAT object.
     * @return ID.
     */
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * Implement a equals check for CHAT objects based on their ID's.
     * @param compared The object to compare to.
     * @return true when objects are equal.
     */
    public boolean equals(Object compared) {
        if (compared instanceof Chat) {
            return getId().equals(((Chat) compared).getId());
        }
        return false;
    }

    /**
     * Return the ID of this CHAT object.
     * @return the ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the topic of this CHAT.
     * @param newValue The new topic.
     * @throws SkypeException when the connection has gone bad.
     */
    public void setTopic(String newValue) throws SkypeException {
        try {
            String command = "ALTER CHAT " + getId() + " SETTOPIC " + newValue;
            String responseHeader = "ALTER CHAT SETTOPIC";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }
    
    /**
     * Set the guidelines of this CHAT.
     * @param newValue The new guidelines.
     * @throws SkypeException when the connection has gone bad.
     */
    public void setGuidelines(String newValue) throws SkypeException {
    	try {
            String command = "ALTER CHAT " + getId() + " SETGUIDELINES " + newValue;
            String responseHeader = "ALTER CHAT SETGUIDELINES";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    /**
     * Add a User to this CHAT.
     * @param addedUser the user to add.
     * @throws SkypeException when connection has gone bad.
     */
    public void addUser(User addedUser) throws SkypeException {
        Utils.checkNotNull("addedUser", addedUser);
        addUsers(new User[] {addedUser});
    }

    /**
     * Add several users to this CHAT.
     * @param addedUsers Users to add.
     * @throws SkypeException when the connection has gone bad.
     */
    public void addUsers(User[] addedUsers) throws SkypeException {
        Utils.checkNotNull("addedUsers", addedUsers);
        try {
            String command = "ALTER CHAT " + getId() + " ADDMEMBERS " + toCommaSeparatedString(addedUsers);
            String responseHeader = "ALTER CHAT ADDMEMBERS";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    /**
     * Leave this CHAT.
     * @throws SkypeException when the connection has gone bad.
     */
    public void leave() throws SkypeException {
        try {
            String command = "ALTER CHAT " + getId() + " LEAVE";
            String responseHeader = "ALTER CHAT LEAVE";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    /**
     * Return all messages posted in this CHAT.
     * @return array of messages.
     * @throws SkypeException when connection has gone bad.
     */
    public ChatMessage[] getAllChatMessages() throws SkypeException {
        try {
            String command = "GET CHAT " + getId() + " CHATMESSAGES";
            String responseHeader = "CHAT " + getId() + " CHATMESSAGES ";
            String response = Connector.getInstance().execute(command, responseHeader);
            if (response.contains("ERROR")) {
            	throw new SkypeException("Error when issuing command: " + command + " - " + response);
            }
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            ChatMessage[] chatMessages = new ChatMessage[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                chatMessages[i] = ChatMessage.getInstance(ids[i]);
            }
            return chatMessages;
        } catch (ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
    }

    /**
     * Get the most recent chatmessages for this CHAT.
     * @return array of recent chatmessages.
     * @throws SkypeException when conenction is gone bad.
     */
    public ChatMessage[] getRecentChatMessages() throws SkypeException {
        try {
            String command = "GET CHAT " + getId() + " RECENTCHATMESSAGES";
            String responseHeader = "CHAT " + getId() + " RECENTCHATMESSAGES ";
            String response = Connector.getInstance().execute(command, responseHeader);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            ChatMessage[] chatMessages = new ChatMessage[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                chatMessages[i] = ChatMessage.getInstance(ids[i]);
            }
            return chatMessages;
        } catch (ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
    }

    /**
     * Send a message to this CHAT.
     * @param message the message to send.
     * @return the newly created message which has been sent.
     * @throws SkypeException when the connection has gone bad.
     */
    public ChatMessage send(String message) throws SkypeException {
        try {
            String responseHeader = "CHATMESSAGE ";
            String response = Connector.getInstance().executeWithId("CHATMESSAGE " + getId() + " " + message, responseHeader);
            Utils.checkError(response);
            String msgId = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return ChatMessage.getInstance(msgId);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Return the timestamp of this CHAT.
     * @return date of this CHAT.
     * @throws SkypeException when the connection has gone bad.
     */
    public Date getTime() throws SkypeException {
        return Utils.parseUnixTime(getProperty("TIMESTAMP"));
    }

    /**
     * Return user who added the current user to chat.
     * @return User who added us.
     * @throws SkypeException when connection has gone bad.
     */
    public User getAdder() throws SkypeException {
        String adder = getProperty("ADDER");
        if ("".equals(adder)) {
            return null;
        } else {
            return User.getInstance(adder);
        }
    }

    /**
     * Return the status of this CHAT.
     * @return chat status.
     * @throws SkypeException when the connection has gone bad.
     */
    public Status getStatus() throws SkypeException {
        return Status.valueOf(Utils.getPropertyWithCommandId("CHAT", getId(), "STATUS"));
    }

    /**
     * Get the friendly name of this chat.
     * @return friendly name of this chat.
     * @throws SkypeException when the connection has gone bad.
     */
    public String getWindowTitle() throws SkypeException {
        return getProperty("FRIENDLYNAME");
    }

    /** 
     * Return all chatting members on this CHAT.
     * @return array of chatting users.
     * @throws SkypeException when connection has gone bad.
     */
    public User[] getAllPosters() throws SkypeException {
        return getUsersProperty("POSTERS");
    }

    /**
     * Return all users in this CHAT.
     * @return array of members.
     * @throws SkypeException when connection has gone bad.
     */
    public User[] getAllMembers() throws SkypeException {
        return getUsersProperty("MEMBERS");
    }

    // TODO examine what are active members
    /**
     * Return all active members of CHAT.
     * @return array of active Users.
     * @throws SkypeException when connection has gone bad.
     */
    public User[] getAllActiveMembers() throws SkypeException {
        return getUsersProperty("ACTIVEMEMBERS");
    }

    /**
     * Indicates if this chat has been bookmarked.
     * @return true if this chat is bookmarked
     * @throws SkypeException when the connection has gone bad.
     */
    public boolean isBookmarked() throws SkypeException {
        return Boolean.parseBoolean(getProperty("BOOKMARKED"));
    }
    
    List<ChatListener> instanceChatListeners = new ArrayList<ChatListener>();
    public void addListener(ChatListener listener) throws SkypeException
    {
    	if (localListener == null)
			createInstanceChatListenerDelegator();
    	synchronized (instanceChatListeners) {
    		instanceChatListeners.add(listener);
		}
    }
    
    public void removeListener(ChatListener listener) {
    	synchronized (instanceChatListeners) {
    		instanceChatListeners.remove(listener);
		}
    }

	private void createInstanceChatListenerDelegator() throws SkypeException {
		localListener = new ChatInstanceListener();
		Skype.addGlobalChatListener(localListener, this);
	}
    
	private GlobalChatListener localListener;
  

    /**
	 * Return a comma seperated String of Usernames.
	 * @param users The users to put into the String.
	 * @return The comma seperated string.
	 */
	private static String toCommaSeparatedString(User[] users) {
	    StringBuilder builder = new StringBuilder();
	    for (int i = 0; i < users.length; i++) {
	        if (i != 0) {
	            builder.append(", ");
	        }
	        builder.append(users[i].getId());
	    }
	    return builder.toString();
	}


	/**
	 * Get a property of a User who is member in this chat.
	 * @param name Username.
	 * @return Array of user.
	 * @throws SkypeException when connection has gone bad.
	 */
	private User[] getUsersProperty(String name) throws SkypeException {
	    try {
	        String command = "GET CHAT " + getId() + " " + name;
	        String responseHeader = "CHAT " + id + " " + name + " ";
	        String response = Connector.getInstance().execute(command, responseHeader);
	        String data = response.substring(responseHeader.length());
	        if ("".equals(data)) {
	            return new User[0];
	        }
	        String[] ids = data.split(" ");
	        User[] users = new User[ids.length];
	        for (int i = 0; i < ids.length; ++i) {
	            users[i] = User.getInstance(ids[i]);
	        }
	        return users;
	    } catch (ConnectorException ex) {
	        Utils.convertToSkypeException(ex);
	        return null;
	    }
	}


	/**
     * Return a property of this CHAT.
     * @param name propertyname.
     * @return value of the property.
     * @throws SkypeException when the connection has gone bad or property ain't found.
     */
    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("CHAT", getId(), name);
    }
}
