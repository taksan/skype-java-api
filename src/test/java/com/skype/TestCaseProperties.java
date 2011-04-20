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
package com.skype;

import java.util.Properties;

import org.junit.Ignore;

import junit.framework.Assert;

@Ignore
@SuppressWarnings("rawtypes")
final class TestCaseProperties {
	private final Class testCaseClass;

    private Properties properties;

    TestCaseProperties(Class testCaseClass) {
        this.testCaseClass = testCaseClass;
        properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream(getPropertyFileName()));
        } catch (Exception e) {
            Assert.fail("Please, create '" + getPropertyFileName() + "' file by '" + getPropertyFileName() + ".base' file in the same directory.");
        }
    }

    private String getPropertyFileName() {
        return testCaseClass.getSimpleName() + ".properties";
    }

    String getProperty(String key) {
        if (!properties.containsKey(key)) {
            throw new IllegalArgumentException("'" + getPropertyFileName() + "' file doesn't have '" + key + "' entry.");
        }
        return properties.getProperty(key);
    }
}
