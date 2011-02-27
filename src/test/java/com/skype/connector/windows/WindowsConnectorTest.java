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
package com.skype.connector.windows;

import junit.framework.TestCase;

import com.skype.TestUtils;
import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorListener;
import com.skype.connector.ConnectorStatusEvent;

public final class WindowsConnectorTest extends TestCase {
    public void testAttachedAfterTryingToConnect() throws Exception {
        TestUtils.showMessageDialog("Open [Tools - Options - Privacy - Manage other programs' access to Skype] and ensure that there is no java application (JAVAW.EXE or JAVA.EXE), please.");
        TestUtils.showMessageDialog("Select [Another program wants to use Skype - Allow this program to use Skype] when Skype shows the authorization dialog, please.");
        assertEquals(Connector.Status.ATTACHED, WindowsConnector.getInstance().connect());
    }

    public void testRefusedAfterTryingToConnect() throws Exception {
        TestUtils.showMessageDialog("Open [Tools - Options - Privacy - Manage other programs' access to Skype] and ensure that there is no java application (JAVAW.EXE or JAVA.EXE), please.");
        TestUtils.showMessageDialog("Select [Another program wants to use Skype - Do not allow this program to use Skype] when Skype shows the authorization dialog, please.");
        assertEquals(Connector.Status.REFUSED, WindowsConnector.getInstance().connect());
        TestUtils.showMessageDialog("Open [Tools - Options - Privacy - Manage other programs' access to Skype] and ensure that there is no java application (JAVAW.EXE or JAVA.EXE) for next test, please.");
    }

    public void testNotAvailableAfterTryingToConnect() throws Exception {
        TestUtils.showMessageDialog("Sign out from Skype, please.");
        assertEquals(Connector.Status.NOT_AVAILABLE, WindowsConnector.getInstance().connect());

        final boolean[] available = new boolean[1];
        ConnectorListener listener = new AbstractConnectorListener() {
            public void statusChanged(ConnectorStatusEvent event) {
                Connector.Status status = event.getStatus();
                if (status == Connector.Status.API_AVAILABLE) {
                    available[0] = true;
                }
            }
        };
        WindowsConnector.getInstance().addConnectorListener(listener, false);
        TestUtils.showMessageDialog("Sign in Skype, please.");
        assertTrue(available[0]);
    }

    public void testNotRunnigAfterTryingToConnect() throws Exception {
        TestUtils.showMessageDialog("Shut down Skype, please.");
        assertEquals(Connector.Status.NOT_RUNNING, WindowsConnector.getInstance().connect());
        TestUtils.showMessageDialog("Launch Skype for next test, please.");
    }

    public void testGetInstalledPath() {
        assertEquals("C:\\Program Files\\Skype\\Phone\\Skype.exe", WindowsConnector.getInstance().getInstalledPath());
    }
}
