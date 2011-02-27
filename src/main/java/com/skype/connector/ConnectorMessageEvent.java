/*******************************************************************************
 * Copyright (c) 2006-2007 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006-2007 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006-2007 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * Skype4Java is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 *
 * If it is possible to cooperate with the publicity of Skype4Java, please add
 * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
 * in your web site or documents.
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype.connector;

/**
 * Event object a connector will use when it fires a message received or sent event.
 */
public final class ConnectorMessageEvent extends ConnectorEvent {
    /**
	 * Needed for serialisation.
	 */
    private static final long serialVersionUID = -8610258526127376241L;
	
	/**
	 * The message that triggered the event.
	 */
	private final String message;

	/**
	 * Constructor with source (connector) and the message.
	 * @param source Connector which threw the event.
	 * @param newMessage The message sent or received.
	 */
    ConnectorMessageEvent(Object source, String newMessage) {
        super(source);
        assert newMessage != null;
        this.message = newMessage;
    }
    
    /**
     * Get the message of this event.
     * @return Message.
     */
    public String getMessage() {
        return message;
    }
}
