package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.fileserver.impl.FileServerNodeImpl;

public class SetPredOperation extends AbstractOperation {
	private DropItPacket responsePacket;
	private FileServerNodeImpl fileServerNodeImpl;

	public SetPredOperation(DropItPacket pkt) {
		fileServerNodeImpl = (FileServerNodeImpl) this.fileServer;
		FileNode predecessor = (FileNode) pkt
				.getAttribute(Constants.PREDECESSOR.toString());
		fileServerNodeImpl.setPredecessor(predecessor);
	}

}
