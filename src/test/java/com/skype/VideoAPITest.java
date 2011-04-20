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

import org.junit.Ignore;
import org.junit.Test;


@Ignore
public class VideoAPITest {
	@Test
    public void testGetVideoDevice() throws Exception {
        String name = Skype.getVideoDevice();
        if (name == null) {
            name = "Default video device";
        }
        TestUtils.showCheckDialog("Webcam is '" + name + "'?");
    }
	@Test
    public void testOpenVideoTestWindow() throws Exception {
        SkypeClient.openVideoTestWindow();
        TestUtils.showCheckDialog("Webcam test window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

	@Test
    public void testOpenVideoOptionsWindow() throws Exception {
        SkypeClient.openVideoOptionsWindow();
        TestUtils.showCheckDialog("Options window with selectiong 'Video' page is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }
}
