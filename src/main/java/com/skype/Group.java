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

import java.util.HashMap;
import java.util.Map;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

/**
 * Implementation of the SKYPE GROUP object.
 * The GROUP object enables users to group contacts. There are two types of GROUP ; custom groups and hardwired groups.
 * @see https://developer.skype.com/Docs/ApiDoc/GROUP_object
 * @author Koji Hisano
 */
public final class Group extends SkypeObject {
    /**
     * Collection of Group objects.
     */
    private static final Map<String, Group> groups = new HashMap<String, Group>();
    
    /**
     * Returns the Group object by the specified id.
     * @param id whose associated Group object is to be returned.
     * @return Group object with ID == id.
     */
    static Group getInstance(final String id) {
        synchronized(groups) {
            if (!groups.containsKey(id)) {
                groups.put(id, new Group(id));
            }
            return groups.get(id);
        }
    }

	/**
	 * Enumeration of the type attribute.
	 */
    public enum Type {
    	/**
    	 * ALL_USERS - This group contains all users I know about, including users in my contactlist, users I recently contacted and blocked users.
		 * ALL_FRIENDS - This group contains all contacts in my contactlist (also known as friends).
		 * SKYPE_FRIENDS - This group contains Skype contacts in my contactlist.
		 * SkypeOut_FRIENDS - This group contains SkypeOut contacts in my contactlist.
		 * ONLINE_FRIENDS - This group contains Skype contacts in my contactlist who are online.
		 * UNKNOWN_OR_PENDINGAUTH_FRIENDS - This group contains contacts in my contactlist who have not yet authorized me.
		 * RECENTLY_CONTACTED_USERS - This group contains contacts I have conversed with recently, including non-friends.
		 * USERS_WAITING_MY_AUTHORIZATION - This group contains contacts who are awating my response to an authorisation request, including non-friends.
		 * USERS_AUTHORIZED_BY_ME - This group contains all contacts I have authorised, including non-friends.
		 * USERS_BLOCKED_BY_ME - This group contains all contacts I have blocked, including non-friends.
		 * UNGROUPED_FRIENDS - This group contains all contacts in my contactlist that do not belong to any custom group.
		 * CUSTOM_GROUP - This group type is reserved for user-defined groups. 
		 * SHARED_GROUP - @TODO: check API docs 
		 * PROPOSED_SHARED_GROUP - @TODO: check API docs
    	 */
        ALL_USERS, ALL_FRIENDS, SKYPE_FRIENDS, SKYPEOUT_FRIENDS, ONLINE_FRIENDS, UNKNOWN_OR_PENDINGAUTH_FRIENDS, RECENTLY_CONTACTED_USERS, USERS_WAITING_MY_AUTHORIZATION, USERS_AUTHORIZED_BY_ME, USERS_BLOCKED_BY_ME, UNGROUPED_FRIENDS, CUSTOM_GROUP, SHARED_GROUP, PROPOSED_SHARED_GROUP;
    }

    /**
     * ID of this GROUP.
     */
    private String id;

    /**
     * Constructor.
     * @param newId ID of this GROUP.
     */
    private Group(String newId) {
        this.id = newId;
    }

    /**
     * Return the ID as an hashcode.
     * @return ID of this GROUP.
     */
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * Compare an other GROUP with this one based in their ID's.
     * @param compared the object to compare to.
     * @return true if object ID's are the same.
     */
    public boolean equals(Object compared) {
        if (this == compared) {
            return true;
        }
        if (compared instanceof Group) {
            return getId().equals(((Group) compared).getId());
        }
        return false;
    }

    /**
     * Return the GROUP ID as a string.
     * @return ID as a string.
     */
    public String toString() {
        return getId();
    }

    /**
     * Return the value of ID of this GROUP.
     * @return ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Add friend to this GROUP.
     * @param friend to add.
     * @throws SkypeException when the connection has gone bad.
     */
    public void addFriend(Friend friend) throws SkypeException {
        Utils.executeWithErrorCheck("ALTER GROUP " + getId() + " ADDUSER " + friend.getId());
    }

    /**
     * Add a regular phonenumber (PSTN) to this group.
     * @param pstn the regular phonenumber.
     * @throws SkypeException when connection has gone bad.
     */
    public void addPSTN(String pstn) throws SkypeException {
        Utils.executeWithErrorCheck("ALTER GROUP " + getId() + " ADDUSER " + pstn);
    }

