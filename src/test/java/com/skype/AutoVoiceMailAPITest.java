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

import java.util.ArrayList;
import java.util.List;

import com.skype.VoiceMail.Status;
import com.skype.connector.test.TestCaseByCSVFile;

public class AutoVoiceMailAPITest extends TestCaseByCSVFile {
    @Override
    protected void setUp() throws Exception {
        setRecordingMode(false);
    }
    
    public void testGetAllVoiceMails() throws Exception {
        VoiceMail[] voiceMails = Skype.getAllVoiceMails();
        String result = toString(voiceMails);
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            // data from https://developer.skype.com/Docs/ApiDoc/SEARCH_VOICEMAILS
            assertEquals("65, 70, 71", result);
        }
    }
    
    private String toString(VoiceMail[] voiceMails) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, length = voiceMails.length; i < length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(voiceMails[i].getId());
        }
        return builder.toString();
    }
    
    public void testVoiceMail() throws Exception {
        VoiceMail voiceMail = Skype.voiceMail("echo123");
        String result = voiceMail.getId() + ", " + voiceMail.getType() + ", " + voiceMail.getPartnerId() + ", " + voiceMail.getPartnerDisplayName() + ", " + voiceMail.getStartTime().getTime() + ", " + voiceMail.getDuration() + ", " + voiceMail.getAllowedDuration();
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            assertEquals("2346, OUTGOING, echo123, Skype Test Call, 1156016035000, 0, 600", result);
        }
    }
    
    public void ignore_testVoiceMailStatusChangedListener() throws Exception {
        VoiceMail voiceMail = Skype.voiceMail("echo123");
        final List<Status> statuses = new ArrayList<Status>();
        final Object wait = new Object();
        voiceMail.addVoiceMailStatusChangedListener(new VoiceMailStatusChangedListener() {
            public void statusChanged(Status status) throws SkypeException {
                statuses.add(status);
                if (status == Status.UPLOADED) {
                    synchronized(wait) {
                        wait.notify();
                    }
                }
            }
        });
        synchronized(wait) {
            wait.wait();
        }
        String result = statuses.toString();
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            assertEquals("[BLANK, RECORDING, UPLOADING, UPLOADED]", result);
        }
    }
}