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
 * Bart Lamot - good ideas for APIs and javadocs
 ******************************************************************************/
package com.skype;

import java.io.File;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

/**
 * Main view (not model) class of the Skype Java API.
 * Use this class staticly to do view actions (show option dialogs or switch tabs, etc).
 * @see Skype
 * @author Koji Hisano
 */
public final class SkypeClient {
    
	/**
	 * 
	 * This class should be used staticly, so making the constructor private prefents instantiation.
	 */
	private SkypeClient() {
	}
	
	/**
     * Enumeration of the OPTIONS page.
     * Options dialog of Skype client.
     */
    public enum OptionsPage {
        /**
         * GENERAL - general options dialog.
         * PRIVACY - privacy options dialog.
         * NOTIFICATIONS - notifications options dialog.
         * SOUNDALERTS - soundalerts options dialog.
         * SOUNDDEVICES - sound devices options dialog.
         * HOTKEYS - hotkeys options dialog.
         * CONNECTION - connection options dialog.
         * VOICEMAIL - voicemail options dialog.
         * CALLFORWARD - callforward options dialog.
         * VIDEO - video options dialog.
         * ADVANCED - advanced options dialog.
         * 
         */
        GENERAL, PRIVACY, NOTIFICATIONS, SOUNDALERTS, SOUNDDEVICES, HOTKEYS, CONNECTION, VOICEMAIL, CALLFORWARD, VIDEO, ADVANCED;
    }

    /**
     * Enumeration of keypad keys.
     */
    public enum Button {
        /**
         * KEY_0 - 0 on the callpad.
         * KEY_1 - 1 on the callpad.
         * KEY_2 - 2 on the callpad.
         * KEY_3 - 3 on the callpad.
         * KEY_4 - 4 on the callpad.
         * KEY_5 - 5 on the callpad.
         * KEY_6 - 6 on the callpad.
         * KEY_7 - 7 on the callpad.
         * KEY_8 - 8 on the callpad.
         * KEY_9 - 9 on the callpad.
         * KEY_A - A on the callpad.
         * KEY_B - B on the callpad.
         * KEY_C - C on the callpad.
         * KEY_D - D on the callpad.
         * KEY_E - E on the callpad.
         * KEY_F - F on the callpad.
         * KEY_G - G on the callpad.
         * KEY_H - H on the callpad.
         * KEY_I - I on the callpad.
         * KEY_J - J on the callpad.
         * KEY_K - K on the callpad.
         * KEY_L - L on the callpad.
         * KEY_M - M on the callpad.
         * KEY_N - N on the callpad.
         * KEY_O - O on the callpad.
         * KEY_P - P on the callpad.
         * KEY_Q - Q on the callpad.
         * KEY_R - R on the callpad.
         * KEY_S - S on the callpad.
         * KEY_T - T on the callpad.
         * KEY_U - U on the callpad.
         * KEY_V - V on the callpad.
         * KEY_W - W on the callpad.
         * KEY_X - X on the callpad.
         * KEY_Y - Y on the callpad.
         * KEY_Z - Z on the callpad.
         * KEY_SHARP - # on the callpad.
         * KEY_ASTERIX - * on the callpad.
         * KEY_PLUS - + on the callpad.
         * KEY_UP - on the callpad.
         * KEY_DOWN - on the callpad.
         * KEY_YES - yes on the callpad.
         * KEY_NO - no on the callpad.
         * KEY_PAGEUP - on the callpad.
         * KEY_PAGEDOW - on the callpad.
         * KEY_SKYPE - on the callpad.
         */
        KEY_0, KEY_1, KEY_2, KEY_3, KEY_4, KEY_5, KEY_6, KEY_7, KEY_8, KEY_9, KEY_A, KEY_B, KEY_C, KEY_D, KEY_E, KEY_F, KEY_G, KEY_H, KEY_I, KEY_J, KEY_K, KEY_L, KEY_M, KEY_N, KEY_O, KEY_P, KEY_Q, KEY_R, KEY_S, KEY_T, KEY_U, KEY_V, KEY_W, KEY_X, KEY_Y, KEY_Z, KEY_SHARP("#"), KEY_ASTERISK("*"), KEY_PLUS("+"), KEY_UP, KEY_DOWN, KEY_YES, KEY_NO, KEY_PAGEUP, KEY_PAGEDOWN, KEY_SKYPE;

        /** Key instance. */
        private String key;
        
        /** Constructor. */
        private Button() {
        }

