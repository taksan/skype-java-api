package com.skype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChatListenerMananager implements ChatMessageListener {
	private List<GlobalChatListener> listeners = new ArrayList<GlobalChatListener>();
	private HashMap<Chat, Set<User>> managed = new LinkedHashMap<Chat, Set<User>>();

	@Override
	public void chatMessageReceived(ChatMessage chatMessage) throws SkypeException {
		Chat chat = chatMessage.getChat();
		handleChatUsers(chat, chat.getAllMembers());
	}

	@Override
	public void chatMessageSent(ChatMessage chatMessage) throws SkypeException {
		Chat chat = chatMessage.getChat();
		handleChatUsers(chat, chat.getAllMembers());
	}

	public void addManaged(Chat chat) throws SkypeException {
		addChatToManaging(chat,chat.getAllMembers());
	}	
	
	void handleChatUsers(Chat chat, User[] users) {
		if (!managed.keySet().contains(chat)) {
			fireChatCreated(chat, users);
			addChatToManaging(chat,users);
		}
		else
			fireUserAddedOrLeft(chat, users);
	}

	private void addChatToManaging(Chat chat, User[] users) {
		Set<User> userSet = new LinkedHashSet<User>();
		userSet.addAll(Arrays.asList(users));
		managed.put(chat, userSet);
	}

	private void fireUserAddedOrLeft(Chat chat, User[] users) {
		Set<User> receivedUsers = new LinkedHashSet<User>();
		receivedUsers.addAll(Arrays.asList(users));
		
		Set<User> currentChatUsers = managed.get(chat);
		for (User user : receivedUsers) {
			if (currentChatUsers.contains(user))
				continue;
			fireChatUserAdded(chat, user);
		}
		for (User user : currentChatUsers) {
			if (receivedUsers.contains(user))
				continue;
			fireChatUserLeft(chat, user);
		}
		addChatToManaging(chat, users);
	}

	private void fireChatUserAdded(Chat chat, User user) {
		for (GlobalChatListener listener : listeners) 
			listener.userAdded(chat, user);
	}
	
	private void fireChatUserLeft(Chat chat, User user) {
		for (GlobalChatListener listener : listeners) 
			listener.userLeft(chat, user);
	}

	private void fireChatCreated(Chat chat, User[] users) {
		for (GlobalChatListener listener : listeners) 
			listener.newChatStarted(chat, users);
	}
	
	void addGlobalChatListener(GlobalChatListener listener) {
		listeners.add(listener);
	}

	void removeListener(GlobalChatListener listener) {
		listeners.remove(listener);
	}

}
