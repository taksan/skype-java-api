package com.skype;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

import com.skype.mocks.ConnectorMock;

public class SkypeTest {
	@Test
	public void addListener_ShouldNotAddListenerIfExceptionIsThrown() throws SkypeException
	{
		Skype.setReplacementConnectorInstance(new ConnectorMock());
		
		CallListener mockListener = createMockListener();
		
		try {
			Skype.addCallListener(mockListener);
			Assert.fail("An exception was expected");
		}catch(Exception e) {
			Assert.assertFalse(Skype.isCallListenerRegistered(mockListener));
		}
	}

	private CallListener createMockListener() {
		return new CallListener() {
			
			@Override
			public void callReceived(Call receivedCall) throws SkypeException {
			}
			
			@Override
			public void callMaked(Call makedCall) throws SkypeException {
			}
		};
	}
	
	@After
	public void tearDown() {
		Skype.setReplacementConnectorInstance(null);
	}
}
