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
 * Koji Hisano - initial API, implementation and changed javadoc
 * Bart Lamot - initial javadocs
 ******************************************************************************/
package com.skype;

/**
 * Listener interface for the VoiceMail object.
 * @author Koji Hisano.
 */
public interface VoiceMailListener {
    /**
     * Called when a new voice mail is received.
     * @param receivedVoiceMail the received voice mail
     * @throws SkypeException if a connection is bad
     */
	void voiceMailReceived(VoiceMail receivedVoiceMail) throws SkypeException;
	
	/**
	 * Called when a new voice mail is made.
	 * @param madeVoiceMail the made voice mail
	 * @throws SkypeException if the connection is bad.
	 */
    void voiceMailMade(VoiceMail madeVoiceMail) throws SkypeException;
}