        /**
         * Constructor using a String as button reference.
         * @param newKey The button.
         */
        private Button(String newKey) {
            this.key = newKey;
        }

        /** 
         * Get the key. 
         * @return The string.
         * */
        private String getKey() {
            if (key != null) {
                return key;
            } else {
                return name().substring(name().indexOf('_') + 1);
            }
        }
    }

    public enum WindowState {
        NORMAL, MINIMIZED, MAXIMIZED, HIDDEN;
    }

    /**
     * Put focus on the Skype client window, not any Java window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showSkypeWindow() throws SkypeException {
        Utils.executeWithErrorCheck("FOCUS");
    }

    /**
     * Remove focus on Skype client window, not any Java window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void hideSkypeWindow() throws SkypeException {
        Utils.executeWithErrorCheck("MINIMIZE");
    }

    /**
     * Open the "Add friend" window of Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showAddFriendWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN ADDAFRIEND");
    }

    /**
     * Open the "Add friend" window if skypeId exist.
     * @param skypeId the Skype ID to check before opening the window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showAddFriendWindow(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.executeWithErrorCheck("OPEN ADDAFRIEND " + skypeId);
    }

    /**
     * Open a chatwindow to another Skype user.
     * @param skypeId The user to open a chat with.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showChatWindow(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.executeWithErrorCheck("OPEN IM " + skypeId);
    }

    /**
     * Open a chatwindow to another skype user and send a message.
     * @param skypeId The other user to chat with.
     * @param message The message to send.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showChatWindow(String skypeId, String message) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.checkNotNull("message", message);
        Utils.executeWithErrorCheck("OPEN IM " + skypeId + " " + message);
    }

    /**
     * Open File transfer window ten send a file to another Skype user.
     * @param skypeId The user to send the file to.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showFileTransferWindow(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.executeWithErrorCheck("OPEN FILETRANSFER " + skypeId);
    }

    /**
     * Show file transfer windows with a specific folder to send a file to another Skype user.
     * @param skypeId the user to send the file to.
     * @param folder the folder to show in the window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showFileTransferWindow(String skypeId, File folder) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.checkNotNull("folder", folder);
        Utils.executeWithErrorCheck("OPEN FILETRANSFER " + skypeId + " IN " + folder,"OK");
    }

    /**
     * Show the file transfer window to send a file to several other Skype users.
     * @param skypeIds multiple Skype users to send file to.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showFileTransferWindow(String[] skypeIds) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        Utils.executeWithErrorCheck("OPEN FILETRANSFER " + Utils.convertToCommaSeparatedString(skypeIds));
    }

    /**
     * Show file transfer window with a specific Folder to send a file to multiple Skype users.
     * @param skypeIds multiple Skype users to send file to.
     * @param folder the folder to open with the dialog.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showFileTransferWindow(String[] skypeIds, File folder) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        Utils.checkNotNull("folder", folder);
        Utils.executeWithErrorCheck("OPEN FILETRANSFER " + Utils.convertToCommaSeparatedString(skypeIds) + " IN " + folder);
    }

    /**
     * Open the Skype client profile window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showProfileWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN PROFILE");
    }

    /**
     * Open the User Information window with the info on a Skype user.
     * @param skypeId The skype user to show.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showUserInformationWindow(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.executeWithErrorCheck("OPEN USERINFO " + skypeId);
    }

    /**
     * Open the conference window of the Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showConferenceWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN CONFERENCE");
    }

    /**
     * Open the search window of the Skype Client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showSearchWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN SEARCH");
    }

    /**
     * Open the Options window of the Skype Client.
     * @see OptionsPage
     * @param page the page to ope in front.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showOptionsWindow(OptionsPage page) throws SkypeException {
        Utils.executeWithErrorCheck("OPEN OPTIONS " + page.toString().toLowerCase());
    }

    /**
     * Focus the Call history tab of the Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showCallHistoryTab() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN CALLHISTORY");
    }

    /**
     * Focus the Contacts tab of the Skype client window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showContactsTab() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN CONTACTS");
    }

    /**
     * Focus dialpad tab on the Skype client window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showDialPadTab() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN DIALPAD");
    }

    /**
     * Open send contacts window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showSendContactsWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN SENDCONTACTS");
    }

    /**
     * Show blacked users window of the Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showBlockedUsersWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN BLOCKEDUSERS");
    }

    /**
     * Show import contacts window of Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showImportContactsWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN IMPORTCONTACTS");
    }

    /**
     * Show the getting started window of the Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showGettingStartedWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN GETTINGSTARTED");
    }

    /**
     * Show the request authorisation window for a Skype user ID.
     * @param skypeId the User to ask authorisation for.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showRequestAuthorizationWindow(String skypeId) throws SkypeException {
        Utils.executeWithErrorCheck("OPEN AUTHORIZATION " + skypeId);
    }

    /**
     * Press a button in the Skype client window.
     * @see Button
     * @param button The button to press.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void pressButton(Button button) throws SkypeException {
        Utils.executeWithErrorCheck("BTN_PRESSED " + button.getKey());
    }

    /**
     * Release a pressed button in the Skype client window.
     * @see Button
     * @param button the button to release.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void releaseButton(Button button) throws SkypeException {
        Utils.executeWithErrorCheck("BTN_RELEASED " + button.getKey());
    }

    /**
     * Open the Test video dialog of the Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void openVideoTestWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN VIDEOTEST");
    }

    /**
     * Open the Video options window of the Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void openVideoOptionsWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN OPTIONS VIDEO");
    }
    
    public static String getUILanguageByISOCode() throws SkypeException {
        return Utils.getProperty("UI_LANGUAGE");
    }

    public static void setUILanguageByISOCode(final String newValue) throws SkypeException {
        Utils.setProperty("UI_LANGUAGE", newValue);
    }
    
    public static File getWallPaper() throws SkypeException {
        return new File(Utils.getProperty("WALLPAPER"));
    }

    public static void setWallPaper(final File newValue) throws SkypeException {
        if (newValue != null) {
            Utils.setProperty("WALLPAPER", newValue.getAbsolutePath());
        } else {
            Utils.setProperty("WALLPAPER", "");
        }
    }

    /**
     * Sets the Skype silent mode.
     * @param on if true turn on the silent mode.
     * @return true if the user responds ok.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @since 2.6.0.84
     */
    public static boolean setSilentMode(boolean on) throws SkypeException {
        try {
            String command = "SET SILENT_MODE " + (on? "ON": "OFF");
            String responseHeader = "SILENT_MODE ";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
            String responseValue = response.substring(responseHeader.length());
            return "ON".equals(responseValue);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return false;
        }
    }

