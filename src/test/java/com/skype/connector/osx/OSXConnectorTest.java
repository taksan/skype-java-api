package com.skype.connector.osx;

import org.junit.Assert;
import org.junit.Test;

public class OSXConnectorTest { 
	@Test
	public void onFixLegacyMessage_ShouldAddChatPrefix()
	{
		String actual = OSXConnector.fixLegacyMessage("MESSAGE 1413257 STATUS RECEIVED");
		Assert.assertEquals("CHATMESSAGE 1413257 STATUS RECEIVED", actual);
		
		actual = OSXConnector.fixLegacyMessage("USER davyjones ONLINESTATUS ONLINE");
		Assert.assertEquals("USER davyjones ONLINESTATUS ONLINE", actual);
		
		actual = OSXConnector.fixLegacyMessage("#1 MESSAGE 1411049 STATUS RECEIVED");
		Assert.assertEquals("#1 CHATMESSAGE 1411049 STATUS RECEIVED", actual);
	}
}