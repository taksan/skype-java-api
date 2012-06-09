package com.skype;

public interface GlobalChatMessageListener extends ChatMessageListener {
	public void newChatStarted(Chat chat, User[] users);
}
