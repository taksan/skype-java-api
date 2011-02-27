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

import java.util.EventListener;

/**
 * Connector event listener interface.
 * Implement this interface when writing an event listener for connector classes.
 */
public interface ConnectorListener extends EventListener {
	/**
	 * This will be triggered when a connector has received a message.
	 * @param event the event source.
	 */
    void messageReceived(ConnectorMessageEvent event);
    
    /**
     * This will be triggered when a connector has send a message to a Skype Client.
     * @param event the event source.
     */
    void messageSent(ConnectorMessageEvent event);
    
    /**
     * This method will be triggered when the status of a connector changes.
     * @param event the event source.
     */
    void statusChanged(ConnectorStatusEvent event);
}
