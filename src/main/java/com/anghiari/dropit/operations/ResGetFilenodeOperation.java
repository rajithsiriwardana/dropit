package com.anghiari.dropit.operations;


import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.fileserver.FileServerNode;
import com.anghiari.dropit.fileserver.impl.RingSetupHandler;

/**
 * This operation retrieves the response from the request server and sends a
 * request to join to the pointed node.
 * 
 * @author Sanka
 * 
 */
public class ResGetFilenodeOperation extends AbstractOperation {
	private FileNode node;
	private DropItPacket responsePacket;
	private RingSetupHandler setupHandler;

	public ResGetFilenodeOperation(DropItPacket packet, FileServerNode nodeServer, RingSetupHandler ringSetupHandler) {
		
		this.fileServer = nodeServer;
		String[] values=(String[]) packet.getAttribute(Constants.INET_ADDRESS.toString());
		System.out.println("values "+values[0]);
		
		node = new FileNode(values[0], Integer.parseInt(values[1]));
		
		responsePacket = new DropItPacket(Constants.FND_SUSC.toString());

		// send the current node data with the packet.
		// responsePacket.setAttribute(Constants.INET_ADDRESS.toString(), );
	}

	@Override
	public void sendRequest() {
		this.fileServer.sendMessage(responsePacket, node, setupHandler);
	}
}