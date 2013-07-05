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
 * Bart Lamot - good javadocs
 ******************************************************************************/
package com.skype;

/**
 * Listener interface for the CALL object.
 * @author Koji Hisano.
 */
public interface CallListener {
    /**
     * Called when a CALL is received.
     * @param receivedCall the received CALL.
     * @throws SkypeException when a connection is gone bad.
     */
    void callReceived(Call receivedCall) throws SkypeException;

    /**
     * Called when a new CALL is started.
     * @param makedCall the new CALL made.
     * @throws SkypeException when the connection is goen bad.
     */
    void callMaked(Call makedCall) throws SkypeException;
}