    /**
     * Remove a friend from this GROUP.
     * @param friend The User to remove from this group.
     * @throws SkypeException when connection has gone bad.
     */
    public void removeFriend(Friend friend) throws SkypeException {
        Utils.executeWithErrorCheck("ALTER GROUP " + getId() + " REMOVEUSER " + friend.getId());
    }

    /**
     * Remove a regular phonenumber (PSTN) from this group.
     * @param pstn The number to remove from this group.
     * @throws SkypeException when the connection has gone bad.
     */
    public void removePSTN(String pstn) throws SkypeException {
        Utils.executeWithErrorCheck("ALTER GROUP " + getId() + " REMOVEUSER " + pstn);
    }

    /**
     * changes the display name for a contact.
     * @TODO: move this command to ContactList.java
     * @param friend The User to change this for.
     * @param displayName The new name.
     * @throws SkypeException when connection has gone bad.
     */
    public void changeFriendDisplayName(Friend friend, String displayName) throws SkypeException {
        friend.setDisplayName(displayName);
    }

    /**
     * changes the display name for a contact.
     * @TODO: move this command to ContactList.java
     * @param pstn The pstn to change this for.
     * @param displayName The new name.
     * @throws SkypeException when connection has gone bad.
     */
    public void changePSTNDisplayName(String pstn, String displayName) throws SkypeException {
        Utils.executeWithErrorCheck("SET USER " + pstn + " DISPLAYNAME " + displayName);
    }

    /**
     * Return all authorized users.
     * @return Array of Friends.
     * @throws SkypeException when the connection has gone bad.
     */
    public Friend[] getAllFriends() throws SkypeException {
        String[] ids = Utils.convertToArray(getProperty("USERS"));
        Friend[] friends = new Friend[ids.length];
        for (int i = 0; i < ids.length; i++) {
            friends[i] = Skype.getContactList().getFriend(ids[i]);
        }
        return friends;
    }

    /**
     * Check for any friends.
     * @param checked the friend to check against.
     * @return True if friend is authorized.
     * @throws SkypeException when connection has gone bad.
     */
    public boolean hasFriend(Friend checked) throws SkypeException {
        for (Friend friend : getAllFriends()) {
            if (checked.equals(friend)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if GROUP is visible.
     * @return true if group is visible.
     * @throws SkypeException when connection has gone bad.
     */
    public boolean isVisible() throws SkypeException {
        return Boolean.parseBoolean(getProperty("VISIBLE"));
    }

    /**
     * Check if GROUP is expanded.
     * @return true if group is expanded.
     * @throws SkypeException when the connection has gone bad.
     */
    public boolean isExpanded() throws SkypeException {
        return Boolean.parseBoolean(getProperty("EXPANDED"));
    }

    /**
     * Return the displayname of this GROUP.
     * @return the displayname of this group.
     * @throws SkypeException when the connection has gone bad.
     */
    public String getDisplayName() throws SkypeException {
        return getProperty("DISPLAYNAME");
    }

    /**
     * Set the displayname of this GROUP.
     * @param newValue the new name.
     * @throws SkypeException when the connection has gone bad.
     */
    public void setDisplayName(String newValue) throws SkypeException {
        setProperty("DISPLAYNAME", newValue);
    }

    /**
     * Get the type of this GROUP.
     * @return the group type.
     * @throws SkypeException when the connection has gone bad.
     */
    public Type getType() throws SkypeException {
        return Type.valueOf(getProperty("TYPE"));
    }

    /**
     * Retrieve a property of this GROUP.
     * @param name name of the property.
     * @return the value of this property.
     * @throws SkypeException when the connection has gone bad.
     */
    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("GROUP", getId(), name);
    }

    /** 
     * Set a property value for this GROUP.
     * @param name name of the property.
     * @param newValue value of the property.
     * @throws SkypeException when the connection has gone bad.
     */
    private void setProperty(String name, String newValue) throws SkypeException {
        Utils.setProperty("GROUP", getId(), name, newValue);
    }

    /**
     * Remove this GROUP.
     * @throws SkypeException when the connection has gone bad.
     */
    public void dispose() throws SkypeException {
        try {
            String response = Connector.getInstance().execute("DELETE GROUP " + getId(), "DELETED GROUP ");
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }
}
