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

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorListener;
import com.skype.connector.ConnectorMessageEvent;

public final class MenuItem {
	public enum Context {
        CHAT, CALL, CONTACT, MYSELF, TOOLS
    }

    private static final Map<String, MenuItem> menuItems = new ConcurrentHashMap<String, MenuItem>();
    private static final AtomicInteger menuItemNumber = new AtomicInteger();
    private static final Object menuItemListenerMutex = new Object();
    private static ConnectorListener menuItemListener;

    static MenuItem getInstance(final String id) {
        return menuItems.get(id);
    }

    static MenuItem addMenuItem(Context context, String caption, String hint, File iconFile, boolean enabled, String targetSkypeId, boolean multipleContactsEnabled) {
        MenuItem menuItem = new MenuItem(context, caption, hint, iconFile, enabled, targetSkypeId, multipleContactsEnabled);
        menuItems.put(menuItem.getId(), menuItem);
        return menuItem;
    }
    
    private final String id = "menuItem" + menuItemNumber.getAndIncrement();
    private final Context context;
    private String caption;
    private String hint;
    private final File iconFile;
    private boolean enabled;
    private final String targetSkypeId;
    private final boolean multipleContactsEnabled;
    private final List<MenuItemListener> menuItemListeners = new CopyOnWriteArrayList<MenuItemListener>();

    MenuItem(final Context context, final String caption, final String hint, final File iconFile, final boolean enabled, final String targetSkypeId, final boolean multipleContactsEnabled) {
        this.context = context;
        this.caption = caption;
        this.hint = hint;
        this.iconFile = iconFile;
        this.enabled = enabled;
        this.targetSkypeId = targetSkypeId;
        this.multipleContactsEnabled = multipleContactsEnabled;
    }
    
    public int hashCode() {
        return getId().hashCode();
    }

    public boolean equals(final Object compared) {
        if (compared instanceof MenuItem) {
            return getId().equals(((MenuItem)compared).getId());
        }
        return false;
    }

    String getId() {
        return id;
    }
    
    public Context getContext() {
        return context;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) throws SkypeException {
        Utils.checkNotNull("caption", caption);
        this.caption = caption;
        setStringProperty("CAPTION", caption);
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) throws SkypeException {
        this.hint = hint;
        if (hint == null) {
            hint = "";
        }
        setStringProperty("HINT", hint);
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) throws SkypeException {
        this.enabled = enabled;
        setProperty("ENABLED", "" + enabled);
    }
    
    private void setStringProperty(String name, String value) throws SkypeException {
        setProperty(name, "\"" + value + "\"");
    }

    private void setProperty(String name, String value) throws SkypeException {
        Utils.setProperty("MENU_ITEM", getId(), name, value);
    }

    public File getIconFile() {
        return iconFile;
    }

    public boolean isMultipleContactsEnabled() {
        return multipleContactsEnabled;
    }

    public String getTargetSkypeId() {
        return targetSkypeId;
    }

    public void addMenuItemListener(MenuItemListener listener) throws SkypeException {
        Utils.checkNotNull("listener", listener);
        menuItemListeners.add(listener);
        synchronized (menuItemListenerMutex) {
            if (menuItemListener == null) {
                menuItemListener = new AbstractConnectorListener() {
                    public void messageReceived(ConnectorMessageEvent event) {
                        String message = event.getMessage();
                        if (message.startsWith("MENU_ITEM ") && message.indexOf(" CLICKED ") != -1) {
                            String notParsed = message.substring("MENU_ITEM ".length());
                            int clickedIndex = notParsed.indexOf(" CLICKED ");
                            String id = notParsed.substring(0, clickedIndex);
                            notParsed = notParsed.substring(clickedIndex + " CLICKED ".length());
                            int contextIndex = notParsed.indexOf("CONTEXT ");
                            String[] skypeIds = Utils.convertToArray(notParsed.substring(0, contextIndex));
                            notParsed = notParsed.substring(contextIndex + "CONTEXT ".length());
                            int spaceIndex = notParsed.indexOf(' ');
                            Context context;
                            String[] contextIds = new String[0];
                            if (spaceIndex == -1) {
                                context = Context.valueOf(notParsed.toUpperCase());
                            } else {
                                context = Context.valueOf(notParsed.substring(0, spaceIndex));
                                notParsed = notParsed.substring(spaceIndex + 1);
                                contextIds = Utils.convertToArray(notParsed.substring("CONTEXT_ID ".length()));
                            }
                            MenuItem menuItem = MenuItem.getInstance(id);
                            if (menuItem != null) {
                                MenuItemListener[] listeners = menuItem.menuItemListeners.toArray(new MenuItemListener[0]);
                                for (MenuItemListener listener : listeners) {
                                    try {
                                        listener.menuItemClicked(new MenuItemClickEvent(MenuItem.this, skypeIds, context, contextIds));
                                    } catch (Throwable e) {
                                        Skype.handleUncaughtException(e);
                                    }
                                }
                            }
                        }
                    }
                };
                try {
                    Connector.getInstance().addConnectorListener(menuItemListener);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
    }

    public void removeMenuItemListener(MenuItemListener listener) {
        Utils.checkNotNull("listener", listener);
        menuItemListeners.remove(listener);
        synchronized (menuItemListenerMutex) {
            boolean isEmpty = true;
            for (MenuItem menuItem: menuItems.values()) {
                isEmpty &= menuItem.menuItemListeners.isEmpty();
            }
            if (isEmpty) {
                Connector.getInstance().removeConnectorListener(menuItemListener);
                menuItemListener = null;
            }
        }
    }

    public void dispose() throws SkypeException {
        try {
            String command = "DELETE MENU_ITEM " + getId();
            String response = Connector.getInstance().execute(command);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }
}
