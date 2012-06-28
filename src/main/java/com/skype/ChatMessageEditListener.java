package com.skype;

import java.util.Date;


public interface ChatMessageEditListener {
	public void messageEdited(ChatMessage editedMessage, Date eventDate, User who);
}
