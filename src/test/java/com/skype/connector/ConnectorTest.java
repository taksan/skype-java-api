package com.skype.connector;

import com.skype.connector.test.TestConnector;

import junit.framework.TestCase;

public class ConnectorTest extends TestCase {
    public void testProperty() throws Exception {
        Connector.setInstance(TestConnector.getInstance());

        Connector connector = Connector.getInstance();

        final String PROPERTY_NAME = "name";
        final String PROPERTY_VALUE = "value";

        assertNull(connector.getStringProperty(PROPERTY_NAME));

        connector.setStringProperty(PROPERTY_NAME, PROPERTY_VALUE);
        assertEquals(PROPERTY_VALUE, connector.getStringProperty(PROPERTY_NAME));

        connector.setStringProperty(PROPERTY_NAME, null);
        assertNull(connector.getStringProperty(PROPERTY_NAME));
    }
}
