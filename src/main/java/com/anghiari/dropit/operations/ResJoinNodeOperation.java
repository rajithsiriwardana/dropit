package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.fileserver.impl.FileServerNodeImpl;

public class ResJoinNodeOperation extends AbstractOperation {
	private DropItPacket responsePacket1, responsePacket2;
	private FileNode successor;
	private FileNode predecessor;

	public ResJoinNodeOperation(DropItPacket pkt) {
		responsePacket1 = new DropItPacket(Constants.SET_PREDECESSOR.toString());
		responsePacket2 = new DropItPacket(Constants.SET_SUCCESSOR.toString());

		successor = (FileNode) pkt.getAttribute(Constants.SUCCESSOR.toString());
		predecessor = (FileNode) pkt.getAttribute(Constants.PREDECESSOR
				.toString());

		FileServerNodeImpl serverNodeImpl = (FileServerNodeImpl) this.fileServer;

		responsePacket1.setAttribute(Constants.PREDECESSOR.toString(),
				serverNodeImpl.getNode());
		responsePacket2.setAttribute(Constants.SUCCESSOR.toString(),
				serverNodeImpl.getNode());
	}

	@Override
	public void sendRequest() {
		this.fileServer.sendMessage(responsePacket1, successor);
		this.fileServer.sendMessage(responsePacket2, predecessor);
	}

}
