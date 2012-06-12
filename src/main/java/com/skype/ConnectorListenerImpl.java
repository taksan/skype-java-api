package com.skype;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.ConnectorMessageEvent;

final class ConnectorListenerImpl extends
		AbstractConnectorListener {
	public void messageReceived(ConnectorMessageEvent event) {
		String message = event.getMessage();
		if (message.startsWith("CALL ")) {
			String data = message.substring("CALL ".length());
			String id = data.substring(0, data.indexOf(' '));
			String propertyNameAndValue = data
					.substring(data.indexOf(' ') + 1);
			String propertyName = propertyNameAndValue.substring(0,
					propertyNameAndValue.indexOf(' '));
			if ("STATUS".equals(propertyName)) {
				String propertyValue = propertyNameAndValue
						.substring(propertyNameAndValue.indexOf(' ') + 1);
				Call.Status status = Call.Status.valueOf(propertyValue);
				Call call = Call.getInstance(id);
				EXIT: if (status == Call.Status.ROUTING || status == Call.Status.RINGING) {
					synchronized (call) {
						if (call.isCallListenerEventFired()) {
							break EXIT;
						}
						call.setCallListenerEventFired(true);
						CallListener[] listeners = Skype.callListeners
								.toArray(new CallListener[0]);
						try {
							switch (call.getType()) {
							case OUTGOING_P2P:
							case OUTGOING_PSTN:
								for (CallListener listener : listeners) {
									try {
										listener.callMaked(call);
									} catch (Throwable e) {
										Skype.handleUncaughtException(e);
									}
								}
								break;
							case INCOMING_P2P:
							case INCOMING_PSTN:
								for (CallListener listener : listeners) {
									try {
										listener.callReceived(call);
									} catch (Throwable e) {
										Skype.handleUncaughtException(e);
									}
								}
								break;
							default:
								// Should an exception be thrown?
								break;
							}
						} catch (Throwable e) {
							Skype.handleUncaughtException(e);
						}
					}
				}
				call.fireStatusChanged(status);
			}
		}
	}
}