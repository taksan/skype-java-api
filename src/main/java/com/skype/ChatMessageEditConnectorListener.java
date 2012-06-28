package com.skype;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.ConnectorMessageEvent;

public class ChatMessageEditConnectorListener extends AbstractConnectorListener {
	class EditData {
		private Date eventDate;
		private User author;
	}
	final List<ChatMessageEditListener> listeners = new CopyOnWriteArrayList<ChatMessageEditListener>();
	Map<ChatMessage, EditData> edits = new LinkedHashMap<ChatMessage, ChatMessageEditConnectorListener.EditData>();

	public void addListener(ChatMessageEditListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(ChatMessageEditListener listener) {
		listeners.remove(listener);
	}

	
	public void messageReceived(ConnectorMessageEvent event) {
		String message = event.getMessage();
		processMessage(message);
	}

	synchronized void processMessage(String message) {
		String cmd = "CHATMESSAGE ";
		if (!message.startsWith(cmd))
			return;
		String remain = message.substring(cmd.length());
		String messageId = remain.substring(0, remain.indexOf(" "));
		ChatMessage chatMessage = ChatMessage.getInstance(messageId);
		
		remain = remain.substring(messageId.length()+1);
		
		String chatEvent = remain.substring(0, remain.indexOf(" "));
		
		String parameter = remain.substring(chatEvent.length()+1);
		
		if (chatEvent.equals("EDITED_TIMESTAMP")) {
			processTimeStamp(chatMessage, parameter);
			return;
		}
		if (chatEvent.equals("EDITED_BY")) {
			processAuthor(chatMessage, parameter);
			return;
		}
		if (chatEvent.equals("BODY"))
			processBody(chatMessage, parameter);
		
	}

	private void processTimeStamp(ChatMessage chatMessage, String parameter) {
		EditData editData = getEditData(chatMessage);
		editData.eventDate = new Date(Integer.parseInt(parameter));
		edits.put(chatMessage, editData);
	}
	
	private void processAuthor(ChatMessage chatMessage, String parameter) {
		EditData editData = getEditData(chatMessage);
		editData.author = User.getInstance(parameter);
	}

	private EditData getEditData(ChatMessage chatMessage) {
		EditData editData = edits.get(chatMessage);
		if (editData == null) {
			editData = new EditData();
			edits.put(chatMessage, editData);
		}
		return editData;
	}
	
	private void processBody(ChatMessage chatMessage, String parameter) {
		fireEdit(chatMessage);
	}

	private void fireEdit(ChatMessage chatMessage) {
		EditData editData = edits.get(chatMessage);
		if (editData == null)
			return;
		for (ChatMessageEditListener l : listeners) {
			l.chatMessageEdited(chatMessage, editData.eventDate, editData.author);
		}
		edits.remove(chatMessage);
	}


}
