package com.skype.mocks;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

public class ConnectorMock extends Connector {

	@Override
	protected void initializeImpl() throws ConnectorException {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	protected Status connect(int timeout) throws ConnectorException {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	protected void disposeImpl() throws ConnectorException {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	protected void sendCommand(String command) {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

}
