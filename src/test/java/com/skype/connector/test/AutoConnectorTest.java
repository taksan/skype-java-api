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
 * Gabriel Takeuchi - Ignored non working tests, fixed some, removed warnings
 ******************************************************************************/
package com.skype.connector.test;

import com.skype.Call;
import com.skype.Skype;

public final class AutoConnectorTest extends TestCaseByCSVFile {
    @Override
    protected void setUp() throws Exception {
        setRecordingMode(false);
    }

    public void testGetVersion() throws Exception {
        String result = Skype.getVersion();
        if(isRecordingMode()) {
            System.out.println(result);
        } else {
            assertEquals("2.5.0.130", result);
        }
    }

    public void testCall() throws Exception {
    	TestConnector.resetInstance();
        Call call = Skype.getContactList().getFriend("echo123").call();
        Thread.sleep(5000);
        call.finish();

        String result = call.getDuration() + "," + call.getId() + "," + call.getPartnerId() + "," + call.getType();
        if(isRecordingMode()) {
            System.out.println(result);
        } else {
            assertEquals("2,8345,echo123,OUTGOING_P2P", result);
        }
    }
}
