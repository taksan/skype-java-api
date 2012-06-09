package com.skype;

public interface ChatListener {
	public void userAdded(User user);
	public void userLeft(User user);
}
