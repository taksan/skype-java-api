package com.skype;

import java.util.Set;
import java.util.HashSet;
import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorMessageEvent;

final class ChatMessageConnectorListener extends AbstractConnectorListener {
	private static final String CHAT_API_MSG = "CHAT ";
	private static final String ACTIVITY_TIMESTAMP_MSG = "ACTIVITY_TIMESTAMP";
	private static final String CHATMESSAGE_API_MSG = "CHATMESSAGE ";
	private static final String READ_MESSAGE_SUFFIX = "READ";
	private static final String RECEIVED_MESSAGE_SUFFIX = "RECEIVED";
	private static final String SENT_MESSAGE_SUFFIX = "SENT";
	private final Object _isReceivedMutex = new Object();
	private Set<Integer> lastReceivedIds = new HashSet<Integer>();
	private Integer currentReceivedId;

	public void messageReceived(ConnectorMessageEvent event) {
		String message = event.getMessage();
		if (message.startsWith(CHATMESSAGE_API_MSG)) {
			String data = message.substring(CHATMESSAGE_API_MSG.length());
			String id = data.substring(0, data.indexOf(' '));
			String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
			String propertyName = propertyNameAndValue.substring(0, propertyNameAndValue.indexOf(' '));
			if ("STATUS".equals(propertyName)) {
				String propertyValue = propertyNameAndValue.substring(propertyNameAndValue.indexOf(' ') + 1);
				try {
					currentReceivedId = Integer.parseInt(id);
				} catch (NumberFormatException ex) {
					return;
				}
			ChatMessageListener[] listeners = Skype.chatMessageListeners.toArray(new ChatMessageListener[0]);
			ChatMessage chatMessage = ChatMessage.getInstance(currentReceivedId.toString());
			if (SENT_MESSAGE_SUFFIX.equals(propertyValue)) {
				fireMessageSent(listeners, chatMessage);
			} else if (RECEIVED_MESSAGE_SUFFIX.equals(propertyValue)) {
				/* need integers because */
				/* Recieved #lastReceivedId : 16649 len: 5 bytes: [B@1592066 */
				/* Recieved #messageReceived: 16649 len: 5 bytes: [B@145fdb3 */
				synchronized (_isReceivedMutex) {
					if (lastReceivedIds.contains(currentReceivedId)) {
						return;
					}
					lastReceivedIds.add(currentReceivedId);
					_isReceivedMutex.notifyAll();
				}

				fireMessageReceived(listeners, chatMessage);
			}
		}
	} else if (message.startsWith(CHAT_API_MSG) && message.contains(ACTIVITY_TIMESTAMP_MSG)) {
		try {
			Connector.getInstance().getMissedMessages();
		} catch (ConnectorException ex) {
			System.out.println(ex);
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
