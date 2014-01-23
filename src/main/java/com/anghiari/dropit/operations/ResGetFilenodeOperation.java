package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;

public class ResGetFilenodeOperation extends AbstractOperation {
	private String host;
	private int port;

	public ResGetFilenodeOperation(DropItPacket packet) {
		String[] data = (String[]) packet.getAttribute(Constants.KEY_ID
				.toString());
		host = data[0];
		port = Integer.parseInt(data[1]);

	}

	@Override
	public void sendRequest() {
		// TODO Auto-generated method stub
		super.sendRequest();
	}

	@Override
	public void sendResponse() {

		super.sendResponse();
	}
}
