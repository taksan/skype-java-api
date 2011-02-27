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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Basis of all Skype objects.
 * @see Call
 * @see Application
 * @see Chat
 * @see ChatMessage
 * @see Group
 * @see SMS
 * @see Stream
 * @see User
 * @see VoiceMail
 */
class SkypeObject {
    /**
     * Map of user data added to this object.
     */
    private Map<String, Object> userDataMap = Collections.synchronizedMap(new HashMap<String, Object>());
    
    /**
     * Returns the user data to which this object maps to the specified by the name.
     * Returns <tt>null</tt> if this object contains no mapping for the name.
     * @see Map#get(Object)
     * @param name name whose associated user data is to be returned.
     * @return the user data to which this object maps the specified name, or <tt>null</tt> if this object contains no mapping for the name.
     */
    public final Object getData(final String name) {
        return userDataMap.get(name);
    }
    
    /**
     * Associates the specified user data with the specified name in this object.
     * @see Map#put(Object, Object)
     * @param name name with which the specified user data is to be associated.
     * @param userData user data to be associated with the specified name.
     */
    public final void setData(final String name, final Object userData) {
        userDataMap.put(name, userData);
    }
    
    /**
     * Copy the contents of another object to this.
     * @param base the Object to copy from.
     */
    void copyFrom(Object base) {
        if (base instanceof SkypeObject) {
            this.userDataMap.putAll(((SkypeObject)base).userDataMap);
        }
    }
}
