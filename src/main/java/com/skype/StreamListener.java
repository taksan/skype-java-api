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
package com.skype;

/**
 * Listener interface for Stream object events.
 * @see Stream
 * @see StreamAdapter
 */
public interface StreamListener {
    /**
     * This method will be fired when a text message is received.
     * @param receivedText the received message.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
	void textReceived(String receivedText) throws SkypeException;
    
	/**
     * This method will be fired when a datagram message is received.
     * @param receivedDatagram the received message.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
	void datagramReceived(String receivedDatagram) throws SkypeException;
}
