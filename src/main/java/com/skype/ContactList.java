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

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

/**
 * This object can be used for all actions normal to a contactlist, like searching users and friends.
 * @author Koji Hisano.
 */
public final class ContactList {
	/**
	 * Constructor.
	 */
    ContactList() {
    }

    /**
     * Get all authorized users.
     * @return array of friends.
     * @throws SkypeException when the connection has gone bad.
     */
    public Friend[] getAllFriends() throws SkypeException {
        try {
            String responseHeader = "USERS ";
            String response = Connector.getInstance().execute("SEARCH FRIENDS", responseHeader);
            Utils.checkError(response);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            Friend[] friends = new Friend[ids.length];
            for (int i = 0; i < ids.length; i++) {
                friends[i] = User.getFriendInstance(ids[i]);
            }
            return friends;
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Get all users waiting for AUTHORIZATION.
     * @return array of users.
     * @throws SkypeException when the connection has gone bad.
     */
    public Friend[] getAllUserWaitingForAuthorization() throws SkypeException {
        try {
            String responseHeader = "USERS ";
            String response = Connector.getInstance().execute("SEARCH USERSWAITINGMYAUTHORIZATION", responseHeader);
            Utils.checkError(response);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            Friend[] users = new Friend[ids.length];
            for (int i = 0; i < ids.length; i++) {
                users[i] = User.getFriendInstance(ids[i]);
            }
            return users;
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }
    
    /**
     * Get the Friend object for one of the authorized users.
     * @param skypeId Skype ID of the friend.
     * @return the friend or null if friend isn't found.
     * @throws SkypeException when a connection has gone bad.
     */
    public Friend getFriend(String skypeId) throws SkypeException {
        Utils.checkNotNull(skypeId, "skypeId");
        for (Friend friend : getAllFriends()) {
            if (skypeId.equals(friend.getId())) {
                return friend;
            }
        }
        return null;
    }


    /**
     * Search for HARDWIRED groups.
     * @return array of found groups or null if groups could not be found.
     * @throws SkypeException when connection has gone bad.
     */
    public Group[] getAllSystemGroups() throws SkypeException {
        return getAllGroups("HARDWIRED");
    }

    /**
     * Search for a group based on it's type.
     * @param type Group type to find.
     * @return group or null if group isn't found.
     * @throws SkypeException when connection has gone bad or type isn't correct, like CUSTOM_GROUP.
     */
    public Group getSystemGroup(Group.Type type) throws SkypeException {
        if (type == Group.Type.CUSTOM_GROUP) {
            throw new IllegalArgumentException("custom type is not supported (use getAllGroups method to resolve)");
        }
        for (Group group : getAllSystemGroups()) {
            if (group.getType() == type) {
                return group;
            }
        }
        return null;
    }

    /**
     * Search for CUSTOM groups.
     * @return Array of found groups.
     * @throws SkypeException when connection has gone bad.
     */
    public Group[] getAllGroups() throws SkypeException {
        return getAllGroups("CUSTOM");
    }

    /**
     * Search for groups based on type.
     * @param type the type to search for.
     * @return Array of groups with type.
     * @throws SkypeException when connection has gone bad.
     */
    private Group[] getAllGroups(String type) throws SkypeException {
        try {
            String responseHeader = "GROUPS ";
            String response = Connector.getInstance().execute("SEARCH GROUPS " + type, responseHeader);
            Utils.checkError(response);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            Group[] groups = new Group[ids.length];
            for (int i = 0; i < ids.length; i++) {
                groups[i] = Group.getInstance(ids[i]);
            }
            return groups;
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Search for group with displayname.
     * @param displayName name of the group.
     * @return found Group or null if none is found.
     * @throws SkypeException when connection has gone bad.
     */
    public Group getGroup(String displayName) throws SkypeException {
        Utils.checkNotNull(displayName, "displayName");
        for (Group group : getAllGroups()) {
            if (displayName.equals(group.getDisplayName())) {
                return group;
            }
        }
        return null;
    }

    /**
     * Add group to contactlist.
     * @param name Name of the group.
     * @return the Group object created.
     * @throws SkypeException when connection has gone bad.
     */
    public Group addGroup(String name) throws SkypeException {
        try {
            String responseHeader = "GROUP ";
            String response = Connector.getInstance().execute("CREATE GROUP " + name, responseHeader).substring(responseHeader.length());
            Utils.checkError(response);
            String id = response.substring(0, response.indexOf(' '));
            return Group.getInstance(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }
    
    /**
     * Add user to contactlist.
     * @param user user to be added.
     * @return Added friend.
     * @throws SkypeException when connection has gone bad.
     */
    public Friend addFriend(User user, String messageForAuthorization) throws SkypeException {
        return addFriend(user.getId(), messageForAuthorization);
    }

    /**
     * Add user to contactlist.
     * @param skypeId skype id of user to be added.
     * @return Added friend.
     * @throws SkypeException when connection has gone bad.
     */
    public Friend addFriend(String skypeId, String messageForAuthorization) throws SkypeException {
        Friend friend = Friend.getFriendInstance(skypeId);
        friend.askForAuthorization(messageForAuthorization);
        return friend;
    }
    
    /**
     * Removes friend from this contact list.
     * @param friend friend to be removed.
     * @throws SkypeException when connection has gone bad.
     */
    public void removeFriend(Friend friend) throws SkypeException {
        friend.removeFromContactList();
    }

    /** 
     * Remove group from contactlist.
     * @param group Group to remove.
     * @throws SkypeException when connection has gone bad.
     */
    public void removeGroup(Group group) throws SkypeException {
        group.dispose();
    }
}
