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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.skype.connector.test.TestCaseByCSVFile;

public class AutoDebugOutTest extends TestCaseByCSVFile {
    private ByteArrayOutputStream out;

    @Override
    protected void setUp() throws Exception {
        setRecordingMode(false);

        if (!isRecordingMode()) {
            initSysOutRecorder();
        }
    }

    private void initSysOutRecorder() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
    }

    public void testBasic() throws Exception {
        Skype.getVersion();
        outputsAreEmpty();

        Skype.setDebug(true);
        Skype.getVersion();
        outputsAre("-> GET SKYPEVERSION ", "<- SKYPEVERSION 3.8.0.139");

        Skype.setDebug(false);
        Skype.getVersion();
        outputsAreEmpty();
    }

    private void outputsAreEmpty() {
        outputsAre();
    }

    private void outputsAre(String... expected) {
        if (isRecordingMode()) {
            return;
        }

        String[] actual = getOutputsFromSysOutRecorder();
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    private String[] getOutputsFromSysOutRecorder() {
        List<String> returned = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
            out.reset();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                returned.add(line);
            }
            return returned.toArray(new String[0]);
        } catch(IOException e) {
            throw new IllegalStateException("Getting outputs failed.", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch(IOException e) {
                }
            }
        }
    }
}