    public static EventMessage addEventMessage(String caption) throws SkypeException {
        return addEventMessage(caption, caption);
    }
    
    public static EventMessage addEventMessage(String caption, String hint) throws SkypeException {
        Utils.checkNotNull("caption", caption);
        Utils.checkNotNull("hint", hint);
        try {
            EventMessage eventMessage = EventMessage.addEventMessage(caption, hint);
            String command = "CREATE EVENT " + eventMessage.getId() + " CAPTION \"" + caption + "\" HINT \"" + hint + "\"";
            String responseHeader = "EVENT " + eventMessage.getId() + " CREATED";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
            return eventMessage;
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }
    
    public static void removeEventMessage(EventMessage eventMessage) throws SkypeException {
        eventMessage.dispose();
    }
    
    public static MenuItem addMenuItem(MenuItem.Context context, String caption, String hint, File iconFile, boolean enabled, String targetSkypeId, boolean multipleContactsEnabled) throws SkypeException {
        try {
            MenuItem menuItem = MenuItem.addMenuItem(context, caption, hint, iconFile, enabled, targetSkypeId, multipleContactsEnabled);
            String command = "CREATE MENU_ITEM " + menuItem.getId() + " CONTEXT " + context.name().toLowerCase() + " CAPTION \"" + caption + "\"";
            if (hint != null) {
                command += " HINT \"" + hint + "\"";
            }
            if (iconFile != null) {
                command += " ICON \"" + iconFile.getAbsolutePath() + "\"";
            }
            if (!enabled) {
                command += " ENABLED false";
            }
            if (targetSkypeId != null) {
                command += " CONTACT_TYPE_FILTER \"" + targetSkypeId + "\"";
            }
            if (!multipleContactsEnabled) {
                command += " ENABLE_MULTIPLE_CONTACTS false";
            }
            String responseHeader = "MENU_ITEM " + menuItem.getId() + " CREATED";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
            return menuItem;
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }
    
    public static void removeMenuItem(MenuItem menuItem) throws SkypeException {
        menuItem.dispose();
    }
    
    public static void setMainWindowState(WindowState newValue) throws SkypeException {
        Utils.setProperty("WINDOWSTATE", newValue.name());
    }
    
    public static WindowState getMainWindowState() throws SkypeException {
        return WindowState.valueOf(Utils.getProperty("WINDOWSTATE"));
    }
}
