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
 * This event will be raised when the connector instance has a change in status.
 */
public final class ConnectorStatusEvent extends ConnectorEvent {
    /**
	 * Needed for serialisation.
	 */
    private static final long serialVersionUID = -7285732323922562464L;
	
	/**
	 * The new status that caused this event.
	 */
	private final Connector.Status status;

	/**
	 * Constructor which sets the connector as source and the new status.
	 * @param source The connector that caused the change.
	 * @param newStatus The new status.
	 */
    ConnectorStatusEvent(Object source, Connector.Status newStatus) {
        super(source);
        assert newStatus != null;
        this.status = newStatus;
    }
    
    /**
     * Return the new Status.
     * @return Status.
     */
    public Connector.Status getStatus() {
        return status;
    }
}
