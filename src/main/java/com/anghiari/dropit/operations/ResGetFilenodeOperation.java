package com.anghiari.dropit.operations;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.fileserver.FileServerNode;
import com.anghiari.dropit.fileserver.impl.FileServerNodeImpl;

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

	public ResGetFilenodeOperation(DropItPacket packet) {
		node = (FileNode) packet
				.getAttribute(Constants.INET_ADDRESS.toString());
		responsePacket = new DropItPacket(Constants.FND_SUSC.toString());

		// send the current node data with the packet.
		// responsePacket.setAttribute(Constants.INET_ADDRESS.toString(), );
	}

	@Override
	public void sendRequest() {
		this.fileServer.sendMessage(responsePacket, node, setupHandler);
	}
}

class RingSetupHandler extends SimpleChannelHandler {
	private FileServerNodeImpl handledNode;

	public RingSetupHandler(final FileServerNode nodeImpl) {
		this.handledNode = (FileServerNodeImpl) nodeImpl;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		DropItPacket pkt = (DropItPacket) e.getMessage();
		String method = pkt.getMethod();

		if (Constants.RES_SUSC.toString().equalsIgnoreCase(method)) {
			DropItPacket packet = new DropItPacket(
					Constants.REQ_JOIN_NODE.toString());
			FileNode node = (FileNode) pkt.getAttribute(Constants.INET_ADDRESS
					.toString());
			packet.setAttribute(Constants.INET_ADDRESS.toString(), node);
			handledNode.sendMessage(packet, node, this);
		} else if (Constants.RES_JOIN_NODE.toString().equalsIgnoreCase(method)) {
			DropItPacket packet = new DropItPacket(
					Constants.REQ_JOIN_FINAL.toString());
			
		}
		super.messageReceived(ctx, e);
	}
}