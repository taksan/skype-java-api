package com.skype;

public interface GlobalChatListener {
	void newChatStarted(Chat chat, User[] users);
	void userLeft(Chat chat, User user);
	void userAdded(Chat chat, User user);
}
