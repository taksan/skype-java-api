package com.skype.connector;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;

import com.skype.connector.win32.Win32Connector;

public class ConnectorUtilsTest {
	@Test
	public void getTempDir_ShouldReturnARandomTemporaryDirUnderTempDir()
	{
		String tempDir = System.getProperty("java.io.tmpdir");
		String actual = ConnectorUtils.getSkypeTempDir();
		
		Assert.assertTrue(actual.contains(tempDir));
		Assert.assertFalse(actual.endsWith(tempDir));
		File actualDir = new File(actual);
		Assert.assertTrue(actualDir.exists());
		Assert.assertTrue(actualDir.isDirectory());
		
		String anotherActual = ConnectorUtils.getSkypeTempDir();
		Assert.assertEquals(actual, anotherActual);
	}
	
	@Test
	public void getConnectorInstance_ShouldReturnValidConnector() {
		ensureSkypeDirIsCleanBeforeTestingInitialization();
		
		if (Connector.getInstance() instanceof Win32Connector) {
			class Win32ConnectorToTest extends Win32Connector {
				public void runInit() {
					initializeImpl();
				}
			}
			new Win32ConnectorToTest().runInit();
		}
	}

	private void ensureSkypeDirIsCleanBeforeTestingInitialization() {
		File skypeTempDir = new File(ConnectorUtils.getSkypeTempDir());
		skypeTempDir.delete();
	}

}
