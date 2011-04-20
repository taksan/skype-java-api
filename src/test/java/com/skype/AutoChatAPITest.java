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
 * Gabriel Takeuchi - Ignored non working tests, fixed some, removed warnings
 ******************************************************************************/
package com.skype;

import com.skype.connector.test.TestCaseByCSVFile;

public class AutoChatAPITest extends TestCaseByCSVFile {
    @Override
    protected void setUp() throws Exception {
        setRecordingMode(false);
    }
    
    public void testGetAllChat() throws Exception {
        Chat[] chats = Skype.getAllChats();
        String result = toString(chats);
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            // data from https://developer.skype.com/Docs/ApiDoc/SEARCH_CHATS
            assertEquals("#bitman/$jessy;eb06e65612353279, #bitman/$jdenton;9244e98f82d7d391", result);
        }
    }
    
    public void testGetAllActiveChat() throws Exception {
        Chat[] chats = Skype.getAllActiveChats();
        String result = toString(chats);
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            // data from https://developer.skype.com/Docs/ApiDoc/SEARCH_ACTIVECHATS
            assertEquals("#bitman/$jessy;eb06e65612353279, #bitman/$jdenton;9244e98f82d7d391", result);
        }
    }
    
    public void testGetAllMissedChat() throws Exception {
        Chat[] chats = Skype.getAllMissedChats();
        String result = toString(chats);
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            //data from https://developer.skype.com/Docs/ApiDoc/SEARCH_MISSEDCHATS
            assertEquals("#bitman/$jessy;eb06e65612353279, #bitman/$jdenton;9244e98f82d7d391", result);
        }
    }
    
    public void testGetAllRecentChat() throws Exception {
        Chat[] chats = Skype.getAllRecentChats();
        String result = toString(chats);
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            // data from https://developer.skype.com/Docs/ApiDoc/SEARCH_RECENTCHATS
            assertEquals("#bitman/$jessy;eb06e65612353279, #bitman/$jdenton;9244e98f82d7d391", result);
        }
    }
    
    public void testGetAllBookmarkedChat() throws Exception {
        Chat[] chats = Skype.getAllBookmarkedChats();
        String result = toString(chats);
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            // data from https://developer.skype.com/Docs/ApiDoc/SEARCH_BOOKMARKEDCHATS
            assertEquals("#bitman/$jessy;eb06e65612353279, #bitman/$jdenton;9244e98f82d7d391", result);
        }
    }

    private String toString(Chat[] chats) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, length = chats.length; i < length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(chats[i].getId());
        }
        return builder.toString();
    }
}
