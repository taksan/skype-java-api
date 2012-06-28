package com.skype;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class ChatMessageEditConnectorListenerTest {
	
	@Test
	public void onProcessMessage_ShouldTriggerListenerEventIfEditSequenceIsMet()
	{
		ChatMessageEditConnectorListener subject = new ChatMessageEditConnectorListener();
		final AtomicReference<Date> actualWhen = new AtomicReference<Date>();
		final AtomicReference<User> actualWho = new AtomicReference<User>() ;
		final AtomicReference<ChatMessage> actualEdited = new AtomicReference<ChatMessage>();
		subject.addListener(new ChatMessageEditListener() {
			@Override
			public void chatMessageEdited(ChatMessage editedMessage, Date when, User who) {
				actualWhen.set(when);
				actualWho.set(who);
				actualEdited.set(editedMessage);
			}
		});
		
		subject.processMessage("CHATMESSAGE 1045193 EDITED_TIMESTAMP 1340851521");
		subject.processMessage("CHATMESSAGE 1045193 EDITED_BY anhanga.tinhoso");
		subject.processMessage("CHATMESSAGE 1045192 EDITED_TIMESTAMP 1340851521");
		subject.processMessage("CHATMESSAGE 1045192 EDITED_BY manhoso");
		subject.processMessage("CHATMESSAGE 1045193 BODY dude not good");
		
		Date expected = new Date(1340851521);
		User user = User.getInstance("anhanga.tinhoso");
		assertEquals(expected, actualWhen.get());
		assertEquals(user.getId(), actualWho.get().getId());
		assertEquals("1045193", actualEdited.get().getId());
	}
}