package com.skype;

import java.util.ArrayList;
import java.util.List;

import com.skype.ChatMessage.LeaveReason;
import com.skype.ChatMessage.Type;

public class ChatListenerMananager implements ChatMessageListener {
	private List<GlobalChatListener> listeners = new ArrayList<GlobalChatListener>();

	@Override
	public void chatMessageReceived(ChatMessage chatMessage) throws SkypeException {
		determineChatEvent(chatMessage);
	}


	@Override
	public void chatMessageSent(ChatMessage chatMessage) throws SkypeException {
		determineChatEvent(chatMessage);
	}
	
	private synchronized void determineChatEvent(ChatMessage chatMessage) throws SkypeException {
		Chat chat = chatMessage.getChat();
		Type type = chatMessage.getType();
		switch(type){
			case ADDEDMEMBERS:
				User[] addedUsers = chatMessage.getAllUsers();
				for (User user : addedUsers) {
					fireChatUserAdded(chat, user);
				}
				break;
			case LEFT:
				LeaveReason leaveReason = chatMessage.getLeaveReason();
				if (leaveReason.equals(LeaveReason.UNSUBSCRIBE)) {
					User user = chatMessage.getSender();
					fireChatUserLeft(chat, user);
				}
				break;
			default:
				// do nothing
		}
	}

	private void fireChatUserAdded(Chat chat, User user) {
		for (GlobalChatListener listener : listeners) 
			listener.userAdded(chat, user);
	}
	
	private void fireChatUserLeft(Chat chat, User user) {
		for (GlobalChatListener listener : listeners) 
			listener.userLeft(chat, user);
	}
	
	void addGlobalChatListener(GlobalChatListener listener) {
		listeners.add(listener);
	}

	void removeListener(GlobalChatListener listener) {
		listeners.remove(listener);
	}

}
