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

import java.io.File;

import junit.framework.TestCase;

public abstract class TestCaseByCSVFile extends TestCase {
    private boolean _isRecordingMode;
    
    public final void setRecordingMode(final boolean on) {
        _isRecordingMode = on;
    }
    
    public final boolean isRecordingMode() {
        return _isRecordingMode;
    }
    
    @Override
    protected final void runTest() throws Throwable {
    	TestConnector.resetInstance();
        if (isRecordingMode()) {
            CSVRecorder recorder = null;
            try {
                recorder = new CSVRecorder(getTestDataFileName());
                TestConnector.getInstance().addRecorder(recorder);
                super.runTest();
            } finally {
                if (recorder != null) {
                    recorder.close();
                }
            }
        } else {
            try {
                TestConnector.getInstance().setPlayer(new CSVPlayer(getTestDataFileName()));
                super.runTest();
            } finally {
                TestConnector.getInstance().clearPlayer();
            }
        }
    }

    private String getTestDataFileName() {
        return File.separator + getClass().getName().replace(".", File.separator) + "_" + getName() + ".csv";
    }
}
