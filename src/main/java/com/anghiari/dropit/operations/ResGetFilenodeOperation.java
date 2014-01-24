package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;

public class ResGetFilenodeOperation extends AbstractOperation {
	private FileNode node;
	private DropItPacket responsePacket;

	public ResGetFilenodeOperation(DropItPacket packet) {
		node = (FileNode) packet
				.getAttribute(Constants.INET_ADDRESS.toString());

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
