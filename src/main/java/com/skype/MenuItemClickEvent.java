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
 * Contributors: Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

import java.util.EventObject;

import com.skype.MenuItem.Context;

public final class MenuItemClickEvent extends EventObject {
    private static final long serialVersionUID = -1777142015080318057L;

    private final String[] _skypeIds;
    private final Context _context;
    private final String[] _contextIds;

    MenuItemClickEvent(MenuItem menuItem, String[] skypeIds, Context context, String[] contextIds) {
        super(menuItem);
        Utils.checkNotNull("menuItem", menuItem);
        Utils.checkNotNull("skypeIds", skypeIds);
        Utils.checkNotNull("context", context);
        Utils.checkNotNull("contextIds", contextIds);
        _skypeIds = skypeIds;
        _context = context;
        _contextIds = contextIds;
    }

    public MenuItem getMenuItem() {
        return (MenuItem) getSource();
    }

    public String[] getSkypeIds() {
        return _skypeIds;
    }

    public Context getContext() {
        return _context;
    }

    public String[] getContextIds() {
        return _contextIds;
    }
}
