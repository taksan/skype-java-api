package com.skype;

import java.util.Date;


public interface ChatMessageEditListener {
	public void chatMessageEdited(ChatMessage editedMessage, Date eventDate, User who);
}
