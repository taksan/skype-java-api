package com.skype.connector;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;

public class ConnectorUtilsTest {
	@Test
	public void getTempDIr_ShouldReturnARandomTemporaryDirUnderTempDir()
	{
		String tempDir = System.getProperty("java.io.tmpdir");
		String actual = ConnectorUtils.getTempDir();
		
		Assert.assertTrue(actual.contains(tempDir));
		Assert.assertFalse(actual.endsWith(tempDir));
		File actualDir = new File(actual);
		Assert.assertTrue(actualDir.exists());
		Assert.assertTrue(actualDir.isDirectory());
	}

}
