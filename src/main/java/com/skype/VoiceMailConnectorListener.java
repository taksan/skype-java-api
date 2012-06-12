package com.skype;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.ConnectorMessageEvent;

final class VoiceMailConnectorListener extends AbstractConnectorListener {
	public void messageReceived(ConnectorMessageEvent event) {
	    String message = event.getMessage();
	    if (message.startsWith("VOICEMAIL ")) {
	        String data = message.substring("VOICEMAIL ".length());
	        String id = data.substring(0, data.indexOf(' '));
	        String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
	        String propertyName = propertyNameAndValue.substring(0, propertyNameAndValue.indexOf(' '));
	        if ("TYPE".equals(propertyName)) {
	            String propertyValue = propertyNameAndValue.substring(propertyNameAndValue.indexOf(' ') + 1);
	            VoiceMail.Type type = VoiceMail.Type.valueOf(propertyValue);
	            VoiceMail voiceMail = VoiceMail.getInstance(id);
	            VoiceMailListener[] listeners = Skype.voiceMailListeners.toArray(new VoiceMailListener[0]);
	            switch (type) {
	                case OUTGOING:
	                    for (VoiceMailListener listener : listeners) {
	                        try {
	                            listener.voiceMailMade(voiceMail);
	                        } catch (Throwable e) {
	                            Skype.handleUncaughtException(e);
	                        }
	                    }
	                    break;
	                case INCOMING:
	                    for (VoiceMailListener listener : listeners) {
	                        try {
	                            listener.voiceMailReceived(voiceMail);
	                        } catch (Throwable e) {
	                            Skype.handleUncaughtException(e);
	                        }
	                    }
	                    break;
	                case DEFAULT_GREETING:
	                case CUSTOM_GREETING:
	                case UNKNOWN:
	                default:
	                    // do nothing
	                    break;
	            }
	        }
	    }
	}
}