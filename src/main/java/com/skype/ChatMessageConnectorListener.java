package com.skype;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.ConnectorMessageEvent;

final class ChatMessageConnectorListener extends AbstractConnectorListener {
	private static final String CHATMESSAGE_API_MSG = "CHATMESSAGE ";
	private static final String READ_MESSAGE_SUFFIX = "READ";
	private static final String RECEIVED_MESSAGE_SUFFIX = "RECEIVED";
	private static final String SENT_MESSAGE_SUFFIX = "SENT";
	String lastReceivedId = "";
	private String lastReceivedReadId = "";

	public void messageReceived(ConnectorMessageEvent event) {
	    String message = event.getMessage();
	    if (message.startsWith(CHATMESSAGE_API_MSG)) {
	        String data = message.substring(CHATMESSAGE_API_MSG.length());
	        String id = data.substring(0, data.indexOf(' '));
	        String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
	        String propertyName = propertyNameAndValue.substring(0, propertyNameAndValue.indexOf(' '));
	        if ("STATUS".equals(propertyName)) {
	            String propertyValue = propertyNameAndValue.substring(propertyNameAndValue.indexOf(' ') + 1);
	            ChatMessageListener[] listeners = Skype.chatMessageListeners.toArray(new ChatMessageListener[0]);
	            ChatMessage chatMessage = ChatMessage.getInstance(id);
	            if (SENT_MESSAGE_SUFFIX.equals(propertyValue)) {
	                fireMessageSent(listeners, chatMessage);
	            } else if (RECEIVED_MESSAGE_SUFFIX.equals(propertyValue)) {
	            	if (lastReceivedReadId.equals(chatMessage.getId())) {
	        			return;
	        		}
	                fireMessageReceived(listeners, chatMessage);
	                lastReceivedId = chatMessage.getId();
	            } else if (READ_MESSAGE_SUFFIX.equals(propertyValue)) {
	            	if (lastReceivedId.equals(chatMessage.getId())) {
	        			return;
	        		}
	            	// sometimes a RECEIVED notification doesnt show up
	                fireMessageReceived(listeners, chatMessage);
	                lastReceivedReadId = chatMessage.getId();
	            }
	        }
	    }
	}

	private void fireMessageSent(ChatMessageListener[] listeners, ChatMessage chatMessage) {
		for (ChatMessageListener listener : listeners) {
		    try {
		        listener.chatMessageSent(chatMessage);
		    } catch (Throwable e) {
		        Skype.handleUncaughtException(e);
		    }
		}
	}

	private void fireMessageReceived(ChatMessageListener[] listeners, ChatMessage chatMessage) {		
		for (ChatMessageListener listener : listeners) {
		    try {
		        listener.chatMessageReceived(chatMessage);
		    } catch (Throwable e) {
		        Skype.handleUncaughtException(e);
		    }
		}
	}
}