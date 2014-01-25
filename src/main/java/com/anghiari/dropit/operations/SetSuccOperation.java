package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.fileserver.impl.FileServerNodeImpl;

/**
 * 
 * @author Sanka
 * 
 */
public class SetSuccOperation extends AbstractOperation {
	private DropItPacket responsePacket;
	private FileServerNodeImpl fileServerNodeImpl;

	public SetSuccOperation(DropItPacket pkt) {
		fileServerNodeImpl = (FileServerNodeImpl) this.fileServer;
		FileNode successor = (FileNode) pkt.getAttribute(Constants.SUCCESSOR
				.toString());
		fileServerNodeImpl.setSuccessor(successor);

	}

}
