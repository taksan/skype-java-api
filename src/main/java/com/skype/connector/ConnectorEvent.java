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

import java.util.Date;
import java.util.EventObject;

/**
 * Event will by raised when a connector has a event.
 */
class ConnectorEvent extends EventObject {
    /**
	 * Needed for all serialization classes. 
	 */
    private static final long serialVersionUID = -4743437008394579910L;
	
	/** Time. */
	private final long time;

	/**
	 * Constructor.
	 * @param source The event source.
	 */
    ConnectorEvent(final Object source) {
        super(source);
        assert source != null;
        time = System.currentTimeMillis();
    }

    /**
     * Get the source Connector.
     * @return Connector.
     */
    public final Connector getConnector() {
        return (Connector)getSource();
    }
    
    /**
     * Get the time of the event.
     * @return Date fo event.
     */
    public final Date getTime() {
        return new Date(time);
    }
}
