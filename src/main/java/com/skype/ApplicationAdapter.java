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
 * Use this class to implement event triggers for AP2AP connections.
 * @author Koji Hisano
 */
public class ApplicationAdapter implements ApplicationListener {
    /**
    * Implement this method to get triggered on AP2AP connected events.
    * @param stream the stream that triggered the event.
    * @throws SkypeException when connection is gone bad.
    */
    public void connected(final Stream stream) throws SkypeException {
    }

    /**
     * Implement this method to get triggered on AP2AP disconnected events.
     * @param stream the stream that triggered the event.
     * @throws SkypeException when connection is gone bad.
     */
    public void disconnected(final Stream stream) throws SkypeException {
    }
}
